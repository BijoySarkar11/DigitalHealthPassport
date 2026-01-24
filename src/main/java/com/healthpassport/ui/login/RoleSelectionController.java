package com.healthpassport.ui.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    // 1. PATIENT LOGIN
    @FXML
    private void handlePatientLogin(MouseEvent event) {
        openScreen(event, "/fxml/PatientLogin.fxml", "Patient Portal Login");
    }

    // 2. DOCTOR LOGIN
    @FXML
    private void handleDoctorLogin(MouseEvent event) {
        System.out.println("Doctor Login clicked - Feature coming soon");
    }

    // 3. ADMIN LOGIN
    @FXML
    private void handleAdminLogin(MouseEvent event) {
        System.out.println("Admin Login clicked - Feature coming soon");
    }

    // Helper method to handle navigation WITHOUT resizing
    private void openScreen(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // FIX: Don't create a new Scene. Just replace the content (Root).
            // This keeps the window size exactly as it is.
            stage.getScene().setRoot(root);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load " + fxmlPath + ". Check if the file exists in src/main/resources/fxml/");
        }
    }
}