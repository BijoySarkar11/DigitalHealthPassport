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

                // FIXED: The order now perfectly matches the new Patient constructor
                // Patient(id, fullName, userId, nationalId, dateOfBirth, gender, bloodGroup, phone, weight, height)
                return new Patient(
                        rs.getInt("id"),                // 1. id
                        rs.getString("full_name"),      // 2. fullName (Moved to 2nd position)
                        rs.getInt("user_id"),           // 3. userId
                        rs.getString("national_id"),    // 4. nationalId
                        dob,                            // 5. dateOfBirth
                        rs.getString("gender"),         // 6. gender
                        rs.getString("blood_group"),    // 7. bloodGroup
                        rs.getString("phone"),          // 8. phone
                        rs.getDouble("weight"),         // 9. weight
                        rs.getDouble("height")          // 10. height
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient profile: " + e.getMessage());
        }
        return null;
    }
}