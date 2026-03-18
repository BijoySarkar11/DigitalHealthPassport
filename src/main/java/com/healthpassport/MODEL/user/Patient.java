package com.healthpassport.MODEL.user;

import java.time.LocalDate;

public class Patient {
    private int id;
    private int userId; // Links back to the Users table
    private String nationalId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phone;
    private double weight; // NEW
    private double height; // NEW

    public Patient(int id, int userId, String nationalId, String fullName, LocalDate dateOfBirth, String gender, String bloodGroup, String phone, double weight, double height) {
        this.id = id;
        this.userId = userId;
        this.nationalId = nationalId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.weight = weight;
        this.height = height;
    }


    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getNationalId() { return nationalId; }
    public String getFullName() { return fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getBloodGroup() { return bloodGroup; }
    public String getPhone() { return phone; }

    public double getWeight() { return weight; }
    public double getHeight() { return height; }
}