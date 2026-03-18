package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.UserDAO;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.UserSession;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();


    public boolean login(String identifier, String password, Role expectedRole) {

        User user = userDAO.authenticate(identifier, password);


        if (user != null && user.getRole() == expectedRole) {


            UserSession.getInstance().setCurrentUser(user);
            return true;
        }

        return false;
    }

    public void logout() {
        UserSession.getInstance().cleanUserSession();
    }
}