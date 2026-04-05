-- Drop the database if it exists to start fresh, then create it
DROP DATABASE IF EXISTS health_passport_db;
CREATE DATABASE health_passport_db;
USE health_passport_db;

-- =========================================================================
-- 1. CREATE ALL TABLES
-- =========================================================================

CREATE TABLE Hospitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    system_id VARCHAR(50) UNIQUE, 
    full_name VARCHAR(255),          
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('DOCTOR', 'PATIENT', 'ADMIN') NOT NULL,
    hospital_id INT, 
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id) ON DELETE SET NULL
);

CREATE TABLE Patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    system_id VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    blood_group VARCHAR(5),
    phone VARCHAR(20),
    address TEXT,
    weight DECIMAL(5,2) DEFAULT 0.00,  
    height DECIMAL(5,2) DEFAULT 0.00,  
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE Doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    hospital_id INT NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    degrees VARCHAR(255),
    years_of_experience INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id) ON DELETE CASCADE
);

CREATE TABLE Medical_History (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    diagnosed_by INT NOT NULL, 
    hospital_id INT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    diagnosis_date DATE NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (diagnosed_by) REFERENCES Doctors(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id)
);

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

CREATE TABLE Test_Reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    added_by_admin_id INT NOT NULL, 
    hospital_id INT NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_data LONGBLOB,
    report_date DATE NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (added_by_admin_id) REFERENCES Users(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id)
);

CREATE TABLE Appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    hospital_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    reason TEXT, 
    created_by INT NOT NULL, 
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctors(id),
    FOREIGN KEY (hospital_id) REFERENCES Hospitals(id),
    FOREIGN KEY (created_by) REFERENCES Users(id)
);

CREATE TABLE Reminders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    type ENUM('MEDICINE', 'APPOINTMENT') NOT NULL,
    reference_id INT NOT NULL, 
    reminder_time DATETIME NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES Patients(id) ON DELETE CASCADE
);

CREATE TABLE Audit_Logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL, 
    entity_id INT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- =========================================================================
-- 2. INSERT HOSPITALS & ADMINS
-- =========================================================================

-- Hospital 1: Dhaka Medical College
INSERT INTO Hospitals (name, address, contact_number) VALUES ('Dhaka Medical College', 'Dhaka, Bangladesh', '01711111111');
INSERT INTO Users (email, password_hash, role, hospital_id) VALUES ('admin.dmc@gov.bd', 'pass123', 'ADMIN', 1);

-- Hospital 2: United Hospital
INSERT INTO Hospitals (name, address, contact_number) VALUES ('United Hospital', 'Gulshan, Dhaka', '01722222222');
INSERT INTO Users (email, password_hash, role, hospital_id) VALUES ('admin.united@hospital.com', 'pass123', 'ADMIN', 2);

-- Hospital 3: Shaheed Suhrawardy Medical College
INSERT INTO Hospitals (name, address, contact_number) VALUES ('Shaheed Suhrawardy Medical College & Hospital', 'Sher-e-Bangla Nagar, Dhaka', '01733333333');
INSERT INTO Users (email, password_hash, role, hospital_id) VALUES ('admin.ssmch@gov.bd', 'pass123', 'ADMIN', 3);

-- Hospital 4: Labaid Hospital
INSERT INTO Hospitals (name, address, contact_number) VALUES ('Labaid Hospital', 'Dhanmondi, Dhaka', '01744444444');
INSERT INTO Users (email, password_hash, role, hospital_id) VALUES ('admin.labaid@hospital.com', 'pass123', 'ADMIN', 4);

-- =========================================================================
-- 3. INSERT DOCTORS
-- =========================================================================

-- Dr. Maruf -> Dhaka Medical College (Hospital 1)
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('DOC-001', 'Maruf Ahmed Tamal', 'maruf.tamal@dmc.gov.bd', 'pass123', 'DOCTOR', 1);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees, years_of_experience) VALUES (LAST_INSERT_ID(), 1, 'Cardiology', 'BMDC-1001', 'MBBS, MD', 15);

