package launcher.lifecycle.start.module_loading.startup;

import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;

import javax.swing.SwingUtilities;

/**
 * Handles startup completion and final progress updates.
 * Manages the "Startup complete" and "Ready!" messages.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class StartupCompletionHandler {
    
    private StartupCompletionHandler() {}
    
    /**
     * Marks the startup process as complete in the progress window.
     * This must run on the Swing Event Dispatch Thread (EDT) since
     * StartupWindowManager is a Swing component.
     * 
     * @param windowManager The progress window to update (Swing component)
     * @param totalSteps Total number of progress steps
     * @param completeStep The step number to use for "Startup complete"
     */
    public static void markStartupComplete(StartupWindowManager windowManager, int totalSteps, int completeStep) {
        // Step 1: Show "Startup complete" message (must run on Swing EDT)
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(completeStep, "Startup complete");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Startup complete' message");
        
        // Step 2: Show final "Ready!" message to reach 100% progress
        // Use the next consecutive step, capped at totalSteps to ensure it reaches 100%
        final int readyStep = Math.min(completeStep + 1, totalSteps); // Cap at totalSteps (the maximum)
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(readyStep, "Ready!");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Ready!' message");
    }
}

