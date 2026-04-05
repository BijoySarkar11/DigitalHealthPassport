package com.healthpassport.MODEL.service;

import com.healthpassport.MODEL.dao.PatientDAO;
import com.healthpassport.MODEL.user.Patient;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PatientService {
    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
    }

    /**
     * Business Logic: Calculates the next sequential ID for a new Patient.
     */
    public String generateSystemId() {
        String query = "SELECT system_id FROM Users WHERE role = 'PATIENT' AND system_id LIKE 'PT-%' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("system_id");
                int nextNum = Integer.parseInt(lastId.replaceAll("[^0-9]", "")) + 1;
                return String.format("PT-%07d", nextNum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "PT-0025000"; // Starting baseline
    }

    /**
     * Validates and registers a new patient.
     */
    public boolean registerNewPatient(Patient patient) {
        // Business Rule: Ensure critical fields exist
        if (patient.getFullName() == null || patient.getSystemId() == null || patient.getPasswordHash() == null) {
            return false;
        }
        return patientDAO.create(patient);
    }

    /**
     * Updates an existing patient.
     */
    public boolean updatePatientProfile(Patient patient) {
        if (patient.getSystemId() == null) return false;
        return patientDAO.update(patient);
    }
}