-- Drop the database if it exists to start fresh, then create it
DROP DATABASE IF EXISTS health_passport_db;
CREATE DATABASE health_passport_db;
USE health_passport_db;

-- 1. HOSPITALS (No dependencies)
CREATE TABLE Hospitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. USERS (Depends on Hospitals)
-- Centralized authentication table for all roles
CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    national_id VARCHAR(50) UNIQUE, -- Nullable for Admins
    full_name VARCHAR(255),         -- NEW: Added full_name here!
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('DOCTOR', 'PATIENT', 'ADMIN') NOT NULL,
    hospital_id INT, 
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id) ON DELETE SET NULL
);

-- 3. PATIENTS (Depends on Users)
CREATE TABLE Patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    national_id VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    blood_group VARCHAR(5),
    phone VARCHAR(20),
    address TEXT,
    weight DECIMAL(5,2) DEFAULT 0.00,  -- NEW: Added Weight
    height DECIMAL(5,2) DEFAULT 0.00,  -- NEW: Added Height
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

-- 4. DOCTORS (Depends on Users, Hospitals)
CREATE TABLE Doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    hospital_id INT NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    years_of_experience INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id) ON DELETE CASCADE
);

-- 5. MEDICAL HISTORY (Depends on Patients, Doctors, Hospitals)
CREATE TABLE Medical_History (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    diagnosed_by INT NOT NULL, -- Refers to Doctors.id
    hospital_id INT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    diagnosis_date DATE NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (diagnosed_by) REFERENCES Doctors(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id)
);

-- 6. PRESCRIPTIONS (Depends on Patients, Doctors, Hospitals)
CREATE TABLE Prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    hospital_id INT NOT NULL,
    prescription_date DATE NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctors(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id)
);

-- 7. PRESCRIPTION ITEMS (Depends on Prescriptions)
CREATE TABLE Prescription_Items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    instructions TEXT,
    FOREIGN KEY (prescription_id) REFERENCES Prescriptions(id) ON DELETE CASCADE
);

-- 8. TEST REPORTS (Depends on Patients, Users, Hospitals)
CREATE TABLE Test_Reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    added_by_admin_id INT NOT NULL, -- Refers to Users.id (Admin)
    hospital_id INT NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    report_date DATE NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (added_by_admin_id) REFERENCES Users(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id)
);

-- 9. APPOINTMENTS (Depends on Patients, Doctors, Hospitals)
CREATE TABLE Appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    hospital_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    created_by INT NOT NULL, -- Refers to Users.id (Who made the appointment)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctors(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id),
    FOREIGN KEY (created_by) REFERENCES Users(id)
);

-- 10. REMINDERS (Depends on Patients)
CREATE TABLE Reminders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    type ENUM('MEDICINE', 'APPOINTMENT') NOT NULL,
    reference_id INT NOT NULL, -- ID of the prescription_item or appointment
    reminder_time DATETIME NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE
);

-- 11. AUDIT LOGS (Depends on Users)
CREATE TABLE Audit_Logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL, -- e.g., 'Patient', 'Prescription'
    entity_id INT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);
-- 1. Create the Reviews Table
CREATE TABLE IF NOT EXISTS Doctor_Reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctors(id) ON DELETE CASCADE
);

-- 1. Wipe the table completely clean so we start fresh!
TRUNCATE TABLE Doctor_Reviews;



-- 1. DR. MARUF (DOC-001)
INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 5, 'Highly recommended!'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-001')) LIMIT 8;

INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 4, 'Very helpful.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-001')) ORDER BY patient_id DESC LIMIT 2;

-- 2. DR. ALISHA (DOC-002)
INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 5, 'Excellent and caring.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-002')) LIMIT 6;

INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 4, 'Very good experience.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-002')) ORDER BY patient_id DESC LIMIT 3;

-- 3. DR. FARIA (DOC-003)
INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 5, 'Very attentive and kind pediatrician.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-003')) LIMIT 5;

INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 4, 'Great doctor, but the waiting room was full.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-003')) ORDER BY patient_id DESC LIMIT 4;

-- 4. DR. SAMEHA (DOC-004)
INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 5, 'The best orthopedics specialist!'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-004')) LIMIT 7;

INSERT INTO Doctor_Reviews (patient_id, doctor_id, rating, review_text)
SELECT DISTINCT patient_id, doctor_id, 4, 'Very knowledgeable and helpful.'
FROM Appointments WHERE doctor_id = (SELECT id FROM Doctors WHERE user_id = (SELECT id FROM Users WHERE national_id='DOC-004')) ORDER BY patient_id DESC LIMIT 2;

