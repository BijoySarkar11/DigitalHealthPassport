package com.healthpassport.ui.patient;

import com.healthpassport.MODEL.user.User;
import com.healthpassport.ui.common.BaseController;
import com.healthpassport.util.DBConnection;
import com.healthpassport.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PatientDashboardController extends BaseController {

    // Sidebar & Layout
    @FXML private Button btnDashboard, btnAppointments, btnPrescriptions, btnTestReports;
    @FXML private VBox viewDashboard, viewAppointments, viewPrescriptions, viewTestReports;
    @FXML private Label sidebarNameLabel, sidebarIdLabel, greetingLabel, weightLabel, heightLabel, bloodGroupLabel, phoneLabel;
    @FXML private Label primaryDoctorNameLabel, primaryDoctorSpecLabel, patientEmojiLabel;
    @FXML private VBox appointmentsContainer, prescriptionsContainer, testReportsContainer, remindersContainer;

    @FXML private VBox diagnosticDetailsContainer, drugsDetailsContainer, testsDetailsContainer;
    @FXML private Button btnToggleDiagnostic, btnToggleDrugs, btnToggleTests;
    @FXML private TextField globalSearchField;

    // Sub-Views for Appointments Tab
    @FXML private Button btnTabMyApts, btnTabBook;
    @FXML private VBox subViewMyApts, subViewBook;
    @FXML private VBox doctorsListContainer;

    // Booking Elements
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private ComboBox<DoctorItem> doctorSelectionCombo;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> appointmentTimeCombo;
    @FXML private TextArea appointmentReasonArea;

    private int currentPatientDbId = -1;
    private int primaryDoctorId = -1;

    private final List<DoctorItem> masterDoctorList = new ArrayList<>();

    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String TAB_ACTIVE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    private final String TAB_INACTIVE = "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 20;";

    // ==========================================
    // SANITIZATION UTILITIES
    // ==========================================
    private String formatDoctorName(String rawName) {
        if (rawName == null) return "Unknown Doctor";
        return "Dr. " + rawName.replaceFirst("^(?i)(\\s*(dr\\.?|doctor)\\s*)+", "").trim();
    }

    public static class DoctorItem {
        private final int docId;
        private final int hospId;
        private final String specialty;
        private final String displayText;

        public DoctorItem(int docId, int hospId, String specialty, String displayText) {
            this.docId = docId;
            this.hospId = hospId;
            this.specialty = specialty;
            this.displayText = displayText;
        }

        public int getDocId() { return docId; }
        public int getHospId() { return hospId; }
        public String getSpecialty() { return specialty; }

        @Override
        public String toString() { return displayText; }
    }

    @FXML
    public void initialize() {
        showDashboard(null);
        loadPatientProfile();

        appointmentTimeCombo.setItems(FXCollections.observableArrayList(
                "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
                "12:00 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM"
        ));
    }

    private void loadPatientProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String profileQuery = "SELECT id, national_id, full_name, weight, height, blood_group, phone, gender FROM Patients WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(profileQuery)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentPatientDbId = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String firstName = fullName.split(" ")[0];

                sidebarNameLabel.setText(fullName);
                sidebarIdLabel.setText("ID: " + rs.getString("national_id"));
                greetingLabel.setText("Hello, " + firstName + "!");
                weightLabel.setText(String.format("%.0f kg", rs.getDouble("weight")));
                heightLabel.setText(String.format("%.0f cm", rs.getDouble("height")));
                bloodGroupLabel.setText(rs.getString("blood_group"));
                phoneLabel.setText("📞 " + rs.getString("phone"));

                String gender = rs.getString("gender");
                if(patientEmojiLabel != null) patientEmojiLabel.setText("FEMALE".equalsIgnoreCase(gender) ? "👩" : "👨");
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (currentPatientDbId != -1) {
            loadPrimaryDoctor();
            loadUpcomingAppointments();

            loadAllDoctorsForBooking();
            loadAvailableDoctors("");

            loadPrescriptions("");
            loadTestReports("");

            loadMedicationReminders();
            loadInlineDiagnostics();
            loadInlineDrugs();
            loadInlineTests();
        }
    }

    @FXML private void handleGlobalSearch(ActionEvent event) {
        String query = globalSearchField.getText();
        if (query == null) query = "";
        query = query.trim();

        if (query.isEmpty()) {
            clearSearchAndReload();
            return;
        }

        boolean foundTest = loadTestReports(query);
        if (foundTest) {
            showTestReports(null);
            return;
        }

        boolean foundPrescription = loadPrescriptions(query);
        if (foundPrescription) {
            showPrescriptions(null);
            return;
        }

        loadAvailableDoctors(query);
        showAppointments(null);
    }

    private void clearSearchAndReload() {
        if (globalSearchField != null) globalSearchField.clear();
        loadAvailableDoctors("");
        loadPrescriptions("");
        loadTestReports("");
    }

    private void loadUpcomingAppointments() {
        if (appointmentsContainer == null || currentPatientDbId == -1) return;
        appointmentsContainer.getChildren().clear();

        String query = "SELECT u.full_name, d.specialization, a.appointment_date, a.status, a.reason " +
                "FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id JOIN Users u ON d.user_id = u.id " +
                "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                LocalDateTime ldt = rs.getTimestamp("appointment_date").toLocalDateTime();
                String status = rs.getString("status");

                String title = formatDoctorName(rs.getString("full_name"));
                String sub = rs.getString("specialization") + " • " + ldt.format(timeFmt) + " - " + status;
                String reason = rs.getString("reason");

                appointmentsContainer.getChildren().add(createAppointmentCard(ldt.format(monthFmt).toUpperCase(), ldt.format(dayFmt), title, sub, status, reason));
            }
            if (!hasData) appointmentsContainer.getChildren().add(new Label("No appointments found."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAllDoctorsForBooking() {
        masterDoctorList.clear();
        if(specialtyCombo != null) specialtyCombo.getItems().clear();
        if(doctorSelectionCombo != null) doctorSelectionCombo.getItems().clear();

        Set<String> specialties = new LinkedHashSet<>();
        specialties.add("All Specialties");

        String query = "SELECT d.id AS doctor_id, u.full_name, d.specialization, h.id AS hospital_id, h.name AS hospital_name " +
                "FROM Doctors d JOIN Users u ON d.user_id = u.id JOIN Hospitals h ON d.hospital_id = h.id ORDER BY u.full_name ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int docId = rs.getInt("doctor_id");
                int hospId = rs.getInt("hospital_id");
                String name = rs.getString("full_name");
                String spec = rs.getString("specialization");
                String hospName = rs.getString("hospital_name");

                String displayText = formatDoctorName(name) + " (" + spec + " • " + hospName + ")";

                DoctorItem item = new DoctorItem(docId, hospId, spec, displayText);
                masterDoctorList.add(item);
                specialties.add(spec);
            }

            if(specialtyCombo != null) {
                specialtyCombo.getItems().addAll(specialties);
                specialtyCombo.getSelectionModel().selectFirst();
            }
            if(doctorSelectionCombo != null) {
                doctorSelectionCombo.getItems().addAll(masterDoctorList);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleSpecialtyFilter(ActionEvent event) {
        if(specialtyCombo == null || doctorSelectionCombo == null) return;

        String selectedSpec = specialtyCombo.getValue();
        doctorSelectionCombo.getItems().clear();

        if (selectedSpec == null || selectedSpec.equals("All Specialties")) {
            doctorSelectionCombo.getItems().addAll(masterDoctorList);
        } else {
            for (DoctorItem doc : masterDoctorList) {
                if (doc.getSpecialty().equals(selectedSpec)) {
                    doctorSelectionCombo.getItems().add(doc);
                }
            }
        }
    }

    private void loadAvailableDoctors(String searchTerm) {
        if(doctorsListContainer == null) return;
        doctorsListContainer.getChildren().clear();

        String query = "SELECT d.id AS doctor_id, u.full_name, d.specialization, h.id AS hospital_id, h.name AS hospital_name " +
                "FROM Doctors d JOIN Users u ON d.user_id = u.id JOIN Hospitals h ON d.hospital_id = h.id " +
                "WHERE u.full_name LIKE ? OR d.specialization LIKE ? OR h.name LIKE ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchParam = "%" + searchTerm + "%";
            stmt.setString(1, searchParam);
            stmt.setString(2, searchParam);
            stmt.setString(3, searchParam);

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int docId = rs.getInt("doctor_id");
                String name = rs.getString("full_name");
                String spec = rs.getString("specialization");
                String hospName = rs.getString("hospital_name");

                doctorsListContainer.getChildren().add(createDirectoryCard(docId, formatDoctorName(name), spec, hospName));
            }
            if (!hasData) {
                if(searchTerm.isEmpty()) doctorsListContainer.getChildren().add(new Label("No doctors available."));
                else doctorsListContainer.getChildren().add(new Label("No doctors found matching '" + searchTerm + "'."));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBookPrimaryDoctor(ActionEvent event) {
        if (primaryDoctorId != -1) {
            showAppointments(null);
            openBookingForm(primaryDoctorId);
        } else {
            showAppointments(null);
            showBookTab();
        }
    }

    private void openBookingForm(int targetDocId) {
        DoctorItem targetDoc = null;
        for (DoctorItem item : masterDoctorList) {
            if (item.getDocId() == targetDocId) {
                targetDoc = item;
                break;
            }
        }

        if (targetDoc != null) {
            specialtyCombo.setValue("All Specialties");
            handleSpecialtyFilter(null);
            doctorSelectionCombo.getSelectionModel().select(targetDoc);
        }

        appointmentDatePicker.setValue(null);
        appointmentTimeCombo.getSelectionModel().clearSelection();
        appointmentReasonArea.clear();

        showBookTab();
    }

    @FXML private void handleConfirmBooking(ActionEvent event) {
        DoctorItem selectedDoc = doctorSelectionCombo.getSelectionModel().getSelectedItem();

        if (selectedDoc == null || currentPatientDbId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a doctor from the dropdown list to book.");
            return;
        }

        LocalDate selectedDate = appointmentDatePicker.getValue();
        String selectedTimeStr = appointmentTimeCombo.getValue();
        String reason = appointmentReasonArea.getText();

        if (selectedDate == null || selectedTimeStr == null || selectedTimeStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Incomplete Form", "Please select both a valid date and time slot.");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US);
            LocalTime time = LocalTime.parse(selectedTimeStr, formatter);
            LocalDateTime appointmentDateTime = selectedDate.atTime(time);

            String insertQuery = "INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, status, created_by, reason) VALUES (?, ?, ?, ?, 'SCHEDULED', ?, ?)";

            try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, currentPatientDbId);
                stmt.setInt(2, selectedDoc.getDocId());
                stmt.setInt(3, selectedDoc.getHospId());
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(appointmentDateTime));
                stmt.setInt(5, UserSession.getInstance().getCurrentUser().getId());
                stmt.setString(6, reason);

                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Your appointment has been successfully booked!");

                appointmentDatePicker.setValue(null);
                appointmentTimeCombo.getSelectionModel().clearSelection();
                appointmentReasonArea.clear();

                loadUpcomingAppointments();
                showMyAptsTab();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to book appointment: " + e.getMessage());
        }
    }

    private boolean loadPrescriptions(String searchTerm) {
        if (prescriptionsContainer == null) return false;
        prescriptionsContainer.getChildren().clear();

        String query = "SELECT pi.medicine_name, pi.dosage, pi.frequency, pi.duration, p.prescription_date, u.full_name as doctor_name " +
                "FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id " +
                "JOIN Doctors d ON p.doctor_id = d.id JOIN Users u ON d.user_id = u.id " +
                "WHERE p.patient_id = ? AND (pi.medicine_name LIKE ? OR u.full_name LIKE ?) " +
                "ORDER BY p.prescription_date DESC";

        boolean hasData = false;
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            stmt.setString(2, "%" + searchTerm + "%");
            stmt.setString(3, "%" + searchTerm + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hasData = true;
                prescriptionsContainer.getChildren().add(createPrescriptionCard(
                        rs.getString("medicine_name"),
                        "Dosage: " + rs.getString("dosage") + " • " + rs.getString("frequency"),
                        "Duration: " + rs.getString("duration"),
                        formatDoctorName(rs.getString("doctor_name"))
                ));
            }
            if (!hasData) {
                if(searchTerm.isEmpty()) prescriptionsContainer.getChildren().add(new Label("No prescriptions found."));
                else prescriptionsContainer.getChildren().add(new Label("No prescriptions found matching '" + searchTerm + "'."));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return hasData;
    }

    private boolean loadTestReports(String searchTerm) {
        if (testReportsContainer == null) return false;
        testReportsContainer.getChildren().clear();

        String query = "SELECT tr.report_type, tr.report_date, h.name as hospital_name " +
                "FROM Test_Reports tr JOIN Hospitals h ON tr.hospital_id = h.id " +
                "WHERE tr.patient_id = ? AND (tr.report_type LIKE ? OR h.name LIKE ?) " +
                "ORDER BY tr.report_date DESC";

        boolean hasData = false;
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            stmt.setString(2, "%" + searchTerm + "%");
            stmt.setString(3, "%" + searchTerm + "%");

            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            while (rs.next()) {
                hasData = true;
                testReportsContainer.getChildren().add(createTestReportCard(
                        rs.getString("report_type"),
                        "Tested at: " + rs.getString("hospital_name"),
                        "Tested on: " + rs.getDate("report_date").toLocalDate().format(dateFmt)
                ));
            }
            if (!hasData) {
                if(searchTerm.isEmpty()) testReportsContainer.getChildren().add(new Label("No test reports available."));
                else testReportsContainer.getChildren().add(new Label("No test reports found matching '" + searchTerm + "'."));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return hasData;
    }

    private void loadPrimaryDoctor() {
        String query = "SELECT d.id AS doc_id, u.full_name, d.specialization FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE a.patient_id = ? ORDER BY a.appointment_date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                primaryDoctorId = rs.getInt("doc_id");
                if (primaryDoctorNameLabel != null) primaryDoctorNameLabel.setText(formatDoctorName(rs.getString("full_name")));
                if (primaryDoctorSpecLabel != null) primaryDoctorSpecLabel.setText(rs.getString("specialization"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadMedicationReminders() {
        if (remindersContainer == null) return;
        remindersContainer.getChildren().clear();
        String query = "SELECT pi.medicine_name, pi.dosage, pi.frequency, pi.duration, u.full_name as doctor_name FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id JOIN Doctors d ON p.doctor_id = d.id JOIN Users u ON d.user_id = u.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                remindersContainer.getChildren().add(createReminderCard(
                        rs.getString("medicine_name"),
                        rs.getString("dosage") + " • " + rs.getString("duration"),
                        formatDoctorName(rs.getString("doctor_name"))
                ));
            }
            if (!hasData) remindersContainer.getChildren().add(new Label("No daily medications."));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void toggleDiagnostic() {
        boolean isVisible = diagnosticDetailsContainer.isVisible();
        diagnosticDetailsContainer.setVisible(!isVisible); diagnosticDetailsContainer.setManaged(!isVisible);
        btnToggleDiagnostic.setText(!isVisible ? "-" : "+");
    }

    @FXML private void toggleDrugs() {
        boolean isVisible = drugsDetailsContainer.isVisible();
        drugsDetailsContainer.setVisible(!isVisible); drugsDetailsContainer.setManaged(!isVisible);
        btnToggleDrugs.setText(!isVisible ? "-" : "+");
    }

    @FXML private void toggleTests() {
        boolean isVisible = testsDetailsContainer.isVisible();
        testsDetailsContainer.setVisible(!isVisible); testsDetailsContainer.setManaged(!isVisible);
        btnToggleTests.setText(!isVisible ? "-" : "+");
    }

    private void loadInlineDiagnostics() {
        if (diagnosticDetailsContainer == null) return;
        diagnosticDetailsContainer.getChildren().clear();
        String query = "SELECT diagnosis, diagnosis_date FROM Medical_History WHERE patient_id = ? ORDER BY diagnosis_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("diagnosis") + " (" + rs.getDate("diagnosis_date") + ")");
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                diagnosticDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) diagnosticDetailsContainer.getChildren().add(new Label("No records found."));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadInlineDrugs() {
        if (drugsDetailsContainer == null) return;
        drugsDetailsContainer.getChildren().clear();
        String query = "SELECT medicine_name, dosage FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("medicine_name") + " - " + rs.getString("dosage"));
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                drugsDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) drugsDetailsContainer.getChildren().add(new Label("No drugs prescribed."));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadInlineTests() {
        if (testsDetailsContainer == null) return;
        testsDetailsContainer.getChildren().clear();
        String query = "SELECT report_type, report_date FROM Test_Reports WHERE patient_id = ? ORDER BY report_date DESC LIMIT 4";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentPatientDbId);
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Label lbl = new Label("• " + rs.getString("report_type") + " (" + rs.getDate("report_date") + ")");
                lbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");
                testsDetailsContainer.getChildren().add(lbl);
            }
            if(!hasData) testsDetailsContainer.getChildren().add(new Label("No test reports available."));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ==========================================
    // UI CARD GENERATORS
    // ==========================================

    private HBox createAppointmentCard(String month, String day, String title, String subtitle, String status, String reason) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left;");

        VBox dateBox = new VBox(new Label(month), new Label(day));
        dateBox.setStyle("-fx-background-color: #1B362F; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");
        dateBox.getChildren().get(0).setStyle("-fx-text-fill: #A3CFC0; -fx-font-weight: bold; -fx-font-size: 12px;");
        dateBox.getChildren().get(1).setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 22px;");

        String displayReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Scheduled Consultation";
        Label reasonLbl = new Label("Reason: " + displayReason);
        reasonLbl.setStyle("-fx-text-fill: #5C8D7D; -fx-font-style: italic; -fx-font-size: 11px;");
        reasonLbl.setWrapText(true);

        VBox infoBox = new VBox(5, new Label(title), new Label(subtitle), reasonLbl);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        infoBox.getChildren().get(0).setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 16px;");
        infoBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        VBox rightBox = new VBox(10);
        rightBox.setStyle("-fx-alignment: center-right;");

        if ("SCHEDULED".equalsIgnoreCase(status)) {
            Button reminderBtn = new Button("🔔 Reminder ON");
            reminderBtn.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #10B981; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5;");
            Label cancelLbl = new Label("Cancel / Reschedule");
            cancelLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px; -fx-underline: true; -fx-cursor: hand;");
            rightBox.getChildren().addAll(reminderBtn, cancelLbl);
        } else {
            Label statusLbl = new Label(status);
            statusLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: bold;");
            rightBox.getChildren().add(statusLbl);
        }

        card.getChildren().addAll(dateBox, infoBox, rightBox);
        return card;
    }

    private HBox createDirectoryCard(int docId, String name, String spec, String hospName) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-alignment: center-left;");

        Label icon = new Label("👨‍⚕️");
        icon.setStyle("-fx-font-size: 24px; -fx-background-color: #E8F3EE; -fx-padding: 10 12; -fx-background-radius: 50; -fx-text-fill: #26463D;");

        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");
        Label specLbl = new Label(spec);
        specLbl.setStyle("-fx-text-fill: #115E59; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label hospLbl = new Label("🏥 " + hospName);
        hospLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        infoBox.getChildren().addAll(nameLbl, specLbl, hospLbl);

        Button btnSelect = new Button("Book");
        btnSelect.setStyle("-fx-background-color: #E8F3EE; -fx-text-fill: #115E59; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 15;");
        btnSelect.setOnAction(e -> openBookingForm(docId));

        card.getChildren().addAll(icon, infoBox, btnSelect);
        return card;
    }

    private HBox createReminderCard(String med, String dose, String doc) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 15 12;");

        VBox icon = new VBox(new Label("💊"));
        icon.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-alignment: center;");
        icon.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;");

        VBox details = new VBox(2);
        Label name = new Label(med);
        name.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label sub = new Label(dose);
        sub.setStyle("-fx-text-fill: #5C8D7D; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label dr = new Label("By " + doc);
        dr.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 10px;");

        details.getChildren().addAll(name, sub, dr);
        HBox.setHgrow(details, Priority.ALWAYS);
        card.getChildren().addAll(icon, details);
        return card;
    }

    private HBox createPrescriptionCard(String medName, String instructions, String duration, String docName) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left; -fx-spacing: 20;");

        VBox iconBox = new VBox(new Label("💊"));
        iconBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");
        iconBox.getChildren().get(0).setStyle("-fx-font-size: 28px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;");

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

        VBox iconBox = new VBox();
        iconBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 12; -fx-min-width: 65; -fx-min-height: 65; -fx-alignment: center;");

        try {
            java.io.InputStream imageStream = getClass().getResourceAsStream("/images/report_icon.png");
            if (imageStream != null) {
                ImageView imgView = new ImageView(new Image(imageStream));
                imgView.setFitWidth(32);
                imgView.setFitHeight(32);
                imgView.setPreserveRatio(true);
                iconBox.getChildren().add(imgView);
            } else {
                throw new Exception("Image not found.");
            }
        } catch (Exception e) {
            Label fallbackIcon = new Label("📄");
            fallbackIcon.setStyle("-fx-font-size: 28px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;");
            iconBox.getChildren().add(fallbackIcon);
        }

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

    // ==========================================
    // TAB & SIDEBAR NAVIGATION
    // ==========================================
    @FXML private void showMyAptsTab() {
        if(subViewMyApts != null) { subViewMyApts.setVisible(true); subViewMyApts.setManaged(true); }
        if(subViewBook != null) { subViewBook.setVisible(false); subViewBook.setManaged(false); }
        if(btnTabMyApts != null) btnTabMyApts.setStyle(TAB_ACTIVE);
        if(btnTabBook != null) btnTabBook.setStyle(TAB_INACTIVE);
    }

    @FXML private void showBookTab() {
        if(subViewMyApts != null) { subViewMyApts.setVisible(false); subViewMyApts.setManaged(false); }
        if(subViewBook != null) { subViewBook.setVisible(true); subViewBook.setManaged(true); }
        if(btnTabBook != null) btnTabBook.setStyle(TAB_ACTIVE);
        if(btnTabMyApts != null) btnTabMyApts.setStyle(TAB_INACTIVE);
    }

    @FXML private void showDashboard(ActionEvent event) { if (event != null) clearSearchAndReload(); hideAllViews(); if (viewDashboard != null) { viewDashboard.setVisible(true); viewDashboard.setManaged(true); } resetButtons(); if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_STYLE); }
    @FXML private void showAppointments(ActionEvent event) { if (event != null) clearSearchAndReload(); hideAllViews(); if (viewAppointments != null) { viewAppointments.setVisible(true); viewAppointments.setManaged(true); } resetButtons(); if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE); showMyAptsTab(); }
    @FXML private void showPrescriptions(ActionEvent event) { if (event != null) clearSearchAndReload(); hideAllViews(); if (viewPrescriptions != null) { viewPrescriptions.setVisible(true); viewPrescriptions.setManaged(true); } resetButtons(); if (btnPrescriptions != null) btnPrescriptions.setStyle(ACTIVE_STYLE); }
    @FXML private void showTestReports(ActionEvent event) { if (event != null) clearSearchAndReload(); hideAllViews(); if (viewTestReports != null) { viewTestReports.setVisible(true); viewTestReports.setManaged(true); } resetButtons(); if (btnTestReports != null) btnTestReports.setStyle(ACTIVE_STYLE); }

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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        try {
            PreparedStatement stmt = DBConnection.getConnection().prepareStatement("SELECT 1"); // dummy
        } catch (Exception e) {}
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}