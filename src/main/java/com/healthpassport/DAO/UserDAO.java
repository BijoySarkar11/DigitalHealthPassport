package com.healthpassport.DAO;

import com.healthpassport.MODEL.user.Role;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Authenticate using EITHER Email OR National ID
    public User authenticate(String identifier, String password) {
        String query = "SELECT * FROM Users WHERE (email = ? OR national_id = ?) AND password_hash = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Safely extract hospital_id (handles SQL NULL values)
                int hospId = rs.getInt("hospital_id");
                Integer hospitalId = rs.wasNull() ? null : hospId;

                // Extract the newly added full_name column from the database
                String fullName = rs.getString("full_name");

                // Create and return the OOP User object
                return new User(
                        rs.getInt("id"),
                        rs.getString("national_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")), // Converts SQL string to Java Enum
                        hospitalId,
                        rs.getBoolean("is_active"),
                        fullName // NEW: Pass the full name to the constructor
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if login fails
    }
}