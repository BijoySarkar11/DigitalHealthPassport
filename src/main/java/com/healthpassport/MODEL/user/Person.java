package com.healthpassport.MODEL.user;

public abstract class Person {
    private int id;
    private String fullName;

    public Person(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // Polymorphism: Subclasses MUST define their own dashboard routing
    public abstract String getRoleDashboard();
}