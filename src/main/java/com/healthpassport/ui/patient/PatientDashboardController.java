package com.healthpassport.ui.patient;

import com.healthpassport.MODEL.user.User;
import com.healthpassport.util.DBConnection;
import com.healthpassport.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientDashboardController {

    @FXML private Button btnDashboard, btnAppointments, btnPrescriptions, btnTestReports;
    @FXML private VBox viewDashboard, viewAppointments, viewPrescriptions, viewTestReports;
    @FXML private Label sidebarNameLabel, sidebarIdLabel, greetingLabel, weightLabel, heightLabel, bloodGroupLabel, phoneLabel;
    @FXML private Label primaryDoctorNameLabel, primaryDoctorSpecLabel;
    @FXML private VBox appointmentsContainer, prescriptionsContainer, testReportsContainer, remindersContainer;

    @FXML private VBox diagnosticDetailsContainer, drugsDetailsContainer, testsDetailsContainer;
    @FXML private Button btnToggleDiagnostic, btnToggleDrugs, btnToggleTests;

    private int currentPatientId = -1;

    // RESTORED: Added back the font-family and cursor to prevent text resizing/glitching
    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    @FXML
    public void initialize() {
        showDashboard(null);
        loadAllPatientData();
    }

    private void loadAllPatientData() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser == null) return;

        String profileQuery = "SELECT id, national_id, full_name, weight, height, blood_group, phone FROM Patients WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(profileQuery)) {
            stmt.setInt(1, loggedInUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentPatientId = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String firstName = fullName.split(" ")[0];
                sidebarNameLabel.setText(firstName);
                sidebarIdLabel.setText("ID: " + rs.getString("national_id"));
                greetingLabel.setText("Hello, " + firstName + "!");
                weightLabel.setText(String.format("%.0f kg", rs.getDouble("weight")));
                heightLabel.setText(String.format("%.0f cm", rs.getDouble("height")));
                bloodGroupLabel.setText(rs.getString("blood_group"));
                phoneLabel.setText("📞 " + rs.getString("phone"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (currentPatientId != -1) {
            loadPrimaryDoctor();
            loadUpcomingAppointments();
            loadPrescriptions();
            loadTestReports();
            loadMedicationReminders();

            loadInlineDiagnostics();
            loadInlineDrugs();
            loadInlineTests();
        }
    }

    @FXML private void toggleDiagnostic() {
        boolean isVisible = diagnosticDetailsContainer.isVisible();
        diagnosticDetailsContainer.setVisible(!isVisible);
        diagnosticDetailsContainer.setManaged(!isVisible);
        btnToggleDiagnostic.setText(!isVisible ? "-" : "+");
    }

    @FXML private void toggleDrugs() {
        boolean isVisible = drugsDetailsContainer.isVisible();
        drugsDetailsContainer.setVisible(!isVisible);
        drugsDetailsContainer.setManaged(!isVisible);
        btnToggleDrugs.setText(!isVisible ? "-" : "+");
    }

    @FXML private void toggleTests() {
        boolean isVisible = testsDetailsContainer.isVisible();
        testsDetailsContainer.setVisible(!isVisible);
        testsDetailsContainer.setManaged(!isVisible);
        btnToggleTests.setText(!isVisible ? "-" : "+");
    }

    private void loadInlineDiagnostics() {
        if (diagnosticDetailsContainer == null) return;
        diagnosticDetailsContainer.getChildren().clear();
        String query = "SELECT diagnosis, diagnosis_date FROM Medical_History WHERE patient_id = ? ORDER BY diagnosis_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("diagnosis") + " (" + rs.getDate("diagnosis_date") + ")");
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                diagnosticDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) diagnosticDetailsContainer.getChildren().add(new Label("No diagnostic records found."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadInlineDrugs() {
        if (drugsDetailsContainer == null) return;
        drugsDetailsContainer.getChildren().clear();
        String query = "SELECT medicine_name, dosage FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("medicine_name") + " - " + rs.getString("dosage"));
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                drugsDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) drugsDetailsContainer.getChildren().add(new Label("No drugs prescribed."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadInlineTests() {
        if (testsDetailsContainer == null) return;
        testsDetailsContainer.getChildren().clear();
        String query = "SELECT report_type, report_date FROM Test_Reports WHERE patient_id = ? ORDER BY report_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("report_type") + " (" + rs.getDate("report_date") + ")");
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                testsDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) testsDetailsContainer.getChildren().add(new Label("No test reports available."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMedicationReminders() {
        if (remindersContainer == null) return;
        remindersContainer.getChildren().clear();
        String query = "SELECT pi.medicine_name, pi.dosage, pi.frequency, u.full_name as doctor_name FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id JOIN Doctors d ON p.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                remindersContainer.getChildren().add(createReminderCard(rs.getString("medicine_name"), rs.getString("dosage") + " • " + rs.getString("frequency"), rs.getString("doctor_name")));
            }
            if (!hasData) remindersContainer.getChildren().add(new Label("No daily medications."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPrimaryDoctor() {
        String query = "SELECT u.full_name, d.specialization FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE a.patient_id = ? ORDER BY a.appointment_date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (primaryDoctorNameLabel != null) primaryDoctorNameLabel.setText(rs.getString("full_name"));
                if (primaryDoctorSpecLabel != null) primaryDoctorSpecLabel.setText(rs.getString("specialization"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUpcomingAppointments() {
        if (appointmentsContainer == null) return;
        appointmentsContainer.getChildren().clear();
        String query = "SELECT u.full_name, d.specialization, a.appointment_date, a.status FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE a.patient_id = ? AND DATE(a.appointment_date) >= CURDATE() ORDER BY a.appointment_date ASC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            DateTimeFormatter monFmt = DateTimeFormatter.ofPattern("MMM");
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");
            while (rs.next()) {
                hasData = true;
                LocalDateTime date = rs.getTimestamp("appointment_date").toLocalDateTime();
                appointmentsContainer.getChildren().add(createAppointmentCard(date.format(monFmt).toUpperCase(), date.format(dayFmt), rs.getString("full_name"), rs.getString("specialization"), date.format(timeFmt) + " - Scheduled"));
            }
            if (!hasData) appointmentsContainer.getChildren().add(new Label("No upcoming appointments."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPrescriptions() {
        if (prescriptionsContainer == null) return;
        prescriptionsContainer.getChildren().clear();
        String query = "SELECT pi.medicine_name, pi.dosage, pi.frequency, pi.duration, p.prescription_date, u.full_name as doctor_name FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id JOIN Doctors d ON p.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                prescriptionsContainer.getChildren().add(createPrescriptionCard(rs.getString("medicine_name"), "Dosage: " + rs.getString("dosage") + " • " + rs.getString("frequency"), "Duration: " + rs.getString("duration"), rs.getString("doctor_name")));
            }
            if (!hasData) prescriptionsContainer.getChildren().add(new Label("No prescriptions found."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTestReports() {
        if (testReportsContainer == null) return;
        testReportsContainer.getChildren().clear();
        String query = "SELECT tr.report_type, tr.report_date, h.name as hospital_name FROM Test_Reports tr JOIN Hospitals h ON tr.hospital_id = h.id WHERE tr.patient_id = ? ORDER BY tr.report_date DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            while (rs.next()) {
                hasData = true;
                testReportsContainer.getChildren().add(createTestReportCard(rs.getString("report_type"), "Tested at: " + rs.getString("hospital_name"), "Tested on: " + rs.getDate("report_date").toLocalDate().format(dateFmt)));
            }
            if (!hasData) testReportsContainer.getChildren().add(new Label("No test reports available."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UI BUILDERS ---
    private HBox createReminderCard(String med, String dose, String doc) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 15 12;");
        VBox icon = new VBox(new Label("💊"));
        icon.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-alignment: center;");
        icon.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji';");
        VBox details = new VBox(2);
        Label name = new Label(med); name.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label sub = new Label(dose); sub.setStyle("-fx-text-fill: #5C8D7D; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label dr = new Label("By " + doc); dr.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 10px;");
        details.getChildren().addAll(name, sub, dr);
        HBox.setHgrow(details, Priority.ALWAYS);
        card.getChildren().addAll(icon, details);
        return card;
    }

    private HBox createAppointmentCard(String month, String day, String docName, String spec, String timeStr) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-border-color: #E8F3EE; -fx-border-width: 2; -fx-border-radius: 15; -fx-alignment: center-left; -fx-spacing: 20;");
        VBox dateBox = new VBox(new Label(month), new Label(day));
        dateBox.setStyle("-fx-background-color: #115E59; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");
        dateBox.getChildren().get(0).setStyle("-fx-text-fill: #A3CFC0; -fx-font-weight: bold; -fx-font-size: 12px;");
        dateBox.getChildren().get(1).setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 22px;");
        VBox infoBox = new VBox();
        infoBox.setSpacing(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label nameLabel = new Label(docName);
        nameLabel.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 18px;");
        Label specLabel = new Label(spec + " • Routine Checkup");
        specLabel.setStyle("-fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-font-size: 13px;");
        HBox timeBox = new HBox(new Label("🕒"), new Label(timeStr));
        timeBox.setSpacing(5);
        timeBox.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #6B7280; -fx-font-size: 11px;");
        timeBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLabel, specLabel, timeBox);
        VBox btnBox = new VBox();
        btnBox.setSpacing(10);
        btnBox.setStyle("-fx-alignment: center-right;");
        Button remBtn = new Button("🔔 Reminder ON");
        remBtn.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #10B981; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';");
        Label cancelLbl = new Label("Cancel / Reschedule");
        cancelLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-underline: true; -fx-cursor: hand;");
        btnBox.getChildren().addAll(remBtn, cancelLbl);
        card.getChildren().addAll(dateBox, infoBox, btnBox);
        return card;
    }

    private HBox createPrescriptionCard(String medName, String instructions, String duration, String docName) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left; -fx-spacing: 20;");
        VBox iconBox = new VBox(new Label("💊"));
        iconBox.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");
        iconBox.getChildren().get(0).setStyle("-fx-font-size: 28px; -fx-font-family: 'Segoe UI Emoji';");
        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        HBox titleBox = new HBox(new Label(medName), new Label("Active"));
        titleBox.setSpacing(10);
        titleBox.setStyle("-fx-alignment: center-left;");
        titleBox.getChildren().get(0).setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 18px;");
        titleBox.getChildren().get(1).setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label instLabel = new Label(instructions);
        instLabel.setStyle("-fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-font-size: 13px;");
        HBox bottomBox = new HBox();
        bottomBox.setSpacing(15);
        HBox durBox = new HBox(new Label("⏳"), new Label(duration));
        durBox.setSpacing(5);
        durBox.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #6B7280; -fx-font-size: 11px;");
        durBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        HBox docBox = new HBox(new Label("👨‍⚕️"), new Label("By " + docName));
        docBox.setSpacing(5);
        docBox.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #6B7280; -fx-font-size: 11px;");
        docBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        bottomBox.getChildren().addAll(durBox, docBox);
        infoBox.getChildren().addAll(titleBox, instLabel, bottomBox);
        card.getChildren().addAll(iconBox, infoBox);
        return card;
    }

    private HBox createTestReportCard(String testName, String details, String dateStr) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left; -fx-spacing: 20;");
        VBox iconBox = new VBox(new Label("🩸"));
        iconBox.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");
        iconBox.getChildren().get(0).setStyle("-fx-font-size: 28px; -fx-font-family: 'Segoe UI Emoji';");
        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        HBox titleBox = new HBox(new Label(testName), new Label("Ready"));
        titleBox.setSpacing(10);
        titleBox.setStyle("-fx-alignment: center-left;");
        titleBox.getChildren().get(0).setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 18px;");
        titleBox.getChildren().get(1).setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-font-size: 13px;");
        HBox dateBox = new HBox(new Label("📅"), new Label(dateStr));
        dateBox.setSpacing(5);
        dateBox.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #6B7280; -fx-font-size: 11px;");
        dateBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(titleBox, detailsLabel, dateBox);
        HBox btnBox = new HBox();
        btnBox.setSpacing(10);
        btnBox.setStyle("-fx-alignment: center-right;");
        Button viewBtn = new Button("👁️ View");
        viewBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-font-family: 'Segoe UI Emoji', 'System';");
        Button downBtn = new Button("⬇️ Download");
        downBtn.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';");
        btnBox.getChildren().addAll(viewBtn, downBtn);
        card.getChildren().addAll(iconBox, infoBox, btnBox);
        return card;
    }

    @FXML private void showDashboard(ActionEvent event) { hideAllViews(); if (viewDashboard != null) { viewDashboard.setVisible(true); viewDashboard.setManaged(true); } resetButtons(); if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_STYLE); }
    @FXML private void showAppointments(ActionEvent event) { hideAllViews(); if (viewAppointments != null) { viewAppointments.setVisible(true); viewAppointments.setManaged(true); } resetButtons(); if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE); }
    @FXML private void showPrescriptions(ActionEvent event) { hideAllViews(); if (viewPrescriptions != null) { viewPrescriptions.setVisible(true); viewPrescriptions.setManaged(true); } resetButtons(); if (btnPrescriptions != null) btnPrescriptions.setStyle(ACTIVE_STYLE); }
    @FXML private void showTestReports(ActionEvent event) { hideAllViews(); if (viewTestReports != null) { viewTestReports.setVisible(true); viewTestReports.setManaged(true); } resetButtons(); if (btnTestReports != null) btnTestReports.setStyle(ACTIVE_STYLE); }

    private void hideAllViews() {
        if (viewDashboard != null) { viewDashboard.setVisible(false); viewDashboard.setManaged(false); }
        if (viewAppointments != null) { viewAppointments.setVisible(false); viewAppointments.setManaged(false); }
        if (viewPrescriptions != null) { viewPrescriptions.setVisible(false); viewPrescriptions.setManaged(false); }
        if (viewTestReports != null) { viewTestReports.setVisible(false); viewTestReports.setManaged(false); }
    }

    private void resetButtons() {
        if (btnDashboard != null) btnDashboard.setStyle(INACTIVE_STYLE);
        if (btnAppointments != null) btnAppointments.setStyle(INACTIVE_STYLE);
        if (btnPrescriptions != null) btnPrescriptions.setStyle(INACTIVE_STYLE);
        if (btnTestReports != null) btnTestReports.setStyle(INACTIVE_STYLE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}