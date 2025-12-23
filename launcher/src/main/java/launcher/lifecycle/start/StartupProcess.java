package launcher.lifecycle.start;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.lifecycle.start.launch_modes.AutoLaunch;
import launcher.lifecycle.start.launch_modes.StandardLaunch;
import launcher.utils.AutoLaunchUtil;

/**
 * Orchestrates the startup process of the GDK application.
 * 
 * This class acts as the main entry point and coordinator for two startup paths:
 * 1. Auto-launch: Automatically launches a previously selected game (via {@link launcher.lifecycle.start.launch_modes.AutoLaunch})
 * 2. Standard launch: Shows the GDK interface for game selection (via {@link launcher.lifecycle.start.launch_modes.StandardLaunch})
 * 
 * The startup process first attempts auto-launch if enabled, then falls back to
 * standard launch if auto-launch is disabled or fails.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public final class StartupProcess {

    private StartupProcess() {}

    /**
     * Orchestrates the GDK application startup process.
     * 
     * This method coordinates the startup sequence:
     * 1. Checks if auto-launch is enabled and attempts to auto-launch
     * 2. Falls back to standard GDK interface if auto-launch is disabled or fails
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @throws RuntimeException if the startup process fails
     */
    public static void start(Stage primaryApplicationStage) {
        Logging.info("Beginning GDK application startup process");
        
        try {
            // Attempt auto-launch first
            boolean autoLaunchEnabled = AutoLaunchUtil.isAutoLaunchEnabled();
            Runnable standardLaunchCallback = () -> StandardLaunch.launch(primaryApplicationStage);
            
            if (autoLaunchEnabled && AutoLaunch.launch(primaryApplicationStage, standardLaunchCallback)) {
                Logging.info("Auto-launch successful");
                return; // Exit - primary stage is now configured and showing the game
            }

            // Fall back to standard startup
            Logging.info("Auto-launch failed or disabled - proceeding with standard startup");
            StandardLaunch.launch(primaryApplicationStage);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
}
