package com.digitalhealthpassport.ui.patient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label bloodGroupLabel;
    @FXML private Label ageLabel;

    public void initialize() {
        // This runs automatically when the screen opens.
        // We set dummy data for now to see if it looks good.
        loadPatientData();
    }

    private void loadPatientData() {
        welcomeLabel.setText("Welcome, Alex Smith");
        patientIdLabel.setText("P-99821");
        bloodGroupLabel.setText("O Positive");
        ageLabel.setText("24 Years");
    }

    @FXML
    private void handleSearchDoctors(ActionEvent event) {
        System.out.println("Navigating to Search Doctors Screen...");
        // Next step: Create SearchDoctors.fxml and link it here!
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Go back to Login Screen
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}