package com.healthpassport.MODEL.dao;

import com.healthpassport.MODEL.user.Admin;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO implements IDAO<Admin> {

    @Override
    public boolean create(Admin admin) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Hospital banano
            if (admin.getHospitalId() <= 0 && admin.getHospitalName() != null && !admin.getHospitalName().isEmpty()) {
                String hospQuery = "INSERT INTO Hospitals (name, address, contact_number) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(hospQuery, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, admin.getHospitalName());
                    stmt.setString(2, admin.getHospitalAddress() != null ? admin.getHospitalAddress() : "");
                    stmt.setString(3, admin.getHospitalContact() != null ? admin.getHospitalContact() : "");
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) admin.setHospitalId(rs.getInt(1));
                }
            }

            // User banano
            String userQuery = "INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, 'ADMIN', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, admin.getSystemId());
                stmt.setString(2, admin.getFullName());
                stmt.setString(3, admin.getEmail());
                stmt.setString(4, admin.getPasswordHash());
                if (admin.getHospitalId() > 0) stmt.setInt(5, admin.getHospitalId());
                else stmt.setNull(5, Types.INTEGER);

                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) admin.setId(rs.getInt(1));
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Admin admin) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String userQuery = "UPDATE Users SET full_name = ?, email = ? WHERE system_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, admin.getFullName());
                stmt.setString(2, admin.getEmail());
                stmt.setString(3, admin.getSystemId());
                stmt.executeUpdate();
            }

            if (admin.getHospitalId() > 0 && admin.getHospitalName() != null) {
                String hospQuery = "UPDATE Hospitals SET name = ?, address = ?, contact_number = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(hospQuery)) {
                    stmt.setString(1, admin.getHospitalName());
                    stmt.setString(2, admin.getHospitalAddress());
                    stmt.setString(3, admin.getHospitalContact());
                    stmt.setInt(4, admin.getHospitalId());
                    stmt.executeUpdate();
                }
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
        String query = "DELETE FROM Users WHERE system_id = ? AND role = 'ADMIN'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, systemId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public Admin findBySystemId(String systemId) {
        // Implementation for finding a specific Admin (Returns Admin object)
        return null;
    }

    @Override
    public List<Admin> findAll() {
        return new ArrayList<>();
    }

     */
}