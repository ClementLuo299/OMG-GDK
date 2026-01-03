package launcher.ui_areas.lobby.factories;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import launcher.features.game_launching.GameLaunchService;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.lifecycle.LobbyShutdownManager;
import launcher.ui_areas.lobby.lifecycle.SettingsNavigationManager;
import launcher.ui_areas.lobby.game_launching.GameLaunchErrorHandler;
import launcher.ui_areas.lobby.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.game_launching.ModuleChangesReporter;
import launcher.ui_areas.lobby.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.messaging.MessageBridgeManager;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;

/**
 * Factory for creating more_managers that depend on subcontrollers.
 * These more_managers must be created after subcontrollers are available.
 * Basic more_managers (that don't depend on subcontrollers) are created by BasicManagerFactory.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class DependentManagerFactory {
    
    /**
     * Result containing only the more_managers created by this factory.
     * These are the more_managers that depend on subcontrollers.
     */
    public record DependentManagerCreationResult(
        GameModuleRefreshManager gameModuleRefreshManager,
        GameLaunchingManager gameLaunchManager,
        MessageBridgeManager messageBridgeManager,
        LobbyShutdownManager lobbyShutdownManager,
        SettingsNavigationManager settingsNavigationManager
    ) {}
    
    /**
     * Create more_managers that depend on subcontrollers.
     * Basic more_managers should be created by BasicManagerFactory before calling this method.
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
     * @param loadingAnimationManager The ui_loading animation manager (already created by BasicManagerFactory)
     * @param moduleCompilationChecker The module compilation checker (already created by BasicManagerFactory)
     * @param gameLaunchErrorHandler The game launch error handler (already created by BasicManagerFactory)
     * @param jsonPersistenceManager The JSON persistence manager (already created by BasicManagerFactory)
     * @return Result containing only the dependent more_managers created by this factory
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

