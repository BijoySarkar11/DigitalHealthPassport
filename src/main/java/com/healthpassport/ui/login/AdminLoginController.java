package com.healthpassport.ui.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Validation Error: Fields cannot be empty");
            return;
        }

        System.out.println("Attempting Admin Login for: " + username);
        // Admin authentication logic will go here
    }

    @FXML
    private void handleBackToRole(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleSelection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Use setRoot to prevent window resizing
            stage.getScene().setRoot(root);
            stage.setTitle("Digital Health Passport - Role Selection");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}