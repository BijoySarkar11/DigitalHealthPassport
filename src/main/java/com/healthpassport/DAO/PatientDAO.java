package com.healthpassport.DAO;

import com.healthpassport.MODEL.user.Patient;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PatientDAO {

    // Fetch the patient profile using the authenticated User's ID
    public Patient getPatientProfileByUserId(int userId) {
        String query = "SELECT * FROM Patients WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Convert SQL Date to Java LocalDate
                LocalDate dob = rs.getDate("date_of_birth").toLocalDate();

                return new Patient(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("national_id"),
                        rs.getString("full_name"),
                        dob,
                        rs.getString("gender"),
                        rs.getString("blood_group"),
                        rs.getString("phone"),
                        rs.getDouble("weight"), // NEW: Fetches weight from database
                        rs.getDouble("height")  // NEW: Fetches height from database
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient profile: " + e.getMessage());
        }
        return null;
    }
}