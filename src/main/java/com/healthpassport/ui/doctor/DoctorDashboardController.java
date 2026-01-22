package com.healthpassport.ui.doctor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class DoctorDashboardController {

    @FXML
    private void handleSearchPatient(ActionEvent event) {
        System.out.println("Opening Search Patient Screen...");
        // navigateTo(event, "/fxml/SearchPatient.fxml"); // We will create this next!
    }

    @FXML
    private void handleNewPatient(ActionEvent event) {
        System.out.println("Opening New Patient Registration...");
        // navigateTo(event, "/fxml/NewPatient.fxml"); // We will create this next!
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigateTo(event, "/fxml/RoleSelection.fxml");
    }

    // Helper method to switch screens easily
    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}