package com.healthpassport.ui.login;

import com.healthpassport.MODEL.service.AuthService;
import com.healthpassport.MODEL.user.Role;
import com.healthpassport.ui.common.BaseController; // Import your new parent
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;


public class PatientLoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String identifier = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        if (authService.login(identifier, password, Role.PATIENT)) {

            navigateTo(event, "/fxml/PatientDashboard.fxml", "Digital Health Passport - Patient Dashboard");
        } else {
            errorLabel.setText("Invalid Patient credentials.");
        }
    }

    @FXML
    private void handleBackToRole(MouseEvent event) {
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}