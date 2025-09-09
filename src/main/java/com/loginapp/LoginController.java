package com.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

public class LoginController {
    @FXML private VBox loginBox;
    @FXML private VBox signupBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField signupUsernameField;
    @FXML private PasswordField signupPasswordField;
    @FXML private Button topRightSignupButton;

    @FXML
    private void handleLogin() {
        if (UserDAO.login(usernameField.getText(), passwordField.getText())) {
            try {
                Main.setRoot("DiaryMain.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Failed to load Diary Main view.");
            }
        } else {
            showAlert("Invalid credentials");
        }
    }

    @FXML
    private void handleSignup() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        signupBox.setVisible(true);
        signupBox.setManaged(true);
        topRightSignupButton.setVisible(false);
        topRightSignupButton.setManaged(false);
    }

    @FXML
    private void handleSignupSubmit() {
        String username = signupUsernameField.getText().trim();
        String password = signupPasswordField.getText().trim();
        
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
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    alert.close();
                    showLoginForm();
                });
            }).start();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Sign up failed (maybe username exists)");
            alert.showAndWait();
        }
    }

    @FXML
    private void showLoginForm() {
        signupBox.setVisible(false);
        signupBox.setManaged(false);
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        if (topRightSignupButton != null) {
            topRightSignupButton.setVisible(true);
            topRightSignupButton.setManaged(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Overloaded method for backward compatibility
    private void showAlert(String message) {
        showAlert("Information", message);
    }
}