-- Dr. Alisha -> United Hospital (Hospital 2)
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('DOC-002', 'Alisha Kabir', 'alisha.kabir@united.com', 'pass123', 'DOCTOR', 2);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees, years_of_experience) VALUES (LAST_INSERT_ID(), 2, 'Neurology', 'BMDC-1002', 'MBBS, FCPS', 8);

-- Dr. Faria -> Shaheed Suhrawardy (Hospital 3)
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('DOC-003', 'Faria Alam', 'faria.alam@ssmch.gov.bd', 'pass123', 'DOCTOR', 3);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees, years_of_experience) VALUES (LAST_INSERT_ID(), 3, 'Dermatologist', 'BMDC-1003', 'MBBS, DDV', 5);

-- Dr. Sameha -> Labaid Hospital (Hospital 4)
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('DOC-004', 'Sameha Kamrul', 'sameha.kamrul@labaid.com', 'pass123', 'DOCTOR', 4);
INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees, years_of_experience) VALUES (LAST_INSERT_ID(), 4, 'Orthopedics', 'BMDC-1004', 'MBBS, MS (Ortho)', 10);

-- =========================================================================
-- 4. INSERT PATIENTS
-- =========================================================================

INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025235', 'Ummey Habiba Pranty', 'pranty@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025235', 'Ummey Habiba Pranty', '1995-08-20', 'FEMALE', 'AB+', '01822222222', 60.00, 160.00);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025236', 'Bijoy Sarkar', 'bijoy@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025236', 'Bijoy Sarkar', '2004-01-01', 'MALE', 'O+', '01733333333', 70.00, 175.00);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025237', 'Saber Hossen', 'saber@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025237', 'Saber Hossen', '2003-05-15', 'MALE', 'B+', '01644444444', 68.00, 170.00);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025238', 'Tasnuva', 'tasnuva38@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025238', 'Tasnuva', '2002-04-12', 'FEMALE', 'A+', '01711000038', 55.0, 160.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025239', 'Fahim Siddique', 'fahim39@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025239', 'Fahim Siddique', '2001-08-25', 'MALE', 'B+', '01711000039', 72.5, 175.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025240', 'Nayeem Uddin', 'nayeem40@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025240', 'Nayeem Uddin', '2000-11-05', 'MALE', 'O+', '01711000040', 68.0, 172.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025241', 'Hasib Bin Hossain', 'hasib41@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025241', 'Hasib Bin Hossain', '2003-02-18', 'MALE', 'AB+', '01711000041', 75.0, 178.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025242', 'Irfanur Rahman', 'irfanur42@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025242', 'Irfanur Rahman', '2002-07-30', 'MALE', 'A+', '01711000042', 65.5, 169.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025243', 'Ragib Mahtab Oyon', 'ragib43@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025243', 'Ragib Mahtab Oyon', '2001-09-14', 'MALE', 'B+', '01711000043', 70.0, 174.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025244', 'Arafat Hossain', 'arafat44@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025244', 'Arafat Hossain', '2000-03-22', 'MALE', 'O+', '01711000044', 74.0, 176.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025245', 'Adiba Binte Atique', 'adiba45@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025245', 'Adiba Binte Atique', '2003-12-10', 'FEMALE', 'AB+', '01711000045', 52.0, 158.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025246', 'Jonayed Ahmed', 'jonayed46@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025246', 'Jonayed Ahmed', '2002-05-08', 'MALE', 'A+', '01711000046', 69.0, 173.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025247', 'Tanvir Ahmed Zidan', 'tanvir47@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025247', 'Tanvir Ahmed Zidan', '2001-01-20', 'MALE', 'B+', '01711000047', 71.5, 175.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025248', 'Momotaz Jahan Momo', 'momotaz48@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025248', 'Momotaz Jahan Momo', '2002-08-16', 'FEMALE', 'O+', '01711000048', 56.0, 161.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025249', 'Mahin Ar Rahman', 'mahin49@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025249', 'Mahin Ar Rahman', '2003-06-25', 'MALE', 'AB+', '01711000049', 66.0, 170.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025250', 'Miraz Morshed', 'miraz50@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025250', 'Miraz Morshed', '2000-09-02', 'MALE', 'A+', '01711000050', 73.0, 177.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025251', 'Saimum Sadman Joy', 'saimum51@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025251', 'Saimum Sadman Joy', '2001-10-18', 'MALE', 'B+', '01711000051', 68.5, 171.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025252', 'Fojla Rabbi', 'fojla52@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025252', 'Fojla Rabbi', '2002-11-30', 'MALE', 'O+', '01711000052', 77.0, 179.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025253', 'Azmain Hasan Abir', 'azmain53@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025253', 'Azmain Hasan Abir', '2003-04-05', 'MALE', 'AB+', '01711000053', 70.5, 174.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025254', 'Zarin Tasnim', 'zarin54@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025254', 'Zarin Tasnim', '2002-01-14', 'FEMALE', 'A+', '01711000054', 54.0, 160.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025255', 'Rifah Tahsin', 'rifah55@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025255', 'Rifah Tahsin', '2001-03-29', 'FEMALE', 'B+', '01711000055', 53.5, 159.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025256', 'Farhan Sadik Tahsin', 'farhan56@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025256', 'Farhan Sadik Tahsin', '2000-07-11', 'MALE', 'O+', '01711000056', 76.0, 178.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025257', 'Afia Mahmuda Ema', 'afia57@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025257', 'Afia Mahmuda Ema', '2003-09-08', 'FEMALE', 'AB+', '01711000057', 58.0, 163.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025258', 'Raisa Rahman', 'raisa58@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025258', 'Raisa Rahman', '2002-12-19', 'FEMALE', 'A+', '01711000058', 51.5, 157.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025259', 'Sazidul Islam Bayeid', 'sazidul59@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025259', 'Sazidul Islam Bayeid', '2001-05-27', 'MALE', 'B+', '01711000059', 69.5, 172.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025260', 'Mabsurah Ferdous', 'mabsurah60@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025260', 'Mabsurah Ferdous', '2003-08-04', 'FEMALE', 'O+', '01711000060', 57.0, 162.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025261', 'Sabit Ur Zaman Arpon', 'sabit61@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025261', 'Sabit Ur Zaman Arpon', '2000-02-14', 'MALE', 'AB+', '01711000061', 72.0, 175.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025262', 'Kaif Rahman', 'kaif62@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025262', 'Kaif Rahman', '2002-10-22', 'MALE', 'A+', '01711000062', 67.5, 171.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025263', 'Khandokar Saiful Islam Sayef', 'saiful63@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025263', 'Khandokar Saiful Islam Sayef', '2001-04-17', 'MALE', 'B+', '01711000063', 78.0, 180.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025264', 'Sumaiya Fahmida', 'sumaiya64@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025264', 'Sumaiya Fahmida', '2003-01-09', 'FEMALE', 'O+', '01711000064', 55.5, 161.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025265', 'Nujhat Nazifa Nawal', 'nujhat65@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025265', 'Nujhat Nazifa Nawal', '2002-06-28', 'FEMALE', 'AB+', '01711000065', 59.0, 164.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025266', 'Turjoy Barua', 'turjoy66@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025266', 'Turjoy Barua', '2000-12-03', 'MALE', 'A+', '01711000066', 71.0, 173.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025267', 'Muhibuzzaman Khan', 'muhibuzzaman67@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025267', 'Muhibuzzaman Khan', '2001-08-19', 'MALE', 'B+', '01711000067', 74.5, 176.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025268', 'Samiha Anjum Nishat', 'samiha68@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025268', 'Samiha Anjum Nishat', '2003-11-21', 'FEMALE', 'O+', '01711000068', 53.0, 159.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025269', 'Tasnuva Jabin Rimpa', 'rimpa69@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025269', 'Tasnuva Jabin Rimpa', '2002-03-15', 'FEMALE', 'AB+', '01711000069', 56.5, 160.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025270', 'Syed Zahin Waheed', 'zahin70@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025270', 'Syed Zahin Waheed', '2000-09-26', 'MALE', 'A+', '01711000070', 73.5, 177.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025271', 'Fariha Nowshin', 'fariha71@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025271', 'Fariha Nowshin', '2001-07-07', 'FEMALE', 'B+', '01711000071', 54.5, 158.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025272', 'Mhamuda Shafiq Shamme', 'mhamuda72@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025272', 'Mhamuda Shafiq Shamme', '2003-02-28', 'FEMALE', 'O+', '01711000072', 58.5, 163.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025273', 'Touhid Islam', 'touhid73@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025273', 'Touhid Islam', '2002-10-12', 'MALE', 'AB+', '01711000073', 70.0, 174.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025274', 'Nasimul Goni', 'nasimul74@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025274', 'Nasimul Goni', '2000-05-01', 'MALE', 'A+', '01711000074', 76.5, 179.5);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025275', 'Nazifa Tabassum', 'nazifa75@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025275', 'Nazifa Tabassum', '2001-12-17', 'FEMALE', 'B+', '01711000075', 52.5, 157.0);
INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES ('PT-0025276', 'Tanvir Foysal', 'foysal76@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025276', 'Tanvir Foysal', '2003-03-09', 'MALE', 'O+', '01711000076', 68.0, 172.0);

INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025277', 'shupti.safatul@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025277', 'Mst. Safatul Jannat Shupti', '2001-04-12', 'FEMALE', 'A+', '01711000101', 54.5, 158.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025278', 'fahim.zahid@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025278', 'Fahim Fazle Aziz Bin Zahid', '1999-11-23', 'MALE', 'B+', '01711000102', 72.0, 175.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025279', 'shariar.tamim@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025279', 'Md. Shariar Tamim', '2002-08-05', 'MALE', 'O+', '01711000103', 68.5, 172.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025280', 'saidul.maruf@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025280', 'Md Saidul Islam Moaji Maruf', '2000-01-19', 'MALE', 'AB+', '01711000104', 76.0, 178.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025281', 'tahia.masud@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025281', 'Tahia Masud', '2003-09-30', 'FEMALE', 'A-', '01711000105', 52.0, 160.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025282', 'mehrin.fatema@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025282', 'Mehrin Fatema', '2001-12-14', 'FEMALE', 'B+', '01711000106', 56.0, 162.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025283', 'najifa.raksanda@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025283', 'Najifa Raksanda', '2002-05-22', 'FEMALE', 'O+', '01711000107', 58.0, 165.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025284', 'shafiqul.islam@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025284', 'Sayed Md. Shafiqul Islam', '1998-03-10', 'MALE', 'AB-', '01711000108', 70.0, 174.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025285', 'mobin.talukder@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025285', 'Al- Mobin Talukder', '2000-07-28', 'MALE', 'A+', '01711000109', 74.5, 177.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025286', 'fahim.mayaz@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025286', 'Fahim Azmul Hasan Mayaz', '2003-02-15', 'MALE', 'B-', '01711000110', 69.0, 171.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025287', 'akhlak.jaman@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025287', 'Md. Akhlak Ud Jaman', '2001-10-09', 'MALE', 'O-', '01711000111', 75.0, 176.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025288', 'mahdi.mahir@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025288', 'Md. Mahdi Israk Mahir', '2002-06-18', 'MALE', 'AB+', '01711000112', 68.0, 170.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025289', 'maliha.tasnim@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025289', 'Maliha Tasnim', '2004-01-25', 'FEMALE', 'A+', '01711000113', 53.5, 159.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025290', 'mantaka.mahir@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025290', 'Mantaka Mahir', '2000-08-08', 'MALE', 'B+', '01711000114', 71.0, 173.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025291', 'rafsen.asif@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025291', 'Rafsen Jony Asif', '1999-05-11', 'MALE', 'O+', '01711000115', 73.5, 176.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025292', 'faisal.saad@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025292', 'Faisal Ahmed Saad', '2001-03-29', 'MALE', 'A-', '01711000116', 67.0, 171.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025293', 'aria.ashka@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025293', 'Aria Nawshin Ashka', '2003-11-04', 'FEMALE', 'B-', '01711000117', 55.0, 161.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025294', 'zubaer.rafi@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025294', 'Zubaer Islam Rafi', '2000-09-17', 'MALE', 'AB+', '01711000118', 78.0, 180.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025295', 'anir.ahammed@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025295', 'Anir Ahammed', '2002-12-01', 'MALE', 'O+', '01711000119', 66.5, 169.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025296', 'lubaina.sakhawat@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025296', 'Lubaina Sakhawat', '2001-02-28', 'FEMALE', 'A+', '01711000120', 57.5, 163.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025297', 'tanvir.rafi@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025297', 'Tanvir Ahmed Rafi', '1999-07-07', 'MALE', 'B+', '01711000121', 70.0, 174.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025298', 'sanzida.afroz@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025298', 'Sanzida Afroz', '2000-04-19', 'FEMALE', 'O-', '01711000122', 51.0, 157.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025299', 'farat.naim@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025299', 'Farat Fahmin Naim', '2003-06-11', 'MALE', 'AB-', '01711000123', 69.5, 172.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025300', 'tahmid.hossain@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025300', 'Tahmid Hossain', '2001-08-22', 'MALE', 'A+', '01711000124', 75.5, 177.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025301', 'tuzana.arna@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025301', 'Tuzana Shahrin Arna', '2002-10-03', 'FEMALE', 'B+', '01711000125', 59.0, 164.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025302', 'swagata.mallick@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025302', 'Swagata Mallick', '1998-11-15', 'FEMALE', 'O+', '01711000126', 55.5, 160.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025303', 'safia.tasnim@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025303', 'Safia Tasnim', '2004-03-27', 'FEMALE', 'A-', '01711000127', 53.0, 158.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025304', 'rakibul.rony@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025304', 'Rakibul Islam Rony', '2000-01-05', 'MALE', 'B-', '01711000128', 71.5, 174.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025305', 'nusrat.mahina@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025305', 'Nusrat Jahan Mahina', '2001-05-14', 'FEMALE', 'O-', '01711000129', 58.5, 166.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025306', 'reajual.nayem@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025306', 'Reajual Islam Nayem', '2003-07-09', 'MALE', 'AB+', '01711000130', 68.0, 170.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025307', 'nitun.swapnil@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025307', 'Nitun Kundu Swapnil', '1999-09-22', 'MALE', 'A+', '01711000131', 74.0, 176.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025308', 'kamrul.erfan@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025308', 'Kamrul Hasan Miazi Erfan', '2002-12-31', 'MALE', 'B+', '01711000132', 77.5, 179.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025309', 'mohammad.rahman@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025309', 'Mohammad Mushfiqur Rahman', '2000-02-18', 'MALE', 'O+', '01711000133', 69.5, 173.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025310', 'chowdhury.jamal@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025310', 'Chowdhury Shah Noor Binte Jamal', '2001-08-01', 'FEMALE', 'AB-', '01711000134', 60.0, 167.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025311', 'rafsan.ratul@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025311', 'Md Rafsan Hasan Ratul', '2003-10-15', 'MALE', 'A+', '01711000135', 72.5, 175.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025312', 'farhan.mugdho@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025312', 'Farhan Intesar Mugdho', '1998-06-04', 'MALE', 'B+', '01711000136', 79.0, 181.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025313', 'shamim.limon@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025313', 'Md. Shamim Ahmed Limon', '2000-11-20', 'MALE', 'O+', '01711000137', 65.0, 168.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025314', 'ashab.raiyan@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025314', 'Ashab Ahmad Raiyan', '2002-04-29', 'MALE', 'A-', '01711000138', 71.0, 172.5);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025315', 'arif.molla@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025315', 'Md. Arif Sadik Molla', '1999-01-11', 'MALE', 'B-', '01711000139', 67.5, 170.0);
INSERT INTO Users (system_id, email, password_hash, role, hospital_id) VALUES ('PT-0025316', 'nazifa.rahman@gmail.com', 'pass123', 'PATIENT', NULL);
INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (LAST_INSERT_ID(), 'PT-0025316', 'Nazifa Rahman', '2001-09-02', 'FEMALE', 'O+', '01711000140', 56.5, 162.0);


