package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.DoctorDAO;
import com.healthpassport.MODEL.user.Doctor;

public class DoctorService {
    private final DoctorDAO doctorDAO = new DoctorDAO();

    // NO authentication methods here anymore! (AuthService handles that)

    public Doctor getProfile(int userId) {
        return doctorDAO.getDoctorProfileByUserId(userId);
    }
}