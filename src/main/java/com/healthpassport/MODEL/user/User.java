package com.healthpassport.MODEL.user;

public class User {
    private int id;
    private String nationalId;
    private String email;
    private String passwordHash;
    private Role role;
    private Integer hospitalId; // Integer object allows for 'null' values (Patients)
    private boolean isActive;

    // NEW: Added to hold the user's name for the UI dashboards
    private String fullName;

    // NEW: Empty constructor (Required so the UserDAO can build this object step-by-step)
    public User() {}

    // Updated full constructor (Includes fullName)
    public User(int id, String nationalId, String email, String passwordHash, Role role, Integer hospitalId, boolean isActive, String fullName) {
        this.id = id;
        this.nationalId = nationalId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.hospitalId = hospitalId;
        this.isActive = isActive;
        this.fullName = fullName;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNationalId() { return nationalId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Integer getHospitalId() { return hospitalId; }
    public boolean isActive() { return isActive; }
    public String getFullName() { return fullName; } // NEW

    // --- Setters --- (Needed for the DAO to populate the object from the database)
    public void setId(int id) { this.id = id; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
    public void setActive(boolean active) { isActive = active; }
    public void setFullName(String fullName) { this.fullName = fullName; } // NEW
}