-- =========================================================================
-- 5. PERFECTED DISTRIBUTION APPOINTMENT SCRIPT (FIXED)
-- =========================================================================

-- DELETE old appointments first to prevent clutter


-- 1. Create PAST VISITS (This fills the Chart)
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, reason, created_by)
SELECT 
    p.id, 
    d.id, 
    d.hospital_id, 
    DATE_SUB(CURDATE(), INTERVAL (p.id % 6) + 1 DAY), 
    'COMPLETED', 
    'Routine Checkup',
    (SELECT id FROM Users WHERE role='ADMIN' AND hospital_id = d.hospital_id LIMIT 1)
FROM Patients p
JOIN Doctors d ON d.id = (p.id % 4) + 1;



-- 2. Create TODAY & FUTURE APPOINTMENTS (This fills "Today's Schedule" and "Upcoming")
INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, reason, created_by)
SELECT p.id, d.id, d.hospital_id, 
    -- Picks a day between TODAY (0) and 10 days from now
    DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 11) DAY) + INTERVAL (9 + (p.id % 8)) HOUR, 
    'SCHEDULED', 'Follow-up', 1
FROM Patients p JOIN Doctors d ON d.id = (p.id % 4) + 1;

-- =========================================================================
-- 6. FILL MEDICAL HISTORIES, PRESCRIPTIONS, AND TEST REPORTS
-- =========================================================================

