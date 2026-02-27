package com.healthpassport.ui.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientDashboardController {

    @FXML private Button btnDashboard;
    @FXML private Button btnAppointments;
    @FXML private Button btnPrescriptions;
    @FXML private Button btnTestReports;

    // The main views inside the StackPane
    @FXML private VBox viewDashboard;
    @FXML private VBox viewAppointments;
    @FXML private VBox viewPrescriptions;
    @FXML private VBox viewTestReports;

    // CSS Styles matching the Deep Green (#26463D) floating sidebar theme
    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    @FXML
    public void initialize() {
        showDashboard(null); // Load Dashboard as default
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        hideAllViews();
        if (viewDashboard != null) {
            viewDashboard.setVisible(true);
            viewDashboard.setManaged(true);
        }
        if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showAppointments(ActionEvent event) {
        hideAllViews();
        if (viewAppointments != null) {
            viewAppointments.setVisible(true);
            viewAppointments.setManaged(true);
        }
        if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showPrescriptions(ActionEvent event) {
        hideAllViews();
        if (viewPrescriptions != null) {
            viewPrescriptions.setVisible(true);
            viewPrescriptions.setManaged(true);
        }
        if (btnPrescriptions != null) btnPrescriptions.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showTestReports(ActionEvent event) {
        hideAllViews();
        if (viewTestReports != null) {
            viewTestReports.setVisible(true);
            viewTestReports.setManaged(true);
        }
        if (btnTestReports != null) btnTestReports.setStyle(ACTIVE_STYLE);
    }

    private void hideAllViews() {
        if (viewDashboard != null) { viewDashboard.setVisible(false); viewDashboard.setManaged(false); }
        if (viewAppointments != null) { viewAppointments.setVisible(false); viewAppointments.setManaged(false); }
        if (viewPrescriptions != null) { viewPrescriptions.setVisible(false); viewPrescriptions.setManaged(false); }
        if (viewTestReports != null) { viewTestReports.setVisible(false); viewTestReports.setManaged(false); }
        resetButtons();
    }

    private void resetButtons() {
        if (btnDashboard != null) btnDashboard.setStyle(INACTIVE_STYLE);
        if (btnAppointments != null) btnAppointments.setStyle(INACTIVE_STYLE);
        if (btnPrescriptions != null) btnPrescriptions.setStyle(INACTIVE_STYLE);
        if (btnTestReports != null) btnTestReports.setStyle(INACTIVE_STYLE);
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