-- Insert a Hospital
INSERT INTO Hospitals (name, address, contact_number)
VALUES ('Dhaka Medical College', 'Dhaka, Bangladesh', '01711111111');

-- Store the hospital ID in a variable to use it for all doctors and admins
SET @hospital_id = LAST_INSERT_ID();

-- Insert an Admin for the Hospital
INSERT INTO Users (email, password_hash, role, hospital_id)
VALUES ('admin_dmc@gov.bd', 'pass123', 'ADMIN', @hospital_id);


-- INSERT DOCTORS


-- 1. Prof Maruf Ahmed Tamal
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('DOC-001', 'maruf.tamal@dmc.gov.bd', 'pass123', 'DOCTOR', @hospital_id);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, years_of_experience) 
VALUES (LAST_INSERT_ID(), @hospital_id, 'Cardiology', 'BMDC-1001', 15);

-- 2. Doctor Alisha Kabir
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('DOC-002', 'alisha.kabir@dmc.gov.bd', 'pass123', 'DOCTOR', @hospital_id);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, years_of_experience) 
VALUES (LAST_INSERT_ID(), @hospital_id, 'Neurology', 'BMDC-1002', 8);

-- 3. Doctor Faria alam
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('DOC-003', 'faria.alam@dmc.gov.bd', 'pass123', 'DOCTOR', @hospital_id);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, years_of_experience) 
VALUES (LAST_INSERT_ID(), @hospital_id, 'Pediatrics', 'BMDC-1003', 5);

-- 4. Doctor Sameha Kamrul
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('DOC-004', 'sameha.kamrul@dmc.gov.bd', 'pass123', 'DOCTOR', @hospital_id);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, years_of_experience) 
VALUES (LAST_INSERT_ID(), @hospital_id, 'Orthopedics', 'BMDC-1004', 10);


-- INSERT PATIENTS


