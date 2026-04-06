package com.healthpassport.MODEL.dao;

import com.healthpassport.MODEL.user.Patient;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO implements IDAO<Patient> {

    @Override
    public boolean create(Patient patient) {
        String insertUserQuery = "INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, 'PATIENT', ?)";
        String insertPatientQuery = "INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int newUserId = -1;

            // User table e insert
            try (PreparedStatement stmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmtUser.setString(1, patient.getSystemId());
                stmtUser.setString(2, patient.getFullName());
                stmtUser.setString(3, patient.getEmail());
                stmtUser.setString(4, patient.getPasswordHash());
                if (patient.getHospitalId() > 0) stmtUser.setInt(5, patient.getHospitalId());
                else stmtUser.setNull(5, Types.INTEGER);

                stmtUser.executeUpdate();
                ResultSet rs = stmtUser.getGeneratedKeys();
                if (rs.next()) newUserId = rs.getInt(1);
            }

            //Patients table e insert
            if (newUserId != -1) {
                try (PreparedStatement stmtPat = conn.prepareStatement(insertPatientQuery)) {
                    stmtPat.setInt(1, newUserId);
                    stmtPat.setString(2, patient.getSystemId());
                    stmtPat.setString(3, patient.getFullName());
                    stmtPat.setDate(4, java.sql.Date.valueOf(patient.getDateOfBirth()));
                    stmtPat.setString(5, patient.getGender());
                    stmtPat.setString(6, patient.getBloodGroup());
                    stmtPat.setString(7, patient.getPhone());
                    stmtPat.setDouble(8, patient.getWeight());
                    stmtPat.setDouble(9, patient.getHeight());
                    stmtPat.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Patient patient) {
        String updateUser = "UPDATE Users SET full_name = ?, email = ? WHERE system_id = ?";
        String updatePatient = "UPDATE Patients SET full_name = ?, date_of_birth = ?, gender = ?, blood_group = ?, phone = ?, weight = ?, height = ? WHERE system_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement(updateUser)) {
                st.setString(1, patient.getFullName());
                st.setString(2, patient.getEmail());
                st.setString(3, patient.getSystemId());
                st.executeUpdate();
            }

            try (PreparedStatement st = conn.prepareStatement(updatePatient)) {
                st.setString(1, patient.getFullName());
                st.setDate(2, java.sql.Date.valueOf(patient.getDateOfBirth()));
                st.setString(3, patient.getGender());
                st.setString(4, patient.getBloodGroup());
                st.setString(5, patient.getPhone());
                st.setDouble(6, patient.getWeight());
                st.setDouble(7, patient.getHeight());
                st.setString(8, patient.getSystemId());
                st.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
/*
    @Override
    public boolean delete(String systemId) {

        String query = "DELETE FROM Users WHERE system_id = ? AND role = 'PATIENT'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, systemId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public Patient findBySystemId(String systemId) { return null; }

    @Override
    public List<Patient> findAll() { return new ArrayList<>(); }

 */
}