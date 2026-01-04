package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui_areas.lobby.GDKGameLobbyController;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for compilation failures and displays warnings.
 * Handles both Swing EDT (progress updates) and JavaFX steps (controller checks).
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CompilationChecker {
    
    /** Temporary storage for compilation failures from startup loading. */
    private static List<String> startupFailures = new ArrayList<>();
    
    private CompilationChecker() {}
    
    /**
     * Stores compilation failures from startup loading for later reporting.
     * 
     * @param failures List of module names that failed to compile
     */
    public static void storeStartupFailures(List<String> failures) {
        startupFailures = failures != null ? new ArrayList<>(failures) : new ArrayList<>();
    }
    
    /**
     * Checks for compilation failures and displays warnings if needed.
     * The controller check must run on the JavaFX steps.
     * 
     * @param lobbyController The controller to check for issues
     */
    public static void checkForCompilationIssues(GDKGameLobbyController lobbyController) {
        // Check for compilation failures (must run on JavaFX steps for controller access)
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    // Report failures from startup loading
                    lobbyController.reportStartupCompilationFailures(startupFailures);
                    // Clear stored failures after reporting
                    startupFailures.clear();
                }
            } catch (Exception e) {
                Logging.error("Error checking compilation issues: " + e.getMessage());
            }
        });
    }
}

