package com.healthpassport.MODEL.user;

public class User {
    private int id;
    private String nationalId;
    private String email;
    private String passwordHash;
    private Role role;
    private Integer hospitalId;
    private boolean isActive;


    private String fullName;


    public User() {}


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


    public int getId() { return id; }
    public String getNationalId() { return nationalId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Integer getHospitalId() { return hospitalId; }
    public boolean isActive() { return isActive; }
    public String getFullName() { return fullName; } // NEW


    public void setId(int id) { this.id = id; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
    public void setActive(boolean active) { isActive = active; }
    public void setFullName(String fullName) { this.fullName = fullName; } // NEW
}