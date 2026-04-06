package com.healthpassport.MODEL.user;

//public abstract class Person implements IDatabaseEntity {
public abstract class Person{
    // Encapsulation
    private int id;
    private String fullName;
    private String phone;

    public Person() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Polymorphism
    public abstract String getProfileSummary();
}