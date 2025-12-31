package launcher.ui.lobby.gui.lobby.ui_logic.managers.core.factories;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import launcher.features.game_launching.GameLaunchService;
import launcher.ui.lobby.gui.lobby.business.GDKViewModel;
import launcher.ui.lobby.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.core.lifecycle.LobbyShutdownManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.core.lifecycle.SettingsNavigationManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching.GameLaunchErrorHandler;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching.GameLaunchingManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching.GameModuleRefreshManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching.ModuleChangesReporter;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching.ModuleCompilationChecker;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.messaging.MessageManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.messaging.MessageBridgeManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.ui.LaunchButtonManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.ui.LoadingAnimationManager;
import launcher.ui.lobby.gui.lobby.ui_logic.managers.ui.StatusLabelManager;
import launcher.ui.lobby.gui.lobby.business.JsonPersistenceManager;
import launcher.ui.lobby.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.ui.lobby.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;

/**
 * Factory for creating managers that depend on subcontrollers.
 * These managers must be created after subcontrollers are available.
 * Basic managers (that don't depend on subcontrollers) are created by BasicManagerFactory.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class DependentManagerFactory {
    
    /**
     * Result containing only the managers created by this factory.
     * These are the managers that depend on subcontrollers.
     */
    public record DependentManagerCreationResult(
        GameModuleRefreshManager gameModuleRefreshManager,
        GameLaunchingManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyShutdownManager lobbyShutdownManager,
        SettingsNavigationManager settingsNavigationManager
    ) {}
    
    /**
     * Create managers that depend on subcontrollers.
     * Basic managers should be created by BasicManagerFactory before calling this method.
     * 
     * @param applicationViewModel The application ViewModel (may be null)
     * @param controller The main lobby controller
     * @param exitButton The exit button
     * @param gameSelector The game selection ComboBox
     * @param availableGameModules The available game modules list
     * @param gameSelectionController The game selection controller
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param messageManager The message manager (already created by BasicManagerFactory)
     * @param statusLabelManager The status label manager (already created by BasicManagerFactory)
     * @param launchButtonManager The launch button manager (already created by BasicManagerFactory)
     * @param moduleChangeReporter The module change reporter (already created by BasicManagerFactory)
     * @param loadingAnimationManager The loading animation manager (already created by BasicManagerFactory)
     * @param moduleCompilationChecker The module compilation checker (already created by BasicManagerFactory)
     * @param gameLaunchErrorHandler The game launch error handler (already created by BasicManagerFactory)
     * @param jsonPersistenceManager The JSON persistence manager (already created by BasicManagerFactory)
     * @return Result containing only the dependent managers created by this factory
     */
    public static DependentManagerCreationResult createDependentManagers(
            GDKViewModel applicationViewModel,
            GDKGameLobbyController controller,
            Button exitButton,
            ComboBox<?> gameSelector,
            ObservableList<?> availableGameModules,
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            MessageManager messageManager,
            StatusLabelManager statusLabelManager,
            LaunchButtonManager launchButtonManager,
            ModuleChangesReporter moduleChangeReporter,
            LoadingAnimationManager loadingAnimationManager,
            ModuleCompilationChecker moduleCompilationChecker,
            GameLaunchErrorHandler gameLaunchErrorHandler,
            JsonPersistenceManager jsonPersistenceManager) {
        
        // ==================== CREATE DEPENDENT MANAGERS ====================
        
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
        
        // Create business service for game launching
        GameLaunchService gameLaunchService = new GameLaunchService(applicationViewModel);
        GameLaunchingManager gameLaunchManager = new GameLaunchingManager(gameLaunchService, jsonActionButtonsController, gameLaunchErrorHandler);
        MessageBridgeManager messageBridgeManager = new MessageBridgeManager(jsonActionButtonsController);
        LobbyShutdownManager lobbyShutdownManager = new LobbyShutdownManager(jsonPersistenceManager, gameSelectionController);
        
        SettingsNavigationManager settingsNavigationManager = new SettingsNavigationManager(controller, () -> {
            if (exitButton != null && exitButton.getScene() != null) {
                return (Stage) exitButton.getScene().getWindow();
            }
            return null;
        });
        
        return new DependentManagerCreationResult(
            gameModuleRefreshManager,
            gameLaunchManager,
            messageBridgeManager,
            lobbyShutdownManager,
            settingsNavigationManager
        );
    }
}

