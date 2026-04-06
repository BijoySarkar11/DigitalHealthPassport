package com.healthpassport.MODEL.user;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Doctor extends User {
    private String specialization;
    private String licenseNumber;
    private String degrees;

    public Doctor() {
        this.setRole(Role.DOCTOR);
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getDegrees() { return degrees; }
    public void setDegrees(String degrees) { this.degrees = degrees; }

    @Override
    public String getProfileSummary() {
        return "Dr. " + getFullName() + " - " + specialization + " (" + licenseNumber + ")";
    }
/*
    @Override
    public boolean saveToDatabase() {
        int baseUserId = saveBaseUserRecord(); // inherited
        if (baseUserId == -1) return false;

        String query = "INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, baseUserId);
            stmt.setInt(2, getHospitalId());
            stmt.setString(3, specialization);
            stmt.setString(4, licenseNumber);
            stmt.setString(5, degrees);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateInDatabase() {
        if (!updateBaseUserRecord()) return false;

        String query = "UPDATE Doctors SET specialization = ?, license_number = ?, degrees = ? WHERE user_id = (SELECT id FROM Users WHERE system_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, specialization);
            stmt.setString(2, licenseNumber);
            stmt.setString(3, degrees);
            stmt.setString(4, getSystemId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

 */
}