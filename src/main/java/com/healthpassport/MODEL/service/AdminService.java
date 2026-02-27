package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.AdminDAO;

public class AdminService {
    private final AdminDAO adminDAO = new AdminDAO();

    public boolean login(String username, String password) {
        return adminDAO.authenticate(username, password);
    }
}