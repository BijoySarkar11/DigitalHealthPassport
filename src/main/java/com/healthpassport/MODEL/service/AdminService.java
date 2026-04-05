package com.healthpassport.MODEL.service;

import com.healthpassport.MODEL.dao.AdminDAO;
import com.healthpassport.MODEL.user.Admin;

public class AdminService {
    private final AdminDAO adminDAO;

    public AdminService() {
        this.adminDAO = new AdminDAO();
    }

    /**
     * Validates and registers a new System Administrator.
     */
    public boolean registerNewAdmin(Admin admin) {
        if (admin.getFullName() == null || admin.getEmail() == null || admin.getPasswordHash() == null) {
            return false;
        }
        return adminDAO.create(admin);
    }

    /**
     * Updates an Admin's profile and linked Hospital details.
     */
    public boolean updateAdminFacility(Admin admin) {
        if (admin.getSystemId() == null) return false;
        return adminDAO.update(admin);
    }
}