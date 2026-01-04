package launcher.ui_areas.lobby.game_launching;

import gdk.internal.Logging;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles reporting of module compilation failures to the UI.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Reporting compilation failures to the UI via messages</li>
 * </ul>
 * 
 * <p>Failures should be obtained from module loading operations (e.g., from
 * {@link launcher.features.module_handling.discovery.ModuleDiscovery.ModuleLoadResult})
 * and passed to this class for UI reporting.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public class ModuleCompilationChecker {
    
    // ==================== INTERFACES ====================
    
    /**
     * Interface for reporting messages to the UI.
     * Allows this checker to report compilation failures without direct UI dependencies.
     */
    public interface MessageReporter {
        /**
         * Adds a message to be displayed to the user.
         * 
         * @param message The message to display
         */
        void addMessage(String message);
    }
    
    // ==================== DEPENDENCIES ====================
    
    /** Callback for reporting messages to the UI. */
    private final MessageReporter messageReporter;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleCompilationChecker.
     * 
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleCompilationChecker(MessageReporter messageReporter) {
        this.messageReporter = messageReporter;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Reports compilation failures to the UI.
     * 
     * <p>This method reports the provided compilation failures to the UI
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
            Logging.info("Reporting " + compilationFailures.size() + " compilation failure(s) to UI");
            reportFailuresToUI(compilationFailures);
        } catch (Exception e) {
            Logging.error("Error reporting compilation failures: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks for compilation failures on startup and reports them to the UI.
     * 
     * @deprecated Use {@link #reportCompilationFailures(List)} with failures from module loading instead
     */
    @Deprecated
    public void checkStartupCompilationFailures() {
        // This method is deprecated - failures should be obtained from module loading operations
        Logging.warning("checkStartupCompilationFailures() is deprecated - use reportCompilationFailures() instead");
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Reports compilation failures to the UI on the JavaFX helpers.
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
                messageReporter.addMessage(message);
            }
            
            // Report summary message
            String summaryMessage = "Compilation failures detected in: " + String.join(", ", failuresForUI);
            messageReporter.addMessage(summaryMessage);
        });
    }
}

