package launcher.lifecycle.start;

import gdk.Logging;
import javafx.stage.Stage;
import launcher.gui.GDKGameLobbyController;

/**
 * Orchestrates the startup process of the GDK application.
 * 
 * @authors Clement Luo
 * @date August 8, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public class Startup {

    public static void start(Stage primaryApplicationStage) {
        Logging.info("Starting GDK application startup process");
        try {
            // 1. Progress window
            StartupWindowManager windowManager = StartupWindowManager.initializeWithCalculatedSteps();

            // 2. UI initialization
            GDKGameLobbyController lobbyController = UIInitializer.initialize(primaryApplicationStage, windowManager);

            // 3. Check readiness and show main stage
            StartupOperations.ensureUIReady(primaryApplicationStage, lobbyController, windowManager);
            StartupOperations.showMainStageWithFade(primaryApplicationStage, windowManager);

            Logging.info("GDK application startup completed successfully");
        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
} 