package com.healthpassport.ui.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    @FXML private void handlePatientLogin(MouseEvent event) { openScreen(event, "/fxml/PatientLogin.fxml", "Patient Portal Login"); }
    @FXML private void handleDoctorLogin(MouseEvent event) { openScreen(event, "/fxml/DoctorLogin.fxml", "Doctor Portal Login"); }
    @FXML private void handleAdminLogin(MouseEvent event) { openScreen(event, "/fxml/AdminLogin.fxml", "Admin Portal Login"); }

    @FXML
    private void handleRequestAdminStatus(ActionEvent event) {
        System.out.println("Request Admin Status Clicked!");
    }

    private void openScreen(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMouseEnter(MouseEvent event) {
        HBox card = (HBox) event.getSource();
        card.setScaleX(1.03);
        card.setScaleY(1.03);
    }

    @FXML
    private void handleMouseExit(MouseEvent event) {
        HBox card = (HBox) event.getSource();
        card.setScaleX(1.0);
        card.setScaleY(1.0);
    }

    @FXML
    private void handleButtonEnter(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setScaleX(1.05);
        btn.setScaleY(1.05);
        btn.setStyle("-fx-background-color: #26463D; -fx-text-fill: #FFFFFF; -fx-background-radius: 10; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-font-size: 13px;");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setScaleX(1.0);
        btn.setScaleY(1.0);
        btn.setStyle("-fx-background-color: #26463D; -fx-text-fill: #FFFFFF; -fx-background-radius: 10; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-font-size: 13px;");
    }
}