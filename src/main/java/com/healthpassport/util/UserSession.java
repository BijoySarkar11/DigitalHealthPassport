package com.healthpassport.util;

import com.healthpassport.MODEL.user.User;

public class UserSession {

    private static UserSession instance;

    private User currentUser;


    private UserSession() {}


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


    public void cleanUserSession() {
        currentUser = null;
    }
}