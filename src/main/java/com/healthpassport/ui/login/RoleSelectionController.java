package com.healthpassport.ui.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    // When user clicks the "Patient" card
    @FXML
    private void handlePatientLogin(MouseEvent event) {
        openLoginScreen(event, "Patient");
    }

    // When user clicks the "Doctor" card
    @FXML
    private void handleDoctorLogin(MouseEvent event) {
        openLoginScreen(event, "Doctor");
    }

    // When user clicks the "Admin" card
    @FXML
    private void handleAdminLogin(MouseEvent event) {
        openLoginScreen(event, "Admin");
    }

    private void openLoginScreen(MouseEvent event, String role) {
        try {
            // We load the generic Login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // We tell the Login Controller which role was clicked
            LoginController controller = loader.getController();
            controller.setRole(role);

            // Show the new screen
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Could not find /fxml/Login.fxml");
        }
    }
}