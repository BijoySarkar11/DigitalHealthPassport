package com.healthpassport.ui.login;

import com.healthpassport.MODEL.service.AuthService;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.ui.BaseController; // Inheriting the shared navigation logic
import com.healthpassport.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class AdminLoginController extends BaseController {

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

            // 2. Verify that the authenticated user is actually an Admin
            if (UserSession.getInstance().getCurrentUser().getRole() == Role.ADMIN) {
                // 3. Use inherited BaseController method for a clean transition
                navigateTo(event, "/fxml/AdminDashboard.fxml", "Digital Health Passport - Hospital Administration");
            } else {
                // If a Patient or Doctor tries to log in here, kick them out
                authService.logoutUser();
                errorLabel.setText("Access Denied: You do not have Administrator privileges.");
            }
        } else {
            errorLabel.setText("Invalid Admin credentials. Please try again.");
        }
    }

    @FXML
    private void handleBackToRole(MouseEvent event) {
        // Inherited cleanly from BaseController (works with MouseEvent now!)
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}