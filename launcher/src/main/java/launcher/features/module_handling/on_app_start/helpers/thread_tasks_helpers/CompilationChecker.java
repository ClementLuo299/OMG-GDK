package launcher.features.module_handling.on_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui_areas.lobby.GDKGameLobbyController;

/**
 * Checks for compilation failures and displays warnings.
 * Handles both Swing EDT (progress updates) and JavaFX helpers (controller checks).
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
     * The controller check must run on the JavaFX helpers.
     * 
     * @param lobbyController The controller to check for issues
     */
    public static void checkForCompilationIssues(GDKGameLobbyController lobbyController) {
        // Check for compilation failures (must run on JavaFX helpers for controller access)
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

