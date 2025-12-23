package launcher.lifecycle.start;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.utils.AutoLaunchUtil;

/**
 * Starts the startup process of the GDK application.
 * 
 * This class acts as the main entry point and coordinator for two startup paths:
 * 1. Auto-launch: Automatically launches a previously selected game (via {@link AutoLaunchStartup})
 * 2. Normal launch: Shows the GDK interface for game selection (via {@link NormalLaunchStartup})
 * 
 * The startup process first attempts auto-launch if enabled, then falls back to
 * normal launch if auto-launch is disabled or fails.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public final class StartupProcess {

    private StartupProcess() {}

    /**
     * Starts the GDK application startup process.
     * 
     * This method starts the startup process of the GDK application.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     */
    public static void start(Stage primaryApplicationStage) {
        try {
            // Attempt auto-launch first
            boolean autoLaunchEnabled = AutoLaunchUtil.isAutoLaunchEnabled();
            Runnable normalLaunchCallback = () -> NormalLaunchStartup.launch(primaryApplicationStage);
            
            if (autoLaunchEnabled && AutoLaunchStartup.launch(primaryApplicationStage, normalLaunchCallback)) {
                Logging.info("Auto-launch successful");
                return; // Exit - primary stage is now configured and showing the game
            }

            // Fall back to normal startup
            Logging.info("Auto-launch failed or disabled - proceeding with normal startup");
            NormalLaunchStartup.launch(primaryApplicationStage);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
}
