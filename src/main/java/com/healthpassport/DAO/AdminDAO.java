package com.healthpassport.DAO;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AdminDAO {
    public boolean authenticate(String username, String password) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}