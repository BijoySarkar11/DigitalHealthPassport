package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.PatientDAO;

public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();

    // THIS is the method your PatientLoginController uses
    public boolean login(String username, String password) {
        return patientDAO.authenticate(username, password);
    }
}