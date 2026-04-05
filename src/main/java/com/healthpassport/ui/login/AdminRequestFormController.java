package com.healthpassport.ui.login;

import com.healthpassport.ui.BaseController; // OOP: Inheriting shared navigation logic
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminRequestFormController extends BaseController {

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
        // OOP: Replaced the raw FXMLLoader try-catch block with our inherited navigation method
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}