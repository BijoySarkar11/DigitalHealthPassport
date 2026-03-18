package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.PatientDAO;
import com.healthpassport.MODEL.user.Patient;

public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();



    public Patient getProfile(int userId) {
        return patientDAO.getPatientProfileByUserId(userId);
    }
}