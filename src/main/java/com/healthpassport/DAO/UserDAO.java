package com.healthpassport.MODEL.dao;

import com.healthpassport.MODEL.user.Admin;
import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.MODEL.user.Patient;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {


    public User authenticate(String identifier, String password) {
        String query = "SELECT id, system_id, full_name, email, password_hash, role, hospital_id FROM Users WHERE (email = ? OR system_id = ?) AND password_hash = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // raw data extractUsers table
                int internalId = rs.getInt("id");
                String systemId = rs.getString("system_id");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String passHash = rs.getString("password_hash");
                Role role = Role.valueOf(rs.getString("role"));

                int hospId = rs.getInt("hospital_id");
                int finalHospitalId = rs.wasNull() ? -1 : hospId;


                User authenticatedUser = null;

                switch (role) {
                    case ADMIN:
                        authenticatedUser = new Admin();
                        break;
                    case DOCTOR:
                        authenticatedUser = new Doctor();
                        break;
                    case PATIENT:
                        authenticatedUser = new Patient();
                        break;
                }


                if (authenticatedUser != null) {
                    authenticatedUser.setId(internalId);
                    authenticatedUser.setSystemId(systemId);
                    authenticatedUser.setFullName(fullName);
                    authenticatedUser.setEmail(email);
                    authenticatedUser.setPasswordHash(passHash);
                    authenticatedUser.setRole(role);
                    authenticatedUser.setHospitalId(finalHospitalId);
                }

                return authenticatedUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}