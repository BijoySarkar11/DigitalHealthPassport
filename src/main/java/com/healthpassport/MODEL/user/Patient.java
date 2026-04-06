package com.healthpassport.MODEL.user;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class Patient extends User {
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private double weight;
    private double height;

    public Patient() {
        this.setRole(Role.PATIENT);
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String getProfileSummary() {
        return "Patient: " + getFullName() + " | Blood: " + bloodGroup + " | Vitals: " + weight + "kg, " + height + "cm";
    }
/*
    @Override
    public boolean saveToDatabase() {
        int baseUserId = saveBaseUserRecord();

        String query = "INSERT INTO Patients (user_id, system_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, baseUserId);
            stmt.setString(2, getSystemId());
            stmt.setString(3, getFullName());
            stmt.setDate(4, java.sql.Date.valueOf(dateOfBirth));
            stmt.setString(5, gender);
            stmt.setString(6, bloodGroup);
            stmt.setString(7, getPhone());
            stmt.setDouble(8, weight);
            stmt.setDouble(9, height);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateInDatabase() {
        if (!updateBaseUserRecord()) return false;

        String query = "UPDATE Patients SET full_name = ?, date_of_birth = ?, gender = ?, blood_group = ?, phone = ?, weight = ?, height = ? WHERE system_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, getFullName());
            stmt.setDate(2, java.sql.Date.valueOf(dateOfBirth));
            stmt.setString(3, gender);
            stmt.setString(4, bloodGroup);
            stmt.setString(5, getPhone());
            stmt.setDouble(6, weight);
            stmt.setDouble(7, height);
            stmt.setString(8, getSystemId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

 */
}