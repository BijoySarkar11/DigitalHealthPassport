package com.healthpassport.ui.patient;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardUIFactory {

    public static HBox createReminderCard(String med, String dose, String doc) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 15 12;");
        VBox icon = new VBox(new Label("💊"));
        icon.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-alignment: center;");
        icon.getChildren().get(0).setStyle("-fx-font-family: 'Segoe UI Emoji';");

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

    public static HBox createAppointmentCard(String month, String day, String docName, String spec, String timeStr) {
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

    public static HBox createPrescriptionCard(String medName, String instructions, String duration, String docName) {
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

    public static HBox createTestReportCard(String testName, String details, String dateStr) {
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
}