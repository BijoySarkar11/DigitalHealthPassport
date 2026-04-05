package com.healthpassport.MODEL.user;

public abstract class Person implements IDatabaseEntity {
    // Encapsulation: All fields are strictly private
    private int id; // Internal Database integer ID
    private String fullName;
    private String phone;

    public Person() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Polymorphism: Forces subclasses to define how their profile looks
    public abstract String getProfileSummary();
}