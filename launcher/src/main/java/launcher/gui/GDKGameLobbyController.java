package launcher.gui;

import gdk.api.GameModule;
import gdk.infrastructure.Logging;
import gdk.infrastructure.MessagingBridge;
import launcher.utils.ModuleDiscovery;
import launcher.utils.ModuleCompiler;

import java.io.File;

import launcher.GDKApplication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import com.jfoenix.controls.JFXToggleButton;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import launcher.utils.DialogUtil;


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
 * @edited August 22, 2025       
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Game Selection Components
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private Button launchGameButton;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator refreshProgressIndicator;
    @FXML private Button settingsButton;
    
    // Message and Logging Components
    @FXML private ScrollPane messageScrollPane;
    @FXML private VBox messageContainer;
    
    // JSON Configuration Components
    @FXML private VBox jsonInputEditorContainer;
    @FXML private VBox jsonOutputEditorContainer;
    private SingleJsonEditor jsonInputEditor;
    private SingleJsonEditor jsonOutputEditor;
    @FXML private Button clearInputButton;

    @FXML private Button clearOutputButton2;
    @FXML private Button metadataRequestButton;
    @FXML private Button sendMessageButton;
    @FXML private JFXToggleButton jsonPersistenceToggle;
    
    // Application Control Components
    @FXML private Button exitButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar loadingProgressBar;
    @FXML private Label loadingStatusLabel;
    


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
    
    // Flag to prevent persistence messages during startup loading
    private boolean isLoadingPersistenceSettings = false;
    
    /**
     * Animation control for loading states
     */
    private boolean isRefreshing = false;
    private javafx.animation.Timeline loadingAnimation;
    private int loadingDots = 0;
    
    /**
     * Current module being processed for progress bar
     */
    private String currentProcessingModule = "";
    
    // Note: Progress window is now handled by PreStartupProgressWindow in Startup class
    
    /**
     * JSON mapper for parsing and validating JSON configuration data
     */
    private ObjectMapper jsonDataMapper;
    
    // ==================== JSON PERSISTENCE ====================
    
    /**
     * File path for saving JSON content
     */
    private static final String JSON_PERSISTENCE_FILE = "saved/gdk-json-persistence.txt";
    
    /**
     * File path for saving persistence toggle state
     */
    private static final String PERSISTENCE_TOGGLE_FILE = "saved/gdk-persistence-toggle.txt";
    private static final String SELECTED_GAME_FILE = "saved/gdk-selected-game.txt";
    private static final String AUTO_LAUNCH_ENABLED_FILE = "saved/gdk-auto-launch-enabled.txt";
    private static final String AUTO_SELECT_ENABLED_FILE = "saved/gdk-auto-select-enabled.txt";

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
        
        // Mirror game 'end' messages into JSON output
        subscribeToEndMessageMirror();
        
        // Register this controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(this);
        
        // Ensure saved directory exists
        ensureSavedDirectoryExists();
        
        // Load saved JSON content and toggle state
        loadPersistenceSettings();
        
        // Start game detection - DELAYED until startup progress window is ready
        // refreshAvailableGameModulesFast(); // Moved to Startup.ensureUIReady()
        
        // Initialize the status label
        updateGameCountStatus();
        
        // Check for compilation failures on startup AFTER modules are loaded
        // DELAYED: Moved to Startup.ensureUIReady() to ensure pre-startup window is visible first
        // Platform.runLater(() -> {
        //     checkStartupCompilationFailures();
        //     
        //     // Clear startup progress window reference after startup is complete
        //     //     startupProgressWindow = null;
        // });
        
        // Note: TextArea doesn't support syntax highlighting like CodeArea
        // JSON syntax highlighting removed for compatibility
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }

    private void subscribeToEndMessageMirror() {
        try {
            MessagingBridge.addConsumer(msg -> {
                try {
                    // Record the message to the transcript
                    launcher.utils.TranscriptRecorder.recordFromGame(msg);
                    
                    Object fn = (msg != null) ? msg.get("function") : null;
                    if (fn != null && "end".equals(String.valueOf(fn))) {
                        String pretty = formatJsonResponse((Map<String, Object>) msg);
                        Platform.runLater(() -> jsonOutputEditor.setText(pretty));
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
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
                    setText(gameModule.getMetadata().getGameName());
                }
            }
        });
        
        // Apply the same cell factory to the button cell (the part that shows the selected item)
        // This ensures consistent display between the dropdown list and the selected item
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        gameSelector.setItems(availableGameModules);
        
        // Message Area Setup
        
        // Apply consistent styling to the message container for better readability
        // Uses a modern font stack with fallbacks and appropriate size
        messageContainer.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Create SingleJsonEditor instances programmatically
        jsonInputEditor = new SingleJsonEditor("JSON Input");
        jsonOutputEditor = new SingleJsonEditor("JSON Output");
        
        // Add the editors to their containers
        jsonInputEditorContainer.getChildren().add(jsonInputEditor);
        jsonOutputEditorContainer.getChildren().add(jsonOutputEditor);
        
        // Remove height constraints to allow natural growth
        // jsonInputEditor.setMinHeight(200);
        // jsonOutputEditor.setMinHeight(200);
        
        // Configure containers to allow reasonable expansion
        // jsonInputEditorContainer.setMinHeight(200);
        // jsonOutputEditorContainer.setMinHeight(200);
        
        // Ensure containers can grow vertically
        VBox.setVgrow(jsonInputEditorContainer, Priority.ALWAYS);
        VBox.setVgrow(jsonOutputEditorContainer, Priority.ALWAYS);
        
        // Ensure the JSON editors themselves can grow within their containers
        VBox.setVgrow(jsonInputEditor, Priority.ALWAYS);
        VBox.setVgrow(jsonOutputEditor, Priority.ALWAYS);
        
        // Message area is ready for user feedback
        
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
            selectedGameModule = gameSelector.getValue();
            String selectedGameName = selectedGameModule != null ? selectedGameModule.getMetadata().getGameName() : "None";
            
            // Update launch button state based on selection
            updateLaunchButtonState();
            
            if (selectedGameModule != null) {
                addUserMessage("🎮 Selected game: " + selectedGameName);
                // Persist selected game
                persistSelectedGame(selectedGameName);
            } else {
                addUserMessage("⚠️ No game selected");
            }
        });
        
        // Initialize launch button state (disabled until a game is selected)
        updateLaunchButtonState();
        
        // Button Event Handlers
        // Launch button: Start the selected game with validation
        launchGameButton.setOnAction(event -> launchSelectedGame());
        
        // Server Simulator button removed; simulator auto-starts/stops with game.
        
        // Refresh button: Reload the list of available games
        refreshButton.setOnAction(event -> {
            // Start loading animation
            startLoadingAnimation();
            
            // Clear the message container immediately
            messageContainer.getChildren().clear();

            // Snapshot current modules on the JavaFX thread for safe background use
            List<GameModule> previousModulesSnapshot = new ArrayList<>(availableGameModules);
            int previousCountSnapshot = previousModuleCount;

            // Reset UI selections immediately to avoid launching stale modules
            availableGameModules.clear();
            selectedGameModule = null;
            gameSelector.getSelectionModel().clearSelection();
            
            // Run the refresh in a background thread to keep UI responsive
            new Thread(() -> {
                try {
                    // Store previous module names for removal detection
                    Set<String> previousModuleNames = new HashSet<>();
                    if (previousCountSnapshot > 0) {
                        for (GameModule module : previousModulesSnapshot) {
                            previousModuleNames.add(module.getMetadata().getGameName());
                        }
                    }

                    // Get the path to the modules directory from application constants
                    String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
                    
                    // Detect disabled/removed modules before recompilation
                    if (previousCountSnapshot == 0) {
                        // First load - detect pre-disabled modules
                        detectDisabledModulesBeforeRecompilation(modulesDirectoryPath);
                    } else {
                        // Subsequent loads - detect newly removed modules
                        detectNewlyRemovedModules(previousModuleNames);
                    }
                    
                    // Skip compilation checks on startup for faster loading
                    // Only recompile if this is not the first load
                    if (previousCountSnapshot > 0) {
                        checkAndRecompileModules(modulesDirectoryPath);
                    }
                    
                    // Use ModuleDiscovery and ModuleCompiler to discover all available game modules
                    List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(new File(modulesDirectoryPath));
                    List<GameModule> discoveredGameModules = ModuleCompiler.loadModules(validModuleDirectories);
                    
                    // Add each discovered module to our observable list
                    for (GameModule gameModule : discoveredGameModules) {
                        String gameName = gameModule.getMetadata().getGameName();
                        String className = gameModule.getClass().getSimpleName();
                        Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
                        
                        // Add user-friendly message for each detected game (only on first load)
                        if (previousCountSnapshot == 0) {
                            addUserMessage("🎮 Detected game: " + gameName);
                        }
                    }
                    
                    // Check for compilation failures and broken modules in the logs and notify user
                    checkForCompilationFailures(modulesDirectoryPath);
                    checkForBrokenModules(modulesDirectoryPath);
                    
                    // Check for compilation failures detected by ModuleCompiler
                    Logging.info("🔍 Checking for ModuleCompiler compilation failures...");
                    checkModuleCompilerCompilationFailures();
                    
                    // Force a more aggressive check for compilation issues
                    forceCompilationCheck(modulesDirectoryPath);
                    
                    // Update the ComboBox with the new list of games
                    Logging.info("🔄 Updating ComboBox with " + discoveredGameModules.size() + " modules");
                    Logging.info("🧭 Scheduling ComboBox UI update...");
                    
                    Platform.runLater(() -> {
                        try {
                            availableGameModules.setAll(discoveredGameModules);
                            selectedGameModule = null;
                            gameSelector.setItems(availableGameModules);
                            gameSelector.getSelectionModel().clearSelection();
                            updateGameCountStatus();
                            updateLaunchButtonState(); // Update button state after clearing selection
                            gameSelector.requestLayout();

                            Logging.info("📋 ComboBox items after refresh: " + gameSelector.getItems().size());
                            for (GameModule module : gameSelector.getItems()) {
                                Logging.info("   • " + module.getMetadata().getGameName());
                            }
                        } catch (Exception e) {
                            Logging.error("❌ Error updating ComboBox items on refresh: " + e.getMessage(), e);
                        }
                    });
                    
                    // Check for module changes and provide user feedback
                    int currentModuleCount = discoveredGameModules.size();
                    
                    if (currentModuleCount == 0) {
                        addUserMessage("⚠️ No game modules found in " + modulesDirectoryPath);
                    } else if (previousCountSnapshot > 0) {
                        // Check for module changes (additions/removals)
                        Set<String> currentModuleNames = new HashSet<>();
                        for (GameModule module : discoveredGameModules) {
                            currentModuleNames.add(module.getMetadata().getGameName());
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
                        
                        addUserMessage("✅ Successfully detected " + currentModuleCount + " game(s)");
                    } else if (previousCountSnapshot == 0) {
                        // First time loading - check for disabled modules that exist but aren't loaded
                        checkForDisabledModulesOnFirstLoad(modulesDirectoryPath, currentModuleCount);
                    } else {
                        addUserMessage("✅ Successfully detected " + currentModuleCount + " game(s)");
                    }
                    
                    // Update the previous count for next comparison
                    previousModuleCount = currentModuleCount;
                    
                } catch (Exception moduleDiscoveryError) {
                    // Handle any errors during module discovery
                    Logging.error("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage(), moduleDiscoveryError);
                    Platform.runLater(() -> {
                        addUserMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
                    });
                } finally {
                    // Stop loading animation when done (regardless of success or failure)
                    Platform.runLater(() -> {
                        stopLoadingAnimation();
                    });
                }
            }).start();
        });
        
        // Exit button: Close the entire application
        exitButton.setOnAction(event -> {
            Logging.info("🔒 GDK Game Lobby closing"); // Log the action
            onApplicationShutdown(); // Save settings before exit
            Platform.exit(); // Terminate the JavaFX application
        });
        
        // Settings button: Open settings page
        settingsButton.setOnAction(event -> openSettingsPage());
        
        // JSON Configuration Handlers
        // Clear input button: Remove all JSON input
        clearInputButton.setOnAction(event -> clearJsonInputData());
        
        // Clear output button: Remove all JSON output

        // Metadata request button: Fill JSON with metadata request
        metadataRequestButton.setOnAction(event -> fillMetadataRequest());
        
        // Send message button: Send a test message
        sendMessageButton.setOnAction(event -> sendMessage());
        
        // Persistence Toggle Handler
        // Save toggle state when changed and clear save file if disabled
        jsonPersistenceToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Skip messages during startup loading
            if (isLoadingPersistenceSettings) {
                return;
            }
            
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
        jsonInputEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            // Use Platform.runLater to debounce rapid changes
            Platform.runLater(() -> {
                if (jsonPersistenceToggle.isSelected()) {
                    saveJsonContent();
                }
            });
        });
        
        Logging.info("🎯 Event handlers configured");
    }

    // ==================== ANIMATION MANAGEMENT ====================
    
    /**
     * Start the loading animation with animated text and progress bar
     */
    private void startLoadingAnimation() {
        isRefreshing = true;
        refreshButton.setDisable(true);
        loadingProgressBar.setVisible(true);
        loadingProgressBar.setProgress(0.0);
        loadingStatusLabel.setVisible(true);
        loadingStatusLabel.setText("Starting module discovery...");
        
        // Debug logging
        Logging.info("🎯 Progress bar made visible and set to 0%");
        
        // Create animated text with dots
        loadingDots = 0;
        loadingAnimation = new Timeline(
            new KeyFrame(Duration.millis(300), event -> { // Faster animation - 300ms instead of 1000ms
                loadingDots = (loadingDots + 1) % 4;
                String dots = ".".repeat(loadingDots);
                
                // Update status message with current task
                String currentTask = getCurrentLoadingTask();
                loadingStatusLabel.setText(currentTask + dots);
                
                // Update progress bar - increment by larger amounts and faster
                double currentProgress = loadingProgressBar.getProgress();
                double newProgress = currentProgress + 0.25; // Larger increment - 25% per step
                if (newProgress > 0.9) newProgress = 0.9; // Don't complete until actually done
                loadingProgressBar.setProgress(newProgress);
                
                // Debug logging for progress
                Logging.info("🎯 Progress bar updated: " + (int)(newProgress * 100) + "%");
            })
        );
        loadingAnimation.setCycleCount(Timeline.INDEFINITE);
        loadingAnimation.play();
    }
    
    /**
     * Set the current module being processed
     * @param moduleName The name of the module being processed
     */
    public void setCurrentProcessingModule(String moduleName) {
        this.currentProcessingModule = moduleName;
        Logging.info("🎯 Now processing module: " + moduleName);
    }
    
    /**
     * Clear the current processing module
     */
    public void clearCurrentProcessingModule() {
        this.currentProcessingModule = "";
        Logging.info("✅ Finished processing all modules");
    }
    
    /**
     * Set the startup progress window for module loading updates
     * @param progressWindow The startup progress window
     */
    // Note: Progress window methods removed - now handled by PreStartupProgressWindow in Startup class
    
    /**
     * Get the current loading task description based on animation state
     */
    private String getCurrentLoadingTask() {
        // Cycle through different tasks based on animation state
        String[] tasks = {
            "Discovering modules",
            "Validating source code",
            "Loading compiled classes",
            "Initializing game modules",
            "Updating UI components"
        };
        
        int taskIndex = (loadingDots / 2) % tasks.length; // Change task every 2 dots
        String baseTask = tasks[taskIndex];
        
        // Add current module name if available
        if (!currentProcessingModule.isEmpty()) {
            return baseTask + " - " + currentProcessingModule;
        }
        
        return baseTask;
    }
    
    /**
     * Stop the loading animation
     */
    private void stopLoadingAnimation() {
        Logging.info("🎯 Stopping loading animation");
        isRefreshing = false;
        refreshButton.setDisable(false);
        loadingProgressBar.setVisible(false);
        loadingStatusLabel.setVisible(false);
        
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        
        // Complete the progress bar
        loadingProgressBar.setProgress(1.0);
        loadingStatusLabel.setText("Reload completed!");
        Logging.info("🎯 Progress bar completed to 100%");
    }
    
    // ==================== GAME MODULE MANAGEMENT ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the UI accordingly with proper error handling.
     */
    /**
     * Fast refresh that skips compilation checks for faster startup
     */
    public void refreshAvailableGameModulesFast() {
        try {
            Logging.info("🔄 refreshAvailableGameModulesFast() called");
            Set<String> previousModuleNames = new HashSet<>();
            for (GameModule module : availableGameModules) {
                previousModuleNames.add(module.getMetadata().getGameName());
            }
            Logging.info("📊 Previous module count: " + previousModuleNames.size());

            // Get the path to the modules directory from application constants
            String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
            Logging.info("📁 Modules directory path: " + modulesDirectoryPath);
            
            // Note: Progress window updates now handled by PreStartupProgressWindow in Startup class
            
            // Skip compilation checks on startup for speed
            // Just discover modules from existing compiled classes
            
            // Use ModuleDiscovery and ModuleCompiler to discover all available game modules
            File modulesDir = new File(modulesDirectoryPath);
            if (!modulesDir.exists()) {
                Logging.error("❌ Modules directory does not exist: " + modulesDirectoryPath);
                Platform.runLater(() -> {
                    addUserMessage("❌ Modules directory not found: " + modulesDirectoryPath);
                    updateGameCountStatus();
                });
                return;
            }
            
            Logging.info("🔍 Discovering modules in: " + modulesDir.getAbsolutePath());
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDir);
            Logging.info("✅ Found " + validModuleDirectories.size() + " valid module directories");
            
            if (validModuleDirectories.isEmpty()) {
                Logging.warning("⚠️ No valid module directories found");
                Platform.runLater(() -> {
                    addUserMessage("⚠️ No valid modules found in: " + modulesDirectoryPath);
                    updateGameCountStatus();
                });
                return;
            }
            
            Logging.info("📦 Loading modules...");
            List<GameModule> discoveredGameModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("✅ Module loading completed. Loaded " + discoveredGameModules.size() + " modules");
            
            // Validate that modules were actually loaded
            if (discoveredGameModules == null) {
                Logging.error("❌ ModuleCompiler.loadModules() returned null!");
                Platform.runLater(() -> {
                    addUserMessage("❌ Error: Module loading returned null");
                    updateGameCountStatus();
                    updateLaunchButtonState();
                });
                return;
            }
            
            // Track newly discovered modules for detailed reporting
            Set<String> newlyDiscoveredModuleNames = new HashSet<>();
            List<String> uiMessages = new ArrayList<>();
            
            for (GameModule gameModule : discoveredGameModules) {
                if (gameModule == null) {
                    Logging.warning("⚠️ Null game module in discovered list - skipping");
                    continue;
                }
                try {
                    String gameName = gameModule.getMetadata().getGameName();
                    String className = gameModule.getClass().getSimpleName();
                    newlyDiscoveredModuleNames.add(gameName);
                    Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
                    uiMessages.add("🎮 Detected game: " + gameName);
                } catch (Exception e) {
                    Logging.error("❌ Error getting metadata from game module: " + e.getMessage(), e);
                }
            }
            
            if (!discoveredGameModules.isEmpty()) {
                uiMessages.add("✅ Successfully detected " + discoveredGameModules.size() + " game(s)");
                Logging.info("✅ Ready to update UI with " + discoveredGameModules.size() + " modules");
            } else {
                uiMessages.add("⚠️ No games detected - check modules directory");
                Logging.warning("⚠️ No modules were loaded. Check logs above for loading errors.");
            }
            
            // Create a final list of valid modules (filter out nulls)
            final List<GameModule> validModules = new ArrayList<>();
            for (GameModule module : discoveredGameModules) {
                if (module != null) {
                    validModules.add(module);
                }
            }
            
            Logging.info("📊 Valid modules to add to UI: " + validModules.size());
            
            // Always update UI on JavaFX thread
            Runnable uiUpdate = () -> {
                try {
                    Logging.info("🎨 Updating UI on JavaFX thread...");
                    Logging.info("   Current availableGameModules size: " + availableGameModules.size());
                    Logging.info("   Modules to add: " + validModules.size());
                    
                    // Clear and update the observable list
                    availableGameModules.clear();
                    availableGameModules.addAll(validModules);
                    
                    Logging.info("   After adding, availableGameModules size: " + availableGameModules.size());
                    
                    // Update selected game module
                    selectedGameModule = null;
                    
                    // Update ComboBox
                    if (gameSelector != null) {
                        gameSelector.setItems(availableGameModules);
                        gameSelector.getSelectionModel().clearSelection();
                        gameSelector.requestLayout();
                        Logging.info("📋 ComboBox items after fast refresh: " + gameSelector.getItems().size());
                        
                        // Verify ComboBox has items
                        if (gameSelector.getItems().isEmpty() && !validModules.isEmpty()) {
                            Logging.error("❌ ComboBox is empty but validModules is not! This is a bug.");
                            // Force refresh
                            gameSelector.setItems(FXCollections.observableArrayList(validModules));
                        }
                    } else {
                        Logging.error("❌ gameSelector is null!");
                    }
                    
                    // Update status
                    updateGameCountStatus();
                    updateLaunchButtonState();
                    
                    // Log all modules in ComboBox
                    if (gameSelector != null) {
                        for (GameModule module : gameSelector.getItems()) {
                            if (module != null) {
                                Logging.info("   • ComboBox item: " + module.getMetadata().getGameName());
                            }
                        }
                    }
                    
                    // Log all modules in observable list
                    Logging.info("📊 Final UI state: " + availableGameModules.size() + " modules in ObservableList");
                    for (GameModule module : availableGameModules) {
                        if (module != null) {
                            Logging.info("   - ObservableList item: " + module.getMetadata().getGameName());
                        }
                    }
                    
                    // Add user messages
                    uiMessages.forEach(this::addUserMessage);
                    
                    // Report module changes
                    reportModuleChanges(previousModuleNames, newlyDiscoveredModuleNames);
                    previousModuleCount = availableGameModules.size();
                    
                    Logging.info("✅ UI update completed successfully");
                } catch (Exception e) {
                    Logging.error("❌ Error updating ComboBox items during fast refresh: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            };
            
            // Always run on JavaFX thread
            if (Platform.isFxApplicationThread()) {
                Logging.info("🔄 Already on JavaFX thread - running UI update directly");
                uiUpdate.run();
            } else {
                Logging.info("🔄 Not on JavaFX thread - scheduling UI update");
                Platform.runLater(uiUpdate);
            }
            
        } catch (Exception e) {
            addUserMessage("❌ Error refreshing game modules: " + e.getMessage());
            Logging.error("❌ Error refreshing game modules: " + e.getMessage(), e);
        }
    }
    
    private void refreshAvailableGameModules() {
        // Run module discovery in background thread to avoid blocking UI
        new Thread(() -> {
            try {
            // Store previous module names for removal detection
            Set<String> previousModuleNames = new HashSet<>();
            if (previousModuleCount > 0) {
                for (GameModule module : availableGameModules) {
                    previousModuleNames.add(module.getMetadata().getGameName());
                }
            }
            
            // Clear existing game modules to start fresh
            availableGameModules.clear();
            
            // Clear the currently selected game module since we're refreshing
            selectedGameModule = null;
            
            // Get the path to the modules directory from application constants
            String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
            
            // Detect disabled/removed modules before recompilation
            if (previousModuleCount == 0) {
                // First load - detect pre-disabled modules
                detectDisabledModulesBeforeRecompilation(modulesDirectoryPath);
            } else {
                // Subsequent loads - detect newly removed modules
                detectNewlyRemovedModules(previousModuleNames);
            }
            
            // Skip compilation checks on startup for faster loading
            // Only recompile if this is not the first load
            if (previousModuleCount > 0) {
                checkAndRecompileModules(modulesDirectoryPath);
            }
            
            // Use ModuleDiscovery and ModuleCompiler to discover all available game modules
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(new File(modulesDirectoryPath));
            List<GameModule> discoveredGameModules = ModuleCompiler.loadModules(validModuleDirectories);
            
            // Add each discovered module to our observable list
            for (GameModule gameModule : discoveredGameModules) {
                availableGameModules.add(gameModule); // Add to observable list for UI binding
                String gameName = gameModule.getMetadata().getGameName();
                String className = gameModule.getClass().getSimpleName();
                Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
                
                // Add user-friendly message for each detected game (only on first load)
                if (previousModuleCount == 0) {
                    addUserMessage("🎮 Detected game: " + gameName);
                }
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
                    currentModuleNames.add(module.getMetadata().getGameName());
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
            Platform.runLater(() -> {
                addUserMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
            });
        }
        }).start();
    }
    
    /**
     * Check for broken modules (modules that load but have Java file issues)
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void checkForBrokenModules(String modulesDirectoryPath) {
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
                        // Check if this module has Java file issues but still loads
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        File classesDir = new File(subdir, "target/classes");
                        File mainClass = new File(classesDir, "Main.class");
                        File metadataClass = new File(classesDir, "Metadata.class");
                        
                        // If core files exist and compile, but there might be other issues
                        if (mainJava.exists() && metadataJava.exists() && 
                            classesDir.exists() && mainClass.exists() && metadataClass.exists()) {
                            
                            // Check if there are other Java files that might have issues
                            List<File> allJavaFiles = findAllJavaFilesInModule(subdir);
                            if (allJavaFiles.size() > 2) { // More than just Main.java and Metadata.java
                                // Check for syntax issues in other files
                                List<String> problematicFiles = new ArrayList<>();
                                for (File javaFile : allJavaFiles) {
                                    if (!javaFile.getName().equals("Main.java") && 
                                        !javaFile.getName().equals("Metadata.java")) {
                                        if (!isJavaFileValid(javaFile)) {
                                            String relativePath = javaFile.getPath().substring(subdir.getPath().length() + 1);
                                            problematicFiles.add(relativePath);
                                        }
                                    }
                                }
                                
                                if (!problematicFiles.isEmpty()) {
                                    addUserMessage("⚠️ Module '" + subdir.getName() + "' has issues in: " + String.join(", ", problematicFiles));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking for broken modules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all Java files in a module directory
     * @param moduleDir The module directory
     * @return List of Java files
     */
    private List<File> findAllJavaFilesInModule(File moduleDir) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursivelyInModule(moduleDir, javaFiles);
        return javaFiles;
    }
    
    /**
     * Recursively find Java files in a module directory
     * @param dir The directory to search
     * @param javaFiles List to add Java files to
     */
    private void findJavaFilesRecursivelyInModule(File dir, List<File> javaFiles) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Skip target and .git directories
                    if (!file.getName().equals("target") && !file.getName().equals(".git")) {
                        findJavaFilesRecursivelyInModule(file, javaFiles);
                    }
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
    
    /**
     * Check if a Java file has valid syntax
     * @param javaFile The Java file to check
     * @return true if the file appears to have valid syntax
     */
    private boolean isJavaFileValid(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            boolean isCommented = false;
            
            while ((line = reader.readLine()) != null) {
                // Skip commented lines
                if (line.trim().startsWith("//")) {
                    continue;
                }
                
                // Handle block comments
                if (line.contains("/*")) {
                    isCommented = true;
                }
                if (line.contains("*/")) {
                    isCommented = false;
                    continue;
                }
                if (isCommented) {
                    continue;
                }
                
                content.append(line).append("\n");
            }
            
            String sourceCode = content.toString();
            
            // Basic syntax checks
            boolean hasClassDeclaration = sourceCode.contains("class ") || sourceCode.contains("public class ");
            boolean hasBalancedBraces = countChar(sourceCode, '{') == countChar(sourceCode, '}');
            boolean hasBalancedParens = countChar(sourceCode, '(') == countChar(sourceCode, ')');
            boolean hasBalancedBrackets = countChar(sourceCode, '[') == countChar(sourceCode, ']');
            
            return hasClassDeclaration && hasBalancedBraces && hasBalancedParens && hasBalancedBrackets;
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error reading Java file " + javaFile.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Counts occurrences of a character in a string
     * @param str The string to search
     * @param ch The character to count
     * @return The count
     */
    private int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
    
    /**
     * Check for compilation failures on startup
     */
    public void checkStartupCompilationFailures() {
        try {
            Logging.info("🚀 Checking for compilation failures on startup...");
            
            // Get the modules directory path
            String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
            
            // Check for compilation failures detected by ModuleCompiler
            checkModuleCompilerCompilationFailures();
            
            // Also check for any existing compilation issues
            checkForCompilationFailures(modulesDirectoryPath);
            
            // Force a compilation check for all modules to detect issues
            forceCompilationCheck(modulesDirectoryPath);
            
            Logging.info("✅ Startup compilation failure check completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during startup compilation failure check: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check for compilation failures detected by ModuleCompiler
     */
    private void checkModuleCompilerCompilationFailures() {
        try {
            Logging.info("🔍 Starting check for ModuleCompiler compilation failures...");
            
            // Get compilation failures from ModuleCompiler
            List<String> compilationFailures = ModuleCompiler.getLastCompilationFailures();
            
            Logging.info("📊 Found " + compilationFailures.size() + " compilation failures to report");
            
            if (!compilationFailures.isEmpty()) {
                // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
                Platform.runLater(() -> {
                    Logging.info("🎯 Adding compilation failure messages to UI...");
                    for (String moduleName : compilationFailures) {
                        String message = "⚠️ Module '" + moduleName + "' failed to compile - check source code for errors";
                        addUserMessage(message);
                        Logging.info("📝 Added message: " + message);
                    }
                    String summaryMessage = "📋 Compilation failures detected in: " + String.join(", ", compilationFailures);
                    addUserMessage(summaryMessage);
                    Logging.info("📝 Added summary message: " + summaryMessage);
                });
                
                Logging.info("📢 Queued compilation failure notifications for UI: " + String.join(", ", compilationFailures));
            } else {
                Logging.info("✅ No compilation failures to report");
            }
            
            // Clear the stored failures after reporting them
            ModuleCompiler.clearCompilationFailures();
            
        } catch (Exception e) {
            Logging.error("❌ Error checking ModuleCompiler compilation failures: " + e.getMessage(), e);
        }
    }
    
    /**
     * Force compilation check for all modules to detect issues
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void forceCompilationCheck(String modulesDirectoryPath) {
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
                        // Check if this module has compilation issues by attempting compilation
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Try to compile the module
                            boolean compilationSuccess = attemptModuleCompilation(subdir);
                            if (!compilationSuccess) {
                                addUserMessage("⚠️ Module '" + subdir.getName() + "' has compilation errors - check the console for details");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error during forced compilation check: " + e.getMessage(), e);
        }
    }
    
    /**
     * Attempt to compile a module and return success status
     * @param moduleDir The module directory
     * @return true if compilation succeeds
     */
    private boolean attemptModuleCompilation(File moduleDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile");
            pb.directory(moduleDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read output for logging
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Wait for compilation to complete
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("✅ Forced compilation successful for " + moduleDir.getName());
                return true;
            } else {
                Logging.warning("⚠️ Forced compilation failed for " + moduleDir.getName() + " (exit code: " + exitCode + ")");
                // Log compilation errors for debugging
                String[] lines = output.toString().split("\n");
                for (String line : lines) {
                    if (line.contains("ERROR") || line.contains("FAILURE") || line.contains("BUILD FAILURE")) {
                        Logging.warning("  " + line);
                    }
                }
                return false;
            }
            
        } catch (Exception e) {
            Logging.warning("⚠️ Exception during forced compilation of " + moduleDir.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check for compilation failures and notify the user
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void checkForCompilationFailures(String modulesDirectoryPath) {
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
                        // Check if this module has compilation issues
                        File classesDir = new File(subdir, "target/classes");
                        File mainClass = new File(classesDir, "Main.class");
                        File metadataClass = new File(classesDir, "Metadata.class");
                        
                        // Check if source files exist
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Source files exist, check if compilation succeeded
                            if (!classesDir.exists() || !mainClass.exists() || !metadataClass.exists()) {
                                addUserMessage("⚠️ Module '" + subdir.getName() + "' failed to compile - check for syntax errors");
                            }
                        } else {
                            // Missing source files
                            if (!mainJava.exists()) {
                                addUserMessage("⚠️ Module '" + subdir.getName() + "' missing Main.java file");
                            }
                            if (!metadataJava.exists()) {
                                addUserMessage("⚠️ Module '" + subdir.getName() + "' missing Metadata.java file");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking for compilation failures: " + e.getMessage(), e);
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
                        
                        // If no Main.java found, mark as disabled
                        if (!mainJavaFile.exists()) {
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
     * Detect newly removed modules and add them to the removed tracking set
     * @param previousModuleNames Set of previously loaded module names
     */
    private void detectNewlyRemovedModules(Set<String> previousModuleNames) {
        try {
            String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
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
                            
                            // If no Main.java found, mark as removed
                            if (!mainJavaFile.exists()) {
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
            
            // Check for Main.java directly in src/main/java/ (standardized structure)
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            
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
     * Report changes in available game modules (only for subsequent reloads).
     * 
     * @param previousModuleNames Set of module names that were available before refresh
     * @param currentModuleNames Set of module names that are available after refresh
     */
    private void reportModuleChanges(Set<String> previousModuleNames, Set<String> currentModuleNames) {
        // Find added modules (in current but not in previous)
        Set<String> addedModules = new HashSet<>(currentModuleNames);
        addedModules.removeAll(previousModuleNames);
        
        // Find removed modules (in previous but not in current)
        Set<String> removedModules = new HashSet<>(previousModuleNames);
        removedModules.removeAll(currentModuleNames);
        
        // Log the changes for debugging
        Logging.info("📊 Module change analysis:");
        Logging.info("   Previous modules: " + (previousModuleNames.isEmpty() ? "none" : String.join(", ", previousModuleNames)));
        Logging.info("   Current modules: " + (currentModuleNames.isEmpty() ? "none" : String.join(", ", currentModuleNames)));
        Logging.info("   Added modules: " + (addedModules.isEmpty() ? "none" : String.join(", ", addedModules)));
        Logging.info("   Removed modules: " + (removedModules.isEmpty() ? "none" : String.join(", ", removedModules)));
        
        // Only report changes for subsequent reloads (not first time loading)
        if (!previousModuleNames.isEmpty()) {
            // Report each added module individually
            for (String moduleName : addedModules) {
                addUserMessage("🆕 Added game module: " + moduleName);
                Logging.info("🆕 Added game module: " + moduleName);
            }
            
            // Report each removed module individually
            for (String moduleName : removedModules) {
                addUserMessage("🗑️ Removed game module: " + moduleName);
                Logging.info("🗑️ Removed game module: " + moduleName);
            }
            
            // Report no changes if nothing changed
            if (addedModules.isEmpty() && removedModules.isEmpty()) {
                String message = "✅ No changes detected - " + currentModuleNames.size() + " game module(s) available";
                addUserMessage(message);
                Logging.info(message);
            }
        }
    }
    
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
    
    /**
     * Update the launch button state based on whether a game is selected.
     * Disables the button when no game is selected, enables it when a game is selected.
     */
    private void updateLaunchButtonState() {
        if (launchGameButton != null) {
            boolean hasSelectedGame = selectedGameModule != null;
            launchGameButton.setDisable(!hasSelectedGame);
            
            if (hasSelectedGame) {
                launchGameButton.setStyle("-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 10 20; -fx-min-width: 120;");
            } else {
                launchGameButton.setStyle("-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: default; -fx-padding: 10 20; -fx-min-width: 120; -fx-opacity: 0.6;");
            }
        }
    }
    
    /**
     * Update the status label with a message.
     * 
     * @param message The status message to display
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            Logging.info("📊 Status updated: " + message);
        }
    }
    
    // ==================== GAME LAUNCHING ====================
    
    /**
     * Launch the selected game with the provided JSON configuration.
     * 
     * @param jsonConfigurationData The parsed JSON configuration data
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    private void launchSelectedGameWithConfiguration(Map<String, Object> jsonConfigurationData, boolean isAutoLaunch) {
        // Double-check that a game is selected (defensive programming)
        if (selectedGameModule == null) {
            Logging.error("❌ launchSelectedGameWithConfiguration called with null selectedGameModule");
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "No game module is selected. Please select a game from the dropdown.");
                addUserMessage("❌ Error: No game selected for launch");
            }
            return;
        }
        
        // Validate ViewModel is available
        if (applicationViewModel == null) {
            Logging.error("❌ applicationViewModel is null - cannot launch game");
            if (!isAutoLaunch) {
                DialogUtil.showError("Application Error", "Application ViewModel is not available. Please restart the application.");
                addUserMessage("❌ Error: ViewModel not available");
            }
            return;
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("🚀 Preparing to launch game: " + gameName);
        
        // Use the utility class for the core launch logic
        boolean configSuccess = launcher.utils.GameLaunchUtil.launchGameWithConfiguration(selectedGameModule, jsonConfigurationData, isAutoLaunch);
        
        if (!configSuccess) {
            Logging.error("❌ Game configuration failed for: " + gameName);
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "Failed to configure game: " + gameName + ". Check the logs for details.");
                addUserMessage("❌ Failed to configure game: " + gameName);
            }
            return; // Exit early if configuration failed
        }
        
        // Get the JSON text for the ViewModel to check game mode
        String jsonText = jsonInputEditor.getText().trim();
        
        // Step 5: Launch the game using the ViewModel
        try {
            Logging.info("🎮 Calling ViewModel to launch game: " + gameName);
            applicationViewModel.handleLaunchGame(selectedGameModule, jsonText); // Delegate to ViewModel with JSON text
            addUserMessage("✅ Launching game: " + gameName);
        } catch (Exception e) {
            Logging.error("❌ Exception while launching game: " + gameName, e);
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "Failed to launch game: " + gameName + "\n\nError: " + e.getMessage());
                addUserMessage("❌ Error launching game: " + e.getMessage());
            }
        }
    }

    /**
     * Launch the selected game using the JSON input from the UI.
     * This is the original method that gets called when the user presses "Launch Game".
     */
    private void launchSelectedGame() {
        // Validate that a game is selected
        if (selectedGameModule == null) {
            Logging.warning("⚠️ Launch button clicked but no game is selected");
            DialogUtil.showError("No Game Selected", 
                "Please select a game from the dropdown before launching.");
            addUserMessage("⚠️ Please select a game from the dropdown first");
            return;
        }
        
        Logging.info("🚀 Launch button clicked for game: " + selectedGameModule.getMetadata().getGameName());
        
        // Parse and validate JSON syntax from UI
        Map<String, Object> jsonConfigurationData = parseJsonConfigurationData();
        if (jsonConfigurationData == null) {
            // Check if it's due to invalid JSON syntax (not empty input)
            String jsonText = jsonInputEditor.getText().trim();
            if (!jsonText.isEmpty()) {
                DialogUtil.showJsonError("Invalid JSON", "Please enter valid JSON configuration.");
                return; // Exit early if JSON syntax is invalid
            }
        }
        
        // Launch with UI configuration
        launchSelectedGameWithConfiguration(jsonConfigurationData, false);
    }
    
    /**
     * Launch a specific game module with saved JSON configuration.
     * This method is designed for auto-launch functionality.
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameWithSavedJson(GameModule gameModule, String savedJson) {
        try {
            // Temporarily set the selected game module
            GameModule previousSelected = selectedGameModule;
            selectedGameModule = gameModule;
            
            // Parse the saved JSON
            Map<String, Object> jsonConfigurationData = null;
            if (savedJson != null && !savedJson.trim().isEmpty()) {
                try {
                    jsonConfigurationData = jsonDataMapper.readValue(savedJson.trim(), Map.class);
                } catch (Exception e) {
                    Logging.error("Auto-launch: Failed to parse saved JSON: " + e.getMessage());
                    // Continue with null configuration (let game use defaults)
                }
            }
            
            // Launch the game with the saved configuration
            launchSelectedGameWithConfiguration(jsonConfigurationData, true);
            
            // Restore the previous selection
            selectedGameModule = previousSelected;
            
            return true;
        } catch (Exception e) {
            Logging.error("Auto-launch: Error launching game with saved JSON: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get the application ViewModel reference.
     * This method is used by auto-launch functionality.
     * 
     * @return The application ViewModel instance
     */
    public GDKViewModel getApplicationViewModel() {
        return applicationViewModel;
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
        String jsonContent = jsonInputEditor.getText().trim();
        String gameModuleName = selectedGameModule.getMetadata().getGameName();
        
        if (jsonContent.isEmpty()) {
            // If the JSON field is empty, send a placeholder message
            addUserMessage("💬 No content to send to " + gameModuleName + " (JSON field is empty)");
            return;
        }
        
        // Validate JSON syntax before sending
        try {
            // Attempt to parse the JSON to validate its syntax
            Map<String, Object> messageData = jsonDataMapper.readValue(jsonContent, Map.class);
            
            // Record message to transcript before sending
            launcher.utils.TranscriptRecorder.recordToGame(messageData);
            
            // Send the message to the game module
            Map<String, Object> response = selectedGameModule.handleMessage(messageData);
            
            // Handle the response if there is one
            if (response != null) {
                String responseJson = formatJsonResponse(response);
                
                // Display in the output area
                jsonOutputEditor.setText(responseJson);
                
                // Record response to transcript
                launcher.utils.TranscriptRecorder.recordFromGame(response);
                
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
     * Clear the JSON input data text area.
     * 
     * This method clears the JSON input area and provides
     * user feedback about the action.
     */
    private void clearJsonInputData() {
        // Remove all text from the JSON input area
        jsonInputEditor.clear();
        
        // Provide user feedback about the action
        addUserMessage("🗑️ Cleared JSON input data");
    }
    
    private void clearJsonOutputData() {
        // Remove all text from the JSON output area
        jsonOutputEditor.clear();
        
        // Provide user feedback about the action
        addUserMessage("🗑️ Cleared JSON output data");
    }
    
    /**
     * Clear the JSON output data text area.
     * 
     * This method clears the JSON output area and provides
     * user feedback about the action.
     */
    private void clearJsonConfigurationData() {
        // Remove all text from the JSON output area
        jsonOutputEditor.clear();
        
        // Provide user feedback about the action
        addUserMessage("🗑️ Cleared JSON output data");
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
        jsonInputEditor.setText(metadataRequest);
        
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
        String jsonConfigurationText = jsonInputEditor.getText().trim();
        
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
        // If this is a loading animation message and we're already refreshing,
        // replace the last message instead of adding a new one
        if (isRefreshing && userMessage.startsWith("🔄 Reload in progress")) {
            // Remove the last message if it's also a loading message
            if (!messageQueue.isEmpty()) {
                String lastMessage = ((LinkedList<String>) messageQueue).peekLast();
                if (lastMessage != null && lastMessage.startsWith("🔄 Reload in progress")) {
                    ((LinkedList<String>) messageQueue).removeLast();
                }
            }
        }
        
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
    

    

    
    // ==================== JSON PERSISTENCE METHODS ====================
    
    /**
     * Load saved JSON content and persistence toggle state on startup.
     */
    private void loadPersistenceSettings() {
        try {
            // Set flag to prevent persistence messages during loading
            isLoadingPersistenceSettings = true;
            
            // Load persistence toggle state
            loadPersistenceToggleState();
            
            
            
            // Load saved JSON content if persistence is enabled
            if (jsonPersistenceToggle.isSelected()) {
                loadSavedJsonContent();
            }
            
            // Load persisted selected game
            loadPersistedSelectedGame();
            
            // Clear flag after loading is complete
            isLoadingPersistenceSettings = false;
            
        } catch (Exception e) {
            Logging.error("❌ Error loading persistence settings: " + e.getMessage(), e);
            // Clear flag even on error
            isLoadingPersistenceSettings = false;
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
                // No startup message - only show when setting changes
            } else {
                // Default to enabled if no saved state
                jsonPersistenceToggle.setSelected(true);
                // No startup message - only show when setting changes
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
                jsonInputEditor.setText(savedJson);
                // No startup message - only show when setting changes
            } else {
                // No startup message - only show when setting changes
            }
        } catch (Exception e) {
            Logging.error("❌ Error loading saved JSON content: " + e.getMessage(), e);
            addUserMessage("❌ Error loading saved JSON content");
        }
    }

    private void ensureSavedDirectoryExists() {
        try {
            Path savedDir = Paths.get("saved");
            if (!Files.exists(savedDir)) {
                Files.createDirectories(savedDir);
                Logging.info("📁 Created saved directory: " + savedDir.toAbsolutePath());
            }
        } catch (Exception e) {
            Logging.error("❌ Failed to create saved directory: " + e.getMessage());
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
            ensureSavedDirectoryExists();
            String jsonContent = jsonInputEditor.getText();
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
            ensureSavedDirectoryExists();
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
     * Persist the selected game module's name.
     * @param gameName The name of the selected game module.
     */
    private void persistSelectedGame(String gameName) {
        try {
            if (gameName == null || gameName.equals("None")) return;
            ensureSavedDirectoryExists();
            java.nio.file.Files.writeString(java.nio.file.Paths.get(SELECTED_GAME_FILE), gameName);
        } catch (Exception ignored) {}
    }

    /**
     * Load the persisted selected game module.
     */
    private void loadPersistedSelectedGame() {
        // Auto-select functionality removed - toggle no longer available in main UI
    }
    
    /**
     * Restore the previously selected game after modules are loaded.
     * This method is called after the ComboBox is populated with modules.
     */
    private void restorePreviouslySelectedGame() {
        // Auto-select functionality removed - toggle no longer available in main UI
    }
    
    /**
     * Open the settings page.
     */
    private void openSettingsPage() {
        try {
            Logging.info("⚙️ Transitioning to settings page");
            
            // Load the settings page FXML
            URL fxmlUrl = getClass().getResource("/gdk-lobby/settings-page.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Could not find settings-page.fxml resource");
            }
            Logging.info("📁 Found FXML resource at: " + fxmlUrl);
            
            // Load the settings page FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Logging.info("📋 FXMLLoader created successfully");
            
            Parent settingsRoot = loader.load();
            Logging.info("📄 FXML loaded successfully");
            
            // Get the settings controller and set the main controller reference
            SettingsPageController settingsController = loader.getController();
            if (settingsController == null) {
                throw new RuntimeException("Settings controller is null");
            }
            Logging.info("🎮 Settings controller loaded successfully");
            
            settingsController.setMainController(this);
            Logging.info("🔗 Main controller reference set");
            
            // Get the current stage and change its scene to show settings
            Stage currentStage = (Stage) exitButton.getScene().getWindow();
            if (currentStage != null) {
                // Store the current scene for returning later
                Scene currentScene = currentStage.getScene();
                
                // Create the settings scene
                Scene settingsScene = new Scene(settingsRoot);
                
                // Load the settings page CSS
                try {
                    URL cssUrl = getClass().getResource("/gdk-lobby/settings-page.css");
                    if (cssUrl != null) {
                        settingsRoot.getStylesheets().add(cssUrl.toExternalForm());
                        Logging.info("🎨 CSS loaded successfully");
                    } else {
                        Logging.warning("⚠️ Could not find settings-page.css resource");
                    }
                } catch (Exception cssError) {
                    Logging.warning("⚠️ Could not load settings page CSS: " + cssError.getMessage());
                }
                
                // Store the current scene in the settings controller for navigation back
                settingsController.setMainScene(currentScene);
                
                // Change the stage to show settings
                currentStage.setScene(settingsScene);
                currentStage.setTitle("GDK Settings");
                
                Logging.info("🔄 Stage transitioned to settings page");
            } else {
                throw new RuntimeException("Could not get current stage");
            }
            
            Logging.info("✅ Settings page transition completed successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error transitioning to settings page: " + e.getMessage(), e);
            DialogUtil.showError("Error", "Failed to open settings page: " + e.getMessage());
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
            
            
            
            // Save selected game
            if (selectedGameModule != null) {
                persistSelectedGame(selectedGameModule.getMetadata().getGameName());
            }
            
            Logging.info("📋 Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("❌ Error saving application settings: " + e.getMessage(), e);
        }
    }

}  
