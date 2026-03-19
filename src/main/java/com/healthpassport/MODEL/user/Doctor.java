package com.healthpassport.MODEL.user;

public class Doctor extends Person {
    private int userId;
    private int hospitalId; // Ties the Doctor to a specific Hospital
    private String specialization;
    private String licenseNumber;
    private int yearsOfExperience;

    public Doctor(int id, String fullName, int userId, int hospitalId, String specialization, String licenseNumber, int yearsOfExperience) {
        super(id, fullName);
        this.userId = userId;
        this.hospitalId = hospitalId;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
    }

    public int getUserId() { return userId; }
    public int getHospitalId() { return hospitalId; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public int getYearsOfExperience() { return yearsOfExperience; }

    @Override
    public String getRoleDashboard() {
        return "/fxml/DoctorDashboard.fxml";
    }
}