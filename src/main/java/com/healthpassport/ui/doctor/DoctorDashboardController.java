package com.healthpassport.ui.doctor;

import com.healthpassport.ui.BaseController;
import com.healthpassport.util.UserSession;
import com.healthpassport.util.DBConnection;
import com.healthpassport.MODEL.user.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoctorDashboardController extends BaseController {

    @FXML private BorderPane rootPane; // FIX: Added the missing rootPane variable here!

    @FXML private Button btnDashboard, btnPatients, btnAppointments, btnLogout;
    @FXML private VBox viewDashboard, viewAppointments;
    @FXML private VBox scheduleVBox, appointmentsContainer;

    @FXML private Label doctorNameLabel, doctorSpecialtyLabel, doctorIdLabel, doctorEmojiLabel, hospitalNameLabel;
    @FXML private Label totalPatientsLabel, totalPatientsSubLabel, activeCasesLabel, activeCasesSubLabel, todaysApptsLabel, todaysApptsSubLabel;

    @FXML private Label dashboardDegreesLabel;
    @FXML private Label dashboardExperienceLabel;
    @FXML private Label dashboardLicenseLabel;

    @FXML private TextField searchField;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField prescribePatientIdField;
    @FXML private TextArea prescribeDiagnosisField;
    @FXML private VBox medicationsListContainer;

    @FXML private VBox viewMyPatients;
    @FXML private Button btnTabRoster, btnTabProfile;
    @FXML private VBox subViewRoster, patientRosterContainer;
    @FXML private VBox subViewProfile, profilePlaceholder, profileDataContainer;
    @FXML private Label profilePlaceholderIcon, profilePlaceholderTitle, profilePlaceholderSubtitle;
    @FXML private Label profileAvatar, profileName, profileId, profilePhone, profileBlood, profileWeight, profileHeight, profileDob;

    @FXML private VBox profileDiagnosesContainer;
    @FXML private VBox profileMedicationsContainer;
    @FXML private VBox profileTestsContainer;

    // Test Report Detail Elements
    @FXML private VBox testReportsListWrapper;
    @FXML private VBox testReportDetailView;
    @FXML private Label reportDetailTitle;
    @FXML private Label reportDetailInfo;
    @FXML private VBox reportDetailContentBox;

    @FXML private Button btnTabSchedule, btnTabPrescribe;
    @FXML private VBox subViewSchedule, subViewPrescribe;

    private String currentProfilePatientId;

    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String TAB_ACTIVE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    private final String TAB_INACTIVE = "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 20;";

    private String formatDoctorName(String rawName) {
        if (rawName == null) return "Unknown Doctor";
        return "Dr. " + rawName.replaceFirst("^(?i)(\\s*(dr\\.?|doctor)\\s*)+", "").trim();
    }

    private String getCleanFileName(String dbFileName) {
        if (dbFileName == null || dbFileName.trim().isEmpty()) return "report_document.pdf";
        String clean = dbFileName.replace("\\", "/");
        if (clean.contains("/")) clean = clean.substring(clean.lastIndexOf('/') + 1);
        return clean.trim().isEmpty() ? "report_document.pdf" : clean;
    }

    @FXML
    public void initialize() {
        showDashboard(null);
        loadDoctorProfile();
        loadDashboardStatistics();
        loadTodaysSchedule();
        loadPatientRoster();
        loadAppointments();

        if (medicationsListContainer != null) {
            handleAddMedicationRow(null);
        }
    }

    @FXML
    private void handleAddMedicationRow(ActionEvent event) {
        HBox row = new HBox(10);

        TextField medName = new TextField();
        medName.setPromptText("Medicine Name (e.g. Paracetamol 500mg)");
        HBox.setHgrow(medName, Priority.ALWAYS);
        medName.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 8;");

        ComboBox<String> dosage = new ComboBox<>();
        dosage.getItems().addAll(
                "1-1-1 (After Meal)", "1-0-1 (After Meal)", "0-0-1 (After Meal)", "1-0-0 (After Meal)",
                "1-1-1 (Before Meal)", "1-0-1 (Before Meal)", "0-0-1 (Before Meal)", "1-0-0 (Before Meal)",
                "When needed", "As directed"
        );
        dosage.setPromptText("Select Dosage");
        dosage.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        TextField duration = new TextField();
        duration.setPromptText("Duration (e.g. 7 Days)");
        duration.setPrefWidth(140);
        duration.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 8;");

        Button removeBtn = new Button("❌");
        removeBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 12;");
        removeBtn.setOnAction(e -> {
            if (medicationsListContainer.getChildren().size() > 1) {
                medicationsListContainer.getChildren().remove(row);
            }
        });

        row.getChildren().addAll(medName, dosage, duration, removeBtn);
        medicationsListContainer.getChildren().add(row);
    }

    @FXML private void handleIssuePrescription(ActionEvent event) {
        String patientSysId = prescribePatientIdField.getText().trim();
        String diagnosis = prescribeDiagnosisField.getText().trim();

        if(patientSysId.isEmpty() || diagnosis.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill out Patient ID and Diagnosis fields.");
            return;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();

        String findIdsQuery = "SELECT (SELECT id FROM Patients WHERE system_id = ?) as pid, id as did, hospital_id as hid FROM Doctors WHERE user_id = ?";
        String insertDiagnosisQuery = "INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date, notes) VALUES (?, ?, ?, ?, CURDATE(), 'Via Quick Prescribe')";
        String insertPrescriptionQuery = "INSERT INTO Prescriptions (patient_id, doctor_id, hospital_id, prescription_date, notes) VALUES (?, ?, ?, CURDATE(), ?)";
        String insertMedsQuery = "INSERT INTO Prescription_Items (prescription_id, medicine_name, dosage, frequency, duration, instructions) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int patientId = -1, doctorId = -1, hospitalId = -1;

            try(PreparedStatement st1 = conn.prepareStatement(findIdsQuery)) {
                st1.setString(1, patientSysId);
                st1.setInt(2, currentUser.getId());
                ResultSet rs1 = st1.executeQuery();
                if(rs1.next()) {
                    patientId = rs1.getInt("pid");
                    doctorId = rs1.getInt("did");
                    hospitalId = rs1.getInt("hid");
                }
            }

            if(patientId == -1 || patientId == 0) {
                showAlert(Alert.AlertType.ERROR, "Patient Not Found", "No patient exists with ID: " + patientSysId);
                conn.rollback();
                return;
            }

            try(PreparedStatement stDiag = conn.prepareStatement(insertDiagnosisQuery)) {
                stDiag.setInt(1, patientId);
                stDiag.setInt(2, doctorId);
                stDiag.setInt(3, hospitalId);
                stDiag.setString(4, diagnosis);
                stDiag.executeUpdate();
            }

            int newPrescriptionId = -1;
            try(PreparedStatement st2 = conn.prepareStatement(insertPrescriptionQuery, Statement.RETURN_GENERATED_KEYS)) {
                st2.setInt(1, patientId);
                st2.setInt(2, doctorId);
                st2.setInt(3, hospitalId);
                st2.setString(4, diagnosis);
                st2.executeUpdate();
                ResultSet keys = st2.getGeneratedKeys();
                if(keys.next()) newPrescriptionId = keys.getInt(1);
            }

            if(newPrescriptionId != -1) {
                boolean addedMeds = false;
                try(PreparedStatement st3 = conn.prepareStatement(insertMedsQuery)) {
                    for (Node node : medicationsListContainer.getChildren()) {
                        if (node instanceof HBox) {
                            HBox row = (HBox) node;
                            TextField nameFld = (TextField) row.getChildren().get(0);
                            @SuppressWarnings("unchecked")
                            ComboBox<String> doseCb = (ComboBox<String>) row.getChildren().get(1);
                            TextField durFld = (TextField) row.getChildren().get(2);

                            String medName = nameFld.getText().trim();
                            String dosage = doseCb.getValue() != null ? doseCb.getValue() : "As directed";
                            String duration = durFld.getText().trim().isEmpty() ? "Until finished" : durFld.getText().trim();

                            if (!medName.isEmpty()) {
                                st3.setInt(1, newPrescriptionId);
                                st3.setString(2, medName);
                                st3.setString(3, dosage);
                                st3.setString(4, "Regularly");
                                st3.setString(5, duration);
                                st3.setString(6, "Follow prescription instructions");
                                st3.addBatch();
                                addedMeds = true;
                            }
                        }
                    }
                    if(addedMeds) {
                        st3.executeBatch();
                    }
                }
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Prescription securely issued to global registry and sent to Patient Portal.");

            prescribePatientIdField.clear();
            prescribeDiagnosisField.clear();
            medicationsListContainer.getChildren().clear();
            handleAddMedicationRow(null);

            loadPatientRoster();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not issue prescription. " + e.getMessage());
        }
    }

    private void loadDoctorProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        if (doctorNameLabel != null) doctorNameLabel.setText(formatDoctorName(currentUser.getFullName()));

        if (doctorEmojiLabel != null) {
            String name = currentUser.getFullName().toLowerCase();
            boolean isFemale = name.contains("alisha") || name.contains("faria") || name.contains("sameha");
            doctorEmojiLabel.setText(isFemale ? "👩‍⚕️" : "👨‍⚕️");
        }

        String query = "SELECT h.name AS hospital_name, d.specialization, d.license_number, d.degrees FROM Doctors d JOIN Hospitals h ON d.hospital_id = h.id WHERE d.user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (hospitalNameLabel != null) hospitalNameLabel.setText(rs.getString("hospital_name"));
                String spec = rs.getString("specialization");
                if (doctorSpecialtyLabel != null) doctorSpecialtyLabel.setText(spec);

                if (doctorIdLabel != null) doctorIdLabel.setText("ID: " + currentUser.getSystemId());

                String degrees = rs.getString("degrees");
                if (dashboardDegreesLabel != null) {
                    dashboardDegreesLabel.setText(degrees != null && !degrees.isEmpty() ? degrees : "MBBS, MD");
                }

                String license = rs.getString("license_number");
                if (dashboardLicenseLabel != null) {
                    dashboardLicenseLabel.setText(license != null && !license.isEmpty() ? license : "Pending Verification");
                }

                int exp = 4 + (currentUser.getId() % 15);
                if (dashboardExperienceLabel != null) {
                    dashboardExperienceLabel.setText(exp + "+ Years in " + (spec != null ? spec : "General Practice"));
                }

            } else {
                if (hospitalNameLabel != null) hospitalNameLabel.setText("Unassigned Hospital");
                if (doctorSpecialtyLabel != null) doctorSpecialtyLabel.setText("General Practitioner");
                if (doctorIdLabel != null) doctorIdLabel.setText("ID: " + currentUser.getSystemId());
                if (dashboardDegreesLabel != null) dashboardDegreesLabel.setText("MBBS");
                if (dashboardLicenseLabel != null) dashboardLicenseLabel.setText("N/A");
                if (dashboardExperienceLabel != null) dashboardExperienceLabel.setText("5+ Years");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (hospitalNameLabel != null) hospitalNameLabel.setText("Error loading data");
            if (doctorIdLabel != null) doctorIdLabel.setText("ID: " + currentUser.getSystemId());
        }
    }

    private void loadDashboardStatistics() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;
        int userId = currentUser.getId();

        try (Connection conn = DBConnection.getConnection()) {

            int totalPatients = 0;
            int activeCases = 0;

            String totalQuery = "SELECT COUNT(DISTINCT a.patient_id) AS total FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id WHERE d.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(totalQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) totalPatients = rs.getInt("total");
            }

            String activeQuery = "SELECT COUNT(DISTINCT a.patient_id) AS active FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id WHERE d.user_id = ? AND a.status = 'SCHEDULED'";
            try (PreparedStatement stmt = conn.prepareStatement(activeQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) activeCases = rs.getInt("active");
            }

            int recoveredCases = Math.max(0, totalPatients - activeCases);

            if (totalPatientsLabel != null) totalPatientsLabel.setText(String.valueOf(totalPatients));
            if (totalPatientsSubLabel != null) totalPatientsSubLabel.setText(activeCases + " Active • " + recoveredCases + " Recovered");

            if (activeCasesLabel != null) activeCasesLabel.setText(String.valueOf(activeCases));
            if (activeCasesSubLabel != null) activeCasesSubLabel.setText("Currently managing " + activeCases + " cases");

            String todayQuery = "SELECT COUNT(*) AS total FROM Appointments a JOIN Doctors d ON a.doctor_id = d.id WHERE d.user_id = ? AND DATE(a.appointment_date) = CURDATE() AND a.status = 'SCHEDULED'";
            try (PreparedStatement stmt = conn.prepareStatement(todayQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int totalToday = rs.getInt("total");
                    if (todaysApptsLabel != null) todaysApptsLabel.setText(String.valueOf(totalToday));
                    if (todaysApptsSubLabel != null) todaysApptsSubLabel.setText(totalToday + " Scheduled Today");
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleGlobalPatientSearch(ActionEvent event) {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) return;
        searchTerm = searchTerm.trim();

        String query = "SELECT system_id FROM Patients WHERE system_id = ? OR full_name LIKE ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, searchTerm);
            stmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sysId = rs.getString("system_id");
                loadPatientProfileData(sysId);
                searchField.clear();
            } else {
                showPatients(null);
                showProfileTab();
                if(profileDataContainer != null) { profileDataContainer.setVisible(false); profileDataContainer.setManaged(false); }
                if(profilePlaceholder != null) { profilePlaceholder.setVisible(true); profilePlaceholder.setManaged(true); }
                if(profilePlaceholderIcon != null) profilePlaceholderIcon.setText("🔍");
                if(profilePlaceholderTitle != null) profilePlaceholderTitle.setText("Patient Not Found");
                if(profilePlaceholderSubtitle != null) profilePlaceholderSubtitle.setText("No records match '" + searchTerm + "'. Please try a different ID or Name.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTodaysSchedule() {
        if (scheduleVBox == null) return;
        scheduleVBox.getChildren().clear();
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String query = "SELECT p.full_name, a.appointment_date FROM Appointments a JOIN Patients p ON a.patient_id = p.id JOIN Doctors d ON a.doctor_id = d.id WHERE d.user_id = ? AND DATE(a.appointment_date) = CURDATE() AND a.status = 'SCHEDULED' ORDER BY a.appointment_date ASC LIMIT 8";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm");
            DateTimeFormatter amPmFmt = DateTimeFormatter.ofPattern("a");

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String patientName = rs.getString("full_name");
                LocalDateTime apptTime = rs.getTimestamp("appointment_date").toLocalDateTime();

                scheduleVBox.getChildren().add(createMiniScheduleCard(apptTime.format(timeFmt), apptTime.format(amPmFmt), patientName, "Follow-up Checkup"));
            }
            if (!hasData) scheduleVBox.getChildren().add(new Label("No appointments today."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPatientRoster() {
        if (patientRosterContainer == null) return;
        patientRosterContainer.getChildren().clear();
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String query = "SELECT p.id, p.full_name, p.system_id, p.gender, p.blood_group, p.weight, p.height, " +
                "(SELECT diagnosis FROM Medical_History mh WHERE mh.patient_id = p.id ORDER BY diagnosis_date DESC LIMIT 1) AS current_diag " +
                "FROM Patients p " +
                "JOIN Appointments a ON p.id = a.patient_id " +
                "JOIN Doctors d ON a.doctor_id = d.id " +
                "WHERE d.user_id = ? " +
                "GROUP BY p.id";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String fullName = rs.getString("full_name");
                String sysId = rs.getString("system_id");
                String gender = rs.getString("gender");
                String bg = rs.getString("blood_group");
                double weight = rs.getDouble("weight");
                double height = rs.getDouble("height");

                String currentDiag = rs.getString("current_diag");
                if (currentDiag == null) currentDiag = "Pending Assessment";

                String vitalsText = String.format("🩸 %s   •   ⚖️ %.1f kg   •   📏 %.1f cm",
                        (bg != null ? bg : "N/A"), weight, height);

                patientRosterContainer.getChildren().add(createPatientCard(fullName, sysId, gender, currentDiag, vitalsText));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAppointments() {
        if (appointmentsContainer == null) return;
        appointmentsContainer.getChildren().clear();
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String query = "SELECT p.full_name, a.appointment_date FROM Appointments a JOIN Patients p ON a.patient_id = p.id JOIN Doctors d ON a.doctor_id = d.id WHERE d.user_id = ? AND DATE(a.appointment_date) >= CURDATE() AND a.status = 'SCHEDULED' ORDER BY a.appointment_date ASC LIMIT 15";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm");
            DateTimeFormatter amPmFmt = DateTimeFormatter.ofPattern("a");
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            while (rs.next()) {
                String patientName = rs.getString("full_name");
                LocalDateTime apptTime = rs.getTimestamp("appointment_date").toLocalDateTime();

                appointmentsContainer.getChildren().add(createLargeAppointmentCard(
                        apptTime.format(timeFmt),
                        apptTime.format(amPmFmt),
                        patientName,
                        apptTime.format(dateFmt)
                ));
            }

            if (appointmentsContainer.getChildren().isEmpty()) {
                appointmentsContainer.getChildren().add(new Label("No upcoming appointments found."));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void showRosterTab() {
        if(subViewRoster != null) { subViewRoster.setVisible(true); subViewRoster.setManaged(true); }
        if(subViewProfile != null) { subViewProfile.setVisible(false); subViewProfile.setManaged(false); }
        if(btnTabRoster != null) btnTabRoster.setStyle(TAB_ACTIVE);
        if(btnTabProfile != null) btnTabProfile.setStyle(TAB_INACTIVE);
    }

    @FXML private void showProfileTab() {
        if(subViewRoster != null) { subViewRoster.setVisible(false); subViewRoster.setManaged(false); }
        if(subViewProfile != null) { subViewProfile.setVisible(true); subViewProfile.setManaged(true); }
        if(btnTabProfile != null) btnTabProfile.setStyle(TAB_ACTIVE);
        if(btnTabRoster != null) btnTabRoster.setStyle(TAB_INACTIVE);
    }

    @FXML private void showScheduleTab() {
        if(subViewSchedule != null) { subViewSchedule.setVisible(true); subViewSchedule.setManaged(true); }
        if(subViewPrescribe != null) { subViewPrescribe.setVisible(false); subViewPrescribe.setManaged(false); }
        if(btnTabSchedule != null) btnTabSchedule.setStyle(TAB_ACTIVE);
        if(btnTabPrescribe != null) btnTabPrescribe.setStyle(TAB_INACTIVE);
    }

    @FXML private void showPrescribeTab() {
        if(subViewSchedule != null) { subViewSchedule.setVisible(false); subViewSchedule.setManaged(false); }
        if(subViewPrescribe != null) { subViewPrescribe.setVisible(true); subViewPrescribe.setManaged(true); }
        if(btnTabPrescribe != null) btnTabPrescribe.setStyle(TAB_ACTIVE);
        if(btnTabSchedule != null) btnTabSchedule.setStyle(TAB_INACTIVE);
    }

    private void jumpToPrescribe(String patientId) {
        hideAllViews();
        if (viewAppointments != null) { viewAppointments.setVisible(true); viewAppointments.setManaged(true); }
        resetButtons();
        if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE);

        showPrescribeTab();

        if (prescribePatientIdField != null) {
            prescribePatientIdField.setText(patientId);
        }
    }

    @FXML private void handlePrescribeFromProfile(ActionEvent event) {
        jumpToPrescribe(currentProfilePatientId);
    }

    private void loadPatientProfileData(String systemId) {
        String query = "SELECT id, full_name, date_of_birth, gender, blood_group, weight, height, phone FROM Patients WHERE system_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, systemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentProfilePatientId = systemId;
                int internalPatientId = rs.getInt("id");

                profileName.setText(rs.getString("full_name"));
                profileId.setText("ID: " + systemId);
                profilePhone.setText("📞 " + rs.getString("phone"));
                profileAvatar.setText("MALE".equalsIgnoreCase(rs.getString("gender")) ? "👨" : "👩");
                profileBlood.setText(rs.getString("blood_group"));
                profileWeight.setText(rs.getDouble("weight") + " kg");
                profileHeight.setText(rs.getDouble("height") + " cm");
                profileDob.setText(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toString() : "N/A");

                if (testReportDetailView != null) { testReportDetailView.setVisible(false); testReportDetailView.setManaged(false); }
                if (testReportsListWrapper != null) { testReportsListWrapper.setVisible(true); testReportsListWrapper.setManaged(true); }

                loadExtendedMedicalHistory(internalPatientId);

                if(profileDataContainer != null) { profileDataContainer.setVisible(true); profileDataContainer.setManaged(true); }
                if(profilePlaceholder != null) { profilePlaceholder.setVisible(false); profilePlaceholder.setManaged(false); }

                if(profilePlaceholderIcon != null) profilePlaceholderIcon.setText("🏥");
                if(profilePlaceholderTitle != null) profilePlaceholderTitle.setText("No Patient Profile Loaded");
                if(profilePlaceholderSubtitle != null) profilePlaceholderSubtitle.setText("Search using the top bar, or select 'Full Profile' from the Patient Roster.");

                showPatients(null);
                showProfileTab();
                if (mainScrollPane != null) mainScrollPane.setVvalue(0.0);

            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadExtendedMedicalHistory(int patientId) {
        if(profileDiagnosesContainer == null || profileMedicationsContainer == null || profileTestsContainer == null) return;

        profileDiagnosesContainer.getChildren().clear();
        profileMedicationsContainer.getChildren().clear();
        profileTestsContainer.getChildren().clear();

        try (Connection conn = DBConnection.getConnection()) {

            try {
                String diagQuery = "SELECT mh.diagnosis, mh.diagnosis_date, u.full_name as doctor_name FROM Medical_History mh JOIN Doctors d ON mh.diagnosed_by = d.id JOIN Users u ON d.user_id = u.id WHERE mh.patient_id = ? ORDER BY mh.diagnosis_date DESC";
                try (PreparedStatement st = conn.prepareStatement(diagQuery)) {
                    st.setInt(1, patientId);
                    ResultSet rs = st.executeQuery();
                    boolean hasData = false;
                    while(rs.next()) {
                        hasData = true;
                        String dateStr = rs.getDate("diagnosis_date") != null ? rs.getDate("diagnosis_date").toString() : "Recent";
                        profileDiagnosesContainer.getChildren().add(createDataCard(rs.getString("diagnosis"), "Diagnosed by " + rs.getString("doctor_name"), dateStr));
                    }
                    if(!hasData) profileDiagnosesContainer.getChildren().add(createEmptyLabel("No prior diagnostic history found."));
                }
            } catch (Exception e) {
                profileDiagnosesContainer.getChildren().add(createEmptyLabel("Error loading diagnoses: " + e.getMessage()));
            }

            try {
                String medQuery = "SELECT pi.medicine_name, pi.dosage, pi.frequency, pi.instructions FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 10";
                try (PreparedStatement st = conn.prepareStatement(medQuery)) {
                    st.setInt(1, patientId);
                    ResultSet rs = st.executeQuery();
                    boolean hasData = false;
                    while(rs.next()) {
                        hasData = true;
                        String instructions = rs.getString("instructions") != null ? " (" + rs.getString("instructions") + ")" : "";
                        profileMedicationsContainer.getChildren().add(createDataCard("💊 " + rs.getString("medicine_name"), "Dosage: " + rs.getString("dosage") + " - " + rs.getString("frequency") + instructions, null));
                    }
                    if(!hasData) profileMedicationsContainer.getChildren().add(createEmptyLabel("No active medications found."));
                }
            } catch (Exception e) {
                profileMedicationsContainer.getChildren().add(createEmptyLabel("Error loading medications: " + e.getMessage()));
            }

            try {
                String testQuery = "SELECT id, report_type, report_date, notes, file_url FROM Test_Reports WHERE patient_id = ? ORDER BY report_date DESC";
                try (PreparedStatement st = conn.prepareStatement(testQuery)) {
                    st.setInt(1, patientId);
                    ResultSet rs = st.executeQuery();
                    boolean hasData = false;
                    while(rs.next()) {
                        hasData = true;
                        String dateStr = rs.getDate("report_date") != null ? rs.getDate("report_date").toString() : "Recent";
                        String notes = rs.getString("notes") != null ? rs.getString("notes") : "File attached.";
                        String fileUrl = rs.getString("file_url");

                        profileTestsContainer.getChildren().add(createTestReportCard(
                                rs.getInt("id"),
                                "🔬 " + rs.getString("report_type"),
                                dateStr,
                                notes,
                                fileUrl
                        ));
                    }
                    if(!hasData) profileTestsContainer.getChildren().add(createEmptyLabel("No test reports found for this patient."));
                }
            } catch (Exception e) {
                profileTestsContainer.getChildren().add(createEmptyLabel("Error loading test reports: " + e.getMessage()));
            }

        } catch(Exception e) { e.printStackTrace(); }
    }

    private VBox createDataCard(String titleText, String subtitleText, String dateText) {
        VBox box = new VBox(3);
        box.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox header = new HBox();
        Label title = new Label(titleText);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 14px;");
        header.getChildren().add(title);

        if (dateText != null) {
            HBox.setHgrow(title, Priority.ALWAYS);
            Label dateLabel = new Label(dateText);
            dateLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
            header.getChildren().add(dateLabel);
            header.setSpacing(10);
        }

        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        subtitle.setWrapText(true);

        box.getChildren().addAll(header, subtitle);
        return box;
    }

    private VBox createTestReportCard(int reportId, String title, String date, String notes, String fileName) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox topBox = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 14px;");
        Label dateLbl = new Label("📅 " + date);
        dateLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        topBox.getChildren().addAll(titleLbl, dateLbl);

        HBox btnBox = new HBox(10);
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: white; -fx-text-fill: #115E59; -fx-border-color: #E2E8F0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;");
        viewBtn.setOnAction(e -> handleViewReport(reportId, title, date, notes, fileName));

        Button downBtn = new Button("Download");
        downBtn.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;");
        downBtn.setOnAction(e -> handleDownloadReport(reportId, fileName));

        btnBox.getChildren().addAll(viewBtn, downBtn);
        box.getChildren().addAll(topBox, btnBox);
        return box;
    }

    private void handleViewReport(int reportId, String testName, String dateStr, String notes, String fileName) {
        testReportsListWrapper.setVisible(false);
        testReportsListWrapper.setManaged(false);
        testReportDetailView.setVisible(true);
        testReportDetailView.setManaged(true);

        reportDetailTitle.setText(testName);
        reportDetailInfo.setText("Tested on: " + dateStr);
        reportDetailContentBox.getChildren().clear();

        VBox notesBox = new VBox(5);
        notesBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        Label notesHeader = new Label("👨‍⚕️ Notes");
        notesHeader.setStyle("-fx-text-fill: #115E59; -fx-font-weight: bold; -fx-font-size: 13px; -fx-font-family: 'Segoe UI Emoji', 'System';");
        Label notesText = new Label(notes != null && !notes.trim().isEmpty() ? notes : "No specific notes provided.");
        notesText.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 12px;");
        notesText.setWrapText(true);
        notesBox.getChildren().addAll(notesHeader, notesText);

        HBox fileBox = new HBox(10);
        fileBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: center-left;");
        Label fileIcon = new Label("📄");
        fileIcon.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;");
        VBox fileInfo = new VBox(2);

        if (fileName != null && !fileName.trim().isEmpty() && !fileName.equals("No File Attached")) {
            Label fileTitle = new Label("Document Attached");
            fileTitle.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 12px;");
            Label fileNameLbl = new Label(getCleanFileName(fileName));
            fileNameLbl.setStyle("-fx-text-fill: #5C8D7D; -fx-font-size: 11px; -fx-font-weight: bold;");
            fileInfo.getChildren().addAll(fileTitle, fileNameLbl);
        } else {
            Label fileTitle = new Label("No Document");
            fileTitle.setStyle("-fx-text-fill: #B91C1C; -fx-font-weight: bold; -fx-font-size: 12px;");
            fileBox.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: center-left;");
            fileIcon.setText("⚠️");
            fileInfo.getChildren().addAll(fileTitle);
        }
        fileBox.getChildren().addAll(fileIcon, fileInfo);

        reportDetailContentBox.getChildren().addAll(notesBox, fileBox);
    }

    @FXML private void hideTestReportDetails() {
        testReportDetailView.setVisible(false);
        testReportDetailView.setManaged(false);
        testReportsListWrapper.setVisible(true);
        testReportsListWrapper.setManaged(true);
    }

    private void handleDownloadReport(int reportId, String fileName) {
        if (fileName == null || fileName.equals("No File Attached") || fileName.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No File", "There is no file attached to this report.");
            return;
        }

        String safeName = getCleanFileName(fileName);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Test Report");
        fileChooser.setInitialFileName(safeName);

        File saveLocation = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (saveLocation != null) {
            String query = "SELECT file_data FROM Test_Reports WHERE id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reportId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    byte[] fileBytes = rs.getBytes("file_data");
                    if (fileBytes != null && fileBytes.length > 0) {
                        Files.write(saveLocation.toPath(), fileBytes);
                        showAlert(Alert.AlertType.INFORMATION, "Success", "File downloaded successfully!");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Empty File", "This record was created before file uploads were fully supported. The file is empty.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Download Error", "Failed to save the file: " + ex.getMessage());
            }
        }
    }

    private Label createEmptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic; -fx-padding: 10 0;");
        l.setWrapText(true);
        return l;
    }

    private HBox createMiniScheduleCard(String hourText, String amPmText, String nameText, String statusText) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: #F8FAF9; -fx-background-radius: 10; -fx-padding: 10 15; -fx-spacing: 15; -fx-alignment: center-left;");

        Label lblHour = new Label(hourText);
        lblHour.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1B362F;");
        lblHour.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

        Label lblAmPm = new Label(amPmText);
        lblAmPm.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #5C8D7D;");

        VBox timeBox = new VBox(lblHour, lblAmPm);
        timeBox.setStyle("-fx-alignment: center; -fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8 10; -fx-min-width: 65; -fx-pref-width: 65;");

        VBox detailsBox = new VBox(new Label(nameText), new Label(statusText));
        detailsBox.setStyle("-fx-spacing: 2; -fx-alignment: center-left;");
        detailsBox.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");
        detailsBox.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        card.getChildren().addAll(timeBox, detailsBox);
        return card;
    }

    private HBox createPatientCard(String name, String id, String gender, String currentDiag, String oneLineVitals) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 25; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 20, 0, 5, 5); -fx-alignment: center-left;");

        VBox iconBox = new VBox();
        iconBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");
        Label icon = new Label("MALE".equalsIgnoreCase(gender) ? "👨" : "👩");
        icon.setStyle("-fx-font-size: 30px; -fx-font-family: 'Segoe UI Emoji';");
        iconBox.getChildren().add(icon);

        VBox centerInfo = new VBox(10);
        HBox.setHgrow(centerInfo, Priority.ALWAYS);

        HBox nameIdBox = new HBox(10);
        nameIdBox.setStyle("-fx-alignment: center-left;");
        Label nameLbl = new Label(name != null ? name : "Unknown");
        nameLbl.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 20px;");
        Label idLbl = new Label("ID: " + id);
        idLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-padding: 4 0 0 0;");
        nameIdBox.getChildren().addAll(nameLbl, idLbl);

        HBox diagnosisBox = new HBox(30);

        VBox diagCol = new VBox(2);
        Label diagTitle = new Label("Current Diagnosis");
        diagTitle.setStyle("-fx-text-fill: #5C8D7D; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label diagVal = new Label(currentDiag);
        diagVal.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 14px;");
        diagVal.setWrapText(true);
        diagCol.getChildren().addAll(diagTitle, diagVal);

        VBox vitalsCol = new VBox(2);
        vitalsCol.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1; -fx-padding: 0 0 0 20;");
        Label vitalsTitle = new Label("Physical Vitals");
        vitalsTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label vitalsVal = new Label(oneLineVitals);
        vitalsVal.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: 'Segoe UI Emoji', 'System';");

        vitalsCol.getChildren().addAll(vitalsTitle, vitalsVal);
        diagnosisBox.getChildren().addAll(diagCol, vitalsCol);
        centerInfo.getChildren().addAll(nameIdBox, diagnosisBox);

        VBox rightButtons = new VBox(10);
        rightButtons.setStyle("-fx-alignment: center-right;");

        Button btnProfile = new Button("📄 Full Profile");
        btnProfile.setPrefWidth(140);
        btnProfile.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-font-family: 'Segoe UI Emoji', 'System';");
        btnProfile.setOnAction(e -> loadPatientProfileData(id));

        Button btnPrescribe = new Button("💊 Prescribe");
        btnPrescribe.setPrefWidth(140);
        btnPrescribe.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';");
        btnPrescribe.setOnAction(e -> jumpToPrescribe(id));

        rightButtons.getChildren().addAll(btnProfile, btnPrescribe);
        card.getChildren().addAll(iconBox, centerInfo, rightButtons);
        return card;
    }

    private HBox createLargeAppointmentCard(String time, String amPm, String name, String dateText) {
        HBox card = new HBox(); card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left; -fx-spacing: 20;");
        VBox timeBox = new VBox(new Label(time), new Label(amPm)); timeBox.setStyle("-fx-background-color: #115E59; -fx-background-radius: 12; -fx-min-width: 75; -fx-pref-width: 75; -fx-min-height: 65; -fx-alignment: center;"); timeBox.getChildren().get(0).setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 18px;"); timeBox.getChildren().get(1).setStyle("-fx-text-fill: #A3CFC0; -fx-font-weight: bold; -fx-font-size: 12px;");
        VBox infoBox = new VBox(new Label(name), new Label("Reason: Scheduled Consultation"), new HBox(new Label("📅"), new Label(dateText))); infoBox.setSpacing(3); HBox.setHgrow(infoBox, Priority.ALWAYS); infoBox.getChildren().get(0).setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 18px;"); infoBox.getChildren().get(1).setStyle("-fx-text-fill: #5C8D7D; -fx-font-weight: bold; -fx-font-size: 13px;"); ((HBox)infoBox.getChildren().get(2)).setSpacing(5); ((HBox)infoBox.getChildren().get(2)).getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #6B7280; -fx-font-size: 11px;"); ((HBox)infoBox.getChildren().get(2)).getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        card.getChildren().addAll(timeBox, infoBox); return card;
    }

    @FXML private void showDashboard(ActionEvent event) { hideAllViews(); if (viewDashboard != null) { viewDashboard.setVisible(true); viewDashboard.setManaged(true); } resetButtons(); if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_STYLE); }

    @FXML private void showPatients(ActionEvent event) {
        hideAllViews();
        if (viewMyPatients != null) { viewMyPatients.setVisible(true); viewMyPatients.setManaged(true); }
        resetButtons();
        if (btnPatients != null) btnPatients.setStyle(ACTIVE_STYLE);
        showRosterTab();
    }

    @FXML private void showAppointments(ActionEvent event) {
        hideAllViews();
        if (viewAppointments != null) { viewAppointments.setVisible(true); viewAppointments.setManaged(true); }
        resetButtons();
        if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE);
        showScheduleTab();
    }

    private void hideAllViews() {
        if (viewDashboard != null) { viewDashboard.setVisible(false); viewDashboard.setManaged(false); }
        if (viewMyPatients != null) { viewMyPatients.setVisible(false); viewMyPatients.setManaged(false); }
        if (viewAppointments != null) { viewAppointments.setVisible(false); viewAppointments.setManaged(false); }
        if (mainScrollPane != null) { mainScrollPane.setVvalue(0.0); }
    }

    private void resetButtons() { if (btnDashboard != null) btnDashboard.setStyle(INACTIVE_STYLE); if (btnPatients != null) btnPatients.setStyle(INACTIVE_STYLE); if (btnAppointments != null) btnAppointments.setStyle(INACTIVE_STYLE); }

    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}