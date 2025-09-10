package com.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DiaryEntryController {
    @FXML private Button backButton;
    @FXML private ToggleButton writeToggle;
    @FXML private ToggleButton viewToggle;
    @FXML private Button okButton;
    @FXML private TextField titleField;
    @FXML private Label dateLabel;
    @FXML private TextArea contentArea;

    @FXML
    public void initialize() {
        System.out.println("DiaryEntryController initialized. writeMode=" + DiaryContext.writeMode);
        ToggleGroup group = new ToggleGroup();
        writeToggle.setToggleGroup(group);
        viewToggle.setToggleGroup(group);
        if (DiaryContext.writeMode) {
            writeToggle.setSelected(true);
        } else {
            viewToggle.setSelected(true);
        }
        updateFieldsFromContext();
        setEditMode(DiaryContext.writeMode);

        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean editable = (newToggle == writeToggle);
            setEditMode(editable);
        });

        backButton.setOnAction(e -> onBackClicked());
        okButton.setOnAction(e -> onOkClicked());
    }

    private void updateFieldsFromContext() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
            
        if (DiaryContext.currentEntry != null) {
            titleField.setText(DiaryContext.currentEntry.getTitle());
            dateLabel.setText(formatter.format(DiaryContext.currentEntry.getCreatedAt()));
            contentArea.setText(DiaryContext.currentEntry.getContent());
        } else {
            titleField.setText("");
            dateLabel.setText(formatter.format(LocalDateTime.now()));
            contentArea.setText("");
        }
    }

    private void setEditMode(boolean editable) {
        System.out.println("Setting edit mode to: " + editable);
        titleField.setEditable(editable);
        contentArea.setEditable(editable);
        okButton.setDisable(!editable);
        
        // Update the toggle buttons to reflect the current mode
        if (editable) {
            writeToggle.setSelected(true);
        } else {
            viewToggle.setSelected(true);
        }
    }

    private void onBackClicked() {
        try {
            Main.setRoot("DiaryMain.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onOkClicked() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        LocalDateTime timestamp = LocalDateTime.now();
        
        if (title.isEmpty()) {
            showAlert("Error", "Title cannot be empty");
            return;
        }
        
        try {
            if (DiaryContext.currentEntry == null) {
                // Create new entry with current timestamp
                Database.addDiaryEntry(title, content, timestamp);
            } else {
                // Update existing entry with current timestamp
                Database.updateDiaryEntry(
                    DiaryContext.currentEntry.getId(),
                    title,
                    content,
                    timestamp
                );
            }
            onBackClicked();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save diary entry: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // TODO: Add methods for handling toggle, back, and ok actions
}
