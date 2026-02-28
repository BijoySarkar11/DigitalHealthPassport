package com.healthpassport.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Updated database name here:
    private static final String URL = "jdbc:mysql://localhost:3306/health_passport_db";
    private static final String USER = "root";
    private static final String PASSWORD = "MySQL@java";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}