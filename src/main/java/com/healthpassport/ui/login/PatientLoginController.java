package com.healthpassport.ui.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // LOGIN BUTTON LOGIC
    @FXML
    private void handleLogin(ActionEvent event) {
        // For now, we bypass the password check and go straight to the dashboard
        System.out.println("Logging in...");

        try {
            // Load the Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PatientDashboard.fxml"));
            Parent root = loader.load();

            // Get the current window (Stage) from the event source (the button)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Switch to Dashboard (Using setRoot to keep window size)
            stage.getScene().setRoot(root);
            stage.setTitle("Digital Health Passport - Patient Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not find /fxml/PatientDashboard.fxml");
        }
    }

    // BACK BUTTON LOGIC
    @FXML
    private void handleBackToRole(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleSelection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Digital Health Passport - Role Selection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}