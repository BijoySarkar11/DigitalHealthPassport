package com.healthpassport.ui.login;

import com.healthpassport.MODEL.service.AuthService;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.ui.BaseController; // OOP: Inheriting shared navigation logic
import com.healthpassport.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class DoctorLoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // OOP: Using the Service Layer to handle the database logic
    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String identifier = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText(""); // Clear previous errors

        // 1. Let the AuthService authenticate against the database
        if (authService.loginUser(identifier, password)) {

            // 2. Verify that the authenticated user is actually a Doctor
            if (UserSession.getInstance().getCurrentUser().getRole() == Role.DOCTOR) {
                // 3. Use inherited BaseController method for a clean transition
                navigateTo(event, "/fxml/DoctorDashboard.fxml", "Digital Health Passport - Doctor Portal");
            } else {
                // If a Patient or Admin tries to log in here, kick them out
                authService.logoutUser();
                errorLabel.setText("Access Denied: You do not have Doctor privileges.");
            }
        } else {
            errorLabel.setText("Invalid Doctor credentials. Please try again.");
        }
    }

    @FXML
    private void handleBackToRole(MouseEvent event) {
        // Inherited cleanly from BaseController
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}