-- 1. Ummey Habiba Pranty
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('PT-0025235', 'pranty@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) 
VALUES (LAST_INSERT_ID(), 'PT-0025235', 'Ummey Habiba Pranty', '1995-08-20', 'FEMALE', 'AB+', '01822222222', 60.00, 160.00);

-- 2. Bijoy Sarkar
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('PT-0025236', 'bijoy@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) 
VALUES (LAST_INSERT_ID(), 'PT-0025236', 'Bijoy Sarkar', '2004-01-01', 'MALE', 'O+', '01733333333', 70.00, 175.00);

-- 3. Md. Saber Hossen
INSERT INTO Users (national_id, email, password_hash, role, hospital_id) 
VALUES ('PT-0025237', 'saber@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) 
VALUES (LAST_INSERT_ID(), 'PT-0025237', 'Saber Hossen', '2003-05-15', 'MALE', 'B+', '01644444444', 68.00, 170.00);


-- 4. Tasnuva
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025238', 'Tasnuva', 'tasnuva38@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025238', 'Tasnuva', '2002-04-12', 'FEMALE', 'A+', '01711000038', 55.0, 160.0);

-- 5. Fahim Siddique
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025239', 'Fahim Siddique', 'fahim39@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025239', 'Fahim Siddique', '2001-08-25', 'MALE', 'B+', '01711000039', 72.5, 175.5);

-- 6. Nayeem Uddin
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025240', 'Nayeem Uddin', 'nayeem40@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025240', 'Nayeem Uddin', '2000-11-05', 'MALE', 'O+', '01711000040', 68.0, 172.0);

-- 7. Hasib Bin Hossain
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025241', 'Hasib Bin Hossain', 'hasib41@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025241', 'Hasib Bin Hossain', '2003-02-18', 'MALE', 'AB+', '01711000041', 75.0, 178.0);

-- 8. Irfanur Rahman
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025242', 'Irfanur Rahman', 'irfanur42@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025242', 'Irfanur Rahman', '2002-07-30', 'MALE', 'A+', '01711000042', 65.5, 169.5);

-- 9. Ragib Mahtab Oyon
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025243', 'Ragib Mahtab Oyon', 'ragib43@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025243', 'Ragib Mahtab Oyon', '2001-09-14', 'MALE', 'B+', '01711000043', 70.0, 174.0);

-- 10. Arafat Hossain
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025244', 'Arafat Hossain', 'arafat44@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025244', 'Arafat Hossain', '2000-03-22', 'MALE', 'O+', '01711000044', 74.0, 176.0);

-- 11. Adiba Binte Atique
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025245', 'Adiba Binte Atique', 'adiba45@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025245', 'Adiba Binte Atique', '2003-12-10', 'FEMALE', 'AB+', '01711000045', 52.0, 158.0);

-- 12. Jonayed Ahmed
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025246', 'Jonayed Ahmed', 'jonayed46@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025246', 'Jonayed Ahmed', '2002-05-08', 'MALE', 'A+', '01711000046', 69.0, 173.0);

-- 13. Tanvir Ahmed Zidan
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025247', 'Tanvir Ahmed Zidan', 'tanvir47@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025247', 'Tanvir Ahmed Zidan', '2001-01-20', 'MALE', 'B+', '01711000047', 71.5, 175.0);

-- 14. Momotaz Jahan Momo
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025248', 'Momotaz Jahan Momo', 'momotaz48@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025248', 'Momotaz Jahan Momo', '2002-08-16', 'FEMALE', 'O+', '01711000048', 56.0, 161.0);

-- 15. Mahin Ar Rahman
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025249', 'Mahin Ar Rahman', 'mahin49@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025249', 'Mahin Ar Rahman', '2003-06-25', 'MALE', 'AB+', '01711000049', 66.0, 170.0);

-- 16. Miraz Morshed
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025250', 'Miraz Morshed', 'miraz50@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025250', 'Miraz Morshed', '2000-09-02', 'MALE', 'A+', '01711000050', 73.0, 177.0);

-- 17. Saimum Sadman Joy
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025251', 'Saimum Sadman Joy', 'saimum51@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025251', 'Saimum Sadman Joy', '2001-10-18', 'MALE', 'B+', '01711000051', 68.5, 171.5);

-- 18. Fojla Rabbi
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025252', 'Fojla Rabbi', 'fojla52@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025252', 'Fojla Rabbi', '2002-11-30', 'MALE', 'O+', '01711000052', 77.0, 179.0);

-- 19. Azmain Hasan Abir
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025253', 'Azmain Hasan Abir', 'azmain53@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025253', 'Azmain Hasan Abir', '2003-04-05', 'MALE', 'AB+', '01711000053', 70.5, 174.5);

-- 20. Zarin Tasnim
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025254', 'Zarin Tasnim', 'zarin54@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025254', 'Zarin Tasnim', '2002-01-14', 'FEMALE', 'A+', '01711000054', 54.0, 160.0);

-- 21. Rifah Tahsin
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025255', 'Rifah Tahsin', 'rifah55@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025255', 'Rifah Tahsin', '2001-03-29', 'FEMALE', 'B+', '01711000055', 53.5, 159.5);

-- 22. Farhan Sadik Tahsin
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025256', 'Farhan Sadik Tahsin', 'farhan56@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025256', 'Farhan Sadik Tahsin', '2000-07-11', 'MALE', 'O+', '01711000056', 76.0, 178.5);

-- 23. Afia Mahmuda Ema
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025257', 'Afia Mahmuda Ema', 'afia57@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025257', 'Afia Mahmuda Ema', '2003-09-08', 'FEMALE', 'AB+', '01711000057', 58.0, 163.0);

-- 24. Raisa Rahman
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025258', 'Raisa Rahman', 'raisa58@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025258', 'Raisa Rahman', '2002-12-19', 'FEMALE', 'A+', '01711000058', 51.5, 157.5);

-- 25. Sazidul Islam Bayeid
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025259', 'Sazidul Islam Bayeid', 'sazidul59@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025259', 'Sazidul Islam Bayeid', '2001-05-27', 'MALE', 'B+', '01711000059', 69.5, 172.5);

-- 26. Mabsurah Ferdous
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025260', 'Mabsurah Ferdous', 'mabsurah60@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025260', 'Mabsurah Ferdous', '2003-08-04', 'FEMALE', 'O+', '01711000060', 57.0, 162.5);

-- 27. Sabit Ur Zaman Arpon
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025261', 'Sabit Ur Zaman Arpon', 'sabit61@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025261', 'Sabit Ur Zaman Arpon', '2000-02-14', 'MALE', 'AB+', '01711000061', 72.0, 175.0);

-- 28. Kaif Rahman
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025262', 'Kaif Rahman', 'kaif62@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025262', 'Kaif Rahman', '2002-10-22', 'MALE', 'A+', '01711000062', 67.5, 171.0);

-- 29. Khandokar Saiful Islam Sayef
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025263', 'Khandokar Saiful Islam Sayef', 'saiful63@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025263', 'Khandokar Saiful Islam Sayef', '2001-04-17', 'MALE', 'B+', '01711000063', 78.0, 180.0);

-- 30. Sumaiya Fahmida
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025264', 'Sumaiya Fahmida', 'sumaiya64@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025264', 'Sumaiya Fahmida', '2003-01-09', 'FEMALE', 'O+', '01711000064', 55.5, 161.5);

-- 31. Nujhat Nazifa Nawal
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025265', 'Nujhat Nazifa Nawal', 'nujhat65@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025265', 'Nujhat Nazifa Nawal', '2002-06-28', 'FEMALE', 'AB+', '01711000065', 59.0, 164.0);

-- 32. Turjoy Barua
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025266', 'Turjoy Barua', 'turjoy66@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025266', 'Turjoy Barua', '2000-12-03', 'MALE', 'A+', '01711000066', 71.0, 173.5);

-- 33. Muhibuzzaman Khan
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025267', 'Muhibuzzaman Khan', 'muhibuzzaman67@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025267', 'Muhibuzzaman Khan', '2001-08-19', 'MALE', 'B+', '01711000067', 74.5, 176.5);

-- 34. Samiha Anjum Nishat
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025268', 'Samiha Anjum Nishat', 'samiha68@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025268', 'Samiha Anjum Nishat', '2003-11-21', 'FEMALE', 'O+', '01711000068', 53.0, 159.0);

-- 35. Tasnuva Jabin Rimpa
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025269', 'Tasnuva Jabin Rimpa', 'rimpa69@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025269', 'Tasnuva Jabin Rimpa', '2002-03-15', 'FEMALE', 'AB+', '01711000069', 56.5, 160.5);

-- 36. Syed Zahin Waheed
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025270', 'Syed Zahin Waheed', 'zahin70@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025270', 'Syed Zahin Waheed', '2000-09-26', 'MALE', 'A+', '01711000070', 73.5, 177.5);

-- 37. Fariha Nowshin
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025271', 'Fariha Nowshin', 'fariha71@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025271', 'Fariha Nowshin', '2001-07-07', 'FEMALE', 'B+', '01711000071', 54.5, 158.5);

-- 38. Mhamuda Shafiq Shamme
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025272', 'Mhamuda Shafiq Shamme', 'mhamuda72@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025272', 'Mhamuda Shafiq Shamme', '2003-02-28', 'FEMALE', 'O+', '01711000072', 58.5, 163.5);

-- 39. Touhid Islam
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025273', 'Touhid Islam', 'touhid73@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025273', 'Touhid Islam', '2002-10-12', 'MALE', 'AB+', '01711000073', 70.0, 174.0);

-- 40. Nasimul Goni
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025274', 'Nasimul Goni', 'nasimul74@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025274', 'Nasimul Goni', '2000-05-01', 'MALE', 'A+', '01711000074', 76.5, 179.5);

-- 41. Nazifa Tabassum
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025275', 'Nazifa Tabassum', 'nazifa75@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025275', 'Nazifa Tabassum', '2001-12-17', 'FEMALE', 'B+', '01711000075', 52.5, 157.0);

-- 42. Tanvir Foysal
INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025276', 'Tanvir Foysal', 'foysal76@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025276', 'Tanvir Foysal', '2003-03-09', 'MALE', 'O+', '01711000076', 68.0, 172.0);



-- INSERT APPOINTMENTS 


-- 1. Fix the blank Doctor name
UPDATE Users SET full_name = 'Dr. Maruf Ahmed Tamal' WHERE national_id = 'DOC-001';
UPDATE Users SET full_name = 'Dr. Alisha Kabir' WHERE national_id = 'DOC-002';
UPDATE Users SET full_name = 'Dr. Faria Alam' WHERE national_id = 'DOC-003';
UPDATE Users SET full_name = 'Dr. Sameha Kamrul' WHERE national_id = 'DOC-004';


-- 1. Assign to Dr. Maruf Ahmed Tamal (DOC-001)
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, created_by)
SELECT 
    p.id, 
    (SELECT d.id FROM Doctors d JOIN Users u ON d.user_id = u.id WHERE u.national_id = 'DOC-001'),
    (SELECT id FROM Hospitals LIMIT 1),
    DATE_ADD(CURDATE(), INTERVAL (p.id % 5) DAY) + INTERVAL (9 + (p.id % 6)) HOUR,
    'SCHEDULED',
    (SELECT id FROM Users WHERE role = 'ADMIN' LIMIT 1)
FROM Patients p
WHERE p.national_id BETWEEN 'PT-0025235' AND 'PT-0025244';

-- 2. Assign to Dr. Alisha Kabir (DOC-002)
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, created_by)
SELECT 
    p.id, 
    (SELECT d.id FROM Doctors d JOIN Users u ON d.user_id = u.id WHERE u.national_id = 'DOC-002'),
    (SELECT id FROM Hospitals LIMIT 1),
    DATE_ADD(CURDATE(), INTERVAL (p.id % 5) DAY) + INTERVAL (9 + (p.id % 6)) HOUR,
    'SCHEDULED',
    (SELECT id FROM Users WHERE role = 'ADMIN' LIMIT 1)
FROM Patients p
WHERE p.national_id BETWEEN 'PT-0025245' AND 'PT-0025254';

-- 3. Assign to Dr. Faria Alam (DOC-003)
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, created_by)
SELECT 
    p.id, 
    (SELECT d.id FROM Doctors d JOIN Users u ON d.user_id = u.id WHERE u.national_id = 'DOC-003'),
    (SELECT id FROM Hospitals LIMIT 1),
    DATE_ADD(CURDATE(), INTERVAL (p.id % 5) DAY) + INTERVAL (9 + (p.id % 6)) HOUR,
    'SCHEDULED',
    (SELECT id FROM Users WHERE role = 'ADMIN' LIMIT 1)
