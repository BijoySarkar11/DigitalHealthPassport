package com.healthpassport.ui.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class DoctorLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        // BYPASS: Validation removed. Directly loading the Doctor Dashboard.
        System.out.println("Bypassing login... Redirecting to Doctor Dashboard.");

        try {
            // Load the Doctor Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoctorDashboard.fxml"));
            Parent root = loader.load();

            // Get the current stage from the event source (the button)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Switch the scene
            stage.getScene().setRoot(root);
            stage.setTitle("Digital Health Passport - Doctor Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading DoctorDashboard.fxml. Please check the file path.");
        }
    }

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