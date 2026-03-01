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
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('DOCTOR', 'PATIENT', 'ADMIN') NOT NULL,
    hospital_id INT, -- Nullable for Patients (since they are national)
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

-- --------------------------------------------------------
-- DUMMY DATA FOR INITIAL TESTING
-- --------------------------------------------------------

-- Insert a Hospital
INSERT INTO Hospitals (name, address, contact_number)
VALUES ('Dhaka Medical College', 'Dhaka, Bangladesh', '01711111111');

-- Insert an Admin for the Hospital
INSERT INTO Users (email, password_hash, role, hospital_id)
VALUES ('admin@dmc.gov.bd', 'hashed_pass123', 'ADMIN', 1);

-- Insert a Doctor
INSERT INTO Users (national_id, email, password_hash, role, hospital_id)
VALUES ('DOC-999', 'doctor@dmc.gov.bd', 'hashed_pass123', 'DOCTOR', 1);

INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, years_of_experience)
VALUES (2, 1, 'Cardiology', 'BMDC-12345', 10);

-- Insert a Patient (Updated for Pranty)
INSERT INTO Users (national_id, email, password_hash, role, hospital_id)
VALUES ('PT-0025235', 'pranty@gmail.com', 'hashed_pass123', 'PATIENT', NULL);

INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height)
VALUES (3, 'PT-0025235', 'Pranty', '1995-08-20', 'FEMALE', 'AB+', '01822222222', 60.00, 160.00);