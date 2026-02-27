package com.healthpassport.ui.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    // Buttons
    @FXML private Button btnSearch;
    @FXML private Button btnNewPatient;
    @FXML private Button btnNewDoctor;
    @FXML private Button btnAddTestReport; // NEW

    // Views
    @FXML private VBox viewSearch;
    @FXML private VBox viewNewPatient;
    @FXML private VBox viewNewDoctor;
    @FXML private VBox viewAddTestReport; // NEW

    // Styles
    private final String ACTIVE_STYLE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 12; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #5C8D7D; -fx-background-radius: 15; -fx-padding: 12; -fx-font-weight: bold; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        showSearch(null); // Default view
    }

    // --- Navigation Logic ---

    @FXML
    private void showSearch(ActionEvent event) {
        switchView(viewSearch, btnSearch);
    }

    @FXML
    private void showNewPatient(ActionEvent event) {
        switchView(viewNewPatient, btnNewPatient);
    }

    @FXML
    private void showNewDoctor(ActionEvent event) {
        switchView(viewNewDoctor, btnNewDoctor);
    }

    @FXML
    private void showAddTestReport(ActionEvent event) {
        switchView(viewAddTestReport, btnAddTestReport);
    }

    private void switchView(VBox activeView, Button activeButton) {
        // Hide all
        viewSearch.setVisible(false); viewSearch.setManaged(false);
        viewNewPatient.setVisible(false); viewNewPatient.setManaged(false);
        viewNewDoctor.setVisible(false); viewNewDoctor.setManaged(false);
        viewAddTestReport.setVisible(false); viewAddTestReport.setManaged(false);

        // Show active
        activeView.setVisible(true);
        activeView.setManaged(true);

        // Reset Styles
        btnSearch.setStyle(INACTIVE_STYLE);
        btnNewPatient.setStyle(INACTIVE_STYLE);
        btnNewDoctor.setStyle(INACTIVE_STYLE);
        btnAddTestReport.setStyle(INACTIVE_STYLE);

        // Highlight Active
        activeButton.setStyle(ACTIVE_STYLE);
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