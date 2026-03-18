package com.healthpassport;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    // USER-DEFINED EXCEPTION
    public static class ApplicationStartupException extends Exception {
        public ApplicationStartupException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    private void handleStartupError(ApplicationStartupException e) {

        System.err.println("[CRITICAL STARTUP ERROR] " + e.getMessage());

        if (e.getCause() != null) {
            System.err.println("Technical Details: " + e.getCause().toString());
        }

        System.err.println("Action Required: Check if 'RoleSelection.fxml' exists in the '/resources/fxml/' directory and is compiled correctly.");


        System.exit(1);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));

            //window title
            primaryStage.setTitle("Digital Health Passport");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.show();

        } catch (IOException | NullPointerException e) {

            handleStartupError(new ApplicationStartupException("Failed to load the primary application window.", e));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}