-- Add 1 Diagnosis to EVERY patient
INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date)
SELECT id, (id % 4) + 1, (id % 4) + 1, 
    CASE WHEN id % 3 = 0 THEN 'Asthma (Mild)' WHEN id % 2 = 0 THEN 'Type 2 Diabetes' ELSE 'Hypertension' END, 
    DATE_SUB(CURDATE(), INTERVAL (id % 40) DAY) 
FROM Patients;

-- Add 1 Prescription to EVERY patient
INSERT INTO Prescriptions (patient_id, doctor_id, hospital_id, prescription_date, notes)
SELECT id, (id % 4) + 1, (id % 4) + 1, DATE_SUB(CURDATE(), INTERVAL (id % 15) DAY), 'Routine follow-up medication'
FROM Patients;

-- Add 2 Medications to EVERY Prescription
INSERT INTO Prescription_Items (prescription_id, medicine_name, dosage, frequency, duration, instructions)
SELECT id, 'Paracetamol', '500mg', '1-1-1', '5 Days', 'After meals' FROM Prescriptions;

INSERT INTO Prescription_Items (prescription_id, medicine_name, dosage, frequency, duration, instructions)
SELECT id, CASE WHEN patient_id % 2 = 0 THEN 'Metformin' ELSE 'Amlodipine' END, '50mg', '0-0-1', '30 Days', 'Before sleeping' FROM Prescriptions;

-- Add 1 Test Report to EVERY patient
INSERT INTO Test_Reports (patient_id, added_by_admin_id, hospital_id, report_type, file_url, report_date, notes)
SELECT p.id, 
    (SELECT id FROM Users WHERE role='ADMIN' AND hospital_id = (p.id % 4) + 1 LIMIT 1), 
    (p.id % 4) + 1, 
    CASE WHEN p.id % 2 = 0 THEN 'Complete Blood Count (CBC)' ELSE 'Chest X-Ray' END, 
    '/docs/test_report.pdf', 
    DATE_SUB(CURDATE(), INTERVAL (p.id % 20) DAY), 
    'Report shows normal bounds.' 
FROM Patients p;