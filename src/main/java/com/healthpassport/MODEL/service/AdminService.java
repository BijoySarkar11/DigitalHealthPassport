package com.healthpassport.MODEL.service;

import com.healthpassport.MODEL.dao.AdminDAO;
import com.healthpassport.MODEL.user.Admin;

public class AdminService {
    private final AdminDAO adminDAO;

    public AdminService() {
        this.adminDAO = new AdminDAO();
    }


    public boolean registerNewAdmin(Admin admin) {
        if (admin.getFullName() == null || admin.getEmail() == null || admin.getPasswordHash() == null) {
            return false;
        }
        return adminDAO.create(admin);
    }


    public boolean updateAdminFacility(Admin admin) {
        if (admin.getSystemId() == null) return false;
        return adminDAO.update(admin);
    }
}