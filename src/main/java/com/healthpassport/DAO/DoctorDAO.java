package com.healthpassport.MODEL.dao;

import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO implements IDAO<Doctor> {

    @Override
    public boolean create(Doctor doctor) {
        String insertUserQuery = "INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, 'DOCTOR', ?)";
        String insertDoctorQuery = "INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int newUserId = -1;

            try (PreparedStatement stmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmtUser.setString(1, doctor.getSystemId());
                stmtUser.setString(2, doctor.getFullName());
                stmtUser.setString(3, doctor.getEmail());
                stmtUser.setString(4, doctor.getPasswordHash());
                stmtUser.setInt(5, doctor.getHospitalId());
                stmtUser.executeUpdate();
                ResultSet rs = stmtUser.getGeneratedKeys();
                if (rs.next()) newUserId = rs.getInt(1);
            }

            if (newUserId != -1) {
                try (PreparedStatement stmtDoc = conn.prepareStatement(insertDoctorQuery)) {
                    stmtDoc.setInt(1, newUserId);
                    stmtDoc.setInt(2, doctor.getHospitalId());
                    stmtDoc.setString(3, doctor.getSpecialization());
                    stmtDoc.setString(4, doctor.getLicenseNumber());
                    stmtDoc.setString(5, doctor.getDegrees());
                    stmtDoc.executeUpdate();
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
    public boolean update(Doctor doctor) {
        String updateUser = "UPDATE Users SET full_name = ?, email = ? WHERE system_id = ?";
        String updateDoctor = "UPDATE Doctors SET specialization = ?, license_number = ?, degrees = ? WHERE user_id = (SELECT id FROM Users WHERE system_id = ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement(updateUser)) {
                st.setString(1, doctor.getFullName());
                st.setString(2, doctor.getEmail());
                st.setString(3, doctor.getSystemId());
                st.executeUpdate();
            }

            try (PreparedStatement st = conn.prepareStatement(updateDoctor)) {
                st.setString(1, doctor.getSpecialization());
                st.setString(2, doctor.getLicenseNumber());
                st.setString(3, doctor.getDegrees());
                st.setString(4, doctor.getSystemId());
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
        String query = "DELETE FROM Users WHERE system_id = ? AND role = 'DOCTOR'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, systemId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public Doctor findBySystemId(String systemId) { return null; }

    @Override
    public List<Doctor> findAll() { return new ArrayList<>(); }

     */
}