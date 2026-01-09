package launcher.core.lifecycle.start.launch;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.lifecycle.startup.LobbyStartup;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.features.development.ProgramDelay;

/**
 * Handles the standard launch flow for the GDK application.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public final class StandardLaunchProcess {

    private StandardLaunchProcess() {}

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
    public static void launch(Stage primaryApplicationStage, StartupWindow windowManager) {
        try {
            // Single development delay point for debugging startup window
            ProgramDelay.delay("Startup process beginning");

            // Initialize the user interface and start module loading
            LobbyStartup.startStandardLaunch(primaryApplicationStage, windowManager);

            Logging.info("GDK application startup completed successfully");
            
        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to ui_initialization GDK application", startupError);
        }
    }
}

