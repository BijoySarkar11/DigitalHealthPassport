
package com.healthpassport.ui.common;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

// ABSTRACTION: This class handles the complex screen-switching logic behind the scenes.
public abstract class BaseController {

    // ENCAPSULATION: 'protected' means only classes that inherit from BaseController can use this.
    // POLYMORPHISM: Accepting the generic 'Event' allows this method to process both ActionEvent and MouseEvent.
    protected void navigateTo(Event event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Extracts the stage from whatever generic event triggered the method
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            System.err.println("OOP Navigation Error: Failed to load " + fxmlPath);
            e.printStackTrace();
        }
    }
}

