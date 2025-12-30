package launcher.gui.lobby.ui_logic.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.subcontrollers.TopBarController;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.gui.lobby.ui_logic.managers.JsonEditorOperations;
import launcher.gui.lobby.ui_logic.managers.StatusLabelManager;
import launcher.gui.lobby.ui_logic.managers.LaunchButtonManager;
import launcher.gui.lobby.ui_logic.managers.ModuleChangeReporter;
import launcher.gui.lobby.ui_logic.managers.GameModuleRefreshManager;
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
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(applicationViewModel, jsonInputEditor, jsonOutputEditor, messageReporter::addMessage);
        
        // Split UI state managers
        StatusLabelManager statusLabelManager = new StatusLabelManager(statusLabel);
        LaunchButtonManager launchButtonManager = new LaunchButtonManager(launchGameButton);
        ModuleChangeReporter moduleChangeReporter = new ModuleChangeReporter(messageReporter::addMessage);
        
        // Initialize subcontrollers
        ObservableList<GameModule> availableGameModules = FXCollections.observableArrayList();
        GameSelectionController gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            availableGameModules,
            messageManager,
            launchButtonManager,
            jsonPersistenceManager
        );
        
        // Create refresh manager
        GameModuleRefreshManager gameModuleRefreshManager = new GameModuleRefreshManager(
            applicationViewModel,
            availableGameModules,
            gameSelector,
            messageManager,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter,
            loadingAnimationManager,
            moduleCompilationChecker
        );
        
        // Set up JSON editor containers
        jsonInputEditorContainer.getChildren().add(jsonInputEditor);
        jsonOutputEditorContainer.getChildren().add(jsonOutputEditor);
        javafx.scene.layout.VBox.setVgrow(jsonInputEditorContainer, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonOutputEditorContainer, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonInputEditor, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonOutputEditor, javafx.scene.layout.Priority.ALWAYS);
        
        JsonActionButtonsController jsonActionButtonsController = new JsonActionButtonsController(
            jsonInputEditor,
            jsonOutputEditor,
            clearInputButton,
            clearOutputButton2,
            metadataRequestButton,
            sendMessageButton,
            jsonPersistenceToggle,
            jsonEditorOperations,
            jsonPersistenceManager,
            messageManager
        );
        
        TopBarController topBarController = new TopBarController(
            exitButton,
            refreshButton,
            settingsButton
        );
        
        // Initialize managers that depend on subcontrollers
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, jsonActionButtonsController, messageManager);
        MessageBridgeManager messageBridgeManager = new MessageBridgeManager(jsonActionButtonsController);
        LobbyLifecycleManager lobbyLifecycleManager = new LobbyLifecycleManager(jsonPersistenceManager, gameSelectionController);
        
        // Initialize SettingsNavigationManager with lazy stage supplier
        SettingsNavigationManager settingsNavigationManager = new SettingsNavigationManager(controller, () -> {
            if (exitButton != null && exitButton.getScene() != null) {
                return (Stage) exitButton.getScene().getWindow();
            }
            return null;
        });
        
        // Wire up callbacks
        wireCallbacks(gameSelectionController, jsonActionButtonsController, topBarController,
                     gameLaunchManager, lobbyLifecycleManager, settingsNavigationManager, gameModuleRefreshManager);
        
        // Set up the UI components
        setupUserInterface();
        
        // Mirror game 'end' messages into JSON output
        messageBridgeManager.subscribeToEndMessageMirror();
        
        // Register controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(controller);
        
        // Set up JSON persistence text change listener (auto-save)
        javafx.application.Platform.runLater(() -> {
            jsonInputEditor.textProperty().addListener((observable, oldValue, newValue) -> {
                if (jsonPersistenceToggle.isSelected()) {
                    jsonPersistenceManager.saveJsonContent();
                }
            });
        });
        
        // Load saved JSON content and toggle state
        jsonPersistenceManager.loadPersistenceSettings();
        
        // Initialize the status label
        if (statusLabelManager != null && gameSelectionController != null) {
            statusLabelManager.updateGameCountStatus(gameSelectionController.getAvailableGameModules().size());
        }
        
        // Initialize all subcontrollers
        gameSelectionController.initialize();
        jsonActionButtonsController.initialize();
        topBarController.initialize();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
        
        return new InitializationResult(
            jsonInputEditor,
            jsonOutputEditor,
            messageManager,
            loadingAnimationManager,
            jsonPersistenceManager,
            moduleCompilationChecker,
            jsonEditorOperations,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter,
            gameLaunchManager,
            messageBridgeManager,
            lobbyLifecycleManager,
            settingsNavigationManager,
            gameSelectionController,
            jsonActionButtonsController,
            topBarController,
            gameModuleRefreshManager
        );
    }
    
    /**
     * Wire up callbacks between subcontrollers and managers.
     */
    private void wireCallbacks(
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            TopBarController topBarController,
            GameLaunchManager gameLaunchManager,
            LobbyLifecycleManager lobbyLifecycleManager,
            SettingsNavigationManager settingsNavigationManager,
            GameModuleRefreshManager gameModuleRefreshManager) {
        
        // Set callbacks for game selection controller
        gameSelectionController.setOnLaunchGame(() -> {
            GameModule selectedGame = gameSelectionController.getSelectedGameModule();
            if (gameLaunchManager != null) {
                gameLaunchManager.launchGameFromUI(selectedGame);
            }
        });
        gameSelectionController.setOnGameSelected(() -> {
            // Update JSON configuration controller with selected game
            jsonActionButtonsController.setSelectedGameModule(gameSelectionController.getSelectedGameModule());
        });
        
        // Set callbacks for application control controller
        topBarController.setOnExit(() -> {
            if (lobbyLifecycleManager != null) {
                lobbyLifecycleManager.handleShutdown();
            }
        });
        topBarController.setOnRefresh(() -> {
            if (gameModuleRefreshManager != null) {
                gameModuleRefreshManager.handleRefresh();
            }
        });
        topBarController.setOnOpenSettings(() -> {
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
        // Use the same font as the startup window
        String fontFamily = launcher.utils.FontLoader.getApplicationFontFamily();
        gdk.internal.Logging.info("🎨 Message container font: " + fontFamily);
        String fontStyle = String.format("-fx-font-family: '%s', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;", fontFamily);
        messageContainer.setStyle(fontStyle);
        Logging.info("🎨 UI components initialized");
    }
    
    /**
     * Update the game launch manager with a new ViewModel.
     */
    public GameLaunchManager updateGameLaunchManager(GDKViewModel applicationViewModel,
                                                     JsonActionButtonsController jsonActionButtonsController,
                                                     MessageManager messageManager) {
        return new GameLaunchManager(applicationViewModel, jsonActionButtonsController, messageManager);
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
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(applicationViewModel, 
            currentResult.jsonInputEditor(), currentResult.jsonOutputEditor(), messageReporter::addMessage);
        
        // Recreate GameSelectionController (no ViewModel dependency)
        GameSelectionController gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            currentResult.gameSelectionController().getAvailableGameModules(),
            currentResult.messageManager(),
            currentResult.launchButtonManager(),
            currentResult.jsonPersistenceManager()
        );
        
        // Recreate GameModuleRefreshManager with new ViewModel
        GameModuleRefreshManager gameModuleRefreshManager = new GameModuleRefreshManager(
            applicationViewModel,
            currentResult.gameSelectionController().getAvailableGameModules(),
            gameSelector,
            currentResult.messageManager(),
            currentResult.statusLabelManager(),
            currentResult.launchButtonManager(),
            currentResult.moduleChangeReporter(),
            currentResult.loadingAnimationManager(),
            moduleCompilationChecker
        );
        
        // Recreate GameLaunchManager with new ViewModel
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, 
            currentResult.jsonActionButtonsController(), currentResult.messageManager());
        
        // Wire up callbacks again with new components
        wireCallbacks(gameSelectionController, currentResult.jsonActionButtonsController(), 
            currentResult.topBarController(), gameLaunchManager, 
            currentResult.lobbyLifecycleManager(), currentResult.settingsNavigationManager(),
            gameModuleRefreshManager);
        
        // Initialize the recreated subcontroller
        gameSelectionController.initialize();
        
        return new InitializationResult(
            currentResult.jsonInputEditor(),
            currentResult.jsonOutputEditor(),
            currentResult.messageManager(),
            currentResult.loadingAnimationManager(),
            currentResult.jsonPersistenceManager(),
            moduleCompilationChecker,
            jsonEditorOperations,
            currentResult.statusLabelManager(),
            currentResult.launchButtonManager(),
            currentResult.moduleChangeReporter(),
            gameLaunchManager,
            currentResult.messageBridgeManager(),
            currentResult.lobbyLifecycleManager(),
            currentResult.settingsNavigationManager(),
            gameSelectionController,
            currentResult.jsonActionButtonsController(),
            currentResult.topBarController(),
            gameModuleRefreshManager
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
        JsonEditorOperations jsonEditorOperations,
        StatusLabelManager statusLabelManager,
        LaunchButtonManager launchButtonManager,
        ModuleChangeReporter moduleChangeReporter,
        GameLaunchManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyLifecycleManager lobbyLifecycleManager,
        SettingsNavigationManager settingsNavigationManager,
        GameSelectionController gameSelectionController,
        JsonActionButtonsController jsonActionButtonsController,
        TopBarController topBarController,
        GameModuleRefreshManager gameModuleRefreshManager
    ) {}
}

