package launcher.ui_areas.lobby.game_launching;

import gdk.internal.Logging;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles reporting of module load_modules failures to the UI.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Reporting load_modules failures to the UI via messages</li>
 * </ul>
 * 
 * <p>Failures should be obtained from module load_modules operations (e.g., from
 * {@link launcher.features.module_handling.load_modules.LoadModules.ModuleLoadResult})
 * and passed to this class for UI reporting.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public class ModuleCompilationChecker {
    
    // ==================== DEPENDENCIES ====================
    
    /** Callback for reporting messages to the UI. */
    private final Consumer<String> messageReporter;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleCompilationChecker.
     * 
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleCompilationChecker(Consumer<String> messageReporter) {
        this.messageReporter = messageReporter;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Reports load_modules failures to the UI.
     * 
     * <p>This method reports the provided load_modules failures to the UI
     * via the MessageReporter. UI updates are scheduled on the JavaFX
     * application thread to ensure thread safety.
     * 
     * @param compilationFailures List of module names that failed to compile
     */
    public void reportCompilationFailures(List<String> compilationFailures) {
        if (compilationFailures == null || compilationFailures.isEmpty()) {
            return;
        }
        
        try {
            Logging.info("Reporting " + compilationFailures.size() + " load_modules failure(s) to UI");
            reportFailuresToUI(compilationFailures);
        } catch (Exception e) {
            Logging.error("Error reporting load_modules failures: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks for load_modules failures on startup and reports them to the UI.
     * 
     * @deprecated Use {@link #reportCompilationFailures(List)} with failures from module load_modules instead
     */
    @Deprecated
    public void checkStartupCompilationFailures() {
        // This method is deprecated - failures should be obtained from module load_modules operations
        Logging.warning("checkStartupCompilationFailures() is deprecated - use reportCompilationFailures() instead");
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Reports load_modules failures to the UI on the JavaFX steps.
     * 
     * @param compilationFailures List of module names that failed to compile
     */
    private void reportFailuresToUI(List<String> compilationFailures) {
        // Create a copy for safe use in Platform.runLater
        final List<String> failuresForUI = new ArrayList<>(compilationFailures);
        
        Platform.runLater(() -> {
            // Report each failure individually
            for (String moduleName : failuresForUI) {
                String message = "Module '" + moduleName + "' failed to compile - check source code for errors";
                messageReporter.accept(message);
            }
            
            // Report summary message
            String summaryMessage = "Compilation failures detected in: " + String.join(", ", failuresForUI);
            messageReporter.accept(summaryMessage);
        });
    }
}

