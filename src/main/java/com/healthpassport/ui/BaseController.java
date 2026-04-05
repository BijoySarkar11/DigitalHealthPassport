package com.healthpassport.ui;

import javafx.event.Event; // Using the generic Event class
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class BaseController {

    /**
     * Shared method to switch between screens securely.
     * Accepts any JavaFX Event (ActionEvent, MouseEvent, etc.)
     * Automatically preserves the current window dimensions.
     */
    protected void navigateTo(Event event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            if (title != null) stage.setTitle(title);

            // OOP Upgrade: Preserve exact window dimensions automatically!
            stage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load page: " + fxmlPath);
        }
    }

    /**
     * Shared method to show universal popup messages.
     */
    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}