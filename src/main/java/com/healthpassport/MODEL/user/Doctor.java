package com.healthpassport.MODEL.user;

public class Doctor {
    private int id;
    private int userId; // Links to Users table
    private int hospitalId; // Links to Hospitals table
    private String specialization;
    private String licenseNumber;
    private int yearsOfExperience;

    public Doctor(int id, int userId, int hospitalId, String specialization, String licenseNumber, int yearsOfExperience) {
        this.id = id;
        this.userId = userId;
        this.hospitalId = hospitalId;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
    }

    // Standard Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getHospitalId() { return hospitalId; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public int getYearsOfExperience() { return yearsOfExperience; }
}