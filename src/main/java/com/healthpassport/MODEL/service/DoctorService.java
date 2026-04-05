package com.healthpassport.MODEL.service;

import com.healthpassport.MODEL.dao.DoctorDAO;
import com.healthpassport.MODEL.user.Doctor;
import com.healthpassport.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DoctorService {
    private final DoctorDAO doctorDAO;

    public DoctorService() {
        this.doctorDAO = new DoctorDAO();
    }

    /**
     * Business Logic: Calculates the next sequential ID for a new Doctor.
     */
    public String generateSystemId() {
        String query = "SELECT system_id FROM Users WHERE role = 'DOCTOR' AND system_id LIKE 'DOC-%' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("system_id");
                int nextNum = Integer.parseInt(lastId.replaceAll("[^0-9]", "")) + 1;
                return String.format("DOC-%03d", nextNum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DOC-001"; // Starting baseline
    }

    /**
     * Validates and registers a new doctor.
     */
    public boolean registerNewDoctor(Doctor doctor) {
        if (doctor.getFullName() == null || doctor.getSpecialization() == null || doctor.getLicenseNumber() == null) {
            return false;
        }
        return doctorDAO.create(doctor);
    }

    /**
     * Updates an existing doctor.
     */
    public boolean updateDoctorProfile(Doctor doctor) {
        if (doctor.getSystemId() == null) return false;
        return doctorDAO.update(doctor);
    }
}