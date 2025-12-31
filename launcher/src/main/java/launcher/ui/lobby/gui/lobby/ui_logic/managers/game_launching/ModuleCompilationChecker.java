package launcher.ui.lobby.gui.lobby.ui_logic.managers.game_launching;

import gdk.internal.Logging;
import launcher.ui.lobby.gui.lobby.business.GDKViewModel;
import launcher.utils.module.ModuleCompiler;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles checking and reporting of module compilation failures.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Checking for compilation failures (delegates to ViewModel or ModuleCompiler)</li>
 *   <li>Reporting compilation failures to the UI via messages</li>
 * </ul>
 * 
 * <p>The business logic of checking for failures is delegated to the ViewModel
 * (or ModuleCompiler as a fallback), while this class handles the UI reporting aspect.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 30, 2025
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
    
    /** ViewModel for business logic (may be null initially). */
    private final GDKViewModel viewModel;
    
    /** Callback for reporting messages to the UI. */
    private final MessageReporter messageReporter;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleCompilationChecker.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleCompilationChecker(GDKViewModel viewModel, MessageReporter messageReporter) {
        this.viewModel = viewModel;
        this.messageReporter = messageReporter;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Checks for compilation failures on startup and reports them to the UI.
     * 
     * <p>This method:
     * <ol>
     *   <li>Delegates compilation checking to ViewModel (or ModuleCompiler as fallback)</li>
     *   <li>Reports any failures to the UI via the MessageReporter</li>
     * </ol>
     * 
     * <p>UI updates are scheduled on the JavaFX application thread to ensure thread safety.
     */
    public void checkStartupCompilationFailures() {
        try {
            Logging.info("Checking for compilation failures on startup...");
            
            // Get compilation failures from ViewModel or fallback to ModuleCompiler
            List<String> compilationFailures = getCompilationFailures();
            
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
     * Gets compilation failures from ViewModel or ModuleCompiler fallback.
     * 
     * @return List of module names that failed to compile
     */
    private List<String> getCompilationFailures() {
        if (viewModel != null) {
            // Use ViewModel for business logic
            return viewModel.checkForCompilationFailures();
        } else {
            // Fallback: get from ModuleCompiler directly
            List<String> failures = ModuleCompiler.getLastCompilationFailures();
            ModuleCompiler.clearCompilationFailures();
            return failures;
        }
    }
    
    /**
     * Reports compilation failures to the UI on the JavaFX thread.
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

