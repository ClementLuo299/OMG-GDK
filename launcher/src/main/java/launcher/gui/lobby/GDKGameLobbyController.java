package launcher.gui.lobby;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.utils.module.ModuleCompiler;

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

import launcher.gui.lobby.managers.MessageManager;
import launcher.gui.lobby.managers.LoadingAnimationManager;
import launcher.gui.lobby.managers.JsonPersistenceManager;
import launcher.gui.lobby.managers.ModuleCompilationChecker;
import launcher.gui.lobby.managers.JsonConfigurationHandler;
import launcher.gui.lobby.managers.UIStateManager;
import launcher.gui.lobby.managers.GameLaunchManager;
import launcher.gui.lobby.managers.SettingsNavigationManager;
import launcher.gui.lobby.managers.MessageBridgeManager;
import launcher.gui.lobby.managers.LobbyLifecycleManager;
import launcher.gui.lobby.managers.LobbyInitializationManager;
import launcher.gui.lobby.subcontrollers.GameSelectionController;
import launcher.gui.lobby.subcontrollers.JsonConfigurationController;
import launcher.gui.lobby.subcontrollers.ApplicationControlController;


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
 * @edited December 28, 2025       
 * @since Beta 1.0
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

    // ==================== DEPENDENCIES & STATE ====================
    
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
    
    /**
     * Manager for game launching operations
     */
    private GameLaunchManager gameLaunchManager;
    
    /**
     * Manager for settings page navigation
     */
    private SettingsNavigationManager settingsNavigationManager;
    
    /**
     * Manager for messaging bridge subscriptions
     */
    private MessageBridgeManager messageBridgeManager;
    
    /**
     * Manager for lobby lifecycle operations
     */
    private LobbyLifecycleManager lobbyLifecycleManager;
    
    /**
     * Manager for lobby initialization
     */
    private LobbyInitializationManager initializationManager;
    
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
        Logging.info("Controller mode set to: " + mode);
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
        Logging.info("Initializing GDK Game Picker Controller (mode: " + controllerMode + ")");
        
        // Skip GUI setup in AUTO_LAUNCH mode
        if (controllerMode == ControllerMode.AUTO_LAUNCH) {
            Logging.info("⏭Skipping GUI setup for AUTO_LAUNCH mode");
            ModuleCompiler.setUIController(this);
            Logging.info("GDK Game Picker Controller initialized (AUTO_LAUNCH mode)");
            return;
        }
        
        // Normal mode: full GUI setup
        initializationManager = new LobbyInitializationManager(
            this,
            this::addUserMessage,
            gameSelector,
            launchGameButton,
            refreshButton,
            settingsButton,
            messageScrollPane,
            messageContainer,
            jsonInputEditorContainer,
            jsonOutputEditorContainer,
            clearInputButton,
            clearOutputButton2,
            metadataRequestButton,
            sendMessageButton,
            jsonPersistenceToggle,
            exitButton,
            statusLabel,
            loadingProgressBar,
            loadingStatusLabel
        );
        
        LobbyInitializationManager.InitializationResult result = initializationManager.initialize(applicationViewModel);
        
        // Store all initialized components
        messageManager = result.messageManager();
        loadingAnimationManager = result.loadingAnimationManager();
        jsonPersistenceManager = result.jsonPersistenceManager();
        moduleCompilationChecker = result.moduleCompilationChecker();
        jsonConfigurationHandler = result.jsonConfigurationHandler();
        uiStateManager = result.uiStateManager();
        gameLaunchManager = result.gameLaunchManager();
        messageBridgeManager = result.messageBridgeManager();
        lobbyLifecycleManager = result.lobbyLifecycleManager();
        settingsNavigationManager = result.settingsNavigationManager();
        gameSelectionController = result.gameSelectionController();
        jsonConfigurationController = result.jsonConfigurationController();
        applicationControlController = result.applicationControlController();
    }

    // ==================== DEPENDENCY INJECTION ====================
    
    /**
     * Set the ViewModel reference for this controller.
     * 
     * @param applicationViewModel The ViewModel to use for business logic
     */
    public void setViewModel(GDKViewModel applicationViewModel) {
        this.applicationViewModel = applicationViewModel;
        if (initializationManager != null && gameLaunchManager != null && jsonConfigurationController != null && messageManager != null) {
            gameLaunchManager = initializationManager.updateGameLaunchManager(applicationViewModel, jsonConfigurationController, messageManager);
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
     * Fast refresh that skips compilation checks for faster startup.
     */
    public void refreshAvailableGameModulesFast() {
        if (gameSelectionController != null) {
            gameSelectionController.refreshAvailableGameModulesFast();
        }
    }
    
    // ==================== MODULE COMPILATION ====================
    
    /**
     * Check for compilation failures on startup.
     */
    public void checkStartupCompilationFailures() {
        if (moduleCompilationChecker != null) {
            moduleCompilationChecker.checkStartupCompilationFailures();
        }
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
     * Open the settings page.
     */
    private void openSettingsPage() {
        if (settingsNavigationManager != null) {
            settingsNavigationManager.openSettingsPage();
        }
    }
    
    /**
     * Handle application shutdown and save settings.
     */
    public void onApplicationShutdown() {
        if (lobbyLifecycleManager != null) {
            lobbyLifecycleManager.handleShutdown();
        }
    }
}  
