package com.healthpassport.ui.doctor;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

import java.io.IOException;

public class DoctorDashboardController {

    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnAppointments;
    @FXML private Button btnLogout;

    @FXML private VBox viewDashboard;
    @FXML private VBox viewPatients;
    @FXML private VBox viewAppointments;

    @FXML private AreaChart<String, Number> trendChart;

    // Matches the Deep Green (#26463D) floating sidebar theme
    private final String ACTIVE_STYLE = "-fx-background-color: #1B362F; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #A3CFC0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji', 'System';";

    @FXML
    public void initialize() {
        setupCharts();
        showDashboard(null);
    }

    private void setupCharts() {
        if (trendChart != null) {
            trendChart.setBackground(Background.EMPTY);

            XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
            trendSeries.getData().add(new XYChart.Data<>("Mon", 120));
            trendSeries.getData().add(new XYChart.Data<>("Tue", 145));
            trendSeries.getData().add(new XYChart.Data<>("Wed", 130));
            trendSeries.getData().add(new XYChart.Data<>("Thu", 180));
            trendSeries.getData().add(new XYChart.Data<>("Fri", 150));
            trendSeries.getData().add(new XYChart.Data<>("Sat", 170));
            trendSeries.getData().add(new XYChart.Data<>("Sun", 140));

            trendChart.getData().add(trendSeries);

            // Turn the line chart into the Sage Green theme natively
            Platform.runLater(() -> {
                Node line = trendChart.lookup(".chart-series-line");
                Node fill = trendChart.lookup(".chart-series-area-fill");

                if (line instanceof Path) {
                    ((Path) line).setStroke(Color.web("#5C8D7D"));
                    ((Path) line).setStrokeWidth(3);
                }

                if (fill instanceof Path) {
                    ((Path) fill).setFill(Color.web("#5C8D7D").deriveColor(0, 1, 1, 0.2));
                }
            });
        }
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        hideAllViews();
        if (viewDashboard != null) {
            viewDashboard.setVisible(true);
            viewDashboard.setManaged(true);
        }
        if (btnDashboard != null) btnDashboard.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showPatients(ActionEvent event) {
        hideAllViews();
        if (viewPatients != null) {
            viewPatients.setVisible(true);
            viewPatients.setManaged(true);
        }
        if (btnPatients != null) btnPatients.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showAppointments(ActionEvent event) {
        hideAllViews();
        if (viewAppointments != null) {
            viewAppointments.setVisible(true);
            viewAppointments.setManaged(true);
        }
        if (btnAppointments != null) btnAppointments.setStyle(ACTIVE_STYLE);
    }

    private void hideAllViews() {
        if (viewDashboard != null) { viewDashboard.setVisible(false); viewDashboard.setManaged(false); }
        if (viewPatients != null) { viewPatients.setVisible(false); viewPatients.setManaged(false); }
        if (viewAppointments != null) { viewAppointments.setVisible(false); viewAppointments.setManaged(false); }
        resetButtons();
    }

    private void resetButtons() {
        if (btnDashboard != null) btnDashboard.setStyle(INACTIVE_STYLE);
        if (btnPatients != null) btnPatients.setStyle(INACTIVE_STYLE);
        if (btnAppointments != null) btnAppointments.setStyle(INACTIVE_STYLE);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleSelection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Digital Health Passport - Role Selection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}