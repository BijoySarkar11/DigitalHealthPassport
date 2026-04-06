package com.healthpassport.ui.login;

import com.healthpassport.MODEL.service.AuthService;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.ui.BaseController;
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

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String identifier = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        try {

            if (password == null || password.length() < 6) {
                throw new ExceptionPassword("Password must be at least 6 characters.");
            }


            if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
                throw new ExceptionPassword("Password must contain both letters and numbers.");
            }

            //Database Login
            if (authService.loginUser(identifier, password)) {
                if (UserSession.getInstance().getCurrentUser().getRole() == Role.ADMIN) {
                    navigateTo(event, "/fxml/AdminDashboard.fxml", "Digital Health Passport - Hospital Administration");
                } else {
                    authService.logoutUser();
                    errorLabel.setText("Access Denied: You do not have Administrator privileges.");
                }
            } else {
                errorLabel.setText("Invalid Admin credentials. Please try again.");
            }

        } catch (ExceptionPassword e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleBackToRole(MouseEvent event) {
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}