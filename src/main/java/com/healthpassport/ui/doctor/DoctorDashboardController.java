package com.healthpassport.ui.doctor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class DoctorDashboardController {

    // --- FXML UI Components ---
    @FXML private Label doctorNameLabel;

    // Navigation Buttons
    @FXML private Button btnSearchPatient;
    @FXML private Button btnAddPatient;

    // Views
    @FXML private VBox viewSearchPatient;
    @FXML private VBox viewAddPatient;

    // Search Logic Components
    @FXML private TextField searchField;
    @FXML private VBox patientDetailsContainer; // The hidden result area
    @FXML private Label resultName;
    @FXML private Label resultId;

    // Styles
    private final String ACTIVE_STYLE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        // Init logic if needed
        showSearchPatient(null);
    }

    // --- Navigation Logic ---
    @FXML
    private void showSearchPatient(ActionEvent event) {
        viewSearchPatient.setVisible(true);
        viewSearchPatient.setManaged(true);
        viewAddPatient.setVisible(false);
        viewAddPatient.setManaged(false);

        btnSearchPatient.setStyle(ACTIVE_STYLE);
        btnAddPatient.setStyle(INACTIVE_STYLE);
    }

    @FXML
    private void showAddPatient(ActionEvent event) {
        viewSearchPatient.setVisible(false);
        viewSearchPatient.setManaged(false);
        viewAddPatient.setVisible(true);
        viewAddPatient.setManaged(true);

        btnSearchPatient.setStyle(INACTIVE_STYLE);
        btnAddPatient.setStyle(ACTIVE_STYLE);
    }

    // --- Search Logic ---
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText();

        // Simple mock logic: If text is not empty, show the "Pranty" mock result
        if (query != null && !query.trim().isEmpty()) {
            patientDetailsContainer.setVisible(true);
            patientDetailsContainer.setManaged(true);

            // In a real app, you would fetch data from DB here
            resultName.setText("Pranty");
            resultId.setText("Patient ID: " + query.toUpperCase());
        }
    }

    // --- Logout Logic ---
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