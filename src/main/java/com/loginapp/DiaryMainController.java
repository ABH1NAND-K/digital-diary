package com.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.application.Platform;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.ContentDisplay;
import java.time.format.DateTimeFormatter;
import javafx.collections.ListChangeListener;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.net.URL;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.beans.binding.Bindings;

public class DiaryMainController implements Initializable {
    
    // UI Components
    @FXML private TextField searchField;
    @FXML private DatePicker datePicker;
    @FXML private ToggleButton dateToggle;
    @FXML private ToggleButton nameToggle;
    @FXML private ListView<Database.DiaryEntry> entryList;
    @FXML private Button deleteButton;
    @FXML private Button fabButton;
    @FXML private ToolBar selectionToolbar;
    @FXML private Label selectionCountLabel;  // For the selected items count
    
    // State
    private boolean selectionMode = false;
    private final ObservableList<Database.DiaryEntry> selectedEntries = FXCollections.observableArrayList();
    private final ObservableList<Database.DiaryEntry> visibleEntries = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int loadedCount = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the database
            Database.init();
            
            // Set up UI components
            setupListView();
            setupSearchBar();
            setupToggleListeners();
            
            // Set up FAB button
            if (fabButton != null) {
                fabButton.setOnAction(e -> handleNewEntry());
            }
            
            // Set up selection toolbar
            if (selectionToolbar != null) {
                selectionToolbar.setVisible(false);
                
                // Bind the selection count label
                if (selectionCountLabel != null) {
                    selectionCountLabel.textProperty().bind(
                        Bindings.createStringBinding(
                            () -> selectedEntries.size() + " selected",
                            selectedEntries
                        )
                    );
                }
            }
            
            // Set controller as user data for the window
            if (searchField != null && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                searchField.getScene().getWindow().setUserData(this);
            }
            
            // Load initial entries
            System.out.println("Loading initial entries...");
            filterAndShowEntries();
            System.out.println("Loaded " + visibleEntries.size() + " entries");
            
