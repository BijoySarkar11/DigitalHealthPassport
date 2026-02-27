package com.healthpassport.ui.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientDashboardController {

    // --- FXML UI Components ---
    @FXML private Label patientNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label ageLabel;

    // --- Navigation Buttons (5 Tabs) ---
    @FXML private Button btnHealthRecords;
    @FXML private Button btnPrescriptions;
    @FXML private Button btnTestReports;
    @FXML private Button btnDailyReminders; // New
    @FXML private Button btnSearchDoctors;  // New

    // --- Content Views (The 5 Sections) ---
    @FXML private VBox viewHealthRecords;
    @FXML private VBox viewPrescriptions;
    @FXML private VBox viewTestReports;
    @FXML private VBox viewDailyReminders; // New
    @FXML private VBox viewSearchDoctors;  // New

    // --- Styling Constants ---
    private final String ACTIVE_STYLE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        // Init Data
        patientNameLabel.setText("Pranty");
        dobLabel.setText("04/01/2005");
        ageLabel.setText("21 Yrs");

        // Ensure only Health Records is visible at start
        showHealthRecords(null);
    }

    // --- Navigation Logic ---

    @FXML
    private void showHealthRecords(ActionEvent event) {
        setViewVisible(viewHealthRecords, btnHealthRecords);
    }

    @FXML
    private void showPrescriptions(ActionEvent event) {
        setViewVisible(viewPrescriptions, btnPrescriptions);
    }

    @FXML
    private void showTestReports(ActionEvent event) {
        setViewVisible(viewTestReports, btnTestReports);
    }

    @FXML
    private void showDailyReminders(ActionEvent event) {
        setViewVisible(viewDailyReminders, btnDailyReminders);
    }

    @FXML
    private void showSearchDoctors(ActionEvent event) {
        setViewVisible(viewSearchDoctors, btnSearchDoctors);
    }

    // --- Helper Method to Handle View Switching ---
    private void setViewVisible(VBox activeView, Button activeButton) {
        // 1. Hide All Views
        viewHealthRecords.setVisible(false);  viewHealthRecords.setManaged(false);
        viewPrescriptions.setVisible(false);  viewPrescriptions.setManaged(false);
        viewTestReports.setVisible(false);    viewTestReports.setManaged(false);
        viewDailyReminders.setVisible(false); viewDailyReminders.setManaged(false);
        viewSearchDoctors.setVisible(false);  viewSearchDoctors.setManaged(false);

        // 2. Show Active View
        if (activeView != null) {
            activeView.setVisible(true);
            activeView.setManaged(true);
        }

        // 3. Reset All Button Styles
        btnHealthRecords.setStyle(INACTIVE_STYLE);
        btnPrescriptions.setStyle(INACTIVE_STYLE);
        btnTestReports.setStyle(INACTIVE_STYLE);
        btnDailyReminders.setStyle(INACTIVE_STYLE);
        btnSearchDoctors.setStyle(INACTIVE_STYLE);

        // 4. Highlight Active Button
        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
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