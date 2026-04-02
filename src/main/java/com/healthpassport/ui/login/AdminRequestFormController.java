package com.healthpassport.ui.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminRequestFormController {

    @FXML private TextField hospNameField;
    @FXML private TextField hospRegNoField;
    @FXML private TextField hospPhoneField;
    @FXML private TextArea hospAddressArea;
    @FXML private TextField requesterNameField;
    @FXML private TextField hospEmailField;

    @FXML private Label statusMessageLabel;
    @FXML private Button submitBtn;
    @FXML private Button cancelBtn;

    @FXML
    private void handleSubmit(ActionEvent event) {
        // 1. Inline Validation (No popups)
        if (hospNameField.getText().trim().isEmpty() ||
                hospRegNoField.getText().trim().isEmpty() ||
                hospEmailField.getText().trim().isEmpty() ||
                requesterNameField.getText().trim().isEmpty()) {

            statusMessageLabel.setText("❌ Please complete all mandatory fields (Hospital Name, Reg No, Admin Name, and Email).");
            statusMessageLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        String hospital = hospNameField.getText().trim();

        // 2. Display Success Inline (No popups)
        statusMessageLabel.setText("✅ Application Queued! Verification pending for " + hospital + ".\nWe will contact you via email within 1-2 business days.");
        statusMessageLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

        // 3. Update the Buttons instantly
        submitBtn.setText("Submitted");
        submitBtn.setDisable(true); // Disable so they can't click it twice
        submitBtn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30;");

        cancelBtn.setText("Return to Login Screen");
        cancelBtn.setStyle("-fx-background-color: #E8F3EE; -fx-text-fill: #115E59; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 25; -fx-cursor: hand;");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Keep window size completely stable when returning to the login menu
            stage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight()));
            stage.setTitle("Digital Health Passport - Role Selection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}