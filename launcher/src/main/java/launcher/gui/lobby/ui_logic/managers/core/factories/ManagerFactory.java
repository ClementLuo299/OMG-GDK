package launcher.gui.lobby.ui_logic.managers.core.factories;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.gui.lobby.ui_logic.managers.core.LobbyInitializationManager;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.LobbyShutdownManager;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.SettingsNavigationManager;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchErrorHandler;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchManager;
import launcher.gui.lobby.ui_logic.managers.game.GameModuleRefreshManager;
import launcher.gui.lobby.ui_logic.managers.game.ModuleChangeReporter;
import launcher.gui.lobby.ui_logic.managers.game.ModuleCompilationChecker;
import launcher.gui.lobby.ui_logic.managers.json.JsonEditorOperations;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageManager;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageBridgeManager;
import launcher.gui.lobby.ui_logic.managers.ui.LaunchButtonManager;
import launcher.gui.lobby.ui_logic.managers.ui.LoadingAnimationManager;
import launcher.gui.lobby.ui_logic.managers.ui.StatusLabelManager;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;

/**
 * Factory for creating all manager instances used in the lobby.
 * Encapsulates manager creation logic to reduce complexity in LobbyInitializationManager.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class ManagerFactory {
    
    /**
     * Result containing all created dependent managers.
     */
    public record ManagerCreationResult(
        MessageManager messageManager,
        LoadingAnimationManager loadingAnimationManager,
        JsonPersistenceManager jsonPersistenceManager,
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations, // May be null if not created here
        GameLaunchErrorHandler gameLaunchErrorHandler,
        StatusLabelManager statusLabelManager,
        LaunchButtonManager launchButtonManager,
        ModuleChangeReporter moduleChangeReporter,
        GameModuleRefreshManager gameModuleRefreshManager,
        GameLaunchManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyShutdownManager lobbyShutdownManager,
        SettingsNavigationManager settingsNavigationManager
    ) {}
    
    /**
     * Create managers that depend on subcontrollers.
     * Basic managers should be created separately before calling this method.
     * 
     * @param applicationViewModel The application ViewModel (may be null)
     * @param messageReporter Callback for reporting messages
     * @param controller The main lobby controller
     * @param exitButton The exit button
     * @param gameSelector The game selection ComboBox
     * @param availableGameModules The available game modules list
     * @param gameSelectionController The game selection controller
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param messageManager The message manager (already created)
     * @param statusLabelManager The status label manager (already created)
     * @param launchButtonManager The launch button manager (already created)
     * @param moduleChangeReporter The module change reporter (already created)
     * @param loadingAnimationManager The loading animation manager (already created)
     * @param moduleCompilationChecker The module compilation checker (already created)
     * @param gameLaunchErrorHandler The game launch error handler (already created)
     * @param jsonPersistenceManager The JSON persistence manager (already created)
     * @return Result containing all created dependent managers
     */
    public static ManagerCreationResult createDependentManagers(
            GDKViewModel applicationViewModel,
            LobbyInitializationManager.MessageReporter messageReporter,
            GDKGameLobbyController controller,
            Button exitButton,
            ComboBox<?> gameSelector,
            ObservableList<?> availableGameModules,
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            MessageManager messageManager,
            StatusLabelManager statusLabelManager,
            LaunchButtonManager launchButtonManager,
            ModuleChangeReporter moduleChangeReporter,
            LoadingAnimationManager loadingAnimationManager,
            ModuleCompilationChecker moduleCompilationChecker,
            GameLaunchErrorHandler gameLaunchErrorHandler,
            JsonPersistenceManager jsonPersistenceManager) {
        
        // Create refresh manager
        GameModuleRefreshManager gameModuleRefreshManager = new GameModuleRefreshManager(
            applicationViewModel,
            (ObservableList) availableGameModules,
            (ComboBox) gameSelector,
            messageManager,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter,
            loadingAnimationManager,
            moduleCompilationChecker
        );
        
        // Create managers that depend on subcontrollers
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, jsonActionButtonsController, gameLaunchErrorHandler);
        MessageBridgeManager messageBridgeManager = new MessageBridgeManager(jsonActionButtonsController);
        LobbyShutdownManager lobbyShutdownManager = new LobbyShutdownManager(jsonPersistenceManager, gameSelectionController);
        
        // Create SettingsNavigationManager with lazy stage supplier
        SettingsNavigationManager settingsNavigationManager = new SettingsNavigationManager(controller, () -> {
            if (exitButton != null && exitButton.getScene() != null) {
                return (Stage) exitButton.getScene().getWindow();
            }
            return null;
        });
        
        return new ManagerCreationResult(
            messageManager,
            loadingAnimationManager,
            jsonPersistenceManager,
            moduleCompilationChecker,
            null, // jsonEditorOperations - not created here
            gameLaunchErrorHandler,
            statusLabelManager,
            launchButtonManager,
            moduleChangeReporter,
            gameModuleRefreshManager,
            gameLaunchManager,
            messageBridgeManager,
            lobbyShutdownManager,
            settingsNavigationManager
        );
    }
}

