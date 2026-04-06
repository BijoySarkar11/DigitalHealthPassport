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

}