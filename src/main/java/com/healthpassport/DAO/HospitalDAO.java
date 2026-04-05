/*
package com.healthpassport.DAO;

import com.healthpassport.MODEL.user.Hospital;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HospitalDAO {

    public Hospital getHospitalById(int hospitalId) {
        String query = "SELECT * FROM Hospitals WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, hospitalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Hospital(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contact_number")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching hospital: " + e.getMessage());
        }
        return null;
    }
}

 */