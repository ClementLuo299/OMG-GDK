package launcher.gui.lobby.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.GDKGameLobbyController;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.subcontrollers.ApplicationControlController;
import launcher.gui.lobby.subcontrollers.GameSelectionController;
import launcher.gui.lobby.subcontrollers.JsonConfigurationController;
import launcher.utils.module.ModuleCompiler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import javafx.stage.Stage;

/**
 * Manages initialization of the lobby controller and all its components.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class LobbyInitializationManager {
    
    private final GDKGameLobbyController controller;
    private final MessageReporter messageReporter;
    
    /**
     * Interface for reporting messages to the UI.
     * This is a common interface used by multiple managers.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    // FXML Components
    private final ComboBox<GameModule> gameSelector;
    private final Button launchGameButton;
    private final Button refreshButton;
    private final Button settingsButton;
    private final ScrollPane messageScrollPane;
    private final VBox messageContainer;
    private final VBox jsonInputEditorContainer;
    private final VBox jsonOutputEditorContainer;
    private final Button clearInputButton;
    private final Button clearOutputButton2;
    private final Button metadataRequestButton;
    private final Button sendMessageButton;
    private final JFXToggleButton jsonPersistenceToggle;
    private final Button exitButton;
    private final Label statusLabel;
    private final ProgressBar loadingProgressBar;
    private final Label loadingStatusLabel;
    
    public LobbyInitializationManager(
            GDKGameLobbyController controller,
            MessageReporter messageReporter,
            ComboBox<GameModule> gameSelector,
            Button launchGameButton,
            Button refreshButton,
            Button settingsButton,
            ScrollPane messageScrollPane,
            VBox messageContainer,
            VBox jsonInputEditorContainer,
            VBox jsonOutputEditorContainer,
            Button clearInputButton,
            Button clearOutputButton2,
            Button metadataRequestButton,
            Button sendMessageButton,
            JFXToggleButton jsonPersistenceToggle,
            Button exitButton,
            Label statusLabel,
            ProgressBar loadingProgressBar,
            Label loadingStatusLabel) {
        this.controller = controller;
        this.messageReporter = messageReporter;
        this.gameSelector = gameSelector;
        this.launchGameButton = launchGameButton;
        this.refreshButton = refreshButton;
        this.settingsButton = settingsButton;
        this.messageScrollPane = messageScrollPane;
        this.messageContainer = messageContainer;
        this.jsonInputEditorContainer = jsonInputEditorContainer;
        this.jsonOutputEditorContainer = jsonOutputEditorContainer;
        this.clearInputButton = clearInputButton;
        this.clearOutputButton2 = clearOutputButton2;
        this.metadataRequestButton = metadataRequestButton;
        this.sendMessageButton = sendMessageButton;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
        this.exitButton = exitButton;
        this.statusLabel = statusLabel;
        this.loadingProgressBar = loadingProgressBar;
        this.loadingStatusLabel = loadingStatusLabel;
    }
    
    /**
     * Initialize all lobby components.
     * 
     * @param applicationViewModel The application ViewModel (may be null initially)
     * @return Initialization result containing all managers and subcontrollers
     */
    public InitializationResult initialize(GDKViewModel applicationViewModel) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        // Create JSON editors first (needed for managers)
        JsonEditor jsonInputEditor = new JsonEditor("JSON Input");
        JsonEditor jsonOutputEditor = new JsonEditor("JSON Output");
        
        // Initialize managers
        MessageManager messageManager = new MessageManager(messageContainer, messageScrollPane);
        LoadingAnimationManager loadingAnimationManager = new LoadingAnimationManager(refreshButton, loadingProgressBar, loadingStatusLabel);
        JsonPersistenceManager jsonPersistenceManager = new JsonPersistenceManager(jsonInputEditor, jsonPersistenceToggle);
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(applicationViewModel, messageReporter::addMessage);
        JsonConfigurationHandler jsonConfigurationHandler = new JsonConfigurationHandler(applicationViewModel, jsonInputEditor, jsonOutputEditor, messageReporter::addMessage);
        UIStateManager uiStateManager = new UIStateManager(statusLabel, launchGameButton, messageReporter::addMessage);
        
        // Initialize subcontrollers
        ObservableList<GameModule> availableGameModules = FXCollections.observableArrayList();
        GameSelectionController gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            refreshButton,
            settingsButton,
            availableGameModules,
            applicationViewModel,
            messageManager,
            uiStateManager,
            loadingAnimationManager,
            moduleCompilationChecker,
            jsonPersistenceManager
        );
        
        JsonConfigurationController jsonConfigurationController = new JsonConfigurationController(
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
        
        ApplicationControlController applicationControlController = new ApplicationControlController(
            exitButton,
            settingsButton,
            statusLabel,
            loadingProgressBar,
            loadingStatusLabel
        );
        
        // Initialize managers that depend on subcontrollers
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, jsonConfigurationController, messageManager);
        MessageBridgeManager messageBridgeManager = new MessageBridgeManager(jsonConfigurationController, jsonConfigurationHandler);
        LobbyLifecycleManager lobbyLifecycleManager = new LobbyLifecycleManager(jsonPersistenceManager, gameSelectionController);
        
        // Initialize SettingsNavigationManager with lazy stage supplier
        SettingsNavigationManager settingsNavigationManager = new SettingsNavigationManager(controller, () -> {
            if (exitButton != null && exitButton.getScene() != null) {
                return (Stage) exitButton.getScene().getWindow();
            }
            return null;
        });
        
        // Wire up callbacks
        wireCallbacks(gameSelectionController, jsonConfigurationController, applicationControlController,
                     gameLaunchManager, lobbyLifecycleManager, settingsNavigationManager);
        
        // Set up the UI components
        setupUserInterface();
        
        // Mirror game 'end' messages into JSON output
        messageBridgeManager.subscribeToEndMessageMirror();
        
        // Register controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(controller);
        
        // Load saved JSON content and toggle state
        jsonPersistenceManager.loadPersistenceSettings();
        
        // Initialize the status label
        if (uiStateManager != null && gameSelectionController != null) {
            uiStateManager.updateGameCountStatus(gameSelectionController.getAvailableGameModules().size());
        }
        
        // Initialize all subcontrollers
        gameSelectionController.initialize();
        jsonConfigurationController.initialize();
        applicationControlController.initialize();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
        
        return new InitializationResult(
            jsonInputEditor,
            jsonOutputEditor,
            messageManager,
            loadingAnimationManager,
            jsonPersistenceManager,
            moduleCompilationChecker,
            jsonConfigurationHandler,
            uiStateManager,
            gameLaunchManager,
            messageBridgeManager,
            lobbyLifecycleManager,
            settingsNavigationManager,
            gameSelectionController,
            jsonConfigurationController,
            applicationControlController
        );
    }
    
    /**
     * Wire up callbacks between subcontrollers and managers.
     */
    private void wireCallbacks(
            GameSelectionController gameSelectionController,
            JsonConfigurationController jsonConfigurationController,
            ApplicationControlController applicationControlController,
            GameLaunchManager gameLaunchManager,
            LobbyLifecycleManager lobbyLifecycleManager,
            SettingsNavigationManager settingsNavigationManager) {
        
        // Set callbacks for game selection controller
        gameSelectionController.setOnLaunchGame(() -> {
            GameModule selectedGame = gameSelectionController.getSelectedGameModule();
            if (gameLaunchManager != null) {
                gameLaunchManager.launchGameFromUI(selectedGame);
            }
        });
        gameSelectionController.setOnOpenSettings(() -> {
            if (settingsNavigationManager != null) {
                settingsNavigationManager.openSettingsPage();
            }
        });
        gameSelectionController.setOnGameSelected(() -> {
            // Update JSON configuration controller with selected game
            jsonConfigurationController.setSelectedGameModule(gameSelectionController.getSelectedGameModule());
        });
        
        // Set callbacks for application control controller
        applicationControlController.setOnExit(() -> {
            if (lobbyLifecycleManager != null) {
                lobbyLifecycleManager.handleShutdown();
            }
        });
        applicationControlController.setOnOpenSettings(() -> {
            if (settingsNavigationManager != null) {
                settingsNavigationManager.openSettingsPage();
            }
        });
    }
    
    /**
     * Set up all user interface components.
     */
    private void setupUserInterface() {
        // Apply consistent styling to the message container for better readability
        messageContainer.setStyle("-fx-font-family: 'Inter', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        Logging.info("🎨 UI components initialized");
    }
    
    /**
     * Update the game launch manager with a new ViewModel.
     */
    public GameLaunchManager updateGameLaunchManager(GDKViewModel applicationViewModel,
                                                     JsonConfigurationController jsonConfigurationController,
                                                     MessageManager messageManager) {
        return new GameLaunchManager(applicationViewModel, jsonConfigurationController, messageManager);
    }
    
    /**
     * Update ViewModel references in all components that need it.
     * Since some components have final ViewModel fields, they need to be recreated.
     * 
     * @param applicationViewModel The ViewModel to set
     * @param currentResult The current initialization result
     * @return Updated initialization result with new ViewModel references
     */
    public InitializationResult updateViewModel(GDKViewModel applicationViewModel, InitializationResult currentResult) {
        // Recreate components with final ViewModel fields
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(applicationViewModel, messageReporter::addMessage);
        JsonConfigurationHandler jsonConfigurationHandler = new JsonConfigurationHandler(applicationViewModel, 
            currentResult.jsonInputEditor(), currentResult.jsonOutputEditor(), messageReporter::addMessage);
        
        // Recreate GameSelectionController with new ViewModel (using stored UI component references)
        GameSelectionController gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            refreshButton,
            settingsButton,
            currentResult.gameSelectionController().getAvailableGameModules(),
            applicationViewModel,
            currentResult.messageManager(),
            currentResult.uiStateManager(),
            currentResult.loadingAnimationManager(),
            moduleCompilationChecker,
            currentResult.jsonPersistenceManager()
        );
        
        // Recreate GameLaunchManager with new ViewModel
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, 
            currentResult.jsonConfigurationController(), currentResult.messageManager());
        
        // Wire up callbacks again with new components
        wireCallbacks(gameSelectionController, currentResult.jsonConfigurationController(), 
            currentResult.applicationControlController(), gameLaunchManager, 
            currentResult.lobbyLifecycleManager(), currentResult.settingsNavigationManager());
        
        // Initialize the recreated subcontroller
        gameSelectionController.initialize();
        
        return new InitializationResult(
            currentResult.jsonInputEditor(),
            currentResult.jsonOutputEditor(),
            currentResult.messageManager(),
            currentResult.loadingAnimationManager(),
            currentResult.jsonPersistenceManager(),
            moduleCompilationChecker,
            jsonConfigurationHandler,
            currentResult.uiStateManager(),
            gameLaunchManager,
            currentResult.messageBridgeManager(),
            currentResult.lobbyLifecycleManager(),
            currentResult.settingsNavigationManager(),
            gameSelectionController,
            currentResult.jsonConfigurationController(),
            currentResult.applicationControlController()
        );
    }
    
    /**
     * Result of initialization containing all created managers and subcontrollers.
     */
    public record InitializationResult(
        JsonEditor jsonInputEditor,
        JsonEditor jsonOutputEditor,
        MessageManager messageManager,
        LoadingAnimationManager loadingAnimationManager,
        JsonPersistenceManager jsonPersistenceManager,
        ModuleCompilationChecker moduleCompilationChecker,
        JsonConfigurationHandler jsonConfigurationHandler,
        UIStateManager uiStateManager,
        GameLaunchManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyLifecycleManager lobbyLifecycleManager,
        SettingsNavigationManager settingsNavigationManager,
        GameSelectionController gameSelectionController,
        JsonConfigurationController jsonConfigurationController,
        ApplicationControlController applicationControlController
    ) {}
}

