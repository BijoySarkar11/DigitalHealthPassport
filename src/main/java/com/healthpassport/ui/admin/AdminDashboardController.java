package com.healthpassport.ui.admin;

import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.MODEL.user.Patient;
import com.healthpassport.MODEL.service.DoctorService;
import com.healthpassport.MODEL.service.PatientService;
import com.healthpassport.MODEL.user.User;
import com.healthpassport.ui.BaseController;
import com.healthpassport.util.DBConnection;
import com.healthpassport.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController extends BaseController {

    @FXML private BorderPane rootPane;

    @FXML private Button btnSearch, btnNewPatient, btnNewDoctor, btnAddTestReport;
    @FXML private VBox viewSearch, viewNewPatient, viewNewDoctor, viewAddTestReport, viewFullProfile;

    // Search View
    @FXML private Label adminNameLabel, hospitalNameLabel;
    @FXML private TextField dbSearchField;
    @FXML private Button btnTabDoctors, btnTabPatients;
    @FXML private VBox subViewDoctors, subViewPatients;
    @FXML private VBox doctorRecordsContainer, patientRecordsContainer;

    // Full Profile Viewer Elements
    @FXML private VBox profileIconBox;
    @FXML private Label profileIconLbl, profileNameLbl, profileRoleBadge, profileIdLbl, profileContactLbl;
    @FXML private HBox profileInfoRow, profileMedicalGrid;
    @FXML private VBox profileDiagnosesContainer, profileMedicationsContainer;

    // Test Report Inline Viewer Elements
    @FXML private VBox testReportsListWrapper;
    @FXML private VBox profileTestsContainer;
    @FXML private VBox testReportDetailView;
    @FXML private Label reportDetailTitle;
    @FXML private Label reportDetailInfo;
    @FXML private VBox reportDetailContentBox;

    private String currentProfileNatId = null;
    private String currentProfileRole = null;

    // Form Dynamic Titles, Status Labels & Buttons
    @FXML private Label lblPatientFormTitle, lblDoctorFormTitle;
    @FXML private Label patStatusLabel, docStatusLabel;
    @FXML private Button btnSubmitPatient, btnSubmitDoctor;
    @FXML private Label patIdLabel, docIdLabel;

    // Patient Form
    @FXML private TextField patFullNameField, patPhoneField, patPasswordField, patIdField;
    @FXML private DatePicker patDobPicker;
    @FXML private ComboBox<String> patGenderCombo, patBloodCombo;
    @FXML private TextField patWeightField, patHeightField;
    @FXML private TextField patInitialDiagnosisField;

    // Doctor Form
    @FXML private TextField docNameField, docSpecialtyField, docLicenseField, docDegreesField, docPasswordField, docIdField;
    @FXML private TextField docExperienceField; // NEW

    // Test Report Form
    @FXML private TextField testPatientIdField;
    @FXML private Label testVerificationLabel;
    @FXML private ComboBox<String> testTypeCombo;
    @FXML private DatePicker testDatePicker;
    @FXML private TextArea testSummaryArea;

    // File Upload Variables
    @FXML private Label fileSelectedLabel;
    @FXML private Button btnCancelFile;
    @FXML private Button btnBrowseFile;
    private File selectedUploadFile;

    private int adminHospitalId = -1;
    private int verifiedPatientDbId = -1;

    // State Trackers for Edit Mode
    private String editingPatientNatId = null;
    private String editingDoctorNatId = null;

    // OOP Services
    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();

    private final String ACTIVE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String TAB_ACTIVE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    private final String TAB_INACTIVE = "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 20;";

    private String cleanNameForDB(String rawName) {
        if (rawName == null) return "";
        return rawName.replaceFirst("^(?i)(\\s*(dr\\.?|doctor)\\s*)+", "").trim();
    }

    private String formatDoctorName(String rawName) {
        return "Dr. " + cleanNameForDB(rawName);
    }

    private String getCleanFileName(String dbFileName) {
        if (dbFileName == null || dbFileName.trim().isEmpty()) return "report_document.pdf";
        String clean = dbFileName.replace("\\", "/");
        if (clean.contains("/")) {
            clean = clean.substring(clean.lastIndexOf('/') + 1);
        }
        return clean.trim().isEmpty() ? "report_document.pdf" : clean;
    }

    @FXML
    public void initialize() {
        showSearch(null);
        loadAdminProfile();
        loadRecords("");
        showDoctorsTab();

        if (patGenderCombo != null) {
            patGenderCombo.setItems(FXCollections.observableArrayList("MALE", "FEMALE", "OTHER"));
            patGenderCombo.setPromptText("Select Gender");
        }
        if (patBloodCombo != null) {
            patBloodCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
            patBloodCombo.setPromptText("Blood Group");
        }
        if (testTypeCombo != null) {
            testTypeCombo.setItems(FXCollections.observableArrayList("Complete Blood Count (CBC)", "Chest X-Ray", "MRI Scan", "CT Scan", "Urinalysis", "Lipid Profile", "ECG", "Other"));
            testTypeCombo.setPromptText("Select Test Type");
        }
    }

    private void loadAdminProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;
        adminHospitalId = currentUser.getHospitalId();

        if (adminNameLabel != null) adminNameLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "System Admin");

        String query = "SELECT name FROM Hospitals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminHospitalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (hospitalNameLabel != null) hospitalNameLabel.setText(rs.getString("name"));
            } else {
                if (hospitalNameLabel != null) hospitalNameLabel.setText("Global Administration");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void resetPatientForm() {
        editingPatientNatId = null;
        if (lblPatientFormTitle != null) lblPatientFormTitle.setText("Register New Patient");
        if (btnSubmitPatient != null) btnSubmitPatient.setText("Confirm & Register Patient");
        if (patStatusLabel != null) patStatusLabel.setText("");
        if (patIdLabel != null) patIdLabel.setText("Assigned System ID (Auto-Generated)");

        try {
            if (patIdField != null) patIdField.setText(patientService.generateSystemId());
        } catch (Exception e) {
            if (patIdField != null) patIdField.setText("Error generating ID");
        }

        patPasswordField.setPromptText("Assign Password");
        patInitialDiagnosisField.setPromptText("Initial Diagnostic History (Optional)");

        patFullNameField.clear(); patPasswordField.clear(); patPhoneField.clear();
        patWeightField.clear(); patHeightField.clear(); patInitialDiagnosisField.clear();
        patDobPicker.setValue(null); patGenderCombo.getSelectionModel().clearSelection(); patBloodCombo.getSelectionModel().clearSelection();
    }

    private void resetDoctorForm() {
        editingDoctorNatId = null;
        if (lblDoctorFormTitle != null) lblDoctorFormTitle.setText("Onboard New Doctor");
        if (btnSubmitDoctor != null) btnSubmitDoctor.setText("Register Doctor to System");
        if (docStatusLabel != null) docStatusLabel.setText("");
        if (docIdLabel != null) docIdLabel.setText("Assigned System ID (Auto-Generated)");

        try {
            if (docIdField != null) docIdField.setText(doctorService.generateSystemId());
        } catch (Exception e) {
            if (docIdField != null) docIdField.setText("Error generating ID");
        }

        docPasswordField.setPromptText("Assign Password");
        docNameField.clear(); docSpecialtyField.clear(); docLicenseField.clear(); docDegreesField.clear(); docPasswordField.clear(); docExperienceField.clear();
    }

    @FXML
    private void handleRegisterPatient(ActionEvent event) {
        if (patFullNameField.getText().isEmpty() || patGenderCombo.getValue() == null) {
            patStatusLabel.setText("❌ Please fill in mandatory patient details (Name, Gender).");
            patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }
        if (editingPatientNatId == null && patPasswordField.getText().isEmpty()) {
            patStatusLabel.setText("❌ A Password is required for new registration.");
            patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        double weight = 0.0, height = 0.0;
        try {
            if (patWeightField.getText() != null && !patWeightField.getText().trim().isEmpty()) weight = Double.parseDouble(patWeightField.getText().trim());
            if (patHeightField.getText() != null && !patHeightField.getText().trim().isEmpty()) height = Double.parseDouble(patHeightField.getText().trim());
        } catch (NumberFormatException e) {
            patStatusLabel.setText("❌ Weight and Height must be valid numbers.");
            patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        Patient patient = new Patient();
        patient.setFullName(patFullNameField.getText().trim());
        patient.setGender(patGenderCombo.getValue());
        patient.setBloodGroup(patBloodCombo.getValue() != null ? patBloodCombo.getValue() : "");
        patient.setPhone(patPhoneField.getText() != null ? patPhoneField.getText().trim() : "");
        patient.setWeight(weight);
        patient.setHeight(height);
        patient.setDateOfBirth(patDobPicker.getValue() != null ? patDobPicker.getValue() : LocalDate.of(2000, 1, 1));
        patient.setHospitalId(adminHospitalId);

        String pass = patPasswordField.getText().trim();
        String intakeDiag = patInitialDiagnosisField.getText() != null ? patInitialDiagnosisField.getText().trim() : "";

        if (editingPatientNatId != null) {
            patient.setSystemId(editingPatientNatId);
            patient.setEmail(editingPatientNatId + "@dhp.com");

            boolean success = patientService.updatePatientProfile(patient);

            if (success) {
                updatePasswordIfProvided(editingPatientNatId, pass);
                syncInitialDiagnosis(editingPatientNatId, intakeDiag);

                resetPatientForm();
                patStatusLabel.setText("✅ Patient Record Updated Successfully!");
                patStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords(dbSearchField.getText());
            } else {
                patStatusLabel.setText("❌ Failed to update patient record.");
                patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        } else {
            String newId = patientService.generateSystemId();
            patient.setSystemId(newId);
            patient.setEmail(newId + "@dhp.com");
            patient.setPasswordHash(pass);

            boolean success = patientService.registerNewPatient(patient);

            if (success) {
                syncInitialDiagnosis(newId, intakeDiag);
                resetPatientForm();
                patStatusLabel.setText("✅ Registration Successful! ID: " + newId);
                patStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords("");
            } else {
                patStatusLabel.setText("❌ Failed to save patient record.");
                patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    private void handleRegisterDoctor(ActionEvent event) {
        if (adminHospitalId <= 0) {
            docStatusLabel.setText("❌ Access Denied: Only local hospital administrators can manage doctors.");
            docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }
        if (docNameField.getText().isEmpty() || docSpecialtyField.getText().isEmpty() || docLicenseField.getText().isEmpty()) {
            docStatusLabel.setText("❌ Name, Specialty, and License are mandatory.");
            docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }
        if (editingDoctorNatId == null && docPasswordField.getText().isEmpty()) {
            docStatusLabel.setText("❌ A Password is required for new registration.");
            docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        int experience = 0;
        try {
            if (!docExperienceField.getText().trim().isEmpty()) {
                experience = Integer.parseInt(docExperienceField.getText().trim());
            }
        } catch (NumberFormatException e) {
            docStatusLabel.setText("❌ Years of Experience must be a valid number.");
            docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        Doctor doctor = new Doctor();
        doctor.setFullName(cleanNameForDB(docNameField.getText().trim()));
        doctor.setSpecialization(docSpecialtyField.getText().trim());
        doctor.setLicenseNumber(docLicenseField.getText().trim());
        doctor.setDegrees(docDegreesField.getText().trim());
        doctor.setHospitalId(adminHospitalId);

        String pass = docPasswordField.getText().trim();

        if (editingDoctorNatId != null) {
            doctor.setSystemId(editingDoctorNatId);
            doctor.setEmail(editingDoctorNatId + "@dhp.com");

            boolean success = doctorService.updateDoctorProfile(doctor);

            if (success) {
                updateExperience(editingDoctorNatId, experience);
                updatePasswordIfProvided(editingDoctorNatId, pass);
                resetDoctorForm();
                docStatusLabel.setText("✅ Doctor Record Updated Successfully!");
                docStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords(dbSearchField.getText());
            } else {
                docStatusLabel.setText("❌ Failed to update doctor record.");
                docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        } else {
            String newId = doctorService.generateSystemId();
            doctor.setSystemId(newId);
            doctor.setEmail(newId + "@dhp.com");
            doctor.setPasswordHash(pass);

            boolean success = doctorService.registerNewDoctor(doctor);

            if (success) {
                updateExperience(newId, experience);
                resetDoctorForm();
                docStatusLabel.setText("✅ Registration Successful! ID: " + newId);
                docStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords("");
            } else {
                docStatusLabel.setText("❌ Failed to save doctor record.");
                docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        }
    }

    private void updateExperience(String sysId, int exp) {
        String query = "UPDATE Doctors SET years_of_experience = ? WHERE user_id = (SELECT id FROM Users WHERE system_id = ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, exp);
            stmt.setString(2, sysId);
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updatePasswordIfProvided(String systemId, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) return;
        String query = "UPDATE Users SET password_hash = ? WHERE system_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, systemId);
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void syncInitialDiagnosis(String systemId, String diagnosis) {
        if (adminHospitalId <= 0) return;
        try (Connection conn = DBConnection.getConnection()) {
            int patientId = -1;
            try (PreparedStatement st = conn.prepareStatement("SELECT id FROM Patients WHERE system_id = ?")) {
                st.setString(1, systemId);
                ResultSet r = st.executeQuery();
                if(r.next()) patientId = r.getInt("id");
            }

            if (patientId != -1) {
                boolean diagExists = false;
                try (PreparedStatement st = conn.prepareStatement("SELECT id FROM Medical_History WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                    st.setInt(1, patientId);
                    ResultSet r = st.executeQuery();
                    diagExists = r.next();
                }

                if (diagExists && (diagnosis == null || diagnosis.isEmpty())) {
                    try (PreparedStatement st = conn.prepareStatement("DELETE FROM Medical_History WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                        st.setInt(1, patientId); st.executeUpdate();
                    }
                } else if (diagExists && !diagnosis.isEmpty()) {
                    try (PreparedStatement st = conn.prepareStatement("UPDATE Medical_History SET diagnosis = ? WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                        st.setString(1, diagnosis); st.setInt(2, patientId); st.executeUpdate();
                    }
                } else if (!diagExists && diagnosis != null && !diagnosis.isEmpty()) {
                    int defaultDocId = 1;
                    try (PreparedStatement ds = conn.prepareStatement("SELECT id FROM Doctors WHERE hospital_id = ? LIMIT 1")) {
                        ds.setInt(1, adminHospitalId); ResultSet r = ds.executeQuery();
                        if (r.next()) defaultDocId = r.getInt("id");
                    }
                    try (PreparedStatement st = conn.prepareStatement("INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date, notes) VALUES (?, ?, ?, ?, CURDATE(), 'Initial Registration Intake')")) {
                        st.setInt(1, patientId); st.setInt(2, defaultDocId); st.setInt(3, adminHospitalId); st.setString(4, diagnosis);
                        st.executeUpdate();
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openEditPatientForm(String natId) {
        String query = "SELECT u.full_name, p.date_of_birth, p.gender, p.blood_group, p.phone, p.weight, p.height " +
                "FROM Users u JOIN Patients p ON u.id = p.user_id WHERE u.system_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, natId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resetPatientForm();
                patFullNameField.setText(rs.getString("full_name"));

                patIdField.setText(natId);
                if (patIdLabel != null) patIdLabel.setText("Assigned System ID");

                patPasswordField.setPromptText("Leave blank to keep current password");

                patInitialDiagnosisField.clear();
                patInitialDiagnosisField.setPromptText("Update Initial Diagnosis (Optional)");
                String diagQuery = "SELECT diagnosis FROM Medical_History WHERE patient_id = (SELECT id FROM Patients WHERE system_id = ?) AND notes = 'Initial Registration Intake' LIMIT 1";
                try (PreparedStatement diagStmt = conn.prepareStatement(diagQuery)) {
                    diagStmt.setString(1, natId);
                    ResultSet r = diagStmt.executeQuery();
                    if (r.next()) patInitialDiagnosisField.setText(r.getString("diagnosis"));
                }

                if (rs.getDate("date_of_birth") != null) patDobPicker.setValue(rs.getDate("date_of_birth").toLocalDate());
                patGenderCombo.setValue(rs.getString("gender"));
                patBloodCombo.setValue(rs.getString("blood_group"));
                patPhoneField.setText(rs.getString("phone"));
                patWeightField.setText(String.valueOf(rs.getDouble("weight")));
                patHeightField.setText(String.valueOf(rs.getDouble("height")));

                editingPatientNatId = natId;
                lblPatientFormTitle.setText("Edit Patient Record");
                btnSubmitPatient.setText("Update Record");

                patStatusLabel.setText("Editing Record: " + natId);
                patStatusLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");

                switchView(viewNewPatient, btnNewPatient);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openEditDoctorForm(String natId) {
        String query = "SELECT u.full_name, d.specialization, d.license_number, d.degrees, d.years_of_experience " +
                "FROM Users u JOIN Doctors d ON u.id = d.user_id WHERE u.system_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, natId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resetDoctorForm();
                docNameField.setText(rs.getString("full_name"));

                docIdField.setText(natId);
                if (docIdLabel != null) docIdLabel.setText("Assigned System ID");

                docPasswordField.setPromptText("Leave blank to keep current password");

                docSpecialtyField.setText(rs.getString("specialization"));
                docLicenseField.setText(rs.getString("license_number"));
                docDegreesField.setText(rs.getString("degrees"));
                docExperienceField.setText(String.valueOf(rs.getInt("years_of_experience")));

                editingDoctorNatId = natId;
                lblDoctorFormTitle.setText("Edit Doctor Record");
                btnSubmitDoctor.setText("Update Record");

                docStatusLabel.setText("Editing Record: " + natId);
                docStatusLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");

                switchView(viewNewDoctor, btnNewDoctor);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleVerifyPatient(ActionEvent event) {
        String inputId = testPatientIdField.getText().trim();
        if (inputId.isEmpty()) return;

        String query = "SELECT id, full_name FROM Patients WHERE system_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, inputId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                verifiedPatientDbId = rs.getInt("id");
                testVerificationLabel.setText("✅ Verified: " + rs.getString("full_name"));
                testVerificationLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 13px;");
            } else {
                verifiedPatientDbId = -1;
                testVerificationLabel.setText("❌ Patient ID not found in system.");
                testVerificationLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleBrowseFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Patient Report");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );

        File tempFile = fileChooser.showOpenDialog(btnBrowseFile.getScene().getWindow());

        if (tempFile != null) {
            selectedUploadFile = tempFile;
            fileSelectedLabel.setText(selectedUploadFile.getName());
            fileSelectedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #10B981;");

            btnCancelFile.setVisible(true);
            btnCancelFile.setManaged(true);
            btnBrowseFile.setText("Change File");
        }
    }

    @FXML
    private void handleCancelFile(ActionEvent event) {
        selectedUploadFile = null;
        fileSelectedLabel.setText("No file selected");
        fileSelectedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        btnCancelFile.setVisible(false);
        btnCancelFile.setManaged(false);
        btnBrowseFile.setText("Browse Files");
    }

    @FXML
    private void handleSubmitTestReport(ActionEvent event) {
        if (verifiedPatientDbId == -1) {
            testVerificationLabel.setText("❌ Please enter and verify a Patient ID first.");
            testVerificationLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }
        if (testTypeCombo.getValue() == null || testDatePicker.getValue() == null || testSummaryArea.getText().trim().isEmpty()) {
            testVerificationLabel.setText("❌ Please select a test type, date, and provide a summary.");
            testVerificationLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        String insertQuery = "INSERT INTO Test_Reports (patient_id, added_by_admin_id, hospital_id, report_type, file_url, report_date, notes, file_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, verifiedPatientDbId);
            stmt.setInt(2, UserSession.getInstance().getCurrentUser().getId());
            stmt.setInt(3, adminHospitalId);
            stmt.setString(4, testTypeCombo.getValue());
            stmt.setDate(6, java.sql.Date.valueOf(testDatePicker.getValue()));
            stmt.setString(7, testSummaryArea.getText().trim());

            if (selectedUploadFile != null) {
                stmt.setString(5, selectedUploadFile.getName());
                FileInputStream fis = new FileInputStream(selectedUploadFile);
                stmt.setBinaryStream(8, fis, (int) selectedUploadFile.length());
            } else {
                stmt.setString(5, "No File Attached");
                stmt.setNull(8, java.sql.Types.BLOB);
            }

            stmt.executeUpdate();

            testVerificationLabel.setText("✅ Test Report securely added to Digital Passport!");
            testVerificationLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px;");

            testPatientIdField.clear();
            verifiedPatientDbId = -1;
            testTypeCombo.getSelectionModel().clearSelection();
            testDatePicker.setValue(null);
            testSummaryArea.clear();

            handleCancelFile(null);

        } catch (Exception e) {
            e.printStackTrace();
            testVerificationLabel.setText("❌ Database Error: Could not submit report.");
            testVerificationLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = dbSearchField.getText() == null ? "" : dbSearchField.getText().trim();
        loadRecords(searchTerm);
    }

    private void loadRecords(String searchTerm) {
        loadDoctors(searchTerm);
        loadPatients(searchTerm);
    }

    private void loadDoctors(String searchTerm) {
        if (doctorRecordsContainer == null) return;
        doctorRecordsContainer.getChildren().clear();

        String query = "SELECT u.system_id, u.full_name, d.specialization, d.license_number, d.degrees " +
                "FROM Doctors d JOIN Users u ON d.user_id = u.id " +
                "WHERE d.hospital_id = ? AND (u.full_name LIKE ? OR u.system_id LIKE ?) " +
                "ORDER BY u.full_name ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setInt(1, adminHospitalId); stmt.setString(2, searchPattern); stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String natId = rs.getString("system_id");
                String name = rs.getString("full_name");
                String spec = rs.getString("specialization");
                String lic = rs.getString("license_number");
                String deg = rs.getString("degrees");

                String displaySpec = (spec != null ? spec : "General");
                if (deg != null && !deg.isEmpty()) displaySpec += " (" + deg + ")";
                String subtitle = displaySpec + " • Lic: " + (lic != null ? lic : "N/A") + " • ID: " + natId;

                HBox card = createDoctorCard(formatDoctorName(name), subtitle);
                VBox btnBox = (VBox) card.getChildren().get(2);
                Button editBtn = (Button) btnBox.getChildren().get(0);
                Button profileBtn = (Button) btnBox.getChildren().get(1);

                editBtn.setOnAction(e -> openEditDoctorForm(natId));
                profileBtn.setOnAction(e -> openFullProfile(natId, "DOCTOR"));

                doctorRecordsContainer.getChildren().add(card);
            }

            if (!hasData) {
                Label emptyLabel = new Label(searchTerm.isEmpty() ? "No doctors assigned to this hospital yet." : "No doctors found matching '" + searchTerm + "'.");
                emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                doctorRecordsContainer.getChildren().add(emptyLabel);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPatients(String searchTerm) {
        if (patientRecordsContainer == null) return;
        patientRecordsContainer.getChildren().clear();

        String query = "SELECT system_id, full_name, created_at " +
                "FROM Patients " +
                "WHERE full_name LIKE ? OR system_id LIKE ? " +
                "ORDER BY created_at DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern); stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String natId = rs.getString("system_id");
                String name = rs.getString("full_name");
                String regDate = rs.getTimestamp("created_at").toLocalDateTime().format(dateFmt);
                String subtitle = "Registered: " + regDate + " • ID: " + natId;

                HBox card = createPatientCard(name, subtitle);
                VBox btnBox = (VBox) card.getChildren().get(2);
                Button editBtn = (Button) btnBox.getChildren().get(0);
                Button profileBtn = (Button) btnBox.getChildren().get(1);

                editBtn.setOnAction(e -> openEditPatientForm(natId));
                profileBtn.setOnAction(e -> openFullProfile(natId, "PATIENT"));

                patientRecordsContainer.getChildren().add(card);
            }

            if (!hasData) {
                Label emptyLabel = new Label(searchTerm.isEmpty() ? "No patients registered in the global system yet." : "No patients found matching '" + searchTerm + "'.");
                emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                patientRecordsContainer.getChildren().add(emptyLabel);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox createDoctorCard(String name, String subtitle) {
        HBox card = new HBox(20); card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20 25; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left;");
        VBox iconBox = new VBox(); iconBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");
        Label icon = new Label("👨‍⚕️"); icon.setStyle("-fx-font-size: 30px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;"); iconBox.getChildren().add(icon);
        VBox infoBox = new VBox(3); HBox.setHgrow(infoBox, Priority.ALWAYS);
        HBox titleRow = new HBox(10); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(name); nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111827;");
        Label roleBadge = new Label("Doctor"); roleBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 5;");
        titleRow.getChildren().addAll(nameLbl, roleBadge);
        Label subLbl = new Label(subtitle); subLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;"); infoBox.getChildren().addAll(titleRow, subLbl);

        Button editBtn = new Button("Edit Record");
        editBtn.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");
        Button profileBtn = new Button("Full Profile");
        profileBtn.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");

        VBox btnBox = new VBox(8, editBtn, profileBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(iconBox, infoBox, btnBox); return card;
    }

    private HBox createPatientCard(String name, String subtitle) {
        HBox card = new HBox(20); card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20 25; -fx-effect: dropshadow(three-pass-box, rgba(38,70,61,0.08), 15, 0, 5, 5); -fx-alignment: center-left;");
        VBox iconBox = new VBox(); iconBox.setStyle("-fx-background-color: #F0F9FF; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");
        Label icon = new Label("👤"); icon.setStyle("-fx-font-size: 30px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #1E3A8A;"); iconBox.getChildren().add(icon);
        VBox infoBox = new VBox(3); HBox.setHgrow(infoBox, Priority.ALWAYS);
        HBox titleRow = new HBox(10); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(name); nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111827;");
        Label roleBadge = new Label("Patient"); roleBadge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 5;");
        titleRow.getChildren().addAll(nameLbl, roleBadge);
        Label subLbl = new Label(subtitle); subLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;"); infoBox.getChildren().addAll(titleRow, subLbl);

        Button editBtn = new Button("Edit Record");
        editBtn.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");
        Button profileBtn = new Button("Full Profile");
        profileBtn.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");

        VBox btnBox = new VBox(8, editBtn, profileBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(iconBox, infoBox, btnBox); return card;
    }

    private void openFullProfile(String natId, String role) {
        currentProfileNatId = natId;
        currentProfileRole = role;

        profileIdLbl.setText("ID: " + natId);
        profileInfoRow.getChildren().clear();
        profileDiagnosesContainer.getChildren().clear();
        profileMedicationsContainer.getChildren().clear();

        if(testReportDetailView != null) {
            testReportDetailView.setVisible(false);
            testReportDetailView.setManaged(false);
        }
        if(testReportsListWrapper != null) {
            testReportsListWrapper.setVisible(true);
            testReportsListWrapper.setManaged(true);
        }
        if(profileTestsContainer != null) {
            profileTestsContainer.getChildren().clear();
        }

        try (Connection conn = DBConnection.getConnection()) {
            if ("PATIENT".equals(role)) {
                profileRoleBadge.setText("Patient");
                profileRoleBadge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 5;");

                profileIconLbl.setText("👤");
                profileIconLbl.setStyle("-fx-font-size: 40px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #1E3A8A;");
                if (profileIconBox != null) {
                    profileIconBox.setStyle("-fx-background-color: #F0F9FF; -fx-background-radius: 50; -fx-min-width: 80; -fx-min-height: 80; -fx-alignment: center;");
                }

                profileMedicalGrid.setVisible(true);
                profileMedicalGrid.setManaged(true);

                String q = "SELECT u.full_name, u.email, p.phone, p.blood_group, p.weight, p.height, p.date_of_birth, p.id AS pid " +
                        "FROM Users u JOIN Patients p ON u.id = p.user_id WHERE u.system_id = ?";
                try (PreparedStatement st = conn.prepareStatement(q)) {
                    st.setString(1, natId);
                    ResultSet rs = st.executeQuery();
                    if (rs.next()) {
                        profileNameLbl.setText(rs.getString("full_name"));
                        profileContactLbl.setText("📞 " + (rs.getString("phone") != null ? rs.getString("phone") : "N/A"));

                        profileInfoRow.getChildren().addAll(
                                createProfileInfoBlock("💧", "Blood Group", rs.getString("blood_group")),
                                createProfileInfoBlock("⚖️", "Weight", rs.getDouble("weight") + " kg"),
                                createProfileInfoBlock("📏", "Height", rs.getDouble("height") + " cm"),
                                createProfileInfoBlock("📅", "Date of Birth", rs.getString("date_of_birth"))
                        );

                        int patientDbId = rs.getInt("pid");
                        loadMedicalHistoryForProfile(conn, patientDbId);
                    }
                }
            } else {
                profileRoleBadge.setText("Doctor");
                profileRoleBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 5;");

                profileIconLbl.setText("👨‍⚕️");
                profileIconLbl.setStyle("-fx-font-size: 40px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: #26463D;");
                if (profileIconBox != null) {
                    profileIconBox.setStyle("-fx-background-color: #E8F3EE; -fx-background-radius: 50; -fx-min-width: 80; -fx-min-height: 80; -fx-alignment: center;");
                }

                profileMedicalGrid.setVisible(false);
                profileMedicalGrid.setManaged(false);

                // Fetch years_of_experience to display in full profile details
                String q = "SELECT u.full_name, u.email, u.created_at, d.specialization, d.license_number, d.degrees, d.years_of_experience " +
                        "FROM Users u JOIN Doctors d ON u.id = d.user_id WHERE u.system_id = ?";
                try (PreparedStatement st = conn.prepareStatement(q)) {
                    st.setString(1, natId);
                    ResultSet rs = st.executeQuery();
                    if (rs.next()) {
                        profileNameLbl.setText(formatDoctorName(rs.getString("full_name")));
                        profileContactLbl.setText("✉️ " + rs.getString("email"));

                        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                        String onboardStr = rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime().format(dateFmt) : "N/A";

                        profileInfoRow.getChildren().addAll(
                                createProfileInfoBlock("⚕️", "Specialty", rs.getString("specialization")),
                                createProfileInfoBlock("🎓", "Degrees", rs.getString("degrees")),
                                createProfileInfoBlock("🔖", "License", rs.getString("license_number")),
                                createProfileInfoBlock("⏳", "Experience", rs.getInt("years_of_experience") + " Yrs") // Added Experience to Profile View
                        );
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        switchView(viewFullProfile, null);
    }

    private void loadMedicalHistoryForProfile(Connection conn, int patientId) throws Exception {
        String diagQuery = "SELECT diagnosis, notes FROM Medical_History WHERE patient_id = ? ORDER BY diagnosis_date DESC";
        try (PreparedStatement st = conn.prepareStatement(diagQuery)) {
            st.setInt(1, patientId);
            ResultSet rs = st.executeQuery();
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                profileDiagnosesContainer.getChildren().add(createMedicalHistoryCard(rs.getString("diagnosis"), rs.getString("notes")));
            }
            if(!hasData) {
                Label l = new Label("No prior diagnostic history found."); l.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                profileDiagnosesContainer.getChildren().add(l);
            }
        }

        String medQuery = "SELECT pi.medicine_name, pi.dosage, pi.frequency FROM Prescription_Items pi JOIN Prescriptions p ON pi.prescription_id = p.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC LIMIT 10";
        try (PreparedStatement st = conn.prepareStatement(medQuery)) {
            st.setInt(1, patientId);
            ResultSet rs = st.executeQuery();
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                profileMedicationsContainer.getChildren().add(createMedicationCard(rs.getString("medicine_name"), "Dosage: " + rs.getString("dosage") + " - " + rs.getString("frequency")));
            }
            if(!hasData) {
                Label l = new Label("No active medications found."); l.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                profileMedicationsContainer.getChildren().add(l);
            }
        }

        String testQuery = "SELECT id, report_type, report_date, notes, file_url FROM Test_Reports WHERE patient_id = ? ORDER BY report_date DESC";
        try (PreparedStatement st = conn.prepareStatement(testQuery)) {
            st.setInt(1, patientId);
            ResultSet rs = st.executeQuery();
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                String dateStr = rs.getDate("report_date") != null ? rs.getDate("report_date").toString() : "";
                profileTestsContainer.getChildren().add(createTestReportCard(
                        rs.getInt("id"),
                        rs.getString("report_type"),
                        dateStr,
                        rs.getString("notes"),
                        rs.getString("file_url")
                ));
            }
            if(!hasData) {
                Label l = new Label("No test reports found for this patient."); l.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                profileTestsContainer.getChildren().add(l);
            }
        }
    }

    private VBox createProfileInfoBlock(String icon, String title, String value) {
        VBox box = new VBox(2);
        HBox top = new HBox(5, new Label(icon), new Label(title));
        top.getChildren().get(1).setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label valLbl = new Label(value != null && !value.isEmpty() ? value : "N/A");
        valLbl.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 16px;");
        box.getChildren().addAll(top, valLbl);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox createMedicalHistoryCard(String title, String notes) {
        VBox box = new VBox(5, new Label(title), new Label(notes));
        box.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 14px;");
        box.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
        return box;
    }

    private VBox createMedicationCard(String title, String details) {
        VBox box = new VBox(5, new Label(title), new Label(details));
        box.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 14px;");
        box.getChildren().get(1).setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
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
                        java.nio.file.Files.write(saveLocation.toPath(), fileBytes);
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

    @FXML private void handleEditFromProfile(ActionEvent event) {
        if ("PATIENT".equals(currentProfileRole)) openEditPatientForm(currentProfileNatId);
        else if ("DOCTOR".equals(currentProfileRole)) openEditDoctorForm(currentProfileNatId);
    }

    @FXML private void showDoctorsTab() {
        if(subViewDoctors != null) { subViewDoctors.setVisible(true); subViewDoctors.setManaged(true); }
        if(subViewPatients != null) { subViewPatients.setVisible(false); subViewPatients.setManaged(false); }
        if(btnTabDoctors != null) btnTabDoctors.setStyle(TAB_ACTIVE);
        if(btnTabPatients != null) btnTabPatients.setStyle(TAB_INACTIVE);
    }

    @FXML private void showPatientsTab() {
        if(subViewDoctors != null) { subViewDoctors.setVisible(false); subViewDoctors.setManaged(false); }
        if(subViewPatients != null) { subViewPatients.setVisible(true); subViewPatients.setManaged(true); }
        if(btnTabPatients != null) btnTabPatients.setStyle(TAB_ACTIVE);
        if(btnTabDoctors != null) btnTabDoctors.setStyle(TAB_INACTIVE);
    }

    @FXML private void showSearch(ActionEvent e) { switchView(viewSearch, btnSearch); }
    @FXML private void showNewPatient(ActionEvent e) { resetPatientForm(); switchView(viewNewPatient, btnNewPatient); }
    @FXML private void showNewDoctor(ActionEvent e) { resetDoctorForm(); switchView(viewNewDoctor, btnNewDoctor); }
    @FXML private void showAddTestReport(ActionEvent e) { switchView(viewAddTestReport, btnAddTestReport); }

    private void switchView(VBox view, Button btn) {
        if(viewSearch != null) { viewSearch.setVisible(false); viewSearch.setManaged(false); }
        if(viewNewPatient != null) { viewNewPatient.setVisible(false); viewNewPatient.setManaged(false); }
        if(viewNewDoctor != null) { viewNewDoctor.setVisible(false); viewNewDoctor.setManaged(false); }
        if(viewAddTestReport != null) { viewAddTestReport.setVisible(false); viewAddTestReport.setManaged(false); }
        if(viewFullProfile != null) { viewFullProfile.setVisible(false); viewFullProfile.setManaged(false); }

        if (view != null) {
            view.setVisible(true);
            view.setManaged(true);
        }

        if(btnSearch != null) btnSearch.setStyle(INACTIVE);
        if(btnNewPatient != null) btnNewPatient.setStyle(INACTIVE);
        if(btnNewDoctor != null) btnNewDoctor.setStyle(INACTIVE);
        if(btnAddTestReport != null) btnAddTestReport.setStyle(INACTIVE);
        if (btn != null) btn.setStyle(ACTIVE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}