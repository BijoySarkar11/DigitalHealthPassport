package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.DoctorDAO;

public class DoctorService {
    private final DoctorDAO doctorDAO = new DoctorDAO();

    // THIS is the method your controller is looking for!
    public boolean login(String username, String password) {
        return doctorDAO.authenticate(username, password);
    }
}