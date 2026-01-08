package launcher.ui_areas.lobby.lifecycle.startup.component_setup.callback_wiring;

import gdk.api.GameModule;
import launcher.ui_areas.lobby.lifecycle.shutdown.LobbyShutdownManager;
import launcher.ui_areas.settings_page.SettingsNavigationManager;
import launcher.ui_areas.lobby.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;

/**
 * Handles wiring of callbacks between subcontrollers and managers.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 6, 2026
 * @since Beta 1.0
 */
public final class CallbackWiring {
    
    private CallbackWiring() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Wire up callbacks between subcontrollers and managers.
     * Connects UI events to business logic handlers.
     * 
     * @param gameSelectionController The game selection controller
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param topBarController The top bar controller
     * @param gameLaunchManager The game launch manager
     * @param lobbyShutdownManager The lobby shutdown manager
     * @param settingsNavigationManager The settings navigation manager
     * @param gameModuleRefreshManager The game module refresh manager
     */
    public static void wireCallbacks(
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            TopBarController topBarController,
            GameLaunchingManager gameLaunchManager,
            LobbyShutdownManager lobbyShutdownManager,
            SettingsNavigationManager settingsNavigationManager,
            GameModuleRefreshManager gameModuleRefreshManager) {
        
        // Game selection callbacks
        gameSelectionController.setOnLaunchGame(() -> {
            GameModule selectedGame = gameSelectionController.getSelectedGameModule();
            gameLaunchManager.launchGameFromUI(selectedGame);
        });
        
        gameSelectionController.setOnGameSelected(() -> {
            jsonActionButtonsController.setSelectedGameModule(gameSelectionController.getSelectedGameModule());
        });
        
        // Top bar callbacks
        topBarController.setOnExit(lobbyShutdownManager::handleShutdown);
        topBarController.setOnRefresh(gameModuleRefreshManager::handleRefresh);
        topBarController.setOnOpenSettings(settingsNavigationManager::openSettingsPage);
    }
}

