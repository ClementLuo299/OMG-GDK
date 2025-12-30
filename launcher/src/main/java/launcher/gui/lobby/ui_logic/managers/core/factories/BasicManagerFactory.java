package launcher.gui.lobby.ui_logic.managers.core.factories;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.managers.core.LobbyInitializationManager;
import launcher.gui.lobby.ui_logic.managers.game_launching.GameLaunchErrorHandler;
import launcher.gui.lobby.ui_logic.managers.game_launching.ModuleChangesReporter;
import launcher.gui.lobby.ui_logic.managers.game_launching.ModuleCompilationChecker;
import launcher.gui.lobby.ui_logic.managers.json.JsonEditorOperations;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageManager;
import launcher.gui.lobby.ui_logic.managers.ui.LaunchButtonManager;
import launcher.gui.lobby.ui_logic.managers.ui.LoadingAnimationManager;
import launcher.gui.lobby.ui_logic.managers.ui.StatusLabelManager;

/**
 * Factory for creating basic managers that don't depend on subcontrollers.
 * Encapsulates basic manager creation logic to reduce complexity in LobbyInitializationManager.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class BasicManagerFactory {
    
    /**
     * Result containing all created basic managers.
     */
    public record BasicManagerCreationResult(
        MessageManager messageManager,
        LoadingAnimationManager loadingAnimationManager,
        JsonPersistenceManager jsonPersistenceManager,
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations,
        GameLaunchErrorHandler gameLaunchErrorHandler,
        StatusLabelManager statusLabelManager,
        LaunchButtonManager launchButtonManager,
        ModuleChangesReporter moduleChangeReporter
    ) {}
    
    /**
     * Create all basic managers that don't depend on subcontrollers.
     * 
     * @param applicationViewModel The application ViewModel (may be null)
     * @param messageReporter Callback for reporting messages
     * @param messageContainer The message container VBox
     * @param messageScrollPane The message scroll pane
     * @param refreshButton The refresh button
     * @param loadingProgressBar The loading progress bar
     * @param loadingStatusLabel The loading status label
     * @param jsonInputEditor The JSON input editor
     * @param jsonOutputEditor The JSON output editor
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param statusLabel The status label
     * @param launchGameButton The launch game button
     * @return Result containing all created basic managers
     */
    public static BasicManagerCreationResult createBasicManagers(
            GDKViewModel applicationViewModel,
            LobbyInitializationManager.MessageReporter messageReporter,
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
        JsonPersistenceManager jsonPersistenceManager = new JsonPersistenceManager(jsonInputEditor, jsonPersistenceToggle);
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(applicationViewModel, messageReporter::addMessage);
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(applicationViewModel, jsonInputEditor, jsonOutputEditor, messageReporter::addMessage);
        GameLaunchErrorHandler gameLaunchErrorHandler = new GameLaunchErrorHandler(messageManager);
        StatusLabelManager statusLabelManager = new StatusLabelManager(statusLabel);
        LaunchButtonManager launchButtonManager = new LaunchButtonManager(launchGameButton);
        ModuleChangesReporter moduleChangeReporter = new ModuleChangesReporter(messageReporter::addMessage);
        
        return new BasicManagerCreationResult(
            messageManager,
            loadingAnimationManager,
            jsonPersistenceManager,
            moduleCompilationChecker,
            jsonEditorOperations,
            gameLaunchErrorHandler,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter
        );
    }
}

