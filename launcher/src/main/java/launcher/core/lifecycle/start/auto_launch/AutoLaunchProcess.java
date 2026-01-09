package launcher.core.lifecycle.start.auto_launch;

import javafx.stage.Stage;
import launcher.ui_areas.lobby.lifecycle.startup.LobbyStartup;

/**
 * Handles the auto-launch flow for the GDK application.
 * 
 * Auto-launch attempts to restore and launch a previously selected game
 * using saved configuration data. If successful, the application skips
 * the standard GDK interface and goes directly to the game.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class AutoLaunchProcess {

    private AutoLaunchProcess() {
        // Utility class - prevent instantiation
    }

    /**
     * Attempts to auto-launch a game from saved state.
     * 
     * This method performs the following steps:
     * 1. Loads and validates saved auto-launch data
     * 2. Finds and loads the selected game module
     * 3. Creates and configures the controller/viewmodel
     * 4. Configures the primary stage for the game
     * 5. Sets up callback for returning to normal GDK
     * 6. Launches the game with saved configuration
     * 
     * @param primaryApplicationStage The primary JavaFX stage
     * @param normalLaunchCallback Callback to execute normal launch if auto-launch fails
     * @return true if auto-launch was successful, false otherwise
     */
    public static boolean launch(Stage primaryApplicationStage, Runnable normalLaunchCallback) {
        return LobbyStartup.startAutoLaunch(primaryApplicationStage, normalLaunchCallback);
    }
}

