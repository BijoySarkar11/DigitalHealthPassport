package com.healthpassport.ui.login;

import com.healthpassport.ui.BaseController; // Ensure correct import path
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class RoleSelectionController extends BaseController {

    @FXML
    private void handlePatientLogin(MouseEvent event) {
        navigateTo(event, "/fxml/PatientLogin.fxml", "Patient Portal Login");
    }

    @FXML
    private void handleDoctorLogin(MouseEvent event) {
        navigateTo(event, "/fxml/DoctorLogin.fxml", "Doctor Portal Login");
    }

    @FXML
    private void handleAdminLogin(MouseEvent event) {
        navigateTo(event, "/fxml/AdminLogin.fxml", "Admin Portal Login");
    }

    @FXML
    private void handleRequestAdminStatus(ActionEvent event) {
        // Now safely uses inherited navigateTo, which automatically preserves window size!
        navigateTo(event, "/fxml/AdminRequestForm.fxml", "Digital Health Passport - Request Access");
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
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setScaleX(1.0);
        btn.setScaleY(1.0);
    }
}