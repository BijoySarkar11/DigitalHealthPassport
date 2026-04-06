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

}