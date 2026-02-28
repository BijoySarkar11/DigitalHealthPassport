CREATE DATABASE IF NOT EXISTS health_passport_db;
USE health_passport_db;

-- ----------------- ADMINS -----------------
CREATE TABLE IF NOT EXISTS admins (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
    );
-- Using 'INSERT IGNORE' prevents errors if the user already exists
INSERT IGNORE INTO admins (username, password) VALUES ('admin123', 'pass123');

-- ----------------- PATIENTS -----------------
CREATE TABLE IF NOT EXISTS patients (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
    );
INSERT IGNORE INTO patients (username, password) VALUES ('patient1', 'pass1');

-- ----------------- DOCTORS -----------------
CREATE TABLE IF NOT EXISTS doctors (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
    );
INSERT IGNORE INTO doctors (username, password) VALUES ('doctor1', 'pass1');