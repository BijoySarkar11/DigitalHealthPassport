package com.healthpassport.ui.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    // --- 1. NAVIGATION METHODS ---

    @FXML
    private void handlePatientLogin(MouseEvent event) {
        openScreen(event, "/fxml/PatientLogin.fxml", "Patient Portal Login");
    }

    @FXML
    private void handleDoctorLogin(MouseEvent event) {
        openScreen(event, "/fxml/DoctorLogin.fxml", "Doctor Portal Login");
    }

    @FXML
    private void handleAdminLogin(MouseEvent event) {
        openScreen(event, "/fxml/AdminLogin.fxml", "Administrator Portal Login");
    }

    // Helper to switch screens without resizing the window
    private void openScreen(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get current window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Replace content (keeps window size)
            stage.getScene().setRoot(root);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load " + fxmlPath);
        }
    }

    // --- 2. HOVER EFFECT METHODS (Required for FXML) ---

    // This method is called when the mouse enters a button
    @FXML
    private void handleMouseEnter(MouseEvent event) {
        // We cast the source to HBox because our buttons are HBoxes
        HBox source = (HBox) event.getSource();

        // Apply "Hover" Style (Shadow, Shift Right, Light Blue BG)
        source.setStyle(
                "-fx-background-color: #F8FAFC; " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(37, 99, 235, 0.1), 15, 0, 0, 5); " +
                        "-fx-translate-x: 5;"
        );
    }

    // This method is called when the mouse leaves a button
    @FXML
    private void handleMouseExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();

        // Reset to "Normal" Style (White BG, No Shadow, No Shift)
        source.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: null; " +
                        "-fx-translate-x: 0;"
        );
    }
}