FROM Patients p
WHERE p.national_id BETWEEN 'PT-0025255' AND 'PT-0025264';

-- 4. Assign to Dr. Sameha Kamrul (DOC-004)
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, created_by)
SELECT 
    p.id, 
    (SELECT d.id FROM Doctors d JOIN Users u ON d.user_id = u.id WHERE u.national_id = 'DOC-004'),
    (SELECT id FROM Hospitals LIMIT 1),
    DATE_ADD(CURDATE(), INTERVAL (p.id % 5) DAY) + INTERVAL (9 + (p.id % 6)) HOUR,
    'SCHEDULED',
    (SELECT id FROM Users WHERE role = 'ADMIN' LIMIT 1)
FROM Patients p
WHERE p.national_id BETWEEN 'PT-0025265' AND 'PT-0025276';

-- =========================================================================
-- BATCH INSERT: MEDICAL HISTORY, PRESCRIPTIONS, & TESTS FOR ALL PATIENTS
-- =========================================================================

-- 1. Give every patient a Clinical Diagnosis (Medical History)
INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date)
SELECT id, 
       (SELECT id FROM Doctors LIMIT 1), 
       1, 
       CASE WHEN id % 2 = 0 THEN 'Type 2 Diabetes (Managed)' ELSE 'Hypertension (Stage 1)' END, 
       DATE_SUB(CURDATE(), INTERVAL (id * 2) DAY)
