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

public class AdminLoginController {

    @FXML private TextField usernameField; // Accepts Admin Email
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        // Pass Role.ADMIN
        if (authService.login(email, password, Role.ADMIN)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setTitle("Digital Health Passport - Hospital Administration");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid Admin credentials.");
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