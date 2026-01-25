package com.healthpassport.ui.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private Label roleTitleLabel;
    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;

    private String currentRole = "Patient"; // Default


    public void setRole(String role) {
        this.currentRole = role;
        roleTitleLabel.setText(role + " Login");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println("Logging in as: " + currentRole);


        try {
            String targetFxml = "";

            if (currentRole.equals("Patient")) {
                targetFxml = "/fxml/PatientDashboard.fxml";
            } else if (currentRole.equals("Doctor")) {
                targetFxml = "/fxml/DoctorDashboard.fxml";
            } else {
                System.out.println("Admin Dashboard not created yet!");
                return;
            }


            Parent root = FXMLLoader.load(getClass().getResource(targetFxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading dashboard.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}