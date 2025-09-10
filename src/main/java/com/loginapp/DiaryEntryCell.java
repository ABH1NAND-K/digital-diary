package com.loginapp;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.ListChangeListener;

public class DiaryEntryCell extends ListCell<Database.DiaryEntry> {
    private final CheckBox checkBox = new CheckBox();
    private final Label titleLabel = new Label();
    private final Label dateLabel = new Label();
    private final HBox content = new HBox(10);
    private final VBox textContainer = new VBox(4);
    private final ContextMenu contextMenu = new ContextMenu();
    private DiaryMainController controller;
    private final ListView<Database.DiaryEntry> listView;
    
    // Default constructor for FXML compatibility
    public DiaryEntryCell() {
        this(null, null);
    }
    
    // Constructor with both parameters (preferred)
    public DiaryEntryCell(ListView<Database.DiaryEntry> listView, DiaryMainController controller) {
        super();
        this.listView = listView;
        this.controller = controller;
        
        // Setup context menu
        MenuItem openItem = new MenuItem("Open");
        MenuItem selectItem = new MenuItem("Select");
        MenuItem deleteItem = new MenuItem("Delete");
        
        openItem.setOnAction(e -> {
            if (getItem() != null && controller != null) {
                controller.openEntry(getItem());
            }
        });
        
        selectItem.setOnAction(e -> {
            if (getItem() != null && controller != null) {
                controller.selectEntry(getItem());
            }
        });
        
        deleteItem.setOnAction(e -> {
            if (getItem() != null) {
                controller.deleteEntry(getItem());
            }
        });
        
        contextMenu.getItems().addAll(openItem, selectItem, new SeparatorMenuItem(), deleteItem);
        
        // Setup checkbox
        checkBox.setFocusTraversable(false);
        checkBox.setVisible(false); // Hidden by default, shown in selection mode
        checkBox.setOnAction(e -> {
            if (getItem() != null) {
                controller.toggleEntrySelection(getItem());
            }
        });
        
        // Setup labels
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #E4E4E7;");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA;");
        
        // Setup layout
        textContainer.getChildren().addAll(titleLabel, dateLabel);
        textContainer.setPadding(new Insets(8, 16, 8, 16));
        textContainer.getStyleClass().add("diary-entry-cell");
        
        content.getChildren().addAll(checkBox, textContainer);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-padding: 4 0 4 0;");
        
        // Handle right-click for context menu
        setOnContextMenuRequested(e -> {
            if (getItem() != null) {
                contextMenu.show(this, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });
        
        // Close context menu on scroll
        setOnScroll(e -> contextMenu.hide());
        
        // Set up initial style
        updateStyle(false);
    }
    
    @Override
    protected void updateItem(Database.DiaryEntry item, boolean empty) {
        super.updateItem(item, empty);
        
        // Try to get controller from listView if not set
        if (controller == null && listView != null && listView.getScene() != null && listView.getScene().getWindow() != null) {
            controller = (DiaryMainController) listView.getScene().getWindow().getUserData();
        }
        
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            // Set the entry title
            String title = item.getTitle() != null ? item.getTitle() : "No Title";
            titleLabel.setText(title);
            
            // Format the date and time
            String dateText = "No date";
            if (item.getCreatedAt() != null) {
                dateText = item.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"));
            }
            dateLabel.setText(dateText);
            
            // Show/hide checkbox based on selection mode
            if (controller != null) {
                checkBox.setSelected(controller.isEntrySelected(item));
                checkBox.setVisible(controller.isInSelectionMode());
            } else {
                checkBox.setVisible(false);
            }
            
            setGraphic(content);
            
            // Make sure the cell takes full width and has proper alignment
            setMaxWidth(Double.MAX_VALUE);
            content.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);
            
            // Set the cell content
            setGraphic(content);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            
            // Set the style based on hover state
            setOnMouseEntered(e -> updateStyle(true));
            setOnMouseExited(e -> updateStyle(false));
            
            // Handle click to select in selection mode
            setOnMouseClicked(e -> {
                Database.DiaryEntry currentItem = getItem();
                if (controller != null && currentItem != null) {
                    if (controller.isInSelectionMode() && e.getClickCount() == 1) {
                        // In selection mode, toggle selection on click
                        controller.toggleEntrySelection(currentItem);
                        e.consume();
                    } else if (e.getClickCount() == 2) {
                        // Double-click to open entry
                        controller.openEntry(currentItem);
                        e.consume();
                    }
                }
            });
            
            // Force a layout pass
            content.requestLayout();
        }
    }
    
    private void handleDoubleClick() {
        Database.DiaryEntry item = getItem();
        if (item != null) {
            System.out.println("Opening diary entry (cell double-click): " + item.getTitle());
            if (controller != null) {
                controller.openEntry(item);
            }
        }
    }
    
    private void updateStyle(boolean hover) {
        String style = "-fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 8;" +
                     (hover ? " -fx-background-color: #2A2A3A; -fx-border-color: #4F4F4F; -fx-cursor: hand;" 
                            : " -fx-background-color: #1E1E2E; -fx-border-color: #3F3F3F;");
        textContainer.setStyle(style);
    }
}
