package launcher.features.module_handling.startup;

import gdk.internal.Logging;
import javafx.application.Platform;
import launcher.ui.lobby.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.ui.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;

import javax.swing.SwingUtilities;

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
     * The progress update must run on the Swing EDT, but the controller check
     * must run on the JavaFX thread.
     * 
     * @param lobbyController The controller to check for issues
     * @param windowManager The progress window to update (Swing component)
     * @param step The step number to use for progress update
     */
    public static void checkForCompilationIssues(GDKGameLobbyController lobbyController, 
                                                StartupWindowManager windowManager, 
                                                int step) {
        // Step 1: Update progress window (must run on Swing EDT since it's a Swing component)
        SwingUtilities.invokeLater(() -> {
            try {
                windowManager.updateProgress(step, "Checking for compilation issues");
            } catch (Exception e) {
                Logging.error("Error updating progress for compilation check: " + e.getMessage());
            }
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Checking for compilation issues' message");
        
        // Step 2: Check for compilation failures (must run on JavaFX thread for controller access)
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

