package com.healthpassport.MODEL.user;
import java.time.LocalDate;

public class Patient extends Person {
    private int userId;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phone;
    private double weight;
    private double height;

    public Patient(int id, String fullName, int userId, String nationalId, LocalDate dateOfBirth, String gender, String bloodGroup, String phone, double weight, double height) {
        super(id, fullName);
        this.userId = userId;
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.weight = weight;
        this.height = height;
    }

    public int getUserId() { return userId; }
    public String getNationalId() { return nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getBloodGroup() { return bloodGroup; }
    public String getPhone() { return phone; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }

    @Override
    public String getRoleDashboard() {
        return "/fxml/PatientDashboard.fxml";
    }
}