            // Set up entry selection
            setupEntrySelection();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", "Failed to initialize the application: " + e.getMessage());
        }
    }
    // Selection mode methods
    private void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (selectionToolbar != null) {
            selectionToolbar.setVisible(enabled);
        }
        
        // Refresh the list view to show/hide checkboxes
        if (entryList != null) {
            entryList.refresh();
        }
    }
    
    @FXML
    private void cancelSelection() {
        selectedEntries.clear();
        setSelectionMode(false);
    }
    
    @FXML
    private void deleteSelected() {
        if (selectedEntries.isEmpty()) {
            return;
        }
        
        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Selected Entries");
        alert.setContentText(String.format("Are you sure you want to delete %d selected entries?", selectedEntries.size()));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (Database.DiaryEntry entry : new ArrayList<>(selectedEntries)) {
                    Database.deleteDiaryEntry(entry.getId());
                    visibleEntries.remove(entry);
                }
                selectedEntries.clear();
                setSelectionMode(false);
            }
        });
    }
    
    // Context menu actions
    public void openEntry(Database.DiaryEntry entry) {
        if (entry != null) {
            try {
                // Open the entry in a new view
                openEntryView(entry);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Failed to open entry: " + e.getMessage());
            }
        }
    }
    
    public void selectEntry(Database.DiaryEntry entry) {
        if (!selectionMode) {
            setSelectionMode(true);
        }
        toggleEntrySelection(entry);
    }
    
    public void deleteEntry(Database.DiaryEntry entry) {
        if (entry != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Entry");
            alert.setContentText("Are you sure you want to delete this entry?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Database.deleteDiaryEntry(entry.getId());
                    visibleEntries.remove(entry);
                    selectedEntries.remove(entry);
                }
            });
        }
    }
    
    public boolean isInSelectionMode() {
        return selectionMode;
    }
    
    public boolean isEntrySelected(Database.DiaryEntry entry) {
        return selectedEntries.contains(entry);
    }
    
    public void toggleEntrySelection(Database.DiaryEntry entry) {
        if (entry != null) {
            if (selectedEntries.contains(entry)) {
                selectedEntries.remove(entry);
            } else {
                selectedEntries.add(entry);
            }
            
            // Update UI
            if (entryList != null) {
                entryList.refresh();
            }
            
            // If no more selected items, exit selection mode
            if (selectedEntries.isEmpty()) {
                setSelectionMode(false);
            }
        }
    }
    
    @FXML
    public void handleDeleteSelected() {
        deleteSelected();
    }
    
    @FXML
    public void handleNewEntry() {
        System.out.println("+ button clicked (handleNewEntry)");
        openEntryView(null);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    

    @FXML
    private void handleLogout() {
        try {
            // Clear any user session data if needed
            
            // Load the login view with .fxml extension
            Main.setRoot("Login.fxml");
            // Clear the search field when logging out
            searchField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Toggle between date and name search modes
    private void setupToggleListeners() {
        ToggleGroup searchToggleGroup = new ToggleGroup();
        dateToggle.setToggleGroup(searchToggleGroup);
        nameToggle.setToggleGroup(searchToggleGroup);
        
        // Default to name search
        nameToggle.setSelected(true);
        setupNameSearch();
        
        // Add listeners to toggle buttons
        dateToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setupDateFormatter();
            }
        });
        
        nameToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setupNameSearch();
            }
        });
    }
    
    // Set up date formatter for date picker
    private void setupDateFormatter() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            datePicker.setOnAction(e -> filterAndShowEntries());
            
            // Show date picker and hide search field
            if (searchField != null) {
                searchField.setVisible(false);
                searchField.setManaged(false);
            }
            datePicker.setVisible(true);
            datePicker.setManaged(true);
        }
    }
    
    // Set up name search functionality
    private void setupNameSearch() {
        if (searchField != null) {
            searchField.clear();
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterAndShowEntries();
            });
            
            // Show search field and hide date picker
            searchField.setVisible(true);
            searchField.setManaged(true);
            if (datePicker != null) {
                datePicker.setVisible(false);
                datePicker.setManaged(false);
            }
        }
    }
    
    private void setupEntrySelection() {
        if (entryList != null) {
            // Enable multiple selection
            entryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            
            // Update delete button state based on selection
            entryList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Database.DiaryEntry>) c -> {
                if (deleteButton != null) {
                    deleteButton.setDisable(entryList.getSelectionModel().getSelectedItems().isEmpty());
                }
            });
        }
    }
    
    private void setupListView() {
        System.out.println("=== Setting up list view ===");
        
        // Make sure entryList is not null
        if (entryList == null) {
            System.err.println("Error: entryList is null!");
            return;
        }
        
        // Clear any existing items
        visibleEntries.clear();
        
        // Set up cell factory with our custom cell
        entryList.setCellFactory(lv -> {
            return new DiaryEntryCell(entryList, this);
        });
        
        // Bind the visible entries to the list view
        entryList.setItems(visibleEntries);
        
        // Set up placeholder for empty list
        Label placeholder = new Label("No diary entries found. Click the + button to create one!");
        placeholder.setStyle("-fx-text-fill: #A1A1AA; -fx-font-style: italic; -fx-padding: 16px; -fx-font-size: 14px;");
        entryList.setPlaceholder(placeholder);
        
        // Set up lazy loading on scroll
        entryList.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0) {
                loadMoreEntries();
            }
        });
        
        // The items are already set above, no need to set them again
        
        // Add a listener to the visibleEntries list to log changes
        visibleEntries.addListener((ListChangeListener<Database.DiaryEntry>) c -> {
            System.out.println("Visible entries changed. New size: " + visibleEntries.size());
            if (!visibleEntries.isEmpty()) {
                System.out.println("First entry title: " + visibleEntries.get(0).getTitle());
            }
        });
        
        // Set up entry selection
        setupEntrySelection();
        
        // Load initial entries
        loadMoreEntries();
        
        System.out.println("=== List view setup complete ===");
    }
    
    private void setupSearchBar() {
        // Set up toggle group
        ToggleGroup group = new ToggleGroup();
        dateToggle.setToggleGroup(group);
        nameToggle.setToggleGroup(group);
        
        // Set initial state - default to date search
        dateToggle.setSelected(true);
        
        // Show only date picker initially
        if (datePicker != null) {
            datePicker.setVisible(true);
            datePicker.setManaged(true);
            // Set today's date as default
            datePicker.setValue(java.time.LocalDate.now());
        }
        
        // Hide search field initially
        searchField.setVisible(false);
        searchField.setManaged(false);
        
        // Setup date formatter
        setupDateFormatter();
        
        // Force the date picker to show immediately
        if (datePicker != null) {
            datePicker.requestLayout();
            datePicker.applyCss();
        }
        
        // Single listener for toggle changes
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == dateToggle) {
                searchField.setPromptText("YYYY-MM-DD");
                setupDateFormatter();
            } else {
                searchField.setPromptText("Search by name");
                setupNameSearch();
            }
            searchField.clear();
            filterAndShowEntries();
        });
        
        // Add text change listener for search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Filter entries based on search text
            filterAndShowEntries();
        });
    }

    private void openEntryView(Database.DiaryEntry entry) {
        System.out.println("openEntryView called. entry=" + (entry == null ? "null (new entry)" : entry.getTitle()));
        if (entry != null) {
            System.out.println("Entry details - ID: " + entry.getId() + ", Title: " + entry.getTitle());
        }
        
        // Set context for DiaryEntryController
        DiaryContext.currentEntry = entry;
        // If entry is not null, it's view mode (read-only). If entry is null, it's a new entry (write mode)
        DiaryContext.writeMode = (entry == null);
        System.out.println("Setting writeMode to: " + DiaryContext.writeMode);
        
        try {
            // Load the entry view
            System.out.println("Loading DiaryEntry.fxml...");
            Main.setRoot("DiaryEntry.fxml");
        } catch (Exception e) {
            System.err.println("Error loading DiaryEntry.fxml:");
            e.printStackTrace();
        }
    }

    private void filterAndShowEntries() {
        System.out.println("\n=== filterAndShowEntries() called ===");
        final String searchText = searchField.getText().trim().toLowerCase();
        final LocalDate selectedDate = datePicker.getValue();
        final boolean searchByDate = dateToggle.isSelected();
        
        // Determine the final search text to use
        final String finalSearchText;
        if (searchByDate) {
            if (selectedDate == null) {
                // If in date mode but no date selected, show all entries
                entryList.setItems(visibleEntries);
                return;
            }
            // Convert LocalDate to string in YYYY-MM-DD format for comparison
            finalSearchText = selectedDate.toString();
        } else {
            finalSearchText = searchText;
        }
        
        System.out.println("Search text: '" + searchText + "', Search by date: " + searchByDate);
        
        try {
            // Get current user from session
            final String currentUser = Database.getCurrentUser();
            System.out.println("Current user from session: " + (currentUser != null ? currentUser : "null"));
            
            if (currentUser == null || currentUser.isEmpty()) {
                System.err.println("ERROR: No user is currently logged in!");
                final String errorMessage = "You must be logged in to view diary entries.";
                Platform.runLater(() -> showError("Not Logged In", errorMessage));
                return;
            }
            
            final int currentUserId = Database.getCurrentUserId();
            System.out.println("Current user ID: " + currentUserId);
            
            if (currentUserId == -1) {
                System.err.println("ERROR: Failed to get current user ID!");
                return;
            }
            
            // Get entries from database
            System.out.println("\n[DEBUG] Fetching diary entries from database...");
            final List<Database.DiaryEntry> allEntries = Database.getDiaryEntries(PAGE_SIZE, 0);
            System.out.println("[DEBUG] Retrieved " + allEntries.size() + " entries from database");
            
            // Print all entries for debugging
            if (!allEntries.isEmpty()) {
                System.out.println("\n[DEBUG] All entries from database:");
                for (int i = 0; i < allEntries.size(); i++) {
                    final Database.DiaryEntry entry = allEntries.get(i);
                    System.out.println(String.format("  [%d] ID: %d, Title: '%s', Created: %s, UserID: %d", 
                        i, entry.getId(), entry.getTitle(), 
                        entry.getCreatedAt(), entry.getUserId()));
                }
            }
            
            // Filter entries based on search criteria
            System.out.println("\n[DEBUG] Filtering entries...");
            List<Database.DiaryEntry> filteredEntries = allEntries.stream()
                .filter(entry -> {
                    if (entry == null) {
                        System.out.println("[WARNING] Found null entry in the list!");
                        return false;
                    }
                    
                    // If search text is empty, include all entries
                    if (finalSearchText.isEmpty()) {
                        System.out.println("[DEBUG] No search text, including entry: " + entry.getTitle());
                        return true;
                    }
                    
                    boolean matches;
                    if (searchByDate) {
                        // Format the date to match the search format (YYYY-MM-DD)
                        final String formattedDate = entry.getCreatedAt().toLocalDate().toString();
                        matches = formattedDate.contains(finalSearchText);
                        System.out.println(String.format("[DEBUG] Date search - Entry: %s, Date: %s, Formatted: %s, Search: '%s', Match: %b", 
                            entry.getTitle(), entry.getCreatedAt(), formattedDate, finalSearchText, matches));
                    } else {
                        final String title = entry.getTitle().toLowerCase();
                        matches = title.contains(finalSearchText);
                        System.out.println(String.format("[DEBUG] Title search - Entry: %s, Title: %s, Search: '%s', Match: %b", 
                            entry.getTitle(), title, finalSearchText, matches));
                    }
                    
                    return matches;
                })
                .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt())) // Newest first
                .collect(Collectors.toList());
            
            System.out.println("\n[DEBUG] Found " + filteredEntries.size() + " entries after filtering");
            final List<Database.DiaryEntry> finalFilteredEntries = filteredEntries;
            
            // Update the visible entries on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    System.out.println("\n[DEBUG] Updating visible entries in UI...");
                    System.out.println("[DEBUG] Current visible entries before update: " + visibleEntries.size());
                    
                    // Clear and add all to trigger list change events
                    visibleEntries.setAll(filteredEntries);
                    
                    // Force refresh of the ListView
                    entryList.refresh();
                    
                    loadedCount = filteredEntries.size();
                    
                    // Debug: Print the first entry if available
                    if (!visibleEntries.isEmpty()) {
                        Database.DiaryEntry first = visibleEntries.get(0);
                        System.out.println("[DEBUG] First visible entry - Title: " + first.getTitle() + 
                                       ", Date: " + first.getCreatedAt() + 
                                       ", ID: " + first.getId());
                    }
                    System.out.println("[DEBUG] Visible entries after update: " + visibleEntries.size());
                    
                    // Force refresh of the ListView
                    System.out.println("[DEBUG] Refreshing ListView...");
                    entryList.refresh();
                    
                    if (!filteredEntries.isEmpty()) {
                        System.out.println("[DEBUG] First entry title: " + filteredEntries.get(0).getTitle());
                        System.out.println("[DEBUG] First entry in visibleEntries: " + 
                            (visibleEntries.isEmpty() ? "empty" : visibleEntries.get(0).getTitle()));
                    } else {
                        System.out.println("[DEBUG] No entries to display after filtering");
                    }
                    
                    // Debug: Print ListView properties
                    System.out.println("\n[DEBUG] ListView properties:");
                    System.out.println("  Items count: " + entryList.getItems().size());
                    System.out.println("  Visible row count: " + entryList.getItems().size());
                    System.out.println("  Fixed cell size: " + entryList.getFixedCellSize());
                    System.out.println("  Layout bounds: " + entryList.getLayoutBounds());
                    
                } catch (Exception e) {
                    System.err.println("[ERROR] Updating UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("\n[ERROR] in filterAndShowEntries: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            Platform.runLater(() -> {
                showError("Error Loading Entries", "An error occurred while loading diary entries: " + e.getMessage());
            });
        }
    }

    private void loadMoreEntries() {
        // Load more entries when scrolling (pagination)
        final String searchText = searchField.getText().trim().toLowerCase();
        final boolean searchByDate = dateToggle.isSelected();
        
        // Determine the final search text to use
        final String finalSearchText;
        if (searchByDate && datePicker.getValue() != null) {
            finalSearchText = datePicker.getValue().toString();
        } else {
            finalSearchText = searchText;
        }
        
        // Get next page of entries
        List<Database.DiaryEntry> newEntries = Database.getDiaryEntries(PAGE_SIZE, loadedCount);
        
        // Filter if searching
        if (!finalSearchText.isEmpty()) {
            if (searchByDate) {
                // Filter by date
                newEntries = newEntries.stream()
                    .filter(entry -> entry.getCreatedAt().toLocalDate().toString().equals(finalSearchText))
                    .collect(Collectors.toList());
            } else {
                // Filter by title
                newEntries = newEntries.stream()
                    .filter(entry -> entry.getTitle().toLowerCase().contains(finalSearchText))
                    .collect(Collectors.toList());
            }
        }
        
        // Add to visible entries if not empty
        if (!newEntries.isEmpty()) {
            visibleEntries.addAll(newEntries);
            loadedCount += newEntries.size();
        }
    }

    // TODO: Add methods for search, lazy loading, and entry selection
}
