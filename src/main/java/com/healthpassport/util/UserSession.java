package com.healthpassport.util;

import com.healthpassport.MODEL.user.User;

public class UserSession {
    // The single instance of the session
    private static UserSession instance;

    // The currently logged-in user
    private User currentUser;

    // Private constructor prevents anyone else from making a new Session
    private UserSession() {}

    // Get the single instance (Singleton principle)
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Call this when the user clicks "Log Out"
    public void cleanUserSession() {
        currentUser = null;
    }
}