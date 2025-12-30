package launcher.gui.lobby.ui_logic.managers.core;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.subcontrollers.TopBarController;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.gui.lobby.ui_logic.managers.ui.LaunchButtonManager;
import launcher.gui.lobby.ui_logic.managers.ui.StatusLabelManager;
import launcher.gui.lobby.ui_logic.managers.ui.LoadingAnimationManager;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchManager;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchErrorHandler;
import launcher.gui.lobby.ui_logic.managers.game.GameModuleRefreshManager;
import launcher.gui.lobby.ui_logic.managers.game.ModuleChangeReporter;
import launcher.gui.lobby.ui_logic.managers.game.ModuleCompilationChecker;
import launcher.gui.lobby.ui_logic.managers.json.JsonEditorOperations;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageManager;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageBridgeManager;
import launcher.gui.lobby.ui_logic.managers.core.factories.BasicManagerFactory;
import launcher.gui.lobby.ui_logic.managers.core.factories.ManagerFactory;
import launcher.gui.lobby.ui_logic.managers.core.factories.SubcontrollerFactory;
import launcher.gui.lobby.ui_logic.managers.core.factories.ViewModelUpdateFactory;
import launcher.gui.lobby.ui_logic.managers.core.setup.CallbackWiring;
import launcher.gui.lobby.ui_logic.managers.core.setup.JsonEditorSetup;
import launcher.gui.lobby.ui_logic.managers.core.setup.PostInitializationSetup;
import launcher.gui.lobby.ui_logic.managers.core.setup.UiSetup;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.LobbyShutdownManager;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.SettingsNavigationManager;
import launcher.utils.module.ModuleCompiler;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Manages initialization of the lobby controller and all its components.
 * 
 * @authors Clement Luo
 * @date December 29, 2025  
 * @edited December 29, 2025
 * @since Beta 1.0
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
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new LobbyInitializationManager.
     * 
     * @param controller The main lobby controller
     * @param messageReporter Callback for reporting messages
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param refreshButton The refresh button
     * @param settingsButton The settings button
     * @param messageScrollPane The message scroll pane
     * @param messageContainer The message container VBox
     * @param jsonInputEditorContainer The JSON input editor container
     * @param jsonOutputEditorContainer The JSON output editor container
     * @param clearInputButton The clear input button
     * @param clearOutputButton2 The clear output button
     * @param metadataRequestButton The metadata request button
     * @param sendMessageButton The send message button
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param exitButton The exit button
     * @param statusLabel The status label
     * @param loadingProgressBar The loading progress bar
     * @param loadingStatusLabel The loading status label
     */
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
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize all lobby components.
     * Creates all managers, subcontrollers, wires callbacks, and sets up the UI.
     * 
     * @param applicationViewModel The application ViewModel (may be null initially)
     * @return Initialization result containing all managers and subcontrollers
     */
    public InitializationResult initialize(GDKViewModel applicationViewModel) {
        Logging.info("Initializing GDK Game Picker Controller");
        
        // Create JSON editors first (needed for managers)
        JsonEditor jsonInputEditor = new JsonEditor("JSON Input");
        JsonEditor jsonOutputEditor = new JsonEditor("JSON Output");
        
        // Create basic managers first (needed by subcontrollers)
        BasicManagerFactory.BasicManagerCreationResult basicManagers = BasicManagerFactory.createBasicManagers(
            applicationViewModel,
            messageReporter,
            messageContainer,
            messageScrollPane,
            refreshButton,
            loadingProgressBar,
            loadingStatusLabel,
            jsonInputEditor,
            jsonOutputEditor,
            jsonPersistenceToggle,
            statusLabel,
            launchGameButton
        );
        
        // Create subcontrollers (they need basic managers)
        SubcontrollerFactory.SubcontrollerCreationResult subcontrollerResult = SubcontrollerFactory.createSubcontrollers(
            gameSelector,
            launchGameButton,
            jsonInputEditor,
            jsonOutputEditor,
            clearInputButton,
            clearOutputButton2,
            metadataRequestButton,
            sendMessageButton,
            jsonPersistenceToggle,
            exitButton,
            refreshButton,
            settingsButton,
            basicManagers.messageManager(),
            basicManagers.launchButtonManager(),
            basicManagers.jsonPersistenceManager(),
            basicManagers.jsonEditorOperations()
        );
        
        // Create managers that depend on subcontrollers
        ManagerFactory.ManagerCreationResult managerResult = ManagerFactory.createDependentManagers(
            applicationViewModel,
            messageReporter,
            controller,
            exitButton,
            gameSelector,
            subcontrollerResult.availableGameModules(),
            subcontrollerResult.gameSelectionController(),
            subcontrollerResult.jsonActionButtonsController(),
            basicManagers.messageManager(),
            basicManagers.statusLabelManager(),
            basicManagers.launchButtonManager(),
            basicManagers.moduleChangeReporter(),
            basicManagers.loadingAnimationManager(),
            basicManagers.moduleCompilationChecker(),
            basicManagers.gameLaunchErrorHandler(),
            basicManagers.jsonPersistenceManager()
        );
        
        // Set up JSON editor containers
        JsonEditorSetup.setupEditorContainers(
            jsonInputEditor,
            jsonOutputEditor,
            jsonInputEditorContainer,
            jsonOutputEditorContainer
        );
        
        // Wire up callbacks
        CallbackWiring.wireCallbacks(
            subcontrollerResult.gameSelectionController(),
            subcontrollerResult.jsonActionButtonsController(),
            subcontrollerResult.topBarController(),
            managerResult.gameLaunchManager(),
            managerResult.lobbyShutdownManager(),
            managerResult.settingsNavigationManager(),
            managerResult.gameModuleRefreshManager()
        );
        
        // Set up the UI components
        UiSetup.setupUserInterface(messageContainer);
        
        // Set up JSON persistence auto-save listener
        JsonEditorSetup.setupPersistenceListener(jsonInputEditor, jsonPersistenceToggle, basicManagers.jsonPersistenceManager());
        
        // Perform post-initialization setup
        PostInitializationSetup.performSetup(
            controller,
            managerResult.messageBridgeManager(),
            basicManagers.jsonPersistenceManager(),
            basicManagers.statusLabelManager(),
            subcontrollerResult.gameSelectionController(),
            subcontrollerResult.jsonActionButtonsController(),
            subcontrollerResult.topBarController()
        );
        
        return new InitializationResult(
            jsonInputEditor,
            jsonOutputEditor,
            basicManagers.messageManager(),
            basicManagers.loadingAnimationManager(),
            basicManagers.jsonPersistenceManager(),
            basicManagers.moduleCompilationChecker(),
            basicManagers.jsonEditorOperations(),
            basicManagers.statusLabelManager(),
            basicManagers.launchButtonManager(),
            basicManagers.moduleChangeReporter(),
            managerResult.gameLaunchManager(),
            managerResult.messageBridgeManager(),
            managerResult.lobbyShutdownManager(),
            managerResult.settingsNavigationManager(),
            subcontrollerResult.gameSelectionController(),
            subcontrollerResult.jsonActionButtonsController(),
            subcontrollerResult.topBarController(),
            managerResult.gameModuleRefreshManager()
        );
    }
    
    // ==================== VIEWMODEL UPDATES ====================
    
    /**
     * Update ViewModel references in all components that need it.
     * Since some components have final ViewModel fields, they need to be recreated.
     * Called when the ViewModel becomes available after initial initialization.
     * 
     * @param applicationViewModel The ViewModel to set
     * @param currentResult The current initialization result
     * @return Updated initialization result with new ViewModel references
     */
    public InitializationResult updateViewModel(GDKViewModel applicationViewModel, InitializationResult currentResult) {
        // Update components with new ViewModel
        ViewModelUpdateFactory.ViewModelUpdateResult updateResult = ViewModelUpdateFactory.updateComponents(
            applicationViewModel,
            messageReporter,
            gameSelector,
            launchGameButton,
            refreshButton,
            loadingProgressBar,
            loadingStatusLabel,
            currentResult
        );
        
        // Wire up callbacks again with new components
        CallbackWiring.wireCallbacks(
            updateResult.gameSelectionController(),
            currentResult.jsonActionButtonsController(),
            currentResult.topBarController(),
            updateResult.gameLaunchManager(),
            currentResult.lobbyShutdownManager(),
            currentResult.settingsNavigationManager(),
            updateResult.gameModuleRefreshManager()
        );
        
        return new InitializationResult(
            currentResult.jsonInputEditor(),
            currentResult.jsonOutputEditor(),
            currentResult.messageManager(),
            updateResult.loadingAnimationManager(),
            currentResult.jsonPersistenceManager(),
            updateResult.moduleCompilationChecker(),
            updateResult.jsonEditorOperations(),
            currentResult.statusLabelManager(),
            currentResult.launchButtonManager(),
            currentResult.moduleChangeReporter(),
            updateResult.gameLaunchManager(),
            currentResult.messageBridgeManager(),
            currentResult.lobbyShutdownManager(),
            currentResult.settingsNavigationManager(),
            updateResult.gameSelectionController(),
            currentResult.jsonActionButtonsController(),
            currentResult.topBarController(),
            updateResult.gameModuleRefreshManager()
        );
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Result of initialization containing all created managers and subcontrollers.
     * Used to pass initialization results between methods and to the main controller.
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
        LobbyShutdownManager lobbyShutdownManager,
        SettingsNavigationManager settingsNavigationManager,
        GameSelectionController gameSelectionController,
        JsonActionButtonsController jsonActionButtonsController,
        TopBarController topBarController,
        GameModuleRefreshManager gameModuleRefreshManager
    ) {}
}

