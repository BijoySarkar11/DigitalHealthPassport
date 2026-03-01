package com.healthpassport.DAO;

import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorDAO {

    public Doctor getDoctorProfileByUserId(int userId) {
        String query = "SELECT * FROM Doctors WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Doctor(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("hospital_id"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getInt("years_of_experience")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor profile: " + e.getMessage());
        }
        return null;
    }
}