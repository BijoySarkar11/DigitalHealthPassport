package com.healthpassport.MODEL.service;

import com.healthpassport.DAO.UserDAO;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.UserSession;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    // The UI Controller calls this method
    public boolean login(String identifier, String password, Role expectedRole) {

        // 1. Ask DAO to find the user
        User user = userDAO.authenticate(identifier, password);

        // 2. Check if user exists AND if they are logging into the correct portal
        if (user != null && user.getRole() == expectedRole) {

            // 3. Save them to the global session so the Dashboard can see who they are
            UserSession.getInstance().setCurrentUser(user);
            return true;
        }

        return false;
    }

    public void logout() {
        UserSession.getInstance().cleanUserSession();
    }
}