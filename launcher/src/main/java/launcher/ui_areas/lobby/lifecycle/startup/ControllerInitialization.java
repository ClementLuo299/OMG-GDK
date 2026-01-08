package launcher.ui_areas.lobby.lifecycle.startup.controller_initialization;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import launcher.ui_areas.lobby.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.game_launching.ModuleChangesReporter;
import launcher.ui_areas.lobby.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.json_editor.JsonEditorOperations;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.messaging.MessageBridgeManager;
import launcher.ui_areas.lobby.factories.BasicManagerFactory;
import launcher.ui_areas.lobby.factories.DependentManagerFactory;
import launcher.ui_areas.lobby.factories.SubcontrollerFactory;
import launcher.ui_areas.lobby.factories.ViewModelDependentsFactory;
import launcher.ui_areas.lobby.lifecycle.startup.component_setup.callback_wiring.CallbackWiring;
import launcher.ui_areas.lobby.lifecycle.startup.component_setup.json_editor.JsonEditorSetup;
import launcher.ui_areas.lobby.lifecycle.startup.controller_initialization.ViewModelInitialization;
import launcher.ui_areas.lobby.lifecycle.shutdown.LobbyShutdownManager;
import launcher.ui_areas.settings_page.SettingsNavigationManager;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.shared.fonts.FontLoader;

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
 * @author Clement Luo
 * @date December 29, 2025  
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public class ControllerInitialization {
    
    private final GDKGameLobbyController controller;
    private final MessageReporter messageReporter;
    
    /**
     * Interface for reporting messages to the UI.
     * This is a common interface used by multiple managers.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    /**
     * Create a new ControllerInitialization.
     * 
     * @param controller The main lobby controller
     * @param messageReporter Callback for reporting messages
     */
    public ControllerInitialization(GDKGameLobbyController controller, MessageReporter messageReporter) {
        this.controller = controller;
        this.messageReporter = messageReporter;
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize all lobby components.
     * Creates all managers, subcontrollers, wires callbacks, and sets up the UI.
     * Note: ViewModel may be null initially and will be set later via ViewModelInitialization.
     * 
     * @param applicationViewModel The application ViewModel (may be null initially)
     * @return Initialization result containing all managers and subcontrollers
     */
    public InitializationResult initialize(GDKViewModel applicationViewModel) {
        Logging.info("Initializing GDK Game Picker Controller");
        
        // ==================== CREATE JSON EDITORS ====================
        
        JsonEditor jsonInputEditor = new JsonEditor("JSON Input");
        JsonEditor jsonOutputEditor = new JsonEditor("JSON Output");
        
        // ==================== CREATE BASIC MANAGERS ====================
        // Basic managers don't depend on subcontrollers and are needed by them
        
        BasicManagerFactory.BasicManagerCreationResult basicManagers = BasicManagerFactory.createBasicManagers(
            applicationViewModel, messageReporter, controller.getMessageContainer(), controller.getMessageScrollPane(), controller.getRefreshButton(),
            controller.getLoadingProgressBar(), controller.getLoadingStatusLabel(), jsonInputEditor, jsonOutputEditor,
            controller.getJsonPersistenceToggle(), controller.getStatusLabel(), controller.getLaunchGameButton()
        );
        
        // ==================== CREATE SUBCONTROLLERS ====================
        // Subcontrollers depend on basic managers
        
        SubcontrollerFactory.SubcontrollerCreationResult subcontrollerResult = SubcontrollerFactory.createSubcontrollers(
            controller.getGameSelector(), controller.getLaunchGameButton(), jsonInputEditor, jsonOutputEditor, controller.getClearInputButton(),
            controller.getClearOutputButton2(), controller.getMetadataRequestButton(), controller.getSendMessageButton(), controller.getJsonPersistenceToggle(),
            controller.getExitButton(), controller.getRefreshButton(), controller.getSettingsButton(), basicManagers.messageManager(),
            basicManagers.launchButtonManager(), basicManagers.jsonEditorOperations()
        );
        
        // ==================== CREATE DEPENDENT MANAGERS ====================
        // These managers depend on subcontrollers
        
        DependentManagerFactory.DependentManagerCreationResult dependentManagers = DependentManagerFactory.createDependentManagers(
            applicationViewModel, controller, controller.getExitButton(), controller.getGameSelector(),
            subcontrollerResult.availableGameModules(), subcontrollerResult.gameSelectionController(),
            subcontrollerResult.jsonActionButtonsController(), basicManagers.messageManager(),
            basicManagers.statusLabelManager(), basicManagers.launchButtonManager(),
            basicManagers.moduleChangeReporter(), basicManagers.loadingAnimationManager(),
            basicManagers.moduleCompilationChecker(), basicManagers.gameLaunchErrorHandler(),
            jsonInputEditor, controller.getJsonPersistenceToggle()
        );
        
        // ==================== SETUP JSON EDITORS ====================
        
        JsonEditorSetup.setupEditorContainers(jsonInputEditor, jsonOutputEditor, controller.getJsonInputEditorContainer(), controller.getJsonOutputEditorContainer());
        
        // ==================== WIRE CALLBACKS ====================
        
        CallbackWiring.wireCallbacks(subcontrollerResult.gameSelectionController(), subcontrollerResult.jsonActionButtonsController(),
            subcontrollerResult.topBarController(), dependentManagers.gameLaunchManager(),
            dependentManagers.lobbyShutdownManager(), dependentManagers.settingsNavigationManager(),
            dependentManagers.gameModuleRefreshManager());
        
        // ==================== JSON PERSISTENCE LISTENER ====================
        
        JsonEditorSetup.setupPersistenceListener(jsonInputEditor, controller.getJsonPersistenceToggle());
        
        // ==================== POST-INITIALIZATION SETUP ====================
        
        // UI styling
        controller.getMessageContainer().setStyle(String.format("-fx-font-family: '%s', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;",
            FontLoader.getApplicationFontFamily()));
        
        // Message bridge setup
        dependentManagers.messageBridgeManager().subscribeToEndMessageMirror();
        
        // Persistence setup
        JsonPersistenceManager.load(jsonInputEditor, controller.getJsonPersistenceToggle());
        
        // UI state initialization
        basicManagers.statusLabelManager().updateGameCountStatus(subcontrollerResult.gameSelectionController().getAvailableGameModules().size());
        
        // Subcontroller initialization
        subcontrollerResult.gameSelectionController().initialize();
        subcontrollerResult.jsonActionButtonsController().initialize();
        subcontrollerResult.topBarController().initialize();
        
        Logging.info("GDK Game Picker Controller initialized successfully");
        
        // ==================== BUILD RESULT ====================
        
        return new InitializationResult(jsonInputEditor, jsonOutputEditor, basicManagers.messageManager(),
            basicManagers.loadingAnimationManager(), basicManagers.moduleCompilationChecker(),
            basicManagers.jsonEditorOperations(), basicManagers.statusLabelManager(),
            basicManagers.launchButtonManager(), basicManagers.moduleChangeReporter(),
            dependentManagers.gameLaunchManager(), dependentManagers.messageBridgeManager(),
            dependentManagers.lobbyShutdownManager(), dependentManagers.settingsNavigationManager(),
            subcontrollerResult.gameSelectionController(), subcontrollerResult.jsonActionButtonsController(),
            subcontrollerResult.topBarController(),             dependentManagers.gameModuleRefreshManager());
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
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations,
        StatusLabelManager statusLabelManager,
        LaunchButtonManager launchButtonManager,
        ModuleChangesReporter moduleChangeReporter,
        GameLaunchingManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyShutdownManager lobbyShutdownManager,
        SettingsNavigationManager settingsNavigationManager,
        GameSelectionController gameSelectionController,
        JsonActionButtonsController jsonActionButtonsController,
        TopBarController topBarController,
        GameModuleRefreshManager gameModuleRefreshManager
    ) {}
}

