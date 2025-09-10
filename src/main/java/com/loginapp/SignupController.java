package com.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

public class SignupController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password cannot be empty");
            return;
        }
        
        if (username.length() < 3) {
            showAlert("Error", "Username must be at least 3 characters long");
            return;
        }
        
        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long");
            return;
        }
        
        if (UserDAO.signup(username, password)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Sign up successful!");
            alert.show();
            // After 1.5 seconds, close alert and redirect to login
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    alert.close();
                    goToLogin();
                });
            }).start();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Sign up failed (maybe username exists)");
            alert.showAndWait();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loginapp/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

