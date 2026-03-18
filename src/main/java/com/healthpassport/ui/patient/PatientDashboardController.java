package com.healthpassport.ui.patient;

import com.healthpassport.MODEL.user.User;
import com.healthpassport.ui.common.BaseController;
import com.healthpassport.util.DBConnection;
import com.healthpassport.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientDashboardController extends BaseController {

    @FXML private Button btnDashboard, btnAppointments, btnPrescriptions, btnTestReports;
    @FXML private VBox viewDashboard, viewAppointments, viewPrescriptions, viewTestReports;
    @FXML private Label sidebarNameLabel, sidebarIdLabel, greetingLabel, weightLabel, heightLabel, bloodGroupLabel, phoneLabel;
    @FXML private Label primaryDoctorNameLabel, primaryDoctorSpecLabel;
    @FXML private VBox appointmentsContainer, prescriptionsContainer, testReportsContainer, remindersContainer;

    @FXML private VBox diagnosticDetailsContainer, drugsDetailsContainer, testsDetailsContainer;
    @FXML private Button btnToggleDiagnostic, btnToggleDrugs, btnToggleTests;

    private int currentPatientId = -1;

    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    public static class PatientDashboardException extends Exception {
        public PatientDashboardException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void handleDashboardError(PatientDashboardException e) {
        System.err.println("[Dashboard Error] " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("Technical Reason: " + e.getCause().getMessage());
        }
    }

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
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load basic patient profile data.", e));
        }

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
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load inline diagnostic history.", e));
        }
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
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load inline prescribed drugs.", e));
        }
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
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load inline test reports.", e));
        }
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
                remindersContainer.getChildren().add(DashboardUIFactory.createReminderCard(
                        rs.getString("medicine_name"),
                        rs.getString("dosage") + " • " + rs.getString("frequency"),
                        rs.getString("doctor_name")
                ));
            }
            if (!hasData) remindersContainer.getChildren().add(new Label("No daily medications."));
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load medication reminders.", e));
        }
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
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load primary doctor info.", e));
        }
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
                appointmentsContainer.getChildren().add(DashboardUIFactory.createAppointmentCard(
                        date.format(monFmt).toUpperCase(),
                        date.format(dayFmt),
                        rs.getString("full_name"),
                        rs.getString("specialization"),
                        date.format(timeFmt) + " - Scheduled"
                ));
            }
            if (!hasData) appointmentsContainer.getChildren().add(new Label("No upcoming appointments."));
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load upcoming appointments.", e));
        }
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
                prescriptionsContainer.getChildren().add(DashboardUIFactory.createPrescriptionCard(
                        rs.getString("medicine_name"),
                        "Dosage: " + rs.getString("dosage") + " • " + rs.getString("frequency"),
                        "Duration: " + rs.getString("duration"),
                        rs.getString("doctor_name")
                ));
            }
            if (!hasData) prescriptionsContainer.getChildren().add(new Label("No prescriptions found."));
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load prescriptions list.", e));
        }
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
                testReportsContainer.getChildren().add(DashboardUIFactory.createTestReportCard(
                        rs.getString("report_type"),
                        "Tested at: " + rs.getString("hospital_name"),
                        "Tested on: " + rs.getDate("report_date").toLocalDate().format(dateFmt)
                ));
            }
            if (!hasData) testReportsContainer.getChildren().add(new Label("No test reports available."));
        } catch (SQLException e) {
            handleDashboardError(new PatientDashboardException("Failed to load test reports.", e));
        }
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
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}