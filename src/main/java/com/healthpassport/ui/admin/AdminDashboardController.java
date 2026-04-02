package com.healthpassport.ui.admin;

import com.healthpassport.MODEL.user.User;
import com.healthpassport.ui.common.BaseController;
import com.healthpassport.util.DBConnection;
import com.healthpassport.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController extends BaseController {

    @FXML private Button btnSearch, btnNewPatient, btnNewDoctor, btnAddTestReport;
    @FXML private VBox viewSearch, viewNewPatient, viewNewDoctor, viewAddTestReport, viewFullProfile;

    // Search View
    @FXML private Label adminNameLabel, hospitalNameLabel;
    @FXML private TextField dbSearchField;
    @FXML private Button btnTabDoctors, btnTabPatients;
    @FXML private VBox subViewDoctors, subViewPatients;
    @FXML private VBox doctorRecordsContainer, patientRecordsContainer;

    // Full Profile Viewer Elements
    @FXML private Label profileIconLbl, profileNameLbl, profileRoleBadge, profileIdLbl, profileContactLbl;
    @FXML private HBox profileInfoRow, profileMedicalGrid;
    @FXML private VBox profileDiagnosesContainer, profileMedicationsContainer, profileTestsContainer;

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

    // Test Report Form
    @FXML private TextField testPatientIdField;
    @FXML private Label testVerificationLabel;
    @FXML private ComboBox<String> testTypeCombo;
    @FXML private DatePicker testDatePicker;
    @FXML private TextArea testSummaryArea;

    private int adminHospitalId = -1;
    private int verifiedPatientDbId = -1;

    // State Trackers for Edit Mode
    private String editingPatientNatId = null;
    private String editingDoctorNatId = null;

    private final String ACTIVE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    private final String TAB_ACTIVE = "-fx-background-color: #26463D; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String TAB_INACTIVE = "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 20; -fx-font-family: 'Segoe UI Emoji', 'System';";

    private String cleanNameForDB(String rawName) {
        if (rawName == null) return "";
        return rawName.replaceFirst("^(?i)(\\s*(dr\\.?|doctor)\\s*)+", "").trim();
    }

    private String formatDoctorName(String rawName) {
        return "Dr. " + cleanNameForDB(rawName);
    }

    @FXML
    public void initialize() {
        showSearch(null);
        loadAdminProfile();
        loadRecords("");
        showDoctorsTab();

        if (patGenderCombo != null) patGenderCombo.setItems(FXCollections.observableArrayList("MALE", "FEMALE", "OTHER"));
        if (patBloodCombo != null) patBloodCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
        if (testTypeCombo != null) testTypeCombo.setItems(FXCollections.observableArrayList("Complete Blood Count (CBC)", "Chest X-Ray", "MRI Scan", "CT Scan", "Urinalysis", "Lipid Profile", "ECG", "Other"));
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

    private String generateNextPatientId() throws Exception {
        String query = "SELECT national_id FROM Users WHERE role = 'PATIENT' AND national_id LIKE 'PT-%' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement st = conn.prepareStatement(query)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("national_id");
                int nextNum = Integer.parseInt(lastId.replaceAll("[^0-9]", "")) + 1;
                return String.format("PT-%07d", nextNum);
            }
        }
        return "PT-0025000";
    }

    private String generateNextDoctorId() throws Exception {
        String query = "SELECT national_id FROM Users WHERE role = 'DOCTOR' AND national_id LIKE 'DOC-%' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement st = conn.prepareStatement(query)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("national_id");
                int nextNum = Integer.parseInt(lastId.replaceAll("[^0-9]", "")) + 1;
                return String.format("DOC-%03d", nextNum);
            }
        }
        return "DOC-001";
    }

    private void resetPatientForm() {
        editingPatientNatId = null;
        if (lblPatientFormTitle != null) lblPatientFormTitle.setText("Register New Patient");
        if (btnSubmitPatient != null) btnSubmitPatient.setText("Confirm & Register Patient");
        if (patStatusLabel != null) patStatusLabel.setText("");
        if (patIdLabel != null) patIdLabel.setText("Assigned System ID (Auto-Generated)");

        try {
            if (patIdField != null) patIdField.setText(generateNextPatientId());
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
            if (docIdField != null) docIdField.setText(generateNextDoctorId());
        } catch (Exception e) {
            if (docIdField != null) docIdField.setText("Error generating ID");
        }

        docPasswordField.setPromptText("Assign Password");
        docNameField.clear(); docSpecialtyField.clear(); docLicenseField.clear(); docDegreesField.clear(); docPasswordField.clear();
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
            patStatusLabel.setText("❌ Weight and Height must be valid numbers (e.g., 70.5).");
            patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        String fullName = patFullNameField.getText().trim();
        String pass = patPasswordField.getText().trim();
        java.sql.Date dobDate = patDobPicker.getValue() != null ? java.sql.Date.valueOf(patDobPicker.getValue()) : java.sql.Date.valueOf("2000-01-01");
        String gender = patGenderCombo.getValue();
        String blood = patBloodCombo.getValue() != null ? patBloodCombo.getValue() : "";
        String phone = patPhoneField.getText() != null ? patPhoneField.getText().trim() : "";
        String intakeDiag = patInitialDiagnosisField.getText() != null ? patInitialDiagnosisField.getText().trim() : "";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (editingPatientNatId != null) {
                String updateUser = "UPDATE Users SET full_name = ? WHERE national_id = ?";
                try (PreparedStatement st = conn.prepareStatement(updateUser)) {
                    st.setString(1, fullName); st.setString(2, editingPatientNatId); st.executeUpdate();
                }

                if (!pass.isEmpty()) {
                    String updatePass = "UPDATE Users SET password_hash = ? WHERE national_id = ?";
                    try (PreparedStatement st = conn.prepareStatement(updatePass)) {
                        st.setString(1, pass); st.setString(2, editingPatientNatId); st.executeUpdate();
                    }
                }

                String updatePatient = "UPDATE Patients SET full_name = ?, date_of_birth = ?, gender = ?, blood_group = ?, phone = ?, weight = ?, height = ? WHERE national_id = ?";
                try (PreparedStatement st = conn.prepareStatement(updatePatient)) {
                    st.setString(1, fullName); st.setDate(2, dobDate); st.setString(3, gender); st.setString(4, blood);
                    st.setString(5, phone); st.setDouble(6, weight); st.setDouble(7, height);
                    st.setString(8, editingPatientNatId);
                    st.executeUpdate();
                }

                int patIdForDiag = -1;
                try (PreparedStatement st = conn.prepareStatement("SELECT id FROM Patients WHERE national_id = ?")) {
                    st.setString(1, editingPatientNatId);
                    ResultSet r = st.executeQuery();
                    if(r.next()) patIdForDiag = r.getInt("id");
                }

                if (patIdForDiag != -1) {
                    boolean diagExists = false;
                    try (PreparedStatement st = conn.prepareStatement("SELECT id FROM Medical_History WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                        st.setInt(1, patIdForDiag);
                        ResultSet r = st.executeQuery();
                        diagExists = r.next();
                    }

                    if (diagExists && intakeDiag.isEmpty()) {
                        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Medical_History WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                            st.setInt(1, patIdForDiag); st.executeUpdate();
                        }
                    } else if (diagExists && !intakeDiag.isEmpty()) {
                        try (PreparedStatement st = conn.prepareStatement("UPDATE Medical_History SET diagnosis = ? WHERE patient_id = ? AND notes = 'Initial Registration Intake'")) {
                            st.setString(1, intakeDiag); st.setInt(2, patIdForDiag); st.executeUpdate();
                        }
                    } else if (!diagExists && !intakeDiag.isEmpty()) {
                        int defaultDoctorId = 1;
                        try (PreparedStatement docStmt = conn.prepareStatement("SELECT id FROM Doctors WHERE hospital_id = ? LIMIT 1")) {
                            docStmt.setInt(1, adminHospitalId); ResultSet drRs = docStmt.executeQuery();
                            if (drRs.next()) defaultDoctorId = drRs.getInt("id");
                        }
                        try (PreparedStatement diagStmt = conn.prepareStatement("INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date, notes) VALUES (?, ?, ?, ?, CURDATE(), 'Initial Registration Intake')")) {
                            diagStmt.setInt(1, patIdForDiag); diagStmt.setInt(2, defaultDoctorId); diagStmt.setInt(3, adminHospitalId); diagStmt.setString(4, intakeDiag);
                            diagStmt.executeUpdate();
                        }
                    }
                }

                conn.commit();
                resetPatientForm();
                patStatusLabel.setText("✅ Patient Record Updated Successfully!");
                patStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords(dbSearchField.getText());

            } else {
                String natId = generateNextPatientId();
                String email = natId + "@dhp.com";
                int newUserId = -1, newPatientId = -1;

                String insertUserQuery = "INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, 'PATIENT', ?)";
                try (PreparedStatement stmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                    stmtUser.setString(1, natId); stmtUser.setString(2, fullName); stmtUser.setString(3, email); stmtUser.setString(4, pass);
                    if (adminHospitalId > 0) stmtUser.setInt(5, adminHospitalId); else stmtUser.setNull(5, Types.INTEGER);
                    stmtUser.executeUpdate();
                    ResultSet rs = stmtUser.getGeneratedKeys();
                    if (rs.next()) newUserId = rs.getInt(1);
                }

                if (newUserId != -1) {
                    String insertPatientQuery = "INSERT INTO Patients (user_id, national_id, full_name, date_of_birth, gender, blood_group, phone, weight, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmtPat = conn.prepareStatement(insertPatientQuery, Statement.RETURN_GENERATED_KEYS)) {
                        stmtPat.setInt(1, newUserId); stmtPat.setString(2, natId); stmtPat.setString(3, fullName); stmtPat.setDate(4, dobDate);
                        stmtPat.setString(5, gender); stmtPat.setString(6, blood); stmtPat.setString(7, phone); stmtPat.setDouble(8, weight); stmtPat.setDouble(9, height);
                        stmtPat.executeUpdate();
                        ResultSet rsPat = stmtPat.getGeneratedKeys();
                        if (rsPat.next()) newPatientId = rsPat.getInt(1);
                    }
                }

                if (newPatientId != -1 && !intakeDiag.isEmpty() && adminHospitalId > 0) {
                    int defaultDoctorId = 1;
                    try (PreparedStatement docStmt = conn.prepareStatement("SELECT id FROM Doctors WHERE hospital_id = ? LIMIT 1")) {
                        docStmt.setInt(1, adminHospitalId); ResultSet drRs = docStmt.executeQuery();
                        if (drRs.next()) defaultDoctorId = drRs.getInt("id");
                    }
                    try (PreparedStatement diagStmt = conn.prepareStatement("INSERT INTO Medical_History (patient_id, diagnosed_by, hospital_id, diagnosis, diagnosis_date, notes) VALUES (?, ?, ?, ?, CURDATE(), 'Initial Registration Intake')")) {
                        diagStmt.setInt(1, newPatientId); diagStmt.setInt(2, defaultDoctorId); diagStmt.setInt(3, adminHospitalId); diagStmt.setString(4, intakeDiag);
                        diagStmt.executeUpdate();
                    }
                }

                conn.commit();
                resetPatientForm();
                patStatusLabel.setText("✅ Registration Successful! ID: " + natId);
                patStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            patStatusLabel.setText("❌ Database Error: Failed to save patient record.");
            patStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
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

        String fullName = cleanNameForDB(docNameField.getText().trim());
        String pass = docPasswordField.getText().trim();
        String spec = docSpecialtyField.getText().trim();
        String lic = docLicenseField.getText().trim();
        String deg = docDegreesField.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (editingDoctorNatId != null) {
                String updateUser = "UPDATE Users SET full_name = ? WHERE national_id = ?";
                try (PreparedStatement st = conn.prepareStatement(updateUser)) {
                    st.setString(1, fullName); st.setString(2, editingDoctorNatId);
                    st.executeUpdate();
                }

                if (!pass.isEmpty()) {
                    String updatePass = "UPDATE Users SET password_hash = ? WHERE national_id = ?";
                    try (PreparedStatement st = conn.prepareStatement(updatePass)) {
                        st.setString(1, pass); st.setString(2, editingDoctorNatId);
                        st.executeUpdate();
                    }
                }

                String updateDoctor = "UPDATE Doctors SET specialization = ?, license_number = ?, degrees = ? WHERE user_id = (SELECT id FROM Users WHERE national_id = ?)";
                try (PreparedStatement st = conn.prepareStatement(updateDoctor)) {
                    st.setString(1, spec); st.setString(2, lic); st.setString(3, deg); st.setString(4, editingDoctorNatId);
                    st.executeUpdate();
                }

                conn.commit();
                resetDoctorForm();
                docStatusLabel.setText("✅ Doctor Record Updated Successfully!");
                docStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords(dbSearchField.getText());

            } else {
                String natId = generateNextDoctorId();
                String email = natId + "@dhp.com";
                int newUserId = -1;

                String insertUserQuery = "INSERT INTO Users (national_id, full_name, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, 'DOCTOR', ?)";
                try (PreparedStatement stmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                    stmtUser.setString(1, natId); stmtUser.setString(2, fullName); stmtUser.setString(3, email); stmtUser.setString(4, pass); stmtUser.setInt(5, adminHospitalId);
                    stmtUser.executeUpdate();
                    ResultSet rs = stmtUser.getGeneratedKeys();
                    if (rs.next()) newUserId = rs.getInt(1);
                }

                if (newUserId != -1) {
                    String insertDoctorQuery = "INSERT INTO Doctors (user_id, hospital_id, specialization, license_number, degrees, years_of_experience) VALUES (?, ?, ?, ?, ?, 0)";
                    try (PreparedStatement stmtDoc = conn.prepareStatement(insertDoctorQuery)) {
                        stmtDoc.setInt(1, newUserId); stmtDoc.setInt(2, adminHospitalId); stmtDoc.setString(3, spec); stmtDoc.setString(4, lic); stmtDoc.setString(5, deg);
                        stmtDoc.executeUpdate();
                    }
                }

                conn.commit();
                resetDoctorForm();
                docStatusLabel.setText("✅ Registration Successful! ID: " + natId);
                docStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                loadRecords("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            docStatusLabel.setText("❌ Database Error: Failed to save doctor record.");
            docStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }
    }

    private void openEditPatientForm(String natId) {
        String query = "SELECT u.full_name, p.date_of_birth, p.gender, p.blood_group, p.phone, p.weight, p.height " +
                "FROM Users u JOIN Patients p ON u.id = p.user_id WHERE u.national_id = ?";
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
                String diagQuery = "SELECT diagnosis FROM Medical_History WHERE patient_id = (SELECT id FROM Patients WHERE national_id = ?) AND notes = 'Initial Registration Intake' LIMIT 1";
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
        String query = "SELECT u.full_name, d.specialization, d.license_number, d.degrees " +
                "FROM Users u JOIN Doctors d ON u.id = d.user_id WHERE u.national_id = ?";
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

        String query = "SELECT id, full_name FROM Patients WHERE national_id = ?";
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
    private void handleSubmitTestReport(ActionEvent event) {
        if (verifiedPatientDbId == -1) {
            showAlert(Alert.AlertType.ERROR, "Verification Required", "Please enter and verify a valid Patient ID first."); return;
        }
        if (testTypeCombo.getValue() == null || testDatePicker.getValue() == null || testSummaryArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Incomplete Form", "Please select a test type, date, and provide a summary."); return;
        }

        String insertQuery = "INSERT INTO Test_Reports (patient_id, added_by_admin_id, hospital_id, report_type, file_url, report_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setInt(1, verifiedPatientDbId);
            stmt.setInt(2, UserSession.getInstance().getCurrentUser().getId());
            stmt.setInt(3, adminHospitalId);
            stmt.setString(4, testTypeCombo.getValue());
            stmt.setString(5, "/docs/uploaded_report.pdf");
            stmt.setDate(6, java.sql.Date.valueOf(testDatePicker.getValue()));
            stmt.setString(7, testSummaryArea.getText().trim());

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Test Report securely added to Patient's Digital Passport!");

            testPatientIdField.clear(); testVerificationLabel.setText("Please verify a Patient ID before uploading."); testVerificationLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 12px; -fx-font-weight: bold;"); verifiedPatientDbId = -1; testTypeCombo.getSelectionModel().clearSelection(); testDatePicker.setValue(null); testSummaryArea.clear();
        } catch (Exception e) { e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Database Error", "Could not submit report."); }
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

        String query = "SELECT u.national_id, u.full_name, d.specialization, d.license_number, d.degrees " +
                "FROM Doctors d JOIN Users u ON d.user_id = u.id " +
                "WHERE d.hospital_id = ? AND (u.full_name LIKE ? OR u.national_id LIKE ?) " +
                "ORDER BY u.full_name ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setInt(1, adminHospitalId); stmt.setString(2, searchPattern); stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String natId = rs.getString("national_id");
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

        String query = "SELECT national_id, full_name, created_at " +
                "FROM Patients " +
                "WHERE full_name LIKE ? OR national_id LIKE ? " +
                "ORDER BY created_at DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern); stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String natId = rs.getString("national_id");
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

        Button editBtn = new Button("✏️ Edit Record");
        editBtn.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");
        Button profileBtn = new Button("📄 Full Profile");
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

        Button editBtn = new Button("✏️ Edit Record");
        editBtn.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #26463D; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");
        Button profileBtn = new Button("📄 Full Profile");
        profileBtn.setStyle("-fx-background-color: #115E59; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 130;");

        VBox btnBox = new VBox(8, editBtn, profileBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(iconBox, infoBox, btnBox); return card;
    }

    // ==========================================
    // 4. FULL PROFILE VIEWER
    // ==========================================
    private void openFullProfile(String natId, String role) {
        currentProfileNatId = natId;
        currentProfileRole = role;

        profileIdLbl.setText("ID: " + natId);
        profileInfoRow.getChildren().clear();
        profileDiagnosesContainer.getChildren().clear();
        profileMedicationsContainer.getChildren().clear();
        profileTestsContainer.getChildren().clear();

        try (Connection conn = DBConnection.getConnection()) {
            if ("PATIENT".equals(role)) {
                profileRoleBadge.setText("Patient");
                profileRoleBadge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 5;");
                profileIconLbl.setText("👤");
                profileMedicalGrid.setVisible(true);
                profileMedicalGrid.setManaged(true);

                String q = "SELECT u.full_name, u.email, p.phone, p.blood_group, p.weight, p.height, p.date_of_birth, p.id AS pid " +
                        "FROM Users u JOIN Patients p ON u.id = p.user_id WHERE u.national_id = ?";
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
                profileMedicalGrid.setVisible(false);
                profileMedicalGrid.setManaged(false);

                String q = "SELECT u.full_name, u.email, u.created_at, d.specialization, d.license_number, d.degrees " +
                        "FROM Users u JOIN Doctors d ON u.id = d.user_id WHERE u.national_id = ?";
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
                                createProfileInfoBlock("📅", "Onboarded", onboardStr)
                        );
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        switchView(viewFullProfile, null);
    }

    private void loadMedicalHistoryForProfile(Connection conn, int patientId) throws Exception {
        // Diagnoses
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

        // Medications
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

        // Tests
        String testQuery = "SELECT report_type, report_date, notes FROM Test_Reports WHERE patient_id = ? ORDER BY report_date DESC";
        try (PreparedStatement st = conn.prepareStatement(testQuery)) {
            st.setInt(1, patientId);
            ResultSet rs = st.executeQuery();
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                String dateStr = rs.getDate("report_date") != null ? rs.getDate("report_date").toString() : "";
                profileTestsContainer.getChildren().add(createTestReportCard(rs.getString("report_type"), dateStr, rs.getString("notes")));
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

    private VBox createTestReportCard(String title, String date, String notes) {
        VBox box = new VBox(5);
        HBox top = new HBox(10, new Label(title), new Label(date));
        top.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 14px;");
        top.getChildren().get(1).setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        HBox.setHgrow(top.getChildren().get(0), Priority.ALWAYS);
        Label notesLbl = new Label("Notes: " + (notes != null ? notes : "None"));
        notesLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        notesLbl.setWrapText(true);
        box.getChildren().addAll(top, notesLbl);
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
        return box;
    }

    @FXML private void handleEditFromProfile(ActionEvent event) {
        if ("PATIENT".equals(currentProfileRole)) openEditPatientForm(currentProfileNatId);
        else if ("DOCTOR".equals(currentProfileRole)) openEditDoctorForm(currentProfileNatId);
    }

    // --- TAB TOGGLES ---
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

    // --- VIEW SWITCHING ---
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/fxml/RoleSelection.fxml", "Digital Health Passport - Role Selection");
    }
}