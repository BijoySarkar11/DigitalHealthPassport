package com.healthpassport.MODEL.service;

import com.healthpassport.MODEL.dao.UserDAO;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.UserSession;

public class AuthService {
    private final UserDAO userDAO;

    // Constructor Injection
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Handles the login process and session initialization.
     * @return true if successful, false if credentials are wrong.
     */
    public boolean loginUser(String identifier, String password) {
        if (identifier == null || password == null || identifier.isEmpty() || password.isEmpty()) {
            return false;
        }

        User authenticatedUser = userDAO.authenticate(identifier, password);

        if (authenticatedUser != null) {
            // Centralized session management
            UserSession.getInstance().setCurrentUser(authenticatedUser);
            return true;
        }
        return false;
    }

    public void logoutUser() {
        UserSession.getInstance().cleanUserSession();
    }
}