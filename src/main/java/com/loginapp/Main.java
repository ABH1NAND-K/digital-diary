package com.loginapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize the database
        Database.init();
        
        primaryStage = stage;
        
        // Set minimum window size
        stage.setMinWidth(1000);
        stage.setMinHeight(750);
        
        // Set initial window size
        stage.setWidth(1000);
        stage.setHeight(750);
        
        // Center the window on the screen
        stage.centerOnScreen();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Login App");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws Exception {
        // Save current window dimensions and position
        boolean wasMaximized = primaryStage.isMaximized();
        
        // Load the new scene
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        // Set minimum size for the new scene
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(750);
        
        // If not maximized, set explicit size
        if (!wasMaximized) {
            primaryStage.setWidth(1000);
            primaryStage.setHeight(750);
            primaryStage.centerOnScreen();
        }
        
        primaryStage.setScene(scene);
        
        // Restore maximized state if needed
        if (wasMaximized) {
            primaryStage.setMaximized(true);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        // Initialize database and run migrations
        Database.init();
        
        // Run password migration (safe to run multiple times)
        new Thread(PasswordMigration::migratePasswords).start();
        
        // Launch the application
        launch(args);
    }
}
