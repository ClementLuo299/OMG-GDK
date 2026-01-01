package launcher.features.lobby_features.managers.core.setup;

import gdk.api.GameModule;
import launcher.features.lobby_features.managers.core.lifecycle.LobbyShutdownManager;
import launcher.features.lobby_features.managers.core.lifecycle.SettingsNavigationManager;
import launcher.ui_areas.lobby.managers.game_launching.GameLaunchingManager;
import launcher.features.lobby_features.managers.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;

/**
 * Handles wiring of callbacks between subcontrollers and managers.
 * Encapsulates callback wiring logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class CallbackWiring {
    
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
        
        // ==================== GAME SELECTION CALLBACKS ====================
        
        // Launch game callback
        gameSelectionController.setOnLaunchGame(() -> {
            GameModule selectedGame = gameSelectionController.getSelectedGameModule();
            if (gameLaunchManager != null) {
                gameLaunchManager.launchGameFromUI(selectedGame);
            }
        });
        
        // Game selection change callback - update JSON editor with selected game
        gameSelectionController.setOnGameSelected(() -> {
            jsonActionButtonsController.setSelectedGameModule(gameSelectionController.getSelectedGameModule());
        });
        
        // ==================== TOP BAR CALLBACKS ====================
        
        // Exit callback
        topBarController.setOnExit(() -> {
            if (lobbyShutdownManager != null) {
                lobbyShutdownManager.handleShutdown();
            }
        });
        
        // Refresh callback
        topBarController.setOnRefresh(() -> {
            if (gameModuleRefreshManager != null) {
                gameModuleRefreshManager.handleRefresh();
            }
        });
        
        // Settings callback
        topBarController.setOnOpenSettings(() -> {
            if (settingsNavigationManager != null) {
                settingsNavigationManager.openSettingsPage();
            }
        });
    }
}

