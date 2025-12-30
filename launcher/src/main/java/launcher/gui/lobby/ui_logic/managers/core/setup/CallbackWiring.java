package launcher.gui.lobby.ui_logic.managers.core.setup;

import gdk.api.GameModule;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.LobbyShutdownManager;
import launcher.gui.lobby.ui_logic.managers.core.lifecycle.SettingsNavigationManager;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchManager;
import launcher.gui.lobby.ui_logic.managers.game.GameModuleRefreshManager;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.gui.lobby.ui_logic.subcontrollers.TopBarController;

/**
 * Handles wiring of callbacks between subcontrollers and managers.
 * Encapsulates callback wiring logic to reduce complexity in LobbyInitializationManager.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
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
            GameLaunchManager gameLaunchManager,
            LobbyShutdownManager lobbyShutdownManager,
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
        
        // Set callbacks for top bar controller
        topBarController.setOnExit(() -> {
            if (lobbyShutdownManager != null) {
                lobbyShutdownManager.handleShutdown();
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
}

