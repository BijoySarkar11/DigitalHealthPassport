package com.healthpassport.MODEL.user;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

public abstract class User extends Person {
    private String systemId;
    private String email;
    private String passwordHash;
    private Role role;
    private int hospitalId;

    public User() {}

    public String getSystemId() { return systemId; }
    public void setSystemId(String systemId) { this.systemId = systemId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }


    protected int saveBaseUserRecord() {
        String query = "INSERT INTO Users (system_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, getSystemId());
            stmt.setString(2, getFullName());
            stmt.setString(3, getEmail());
            stmt.setString(4, getPasswordHash());
            stmt.setString(5, getRole().name());

            if (getHospitalId() > 0) {
                stmt.setInt(6, getHospitalId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                this.setId(generatedId);
                return generatedId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    protected boolean updateBaseUserRecord() {
        String query = "UPDATE Users SET full_name = ?, email = ? WHERE system_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, getFullName());
            stmt.setString(2, getEmail());
            stmt.setString(3, getSystemId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}