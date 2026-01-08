package launcher.ui_areas.lobby.factories;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.lifecycle.startup.controller_initialization.ControllerInitialization;
import launcher.ui_areas.lobby.game_launching.GameLaunchErrorHandler;
import launcher.ui_areas.lobby.game_launching.ModuleChangesReporter;
import launcher.ui_areas.lobby.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.json_editor.JsonEditorOperations;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;

/**
 * Factory for creating basic more_managers that don't depend on subcontrollers.
 * Encapsulates basic manager creation logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class BasicManagerFactory {
    
    /**
     * Result containing all created basic more_managers.
     */
    public record BasicManagerCreationResult(
        MessageManager messageManager,
        LoadingAnimationManager loadingAnimationManager,
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations,
        GameLaunchErrorHandler gameLaunchErrorHandler,
        StatusLabelManager statusLabelManager,
        LaunchButtonManager launchButtonManager,
        ModuleChangesReporter moduleChangeReporter
    ) {}
    
    /**
     * Create all basic more_managers that don't depend on subcontrollers.
     * 
     * @param applicationViewModel The application ViewModel (may be null)
     * @param messageReporter Callback for reporting messages
     * @param messageContainer The message container VBox
     * @param messageScrollPane The message scroll pane
     * @param refreshButton The refresh button
     * @param loadingProgressBar The ui_loading progress bar
     * @param loadingStatusLabel The ui_loading status label
     * @param jsonInputEditor The JSON input editor
     * @param jsonOutputEditor The JSON output editor
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param statusLabel The status label
     * @param launchGameButton The launch game button
     * @return Result containing all created basic more_managers
     */
    public static BasicManagerCreationResult createBasicManagers(
            GDKViewModel applicationViewModel,
            ControllerInitialization.MessageReporter messageReporter,
            VBox messageContainer,
            ScrollPane messageScrollPane,
            Button refreshButton,
            ProgressBar loadingProgressBar,
            Label loadingStatusLabel,
            JsonEditor jsonInputEditor,
            JsonEditor jsonOutputEditor,
            JFXToggleButton jsonPersistenceToggle,
            Label statusLabel,
            Button launchGameButton) {
        
        MessageManager messageManager = new MessageManager(messageContainer, messageScrollPane);
        LoadingAnimationManager loadingAnimationManager = new LoadingAnimationManager(refreshButton, loadingProgressBar, loadingStatusLabel);
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(messageReporter::addMessage);
        
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(jsonInputEditor, jsonOutputEditor, messageReporter::addMessage);
        GameLaunchErrorHandler gameLaunchErrorHandler = new GameLaunchErrorHandler(messageManager);
        StatusLabelManager statusLabelManager = new StatusLabelManager(statusLabel);
        LaunchButtonManager launchButtonManager = new LaunchButtonManager(launchGameButton);
        ModuleChangesReporter moduleChangeReporter = new ModuleChangesReporter(messageReporter::addMessage);
        
        return new BasicManagerCreationResult(
            messageManager,
            loadingAnimationManager,
            moduleCompilationChecker,
            jsonEditorOperations,
            gameLaunchErrorHandler,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter
        );
    }
}

