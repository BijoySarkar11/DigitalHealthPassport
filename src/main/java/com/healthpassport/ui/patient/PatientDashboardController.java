package com.healthpassport.ui.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientDashboardController {

    @FXML private Label patientNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label ageLabel;

    @FXML
    public void initialize() {
        // Placeholder Data - In a real app, fetch this from a database
        patientNameLabel.setText("Pranty");
        dobLabel.setText("04/01/2005");
        ageLabel.setText("21 Yrs"); // Calculated from DOB
    }

    @FXML
    private void handleLogout(ActionEvent event) {
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