package launcher.features.module_handling.loading;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindowManager;
import launcher.features.development_features.StartupDelayUtil;

/**
 * Checks for compilation failures and displays warnings.
 * Handles both Swing EDT (progress updates) and JavaFX thread (controller checks).
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class CompilationChecker {
    
    private CompilationChecker() {}
    
    /**
     * Checks for compilation failures and displays warnings if needed.
     * The controller check must run on the JavaFX thread.
     * 
     * @param lobbyController The controller to check for issues
     * @param windowManager The startup window manager
     */
    public static void checkForCompilationIssues(GDKGameLobbyController lobbyController, 
                                                StartupWindowManager windowManager) {
        StartupDelayUtil.addDevelopmentDelay("After 'Checking for compilation issues' message");
        
        // Check for compilation failures (must run on JavaFX thread for controller access)
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    // Check if any modules failed to compile and show warnings if needed
                    lobbyController.checkStartupCompilationFailures();
                }
            } catch (Exception e) {
                Logging.error("Error checking compilation issues: " + e.getMessage());
            }
        });
    }
}

