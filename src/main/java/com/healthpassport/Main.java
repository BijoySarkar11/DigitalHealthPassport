package com.healthpassport;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));            // Sets the window title
            primaryStage.setTitle("Digital Health Passport");

            primaryStage.setScene(new Scene(root, 1000, 700));

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Could not load FXML file. Check if the file name is correct in /resources/fxml/");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}