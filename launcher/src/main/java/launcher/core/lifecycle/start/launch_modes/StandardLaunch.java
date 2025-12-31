package launcher.core.lifecycle.start.launch_modes;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui.lobby.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.ui.lobby.LobbyUIInitializer;
import launcher.features.module_handling.LoadModules;
import launcher.ui.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;

/**
 * Handles the standard launch path for the GDK application.
 * 
 * Standard launch initializes the full GDK interface with game selection:
 * 1. Creates and displays the startup progress window
 * 2. Initializes the user interface
 * 3. Loads game modules in the background
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public final class StandardLaunch {

    private StandardLaunch() {}

    /**
     * Starts the standard GDK interface with game selection.
     * 
     * This method initializes the full GDK application:
     * 1. Creates and displays the startup progress window
     * 2. Initializes the user interface
     * 3. Loads game modules in the background
     * 
     * @param primaryApplicationStage The primary JavaFX stage
     * @throws RuntimeException if the startup process fails
     */
    public static void launch(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        try {
            // Startup window is already shown by StartupProcess
            // Step 1: Update progress
            windowManager.updateProgress(0, "Starting GDK application");
            StartupDelayUtil.addDevelopmentDelay("After 'Starting GDK application' message");

            // Step 2: Initialize the user interface
            GDKGameLobbyController lobbyController = 
                LobbyUIInitializer.initialize(primaryApplicationStage, windowManager);
            StartupDelayUtil.addDevelopmentDelay("After 'Loading user interface' message");

            // Step 3: Start loading modules in the background
            LoadModules.load(primaryApplicationStage, lobbyController, windowManager);

            Logging.info("GDK application startup completed successfully");
            
        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
}

