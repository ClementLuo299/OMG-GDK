package launcher.gui.lobby;

import gdk.api.GameModule;
import gdk.internal.Logging;
import gdk.internal.MessagingBridge;
import launcher.utils.module.ModuleDiscovery;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.path.PathUtil;

import java.io.File;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import launcher.utils.gui.DialogUtil;
import launcher.gui.lobby.managers.MessageManager;
import launcher.gui.lobby.managers.LoadingAnimationManager;
import launcher.gui.lobby.managers.JsonPersistenceManager;
import launcher.gui.lobby.managers.ModuleCompilationChecker;
import launcher.gui.lobby.managers.JsonConfigurationHandler;
import launcher.gui.lobby.managers.UIStateManager;
import launcher.gui.lobby.subcontrollers.GameSelectionController;
import launcher.gui.lobby.subcontrollers.JsonConfigurationController;
import launcher.gui.lobby.subcontrollers.ApplicationControlController;
import launcher.gui.json_editor.SingleJsonEditor;
import launcher.gui.settings_page.SettingsPageController;


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
     * The mode this controller is running in.
     * AUTO_LAUNCH: No GUI loaded, controller used for ViewModel management only
     * NORMAL: Full GUI loaded, normal operation
     */
    public enum ControllerMode {
        AUTO_LAUNCH,
        NORMAL
    }
    
    /**
     * Current mode of operation for this controller
     */
    private ControllerMode controllerMode = ControllerMode.NORMAL;
    
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
     * JSON mapper for parsing and validating JSON configuration data
     */
    private ObjectMapper jsonDataMapper;
    
    // ==================== MANAGERS ====================
    
    /**
     * Manager for handling message display queue
     */
    private MessageManager messageManager;
    
    /**
     * Manager for loading animations
     */
    private LoadingAnimationManager loadingAnimationManager;
    
    /**
     * Manager for JSON persistence
     */
    private JsonPersistenceManager jsonPersistenceManager;
    
    /**
     * Manager for module compilation checking
     */
    private ModuleCompilationChecker moduleCompilationChecker;
    
    /**
     * Handler for JSON configuration operations
     */
    private JsonConfigurationHandler jsonConfigurationHandler;
    
    /**
     * Manager for UI state updates
     */
    private UIStateManager uiStateManager;
    
    // ==================== SUBCONTROLLERS ====================
    
    /**
     * Subcontroller for game selection UI area
     */
    private GameSelectionController gameSelectionController;
    
    /**
     * Subcontroller for JSON configuration UI area
     */
    private JsonConfigurationController jsonConfigurationController;
    
    /**
     * Subcontroller for application control UI area
     */
    private ApplicationControlController applicationControlController;

    // ==================== INITIALIZATION ====================
    
    /**
     * Set the controller mode (AUTO_LAUNCH or NORMAL).
     * This should be called before initialize() for auto-launch mode.
     * 
     * @param mode The mode to set
     */
    public void setControllerMode(ControllerMode mode) {
        this.controllerMode = mode;
        Logging.info("🎮 Controller mode set to: " + mode);
    }
    
    /**
     * Get the current controller mode.
     * 
     * @return The current mode
     */
    public ControllerMode getControllerMode() {
        return controllerMode;
    }
    
    /**
     * Initialize the controller when FXML is loaded.
     * 
     * This method is called automatically by JavaFX when the FXML
     * file is loaded. It sets up all components and event handlers.
     * In AUTO_LAUNCH mode, GUI setup is skipped.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller (mode: " + controllerMode + ")");
        
        // Initialize dependencies (needed for both modes)
        jsonDataMapper = new ObjectMapper();
        availableGameModules = FXCollections.observableArrayList();
        
        // Skip GUI setup in AUTO_LAUNCH mode
        if (controllerMode == ControllerMode.AUTO_LAUNCH) {
            Logging.info("⏭️ Skipping GUI setup for AUTO_LAUNCH mode");
            // Still need to register with ModuleCompiler for consistency
            ModuleCompiler.setUIController(this);
            Logging.info("✅ GDK Game Picker Controller initialized (AUTO_LAUNCH mode)");
            return;
        }
        
        // Normal mode: full GUI setup
        // Create JSON editors first (needed for managers)
        jsonInputEditor = new SingleJsonEditor("JSON Input");
        jsonOutputEditor = new SingleJsonEditor("JSON Output");
        
        // Initialize managers
        messageManager = new MessageManager(messageContainer, messageScrollPane);
        loadingAnimationManager = new LoadingAnimationManager(refreshButton, loadingProgressBar, loadingStatusLabel);
        jsonPersistenceManager = new JsonPersistenceManager(jsonInputEditor, jsonPersistenceToggle);
        moduleCompilationChecker = new ModuleCompilationChecker(this::addUserMessage);
        jsonConfigurationHandler = new JsonConfigurationHandler(jsonDataMapper, jsonInputEditor, jsonOutputEditor, this::addUserMessage);
        uiStateManager = new UIStateManager(statusLabel, launchGameButton, this::addUserMessage);
        
        // Initialize subcontrollers
        initializeSubcontrollers();
        
        // Set up the UI components
        setupUserInterface();
        
        // Mirror game 'end' messages into JSON output
        subscribeToEndMessageMirror();
        
        // Register this controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(this);
        
        // Load saved JSON content and toggle state
        jsonPersistenceManager.loadPersistenceSettings();
        
        // Start game detection - DELAYED until startup progress window is ready
        // refreshAvailableGameModulesFast(); // Moved to Startup.ensureUIReady()
        
        // Initialize the status label
        if (uiStateManager != null) {
            uiStateManager.updateGameCountStatus(availableGameModules.size());
        }
        
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
                    launcher.utils.game.TranscriptRecorder.recordFromGame(msg);
                    
                    Object fn = (msg != null) ? msg.get("function") : null;
                    if (fn != null && "end".equals(String.valueOf(fn)) && jsonConfigurationController != null) {
                        String pretty = jsonConfigurationHandler.formatJsonResponse((Map<String, Object>) msg);
                        Platform.runLater(() -> jsonConfigurationController.getJsonOutputEditor().setText(pretty));
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
    
    // ==================== SUBCONTROLLER INITIALIZATION ====================
    
    /**
     * Initialize all subcontrollers with their UI components and dependencies.
     */
    private void initializeSubcontrollers() {
        // Initialize GameSelectionController
        gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            refreshButton,
            settingsButton,
            availableGameModules,
            messageManager,
            uiStateManager,
            loadingAnimationManager,
            moduleCompilationChecker,
            jsonPersistenceManager
        );
        
        // Set callbacks for game selection controller
        gameSelectionController.setOnLaunchGame(this::launchSelectedGame);
        gameSelectionController.setOnOpenSettings(this::openSettingsPage);
        gameSelectionController.setOnGameSelected(() -> {
            // Update JSON configuration controller with selected game
            if (jsonConfigurationController != null) {
                jsonConfigurationController.setSelectedGameModule(gameSelectionController.getSelectedGameModule());
            }
            selectedGameModule = gameSelectionController.getSelectedGameModule();
        });
        
        // Initialize JsonConfigurationController
        jsonConfigurationController = new JsonConfigurationController(
            jsonInputEditorContainer,
            jsonOutputEditorContainer,
            jsonInputEditor,
            jsonOutputEditor,
            clearInputButton,
            clearOutputButton2,
            metadataRequestButton,
            sendMessageButton,
            jsonPersistenceToggle,
            jsonConfigurationHandler,
            jsonPersistenceManager,
            messageManager
        );
        
        // Initialize ApplicationControlController
        applicationControlController = new ApplicationControlController(
            exitButton,
            settingsButton,
            statusLabel,
            loadingProgressBar,
            loadingStatusLabel
        );
        
        // Set callbacks for application control controller
        applicationControlController.setOnExit(this::onApplicationShutdown);
        applicationControlController.setOnOpenSettings(this::openSettingsPage);
        
        // Initialize all subcontrollers
        gameSelectionController.initialize();
        jsonConfigurationController.initialize();
        applicationControlController.initialize();
    }

    // ==================== USER INTERFACE SETUP ====================
    
    /**
     * Set up all user interface components.
     * 
     * This method configures the visual appearance and behavior
     * of all UI components in the lobby interface.
     */
    private void setupUserInterface() {
        // Message Area Setup
        
        // Apply consistent styling to the message container for better readability
        // Uses a modern font stack with fallbacks and appropriate size
        messageContainer.setStyle("-fx-font-family: 'Inter', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Log successful UI initialization for debugging purposes
        Logging.info("🎨 UI components initialized");
    }
    



    // ==================== ANIMATION MANAGEMENT ====================
    
    /**
     * Set the current module being processed
     * @param moduleName The name of the module being processed
     */
    public void setCurrentProcessingModule(String moduleName) {
        if (loadingAnimationManager != null) {
            loadingAnimationManager.setCurrentProcessingModule(moduleName);
        }
    }
    
    /**
     * Clear the current processing module
     */
    public void clearCurrentProcessingModule() {
        if (loadingAnimationManager != null) {
            loadingAnimationManager.clearCurrentProcessingModule();
        }
    }
    
    // ==================== GAME MODULE MANAGEMENT ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the UI accordingly with proper error handling.
     */
    /**
     * Fast refresh that skips compilation checks for faster startup.
     */
    public void refreshAvailableGameModulesFast() {
        if (gameSelectionController != null) {
            gameSelectionController.refreshAvailableGameModulesFast();
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
            
            // Get the path to the modules directory
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            
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
                if (uiStateManager != null) {
                    uiStateManager.updateGameCountStatus(availableGameModules.size());
                }
                
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
     * Check for compilation failures on startup
     */
    public void checkStartupCompilationFailures() {
        if (moduleCompilationChecker != null) {
            moduleCompilationChecker.checkStartupCompilationFailures();
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
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
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
            
            // Check if Main.java exists in source (using same logic as ModuleRuntimeLoader)
            
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
        boolean configSuccess = launcher.utils.game.GameLaunchUtil.launchGameWithConfiguration(selectedGameModule, jsonConfigurationData, isAutoLaunch);
        
        if (!configSuccess) {
            Logging.error("❌ Game configuration failed for: " + gameName);
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "Failed to configure game: " + gameName + ". Check the logs for details.");
                addUserMessage("❌ Failed to configure game: " + gameName);
            }
            return; // Exit early if configuration failed
        }
        
        // Get the JSON text for the ViewModel to check game mode
        String jsonText = jsonConfigurationController != null ? 
            jsonConfigurationController.getJsonInputEditor().getText().trim() : "";
        
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
        // Get selected game from game selection controller
        GameModule selectedGame = gameSelectionController != null ? 
            gameSelectionController.getSelectedGameModule() : selectedGameModule;
        
        // Validate that a game is selected
        if (selectedGame == null) {
            Logging.warning("⚠️ Launch button clicked but no game is selected");
            DialogUtil.showError("No Game Selected", 
                "Please select a game from the dropdown before launching.");
            addUserMessage("⚠️ Please select a game from the dropdown first");
            return;
        }
        
        Logging.info("🚀 Launch button clicked for game: " + selectedGame.getMetadata().getGameName());
        
        // Parse and validate JSON syntax from UI
        Map<String, Object> jsonConfigurationData = jsonConfigurationController != null ? 
            jsonConfigurationController.parseJsonConfigurationData() : null;
        if (jsonConfigurationData == null) {
            // Check if it's due to invalid JSON syntax (not empty input)
            String jsonText = jsonConfigurationController != null ? 
                jsonConfigurationController.getJsonInputEditor().getText().trim() : "";
            if (!jsonText.isEmpty()) {
                DialogUtil.showJsonError("Invalid JSON", "Please enter valid JSON configuration.");
                return; // Exit early if JSON syntax is invalid
            }
        }
        
        // Temporarily set selected game module for launch
        selectedGameModule = selectedGame;
        
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


    // ==================== UTILITY METHODS ====================
    
    /**
     * Add a message to the user message area with queue system.
     * 
     * @param userMessage The message to display to the user
     */
    private void addUserMessage(String userMessage) {
        if (messageManager != null) {
            messageManager.addMessage(userMessage);
        }
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
            if (jsonPersistenceManager != null) {
                // Save JSON content if persistence is enabled
                jsonPersistenceManager.saveJsonContent();
                
                // Save persistence toggle state
                jsonPersistenceManager.savePersistenceToggleState();
                
                // Save selected game
                if (selectedGameModule != null) {
                    jsonPersistenceManager.persistSelectedGame(selectedGameModule.getMetadata().getGameName());
                }
            }
            
            Logging.info("📋 Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("❌ Error saving application settings: " + e.getMessage(), e);
        }
    }

}  
