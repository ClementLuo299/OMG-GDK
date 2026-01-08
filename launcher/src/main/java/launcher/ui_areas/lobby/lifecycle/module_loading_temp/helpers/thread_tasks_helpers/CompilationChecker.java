package launcher.ui_areas.lobby.lifecycle.module_loading_temp.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui_areas.lobby.GDKGameLobbyController;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for loading failures and displays warnings.
 * Handles both Swing EDT (progress updates) and JavaFX thread (controller checks).
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CompilationChecker {
    
    /** Temporary storage for loading failures from startup loading. */
    private static List<String> startupFailures = new ArrayList<>();
    
    private CompilationChecker() {}
    
    /**
     * Stores loading failures from startup loading for later reporting.
     * 
     * @param failures List of module names that failed to compile
     */
    public static void storeStartupFailures(List<String> failures) {
        startupFailures = failures != null ? new ArrayList<>(failures) : new ArrayList<>();
    }
    
    /**
     * Checks for loading failures and displays warnings if needed.
     * The controller check must run on the JavaFX thread.
     * 
     * @param lobbyController The controller to check for issues
     */
    public static void checkForCompilationIssues(GDKGameLobbyController lobbyController) {
        // Check for loading failures (must run on JavaFX thread for controller directory_access)
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    // Report failures from startup loading
                    lobbyController.reportStartupCompilationFailures(startupFailures);
                    // Clear stored failures after reporting
                    startupFailures.clear();
                }
            } catch (Exception e) {
                Logging.error("Error checking loading issues: " + e.getMessage());
            }
        });
    }
}

