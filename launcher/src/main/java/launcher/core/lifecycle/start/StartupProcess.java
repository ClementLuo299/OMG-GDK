package launcher.core.lifecycle.start;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.core.lifecycle.start.launch_modes.AutoLaunch;
import launcher.core.lifecycle.start.launch_modes.StandardLaunch;
import launcher.startup_window.StartupWindowManager;
import launcher.utils.AutoLaunchUtil;

/**
 * Orchestrates the startup process of the GDK application.
 * 
 * This class coordinates the startup sequence:
 * 1. Shows the startup progress window
 * 2. Determines and executes the appropriate launch mode (auto-launch or standard)
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 24, 2025
 * @since Beta 1.0
 */
public final class StartupProcess {

    private StartupProcess() {}

    /**
     * Orchestrates the GDK application startup process.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @throws RuntimeException if the startup process fails
     */
    public static void start(Stage primaryApplicationStage) {

        Logging.info("Beginning GDK application startup process");
        
        // Show startup window
        StartupWindowManager windowManager = StartupWindowManager.createAndShow();
        
        // Execute launch mode
        try {

            // Attempt auto-launch
            if (AutoLaunchUtil.isAutoLaunchEnabled() && 
                AutoLaunch.launch(primaryApplicationStage, () -> StandardLaunch.launch(primaryApplicationStage, windowManager))) {
                Logging.info("Auto-launch successful");
                return;
            }
            
            // Fall back to standard launch
            Logging.info("Proceeding with standard launch");
            StandardLaunch.launch(primaryApplicationStage, windowManager);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
}
