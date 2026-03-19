package com.healthpassport.DAO;

import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorDAO {

    public Doctor getDoctorProfileByUserId(int userId) {
        // We use a JOIN here to fetch the full_name from the Users table
        // to satisfy the new Person base class constructor requirement!
        String query = """
            SELECT d.*, u.full_name 
            FROM Doctors d 
            JOIN Users u ON d.user_id = u.id 
            WHERE d.user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // FIXED: The order now perfectly matches the new Doctor constructor
                // Doctor(id, fullName, userId, hospitalId, specialization, licenseNumber, yearsOfExperience)
                return new Doctor(
                        rs.getInt("id"),                   // 1. id
                        rs.getString("full_name"),         // 2. fullName (Fetched via the JOIN)
                        rs.getInt("user_id"),              // 3. userId
                        rs.getInt("hospital_id"),          // 4. hospitalId
                        rs.getString("specialization"),    // 5. specialization
                        rs.getString("license_number"),    // 6. licenseNumber
                        rs.getInt("years_of_experience")   // 7. yearsOfExperience
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor profile: " + e.getMessage());
        }
        return null;
    }
}