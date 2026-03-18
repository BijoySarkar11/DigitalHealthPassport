package com.healthpassport.ui.login;

import com.healthpassport.MODEL.service.AuthService;
import com.healthpassport.MODEL.user.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class DoctorLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String identifier = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");


        if (authService.login(identifier, password, Role.DOCTOR)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoctorDashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setTitle("Digital Health Passport - Doctor Portal");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid Doctor credentials. ");
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