import gdk.GameModule;
import gdk.Logging;
import launcher.ProfessionalJsonEditor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Controller for the GDK Game Lobby interface.
 * 
 * This class manages the main lobby interface where users can select and launch
 * game modules. It provides functionality for game selection, JSON configuration
 * validation, and game launching with proper error handling.
 * 
 * Key responsibilities:
 * - Manage the game selection interface
 * - Handle JSON configuration input and validation
 * - Coordinate game launching with the ViewModel
 * - Provide user feedback through messages and error dialogs
 * - Manage the refresh of available game modules
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Game Selection Components
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private Button launchGameButton;
    @FXML private Button refreshButton;
    
    // Message and Logging Components
    @FXML private ScrollPane messageScrollPane;
    @FXML private VBox messageContainer;
    
    // JSON Configuration Components
    @FXML private ProfessionalJsonEditor jsonDataTextArea;
    @FXML private Button clearJsonButton;
    @FXML private Button metadataRequestButton;
    @FXML private Button sendMessageButton;
    @FXML private JFXToggleButton jsonPersistenceToggle;
    
    // Application Control Components
    @FXML private Button exitButton;
    @FXML private Label statusLabel;

    // ==================== DEPENDENCIES ====================
    
    /**
     * The ViewModel that manages application state and business logic
     */
    private GDKViewModel applicationViewModel;
    
    /**
     * List of available game modules discovered by the module loader
     */
    private ObservableList<GameModule> availableGameModules;
    
    /**
     * The currently selected game module for launching
     */
    private GameModule selectedGameModule;
    
    /**
     * Track the previous module count to detect removals
     */
    private int previousModuleCount = 0;
    
    /**
     * Track removed module names to avoid recompilation messages
     */
    private Set<String> removedModuleNames = new HashSet<>();
    
    /**
     * Message queue system to prevent spam
     */
    private Queue<String> messageQueue = new LinkedList<>();
    private boolean messageTimerRunning = false;
    private static final long MESSAGE_INTERVAL_MS = 250;
    
    /**
     * JSON mapper for parsing and validating JSON configuration data
     */
    private ObjectMapper jsonDataMapper;
    
    // ==================== JSON PERSISTENCE ====================
    
    /**
     * File path for saving JSON content
     */
    private static final String JSON_PERSISTENCE_FILE = "gdk-json-persistence.txt";
    
    /**
     * File path for saving persistence toggle state
     */
    private static final String PERSISTENCE_TOGGLE_FILE = "gdk-persistence-toggle.txt";

    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the controller when FXML is loaded.
     * 
     * This method is called automatically by JavaFX when the FXML
     * file is loaded. It sets up all components and event handlers.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        // Initialize dependencies
        jsonDataMapper = new ObjectMapper();
        availableGameModules = FXCollections.observableArrayList();
        
        // Set up the UI components
        setupUserInterface();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Load saved JSON content and toggle state
        loadPersistenceSettings();
        
        // Refresh the game list on startup
        refreshAvailableGameModules();
        
        // Initialize the status label
        updateGameCountStatus();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }

    // ==================== DEPENDENCY INJECTION ====================
    
    /**
     * Set the ViewModel reference for this controller.
     * 
     * @param applicationViewModel The ViewModel to use for business logic
     */
    public void setViewModel(GDKViewModel applicationViewModel) {
        this.applicationViewModel = applicationViewModel;
    }

    // ==================== USER INTERFACE SETUP ====================
    
    /**
     * Set up all user interface components.
     * 
     * This method configures the visual appearance and behavior
     * of all UI components in the lobby interface.
     */
    private void setupUserInterface() {
        // Game Selector Setup
        
        // Configure the ComboBox to display game module names from metadata
        // This creates a custom cell factory that shows the game name of each game module
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule gameModule, boolean empty) {
                super.updateItem(gameModule, empty);
                if (empty || gameModule == null) {
                    // Show placeholder text when no game is selected
                    setText("Select a game...");
                } else {
                    // Display the game name from the GameModule interface
                    setText(gameModule.getGameName());
                }
            }
        });
        
        // Apply the same cell factory to the button cell (the part that shows the selected item)
        // This ensures consistent display between the dropdown list and the selected item
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        
        // Message Area Setup
        
        // Apply consistent styling to the message container for better readability
        // Uses a modern font stack with fallbacks and appropriate size
        messageContainer.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Display a welcome message to inform users they're in the right place
        addUserMessage("🎮 Welcome to GDK Game Picker!");
        
        // Log successful UI initialization for debugging purposes
        Logging.info("🎨 UI components initialized");
    }

    // ==================== EVENT HANDLER SETUP ====================
    
    /**
     * Set up all event handlers for user interactions.
     * 
     * This method configures event handlers for all interactive
     * components in the lobby interface.
     */
    private void setupEventHandlers() {
        // Game Selection Handler
        // When user selects a game from the dropdown, update the selected game and show feedback
        gameSelector.setOnAction(event -> {
            selectedGameModule = gameSelector.getValue(); // Store the selected game module
            String selectedGameName = selectedGameModule != null ? 
                selectedGameModule.getGameName() : "None"; // Get game name from interface
            addUserMessage("🎮 Selected game: " + selectedGameName); // Show user feedback
        });
        
        // Button Event Handlers
        // Launch button: Start the selected game with validation
        launchGameButton.setOnAction(event -> launchSelectedGame());
        
        // Refresh button: Reload the list of available games
        refreshButton.setOnAction(event -> {
            // Clear the message container immediately
            messageContainer.getChildren().clear();
            
            // Add a small delay to show the user that reload was triggered
            Platform.runLater(() -> {
                try {
                    Thread.sleep(250); // 250ms delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                addUserMessage("🔄 Reload in progress..."); // Show progress indicator
                
                // Run the refresh in a background thread to keep UI responsive
                new Thread(() -> {
                    refreshAvailableGameModules(); // Reload games from modules directory
                }).start();
            });
        });
        

        
        // Exit button: Close the entire application
        exitButton.setOnAction(event -> {
            Logging.info("🔒 GDK Game Lobby closing"); // Log the action
            onApplicationShutdown(); // Save settings before exit
            Platform.exit(); // Terminate the JavaFX application
        });
        
        // JSON Configuration Handlers
        // Clear button: Remove all JSON input
        clearJsonButton.setOnAction(event -> clearJsonConfigurationData());
        
        // Metadata request button: Fill JSON with metadata request
        metadataRequestButton.setOnAction(event -> fillMetadataRequest());
        
        // Send message button: Send a test message
        sendMessageButton.setOnAction(event -> sendMessage());
        
        // Persistence Toggle Handler
        // Save toggle state when changed and clear save file if disabled
        jsonPersistenceToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEnabled = newValue;
            savePersistenceToggleState();
            
            if (!isEnabled) {
                // Clear the save file when persistence is disabled
                clearJsonPersistenceFile();
                addUserMessage("📋 JSON persistence disabled");
            } else {
                addUserMessage("📋 JSON persistence enabled");
            }
        });
        
        // JSON Text Area Change Handler
        // Save JSON content when modified (with debouncing)
        jsonDataTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            // Use Platform.runLater to debounce rapid changes
            Platform.runLater(() -> {
                if (jsonPersistenceToggle.isSelected()) {
                    saveJsonContent();
                }
            });
        });
        
        Logging.info("🎯 Event handlers configured");
    }

    // ==================== GAME MODULE MANAGEMENT ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the UI accordingly with proper error handling.
     */
    private void refreshAvailableGameModules() {
        try {
            // Store previous module names for removal detection
            Set<String> previousModuleNames = new HashSet<>();
            if (previousModuleCount > 0) {
                for (GameModule module : availableGameModules) {
                    previousModuleNames.add(module.getGameName());
                }
            }
            
            // Clear existing game modules to start fresh
            availableGameModules.clear();
            
            // Clear the currently selected game module since we're refreshing
            selectedGameModule = null;
            
            // Get the path to the modules directory from application constants
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            
            // Detect disabled/removed modules before recompilation
            if (previousModuleCount == 0) {
                // First load - detect pre-disabled modules
                detectDisabledModulesBeforeRecompilation(modulesDirectoryPath);
            } else {
                // Subsequent loads - detect newly removed modules
                detectNewlyRemovedModules(previousModuleNames);
            }
            
            // Check and recompile only modules that need it (source code changed)
            checkAndRecompileModules(modulesDirectoryPath);
            
            // Use the ModuleLoader to discover all available game modules
            List<GameModule> discoveredGameModules = ModuleLoader.discoverModules(modulesDirectoryPath);
            
            // Add each discovered module to our observable list
            for (GameModule gameModule : discoveredGameModules) {
                availableGameModules.add(gameModule); // Add to observable list for UI binding
                String gameName = gameModule.getGameName();
                String className = gameModule.getClass().getSimpleName();
                Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
            }
            
            // Update the ComboBox with the new list of games
            Logging.info("🔄 Updating ComboBox with " + availableGameModules.size() + " modules");
            gameSelector.setItems(availableGameModules);
            
            // Clear the ComboBox selection since we refreshed
            gameSelector.getSelectionModel().clearSelection();
            
            // Force UI refresh on the JavaFX Application Thread
            Platform.runLater(() -> {
                // Update the status label with the new count
                updateGameCountStatus();
                
                // Force ComboBox to refresh its display
                gameSelector.requestLayout();
            });
            
            // Check for module changes and provide user feedback
            int currentModuleCount = availableGameModules.size();
            
            if (availableGameModules.isEmpty()) {
                addUserMessage("⚠️ No game modules found in " + modulesDirectoryPath);
            } else if (previousModuleCount > 0) {
                // Check for module changes (additions/removals)
                Set<String> currentModuleNames = new HashSet<>();
                for (GameModule module : availableGameModules) {
                    currentModuleNames.add(module.getGameName());
                }
                
                // Find removed module names
                Set<String> removedNames = new HashSet<>(previousModuleNames);
                removedNames.removeAll(currentModuleNames);
                
                // Find added module names
                Set<String> addedNames = new HashSet<>(currentModuleNames);
                addedNames.removeAll(previousModuleNames);
                
                // Add removed names to tracking set
                removedModuleNames.addAll(removedNames);
                
                // Show specific removal messages
                for (String removedName : removedNames) {
                    addUserMessage("⚠️ Game module '" + removedName + "' was removed or disabled");
                }
                
                // Show specific addition messages
                for (String addedName : addedNames) {
                    addUserMessage("✅ New game module '" + addedName + "' was added");
                }
                
                addUserMessage("✅ Reload completed - " + currentModuleCount + " modules available");
            } else if (previousModuleCount == 0) {
                // First time loading - check for disabled modules that exist but aren't loaded
                checkForDisabledModulesOnFirstLoad(modulesDirectoryPath, currentModuleCount);
            } else {
                addUserMessage("✅ Reload completed - " + currentModuleCount + " modules available");
            }
            
            // Update the previous count for next comparison
            previousModuleCount = currentModuleCount;
            
        } catch (Exception moduleDiscoveryError) {
            // Handle any errors during module discovery
            Logging.error("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage(), moduleDiscoveryError);
            addUserMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
        }
        }
    
    /**
     * Detect disabled modules before recompilation on first load
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void detectDisabledModulesBeforeRecompilation(String modulesDirectoryPath) {
        try {
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        // Check if Main.java exists and is valid
                        File mainJavaFile = new File(subdir, "src/main/java/Main.java");
                        if (!mainJavaFile.exists()) {
                            // Check for Main.java in any subdirectory of src/main/java/ (legacy support)
                            File srcDir = new File(subdir, "src/main/java");
                            if (srcDir.exists()) {
                                File[] subdirs2 = srcDir.listFiles(File::isDirectory);
                                if (subdirs2 != null) {
                                    for (File subdir2 : subdirs2) {
                                        File mainInSubdir = new File(subdir2, "Main.java");
                                        if (mainInSubdir.exists()) {
                                            mainJavaFile = mainInSubdir;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        // If no Main.java found or it's commented out, mark as disabled
                        if (!mainJavaFile.exists() || isFileCommentedOut(mainJavaFile)) {
                            removedModuleNames.add(subdir.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error detecting disabled modules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a file is effectively commented out (all content is comments or empty)
     * @param file The file to check
     * @return true if the file is commented out
     */
    private boolean isFileCommentedOut(File file) {
        try {
            if (!file.exists()) return true;
            
            String content = Files.readString(file.toPath()).trim();
            if (content.isEmpty()) return true;
            
            // Check if all non-empty lines are comments
            String[] lines = content.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                    return false; // Found non-comment content
                }
            }
            return true; // All lines are comments or empty
        } catch (Exception e) {
            return true; // If we can't read it, assume it's disabled
        }
    }
    
    /**
     * Detect newly removed modules and add them to the removed tracking set
     * @param previousModuleNames Set of previously loaded module names
     */
    private void detectNewlyRemovedModules(Set<String> previousModuleNames) {
        try {
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    // Check if this module was previously loaded but isn't now
                    if (previousModuleNames.contains(subdir.getName())) {
                        // Check if the module directory still exists but module isn't loaded
                        File pomFile = new File(subdir, "pom.xml");
                        if (pomFile.exists()) {
                            // Check if Main.java exists and is valid
                            File mainJavaFile = new File(subdir, "src/main/java/Main.java");
                            if (!mainJavaFile.exists()) {
                                // Check for Main.java in any subdirectory of src/main/java/ (legacy support)
                                File srcDir = new File(subdir, "src/main/java");
                                if (srcDir.exists()) {
                                    File[] subdirs2 = srcDir.listFiles(File::isDirectory);
                                    if (subdirs2 != null) {
                                        for (File subdir2 : subdirs2) {
                                            File mainInSubdir = new File(subdir2, "Main.java");
                                            if (mainInSubdir.exists()) {
                                                mainJavaFile = mainInSubdir;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // If no Main.java found or it's commented out, mark as removed
                            if (!mainJavaFile.exists() || isFileCommentedOut(mainJavaFile)) {
                                removedModuleNames.add(subdir.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error detecting newly removed modules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check for disabled modules on first load and add them to removed tracking
     * @param modulesDirectoryPath Path to the modules directory
     * @param currentModuleCount Current number of loaded modules
     */
    private void checkForDisabledModulesOnFirstLoad(String modulesDirectoryPath, int currentModuleCount) {
        // Show completion message
        addUserMessage("✅ Loaded " + currentModuleCount + " game module(s)");
    }
    
    // ==================== MODULE COMPILATION ====================
    
    /**
     * Checks and recompiles only modules that need it (source code changed)
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void checkAndRecompileModules(String modulesDirectoryPath) {
        try {
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        // Skip recompilation messages for removed modules
                        if (!removedModuleNames.contains(subdir.getName())) {
                            checkAndRecompileModule(subdir);
                        }
                    }
                }
            }
        } catch (Exception e) {
            addUserMessage("❌ Error checking modules: " + e.getMessage());
            Logging.error("❌ Error checking modules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a module needs recompilation and recompiles if needed
     * @param moduleDir The module directory to check
     */
    private void checkAndRecompileModule(File moduleDir) {
        try {
            String moduleName = moduleDir.getName();
            
            // Check if Main.java exists in source (using same logic as ModuleLoader)
            
            // Check for Main.java directly in src/main/java/ (simplified structure)
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            if (!mainJavaFile.exists()) {
                // Check for Main.java in any subdirectory of src/main/java/ (legacy support)
                File srcDir = new File(moduleDir, "src/main/java");
                if (srcDir.exists()) {
                    File[] subdirs = srcDir.listFiles(File::isDirectory);
                    if (subdirs != null) {
                        for (File subdir : subdirs) {
                            File mainInSubdir = new File(subdir, "Main.java");
                            if (mainInSubdir.exists()) {
                                mainJavaFile = mainInSubdir;
                                break;
                            }
                        }
                    }
                }
            }
            
            // If no Main.java found, skip this module
            if (!mainJavaFile.exists()) {
                return;
            }
            
            // Check if the Main class implements GameModule interface
            // This is done during module loading, not during compilation check
            
            // Check if compiled classes exist
            File targetClassesDir = new File(moduleDir, "target/classes");
            File mainClassFile = new File(targetClassesDir, "Main.class");
            
            // Check if source is newer than compiled classes
            boolean needsRecompilation = !mainClassFile.exists() || 
                                       mainJavaFile.lastModified() > mainClassFile.lastModified();
            
            if (needsRecompilation) {
                recompileModule(moduleDir);
            }
            // No message for modules that are up to date
            
            // Note: GameModule interface validation happens during module loading,
            // not during compilation check. A module may compile successfully but
            // still not implement the required interface.
            
        } catch (Exception e) {
            addUserMessage("❌ Error checking module " + moduleDir.getName() + ": " + e.getMessage());
            Logging.error("❌ Error checking module " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Recompiles a single module
     * @param moduleDir The module directory to recompile
     */
    private void recompileModule(File moduleDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile", "-q");
            pb.directory(moduleDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                addUserMessage("⚠️ Module compilation failed for " + moduleDir.getName() + " (exit code: " + exitCode + ")");
            }
            // No success message for successful compilation
        } catch (Exception e) {
            addUserMessage("❌ Error recompiling module " + moduleDir.getName() + ": " + e.getMessage());
            Logging.error("❌ Error recompiling module " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
    }
    

    
    // ==================== UI UPDATES ====================
    
    /**
     * Update the game count status label
     */
    private void updateGameCountStatus() {
        if (statusLabel != null) {
            int count = availableGameModules.size();
            statusLabel.setText("Available Games: " + count);
            Logging.info("📊 UI Status updated: " + count + " games available");
        }
    }
    
    // ==================== GAME LAUNCHING ====================
    
    /**
     * Launch the currently selected game with validation.
     * 
     * This method validates the game selection and JSON configuration,
     * then delegates the actual launching to the ViewModel.
     */
    private void launchSelectedGame() {
        // Step 1: Validate that a game is selected
        if (selectedGameModule == null) {
            showErrorDialog("No Game Selected", "Please select a game to launch."); // Show error to user
            return; // Exit early if no game is selected
        }

        // Step 2: Parse and validate JSON syntax
        Map<String, Object> jsonConfigurationData = parseJsonConfigurationData();
        if (jsonConfigurationData == null) {
            // Check if it's due to invalid JSON syntax (not empty input)
            String jsonText = jsonDataTextArea.getText().trim();
            if (!jsonText.isEmpty()) {
                showErrorDialog("Invalid JSON", "Please enter valid JSON configuration."); // Show error to user
                return; // Exit early if JSON syntax is invalid
            }
            // If JSON is empty, continue with null data (let game decide)
            addUserMessage("📦 No JSON data provided (game will use its own defaults)");
        } else {
            // Log information about the JSON data being included
            addUserMessage("📦 Including custom JSON data with " + jsonConfigurationData.size() + " fields");
        }
        
        addUserMessage("🚀 Launching " + selectedGameModule.getGameName());
        
        // Step 5: Launch the game using the ViewModel
        if (applicationViewModel != null) {
            applicationViewModel.handleLaunchGame(selectedGameModule); // Delegate to ViewModel
        } else {
            showErrorDialog("Application Error", "ViewModel reference is not available."); // Show error if ViewModel is missing
        }
    }

    // ==================== JSON CONFIGURATION HANDLING ====================
    
    /**
     * Send the JSON text field content as a message to the selected game module.
     * 
     * This method validates JSON syntax and sends the content to the selected game module.
     * It also handles metadata requests and displays the response.
     */
    private void sendMessage() {
        // Check if a game module is selected
        if (selectedGameModule == null) {
            addUserMessage("⚠️ Please select a game module first before sending a message");
            return;
        }
        
        // Get the content from the JSON input area
        String jsonContent = jsonDataTextArea.getInputText().trim();
        String gameModuleName = selectedGameModule.getGameName();
        
        if (jsonContent.isEmpty()) {
            // If the JSON field is empty, send a placeholder message
            addUserMessage("💬 No content to send to " + gameModuleName + " (JSON field is empty)");
            return;
        }
        
        // Validate JSON syntax before sending
        try {
            // Attempt to parse the JSON to validate its syntax
            Map<String, Object> messageData = jsonDataMapper.readValue(jsonContent, Map.class);
            
            // Send the message to the game module
            Map<String, Object> response = selectedGameModule.handleMessage(messageData);
            
            // Handle the response if there is one
            if (response != null) {
                String responseJson = formatJsonResponse(response);
                
                // Display in the output area
                jsonDataTextArea.setOutputText(responseJson);
                
                // Add status message to message area
                addUserMessage("✅ Message sent successfully to " + gameModuleName + " - Response received");
            } else {
                addUserMessage("📭 No response from " + gameModuleName);
            }
            
        } catch (JsonProcessingException jsonProcessingError) {
            // If JSON is invalid, notify the user and don't send
            addUserMessage("❌ Invalid JSON syntax - message not sent");
        } catch (Exception e) {
            // Handle any other errors
            addUserMessage("❌ Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Clear the JSON configuration data text area.
     * 
     * This method clears the JSON input area and provides
     * user feedback about the action.
     */
    private void clearJsonConfigurationData() {
        // Remove all text from the JSON input area
        jsonDataTextArea.clearInput();
        
        // Provide user feedback about the action
        addUserMessage("🗑️ Cleared JSON input data");
    }
    
    /**
     * Fill the JSON text area with a metadata request.
     * 
     * This method automatically fills the JSON input area with
     * a standard metadata request that can be sent to game modules.
     */
    private void fillMetadataRequest() {
        // Create a standard metadata request JSON
        String metadataRequest = "{\n  \"function\": \"metadata\"\n}";
        
        // Set the JSON input area content
        jsonDataTextArea.setInputText(metadataRequest);
        
        // Provide user feedback about the action
        addUserMessage("📋 Filled JSON input with metadata request");
    }
    
    /**
     * Format a JSON response for better display in the message area.
     * 
     * @param response The response map to format
     * @return A formatted JSON string with proper indentation
     */
    private String formatJsonResponse(Map<String, Object> response) {
        try {
            // Use pretty printing for better readability
            return jsonDataMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            // Fallback to simple formatting if pretty printing fails
            try {
                return jsonDataMapper.writeValueAsString(response);
            } catch (JsonProcessingException ex) {
                return "Error formatting response: " + ex.getMessage();
            }
        }
    }
    
    /**
     * Parse the JSON configuration data from the text area.
     * 
     * @return The parsed JSON data as a Map, or null if parsing fails or input is empty
     */
    private Map<String, Object> parseJsonConfigurationData() {
        // Get the JSON text from the text area and remove leading/trailing whitespace
        String jsonConfigurationText = jsonDataTextArea.getText().trim();
        
        // If JSON is empty, return null (let the game decide what to do)
        if (jsonConfigurationText.isEmpty()) {
            return null;
        }
        
        try {
            // Parse the JSON text into a Map<String, Object> for easy access
            @SuppressWarnings("unchecked")
            Map<String, Object> configurationData = jsonDataMapper.readValue(jsonConfigurationText, Map.class);
            return configurationData; // Return the parsed configuration
        } catch (JsonProcessingException jsonProcessingError) {
            // Log the parsing error for debugging
            Logging.error("❌ Failed to parse JSON: " + jsonProcessingError.getMessage());
            return null; // Return null to indicate parsing failure
        }
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Add a message to the user message area with queue system.
     * 
     * This method adds a message to a queue and displays messages
     * one by one every 250ms to prevent message spam while ensuring
     * all messages are shown.
     * 
     * @param userMessage The message to display to the user
     */
    private void addUserMessage(String userMessage) {
        // Add message to queue
        messageQueue.offer(userMessage);
        
        // Start the message timer if it's not already running
        if (!messageTimerRunning) {
            startMessageTimer();
        }
    }
    
    /**
     * Start the message timer to process queued messages
     */
    private void startMessageTimer() {
        messageTimerRunning = true;
        
        // Create a timer that runs every 250ms
        new Thread(() -> {
            while (!messageQueue.isEmpty()) {
                try {
                    Thread.sleep(MESSAGE_INTERVAL_MS);
                    
                    // Process one message from the queue
                    String message = messageQueue.poll();
                    if (message != null) {
                        Platform.runLater(() -> displayMessage(message));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            messageTimerRunning = false;
        }).start();
    }
    
    /**
     * Display a single message to the user interface
     * @param userMessage The message to display
     */
    private void displayMessage(String userMessage) {
        // Create a timestamp in HH:mm:ss format for the message
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        // Create a simple text label for regular messages
        javafx.scene.control.Label messageLabel = new javafx.scene.control.Label("[" + timestamp + "] " + userMessage);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("simple-message");
        
        // Add the message to the container
        messageContainer.getChildren().add(messageLabel);
        
        // Auto-scroll to the bottom to show the latest message
        messageScrollPane.setVvalue(1.0);
    }
    

    
    /**
     * Show an error dialog to the user.
     * 
     * This method displays a modal error dialog with the specified
     * title and content to inform the user about errors.
     * 
     * @param dialogTitle The title of the error dialog
     * @param errorMessage The error message to display
     */
    private void showErrorDialog(String dialogTitle, String errorMessage) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(dialogTitle);
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
    
    // ==================== JSON PERSISTENCE METHODS ====================
    
    /**
     * Load saved JSON content and persistence toggle state on startup.
     */
    private void loadPersistenceSettings() {
        try {
            // Load persistence toggle state
            loadPersistenceToggleState();
            
            // Load saved JSON content if persistence is enabled
            if (jsonPersistenceToggle.isSelected()) {
                loadSavedJsonContent();
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error loading persistence settings: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load the persistence toggle state from file.
     */
    private void loadPersistenceToggleState() {
        try {
            Path toggleFile = Paths.get(PERSISTENCE_TOGGLE_FILE);
            if (Files.exists(toggleFile)) {
                String toggleState = Files.readString(toggleFile).trim();
                boolean isEnabled = Boolean.parseBoolean(toggleState);
                jsonPersistenceToggle.setSelected(isEnabled);
                Logging.info("📋 Loaded persistence toggle state: " + (isEnabled ? "enabled" : "disabled"));
            } else {
                // Default to enabled if no saved state
                jsonPersistenceToggle.setSelected(true);
                Logging.info("📋 No saved persistence toggle state found, defaulting to enabled");
            }
        } catch (Exception e) {
            Logging.error("❌ Error loading persistence toggle state: " + e.getMessage(), e);
            // Default to enabled on error
            jsonPersistenceToggle.setSelected(true);
        }
    }
    
    /**
     * Load saved JSON content from file.
     */
    private void loadSavedJsonContent() {
        try {
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                String savedJson = Files.readString(jsonFile);
                jsonDataTextArea.setInputText(savedJson);
                addUserMessage("📋 Restored saved JSON content to input area");
                Logging.info("📋 Loaded saved JSON content from file");
            } else {
                Logging.info("📋 No saved JSON content found");
            }
        } catch (Exception e) {
            Logging.error("❌ Error loading saved JSON content: " + e.getMessage(), e);
            addUserMessage("❌ Error loading saved JSON content");
        }
    }
    
    /**
     * Save JSON content to file if persistence is enabled.
     */
    private void saveJsonContent() {
        if (!jsonPersistenceToggle.isSelected()) {
            return; // Don't save if persistence is disabled
        }
        
        try {
            String jsonContent = jsonDataTextArea.getInputText();
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            Files.writeString(jsonFile, jsonContent);
            Logging.info("📋 Saved JSON input content to file");
        } catch (Exception e) {
            Logging.error("❌ Error saving JSON content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save persistence toggle state to file.
     */
    private void savePersistenceToggleState() {
        try {
            boolean isEnabled = jsonPersistenceToggle.isSelected();
            Path toggleFile = Paths.get(PERSISTENCE_TOGGLE_FILE);
            Files.writeString(toggleFile, String.valueOf(isEnabled));
            Logging.info("📋 Saved persistence toggle state: " + (isEnabled ? "enabled" : "disabled"));
        } catch (Exception e) {
            Logging.error("❌ Error saving persistence toggle state: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear the JSON persistence file.
     * 
     * This method removes the saved JSON content file when
     * persistence is disabled.
     */
    private void clearJsonPersistenceFile() {
        try {
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                Files.delete(jsonFile);
                Logging.info("🗑️ Cleared JSON persistence file");
            }
        } catch (Exception e) {
            Logging.error("❌ Error clearing JSON persistence file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle application shutdown and save settings.
     */
    public void onApplicationShutdown() {
        try {
            // Save JSON content if persistence is enabled
            saveJsonContent();
            
            // Save persistence toggle state
            savePersistenceToggleState();
            
            Logging.info("📋 Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("❌ Error saving application settings: " + e.getMessage(), e);
        }
    }
}  