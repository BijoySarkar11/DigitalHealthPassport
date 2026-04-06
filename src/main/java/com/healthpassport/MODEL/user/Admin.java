package com.healthpassport.MODEL.user;

import com.healthpassport.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Admin extends User {
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalContact;

    public Admin() {
        this.setRole(Role.ADMIN);
    }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }

    public String getHospitalContact() { return hospitalContact; }
    public void setHospitalContact(String hospitalContact) { this.hospitalContact = hospitalContact; }

    @Override
    public String getProfileSummary() {
        return "Administrator: " + getFullName() + " | Hospital: " + (hospitalName != null ? hospitalName : "Global Network");
    }

}