FROM Patients;

-- 2. Give every patient an Active Prescription Document
INSERT INTO Prescriptions (patient_id, doctor_id, hospital_id, prescription_date, notes)
SELECT id, 
       (SELECT id FROM Doctors LIMIT 1), 
       1, 
       DATE_SUB(CURDATE(), INTERVAL 5 DAY), 
       'Routine maintenance medication'
FROM Patients;

-- 3. Add Medication Item #1 to every Prescription
INSERT INTO Prescription_Items (prescription_id, medicine_name, dosage, frequency, duration, instructions)
SELECT id, 
       CASE WHEN patient_id % 2 = 0 THEN 'Metformin' ELSE 'Amlodipine' END, 
       '500mg', 
       '2 times a day', 
       '30 Days', 
       'After meals'
FROM Prescriptions;

-- 4. Add Medication Item #2 to every Prescription (so the list looks full!)
INSERT INTO Prescription_Items (prescription_id, medicine_name, dosage, frequency, duration, instructions)
SELECT id, 
       'Atorvastatin', 
       '20mg', 
       'Once daily', 
       '90 Days', 
       'Before bedtime'
FROM Prescriptions;

-- 5. Give every patient a Test Report
INSERT INTO Test_Reports (patient_id, added_by_admin_id, hospital_id, report_type, file_url, report_date, notes)
SELECT id, 
       (SELECT id FROM Users WHERE role='ADMIN' LIMIT 1), 
       1, 
       'Complete Blood Count (CBC)', 
       '/docs/cbc_report.pdf', 
       DATE_SUB(CURDATE(), INTERVAL (id + 3) DAY), 
       'Normal parameters observed.'
FROM Patients;