package launcher.ui_areas.lobby;

import gdk.api.GameModule;
import gdk.internal.Logging;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import launcher.ui_areas.lobby.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.lifecycle.startup.controller_initialization.ControllerInitialization;
import launcher.ui_areas.lobby.lifecycle.startup.controller_initialization.ViewModelInitialization;
import launcher.ui_areas.lobby.lifecycle.shutdown.LobbyShutdownManager;
import launcher.ui_areas.lobby.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;

/**
 * Controller for the GDK Game Lobby interface.
 * 
 * This class manages the main lobby interface where users can select and launch
 * game modules. It provides functionality for game selection, JSON configuration
 * module_source_validation, and game launching with proper error handling.
 * 
 * Key responsibilities:
 * - Manage the game selection interface
 * - Handle JSON configuration input and module_source_validation
 * - Coordinate game launching with the ViewModel
 * - Provide user feedback through messages and error pop_up_dialogs
 * - Manage the refresh of available game modules
 *
 * @author Clement Luo
 * @date July 25, 2025
 * @edited December 29, 2025       
 * @since Beta 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Game Selection Components
    @FXML ComboBox<GameModule> gameSelector;
    @FXML Button launchGameButton;
    @FXML Button refreshButton;
    @FXML private ProgressIndicator refreshProgressIndicator;
    @FXML Button settingsButton;
    
    // Message and Logging Components
    @FXML ScrollPane messageScrollPane;
    @FXML VBox messageContainer;
    
    // JSON Configuration Components
    @FXML VBox jsonInputEditorContainer;
    @FXML VBox jsonOutputEditorContainer;
    @FXML Button clearInputButton;
    @FXML Button clearOutputButton2;
    @FXML Button metadataRequestButton;
    @FXML Button sendMessageButton;
    @FXML JFXToggleButton jsonPersistenceToggle;
    
    // Application Control Components
    @FXML Button exitButton;
    @FXML Label statusLabel;
    @FXML ProgressBar loadingProgressBar;
    @FXML Label loadingStatusLabel;

    // ==================== DEPENDENCIES & STATE ====================
    
    /**
     * The ViewModel that manages application state and business logic
     */
    private GDKViewModel applicationViewModel;
    
    /**
     * Controller mode (set before initialization for auto-launch)
     */
    private ControllerMode controllerMode = ControllerMode.NORMAL;
    
    // ==================== MANAGERS ====================
    
    /**
     * Manager for handling message display queue
     */
    private MessageManager messageManager;
    
    /**
     * Manager for ui_loading animations
     */
    private LoadingAnimationManager loadingAnimationManager;
    
    /**
     * Manager for module load_modules checking
     */
    private ModuleCompilationChecker moduleCompilationChecker;
    
    /**
     * Manager for game launching operations
     */
    private GameLaunchingManager gameLaunchManager;
    
    /**
     * Manager for lobby shutdown operations
     */
    private LobbyShutdownManager lobbyShutdownManager;
    
    /**
     * Manager for lobby controller initialization
     */
    private ControllerInitialization initializationManager;
    
    /**
     * Manager for ViewModel initialization and updates
     */
    private ViewModelInitialization viewModelInitialization;
    
    // ==================== SUBCONTROLLERS ====================
    
    /**
     * Subcontroller for game selection UI area
     */
    private GameSelectionController gameSelectionController;
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Set the controller mode (AUTO_LAUNCH or NORMAL).
     * This should be called before initialize() for auto-launch mode.
     * 
     * @param mode The mode to set
     */
    public void setControllerMode(ControllerMode mode) {
        this.controllerMode = mode;
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
        // Skip GUI setup in AUTO_LAUNCH mode
        if (controllerMode == ControllerMode.AUTO_LAUNCH) {
            Logging.info("Skipping GUI setup for AUTO_LAUNCH mode");
            Logging.info("GDK Game Picker Controller initialized (AUTO_LAUNCH mode)");
            return;
        }
        
        initializationManager = new ControllerInitialization(this, this::addUserMessage);
        viewModelInitialization = new ViewModelInitialization(this, this::addUserMessage);
        
        ControllerInitialization.InitializationResult result = initializationManager.initialize(applicationViewModel);
        
        // Store all initialized components
        messageManager = result.messageManager();
        loadingAnimationManager = result.loadingAnimationManager();
        moduleCompilationChecker = result.moduleCompilationChecker();
        gameLaunchManager = result.gameLaunchManager();
        lobbyShutdownManager = result.lobbyShutdownManager();
        gameSelectionController = result.gameSelectionController();
        gameModuleRefreshManager = result.gameModuleRefreshManager();
        
        // Store the initialization result for later ViewModel updates
        lastInitializationResult = result;
    }
    
    /**
     * Store the last initialization result for ViewModel updates.
     */
    private ControllerInitialization.InitializationResult lastInitializationResult;

    // ==================== DEPENDENCY INJECTION ====================
    
    /**
     * Set the ViewModel reference for this controller.
     * 
     * @param applicationViewModel The ViewModel to use for business logic
     */
    public void setViewModel(GDKViewModel applicationViewModel) {
        this.applicationViewModel = applicationViewModel;
        
        // Update ViewModel in all components that need it
        if (viewModelInitialization != null && lastInitializationResult != null) {
            ControllerInitialization.InitializationResult updatedResult =
                viewModelInitialization.updateViewModel(applicationViewModel, lastInitializationResult);
            
            // Update all references with the new components
            moduleCompilationChecker = updatedResult.moduleCompilationChecker();
            gameLaunchManager = updatedResult.gameLaunchManager();
            gameSelectionController = updatedResult.gameSelectionController();
            gameModuleRefreshManager = updatedResult.gameModuleRefreshManager();
            
            // Update the stored result
            lastInitializationResult = updatedResult;
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
    
    // ==================== MESSAGING & COMMUNICATION ====================
    
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
    
    // ==================== LOADING ANIMATION MANAGEMENT ====================
    
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
    
    // ==================== MODULE DISCOVERY & REFRESH ====================
    
    /**
     * Manager for refreshing game modules
     */
    private GameModuleRefreshManager gameModuleRefreshManager;
    
    /**
     * Fast refresh that skips load_modules checks for faster startup.
     */
    public void refreshAvailableGameModulesFast() {
        if (gameModuleRefreshManager != null) {
            gameModuleRefreshManager.refreshAvailableGameModulesFast();
        }
    }
    
    // ==================== MODULE COMPILATION ====================
    
    /**
     * Reports load_modules failures from startup load_modules.
     * 
     * @param failures List of module names that failed to compile
     */
    public void reportStartupCompilationFailures(java.util.List<String> failures) {
        if (moduleCompilationChecker != null) {
            moduleCompilationChecker.reportCompilationFailures(failures);
        }
    }
    
    // ==================== GETTERS FOR INITIALIZATION ====================
    // Public getters for ControllerInitialization to access UI components
    
    public ComboBox<GameModule> getGameSelector() { return gameSelector; }
    public Button getLaunchGameButton() { return launchGameButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getSettingsButton() { return settingsButton; }
    public ScrollPane getMessageScrollPane() { return messageScrollPane; }
    public VBox getMessageContainer() { return messageContainer; }
    public VBox getJsonInputEditorContainer() { return jsonInputEditorContainer; }
    public VBox getJsonOutputEditorContainer() { return jsonOutputEditorContainer; }
    public Button getClearInputButton() { return clearInputButton; }
    public Button getClearOutputButton2() { return clearOutputButton2; }
    public Button getMetadataRequestButton() { return metadataRequestButton; }
    public Button getSendMessageButton() { return sendMessageButton; }
    public JFXToggleButton getJsonPersistenceToggle() { return jsonPersistenceToggle; }
    public Button getExitButton() { return exitButton; }
    public Label getStatusLabel() { return statusLabel; }
    public ProgressBar getLoadingProgressBar() { return loadingProgressBar; }
    public Label getLoadingStatusLabel() { return loadingStatusLabel; }
    
    /**
     * Check for load_modules failures on startup.
     * 
     * @deprecated Use {@link #reportStartupCompilationFailures(List)} instead
     */
    @Deprecated
    public void checkStartupCompilationFailures() {
        // Deprecated - failures are now passed directly from load_modules process
    }
    
    // ==================== GAME LAUNCHING ====================
    
    /**
     * Launch a specific game module with saved JSON configuration.
     * This method is designed for auto-launch functionality.
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameWithSavedJson(GameModule gameModule, String savedJson) {
        if (gameLaunchManager != null) {
            return gameLaunchManager.launchGameWithSavedJson(gameModule, savedJson);
        }
        return false;
    }
    
    // ==================== NAVIGATION & LIFECYCLE ====================
    
    /**
     * Handle application shutdown and save settings.
     */
    public void onApplicationShutdown() {
        if (lobbyShutdownManager != null) {
            lobbyShutdownManager.handleShutdown();
        }
    }
}  
