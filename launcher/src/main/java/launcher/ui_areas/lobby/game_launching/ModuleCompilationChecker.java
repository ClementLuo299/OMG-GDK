package launcher.ui_areas.lobby.game_launching;

import gdk.internal.Logging;
import launcher.features.module_handling.compilation.CompilationFailures;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles checking and reporting of module compilation failures.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Checking for compilation failures (delegates to ModuleCompiler)</li>
 *   <li>Reporting compilation failures to the UI via messages</li>
 * </ul>
 * 
 * <p>The business logic of checking for failures is delegated to ModuleCompiler,
 * while this class handles the UI reporting aspect.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 1, 2026
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
     * Checks for compilation failures on startup and reports them to the UI.
     * 
 * <p>This method:
 * <ol>
 *   <li>Delegates compilation checking to ModuleCompiler</li>
 *   <li>Reports any failures to the UI via the MessageReporter</li>
 * </ol>
     * 
     * <p>UI updates are scheduled on the JavaFX application helpers to ensure helpers safety.
     */
    public void checkStartupCompilationFailures() {
        try {
            Logging.info("Checking for compilation failures on startup...");
            
            // Get compilation failures from ModuleCompiler
            List<String> compilationFailures = CompilationFailures.check();
            
            // Report failures to UI if any were found
            if (!compilationFailures.isEmpty()) {
                reportFailuresToUI(compilationFailures);
            }
            
            Logging.info("Startup compilation failure check completed");
            
        } catch (Exception e) {
            Logging.error("Error during startup compilation failure check: " + e.getMessage(), e);
        }
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

