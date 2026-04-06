package com.healthpassport.MODEL.user;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Admin extends User {
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalContact;

    public Admin() {
        this.setRole(Role.ADMIN);
    }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }

    public String getHospitalContact() { return hospitalContact; }
    public void setHospitalContact(String hospitalContact) { this.hospitalContact = hospitalContact; }

    @Override
    public String getProfileSummary() {
        return "Administrator: " + getFullName() + " | Hospital: " + (hospitalName != null ? hospitalName : "Global Network");
    }
/*
    @Override
    public boolean saveToDatabase() {
        // If this admin represents a new hospital, create the Hospitals record first
        if (getHospitalId() <= 0 && hospitalName != null && !hospitalName.isEmpty()) {
            String hospQuery = "INSERT INTO Hospitals (name, address, contact_number) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(hospQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, hospitalName);
                stmt.setString(2, hospitalAddress != null ? hospitalAddress : "");
                stmt.setString(3, hospitalContact != null ? hospitalContact : "");
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    setHospitalId(rs.getInt(1)); // Link the generated ID to this Admin
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        //Admin user credentials save
        return saveBaseUserRecord() != -1;
    }

    @Override
    public boolean updateInDatabase() {
        boolean userUpdated = updateBaseUserRecord();

        // linked hos details update simultaneouslyyy
        if (userUpdated && getHospitalId() > 0 && hospitalName != null) {
            String hospQuery = "UPDATE Hospitals SET name = ?, address = ?, contact_number = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(hospQuery)) {
                stmt.setString(1, hospitalName);
                stmt.setString(2, hospitalAddress);
                stmt.setString(3, hospitalContact);
                stmt.setInt(4, getHospitalId());
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userUpdated;
    }

 */
}