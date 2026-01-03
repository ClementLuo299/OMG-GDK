package launcher.features.module_handling.on_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui_areas.lobby.GDKGameLobbyController;

/**
 * Handles JavaFX UI updates during startup.
 * Responsible for updating the UI with loaded game modules.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleUIUpdater {
    
    private ModuleUIUpdater() {}
    
    /**
     * Updates the UI with the loaded game modules.
     * This must run on the JavaFX Application Thread.
     * 
     * @param lobbyController The controller to refresh with loaded games
     */
    public static void updateUIWithLoadedGames(GDKGameLobbyController lobbyController) {
        // Schedule UI update to run on JavaFX Application Thread (required for UI operations)
        Logging.info("Scheduling UI refresh on JavaFX helpers...");
        Platform.runLater(() -> {
            try {
                Logging.info("Refreshing available game modules in UI...");
                if (lobbyController != null) {
                    // Refresh the game list in the UI with the newly loaded modules
                    lobbyController.refreshAvailableGameModulesFast();
                    Logging.info("UI refreshed with loaded games");
                } else {
                    Logging.error("Lobby controller is null - cannot refresh UI!");
                }
            } catch (Exception e) {
                // Log error but don't fail the startup process if UI refresh fails
                Logging.error("Error refreshing game modules: " + e.getMessage(), e);
                e.printStackTrace();
            }
        });
    }
}

