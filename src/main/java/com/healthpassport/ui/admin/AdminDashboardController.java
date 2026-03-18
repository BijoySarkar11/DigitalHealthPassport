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

    @FXML
    private Button btnSearch, btnNewPatient, btnNewDoctor, btnAddTestReport;
    @FXML
    private VBox viewSearch, viewNewPatient, viewNewDoctor, viewAddTestReport;

    private final String ACTIVE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    @FXML
    public void initialize() {
        showSearch(null);
    }

    @FXML
    private void showSearch(ActionEvent e) {
        switchView(viewSearch, btnSearch);
    }

    @FXML
    private void showNewPatient(ActionEvent e) {
        switchView(viewNewPatient, btnNewPatient);
    }

    @FXML
    private void showNewDoctor(ActionEvent e) {
        switchView(viewNewDoctor, btnNewDoctor);
    }

    @FXML
    private void showAddTestReport(ActionEvent e) {
        switchView(viewAddTestReport, btnAddTestReport);
    }

    private void switchView(VBox view, Button btn) {

        viewSearch.setVisible(false);
        viewSearch.setManaged(false);
        viewNewPatient.setVisible(false);
        viewNewPatient.setManaged(false);
        viewNewDoctor.setVisible(false);
        viewNewDoctor.setManaged(false);
        viewAddTestReport.setVisible(false);
        viewAddTestReport.setManaged(false);


        view.setVisible(true);
        view.setManaged(true);


        btnSearch.setStyle(INACTIVE);
        btnNewPatient.setStyle(INACTIVE);
        btnNewDoctor.setStyle(INACTIVE);
        btnAddTestReport.setStyle(INACTIVE);
        btn.setStyle(ACTIVE);
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
