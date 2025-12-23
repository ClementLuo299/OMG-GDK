package launcher.lifecycle.start.module_loading.thread;

import gdk.internal.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.gui.GDKGameLobbyController;
import launcher.lifecycle.start.module_loading.progress.ModuleLoadingProgressManager;
import launcher.lifecycle.start.module_loading.steps.ModuleDiscoverySteps;
import launcher.lifecycle.start.module_loading.steps.ModuleLoadingSteps;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;
import launcher.utils.StartupTransitionUtil;

import javax.swing.SwingUtilities;

/**
 * Handles the background thread that loads game modules during startup.
 * 
 * The thread performs these steps:
 * 1. Loads all game modules (discovery, compilation, loading)
 * 2. Checks for compilation issues (displays warnings before UI update)
 * 3. Updates the UI with loaded games (on JavaFX thread)
 * 4. Marks startup as complete
 * 5. Shows the main stage and hides the startup window
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingThread {

    private ModuleLoadingThread() {}

    /**
     * Creates and configures the background thread that loads modules.
     * 
     * @param primaryApplicationStage The primary stage to show when ready
     * @param lobbyController The controller to update with loaded games
     * @param windowManager The progress window manager
     * @param totalSteps Total number of progress steps
     * @return The configured but not-yet-started thread
     */
    public static Thread create(Stage primaryApplicationStage,
                                GDKGameLobbyController lobbyController, 
                                StartupWindowManager windowManager, 
                                int totalSteps) {

        return new Thread(() -> {
            try {
                Logging.info("Starting module loading on background thread");
                
                // PHASE 1: Load all game modules
                int currentStep = loadModulesWithProgress(windowManager, totalSteps);
                Logging.info("Module loading completed");
                StartupDelayUtil.addDevelopmentDelay("After module loading process completed");
                
                // PHASE 2: Check for compilation issues 
                // Always move forward: ensure consecutive steps
                // Reserve last 3 steps: compilation (totalSteps-2), complete (totalSteps-1), ready (totalSteps)
                int compilationStep = Math.max(currentStep + 1, totalSteps - 2);
                compilationStep = Math.min(compilationStep, totalSteps - 2); // Cap at reserved step
                checkForCompilationIssues(lobbyController, windowManager, totalSteps, compilationStep);
                StartupDelayUtil.addDevelopmentDelay("After checking for compilation issues");
                
                // PHASE 3: Update UI with loaded games
                updateUIWithLoadedGames(lobbyController);
                StartupDelayUtil.addDevelopmentDelay("After refreshing game modules");
                
                // PHASE 4: Mark startup as complete
                // Always move forward from compilation step - ensure consecutive steps
                int completeStep = compilationStep + 1;
                completeStep = Math.min(completeStep, totalSteps - 1); // Cap at reserved step
                markStartupComplete(windowManager, totalSteps, completeStep);
                StartupDelayUtil.addDevelopmentDelay("After startup complete and ready");
                
                // PHASE 5: Show main stage and hide startup window
                Logging.info("All startup tasks complete - showing main stage");
                StartupTransitionUtil.showMainStage(primaryApplicationStage, windowManager);
                
                Logging.info("Module loading thread completed successfully");
                
            } catch (Exception e) {
                handleModuleLoadingError(e, primaryApplicationStage, windowManager);
            }
        });
    }
    
    /**
     * Handles errors that occur during module loading.
     * Attempts to show the main stage even if errors occurred.
     * 
     * @param error The exception that occurred
     * @param primaryApplicationStage The primary stage to show
     * @param windowManager The startup window manager
     */
    private static void handleModuleLoadingError(Exception error, 
                                                 Stage primaryApplicationStage, 
                                                 StartupWindowManager windowManager) {
        Logging.error("Critical error in module loading thread: " + error.getMessage(), error);
        
        // Even on error, try to show the main stage
        try {
            StartupTransitionUtil.showMainStage(primaryApplicationStage, windowManager);
        } catch (Exception showError) {
            Logging.error("Failed to show main stage after error: " + showError.getMessage());
        }
    }
    
    /**
     * Updates the UI with the loaded game modules.
     * This must run on the JavaFX Application Thread.
     * 
     * @param lobbyController The controller to refresh with loaded games
     */
    private static void updateUIWithLoadedGames(GDKGameLobbyController lobbyController) {
        Logging.info("Scheduling UI refresh on JavaFX thread...");
        Platform.runLater(() -> {
            try {
                Logging.info("Refreshing available game modules in UI...");
                if (lobbyController != null) {
                    lobbyController.refreshAvailableGameModulesFast();
                    Logging.info("UI refreshed with loaded games");
                } else {
                    Logging.error("Lobby controller is null - cannot refresh UI!");
                }
            } catch (Exception e) {
                Logging.error("Error refreshing game modules: " + e.getMessage(), e);
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Checks for compilation failures and displays warnings if needed.
     * The progress update must run on the Swing EDT, but the controller check
     * must run on the JavaFX thread.
     * 
     * @param lobbyController The controller to check for issues
     * @param windowManager The progress window to update (Swing component)
     * @param totalSteps Total number of progress steps
     * @param step The step number to use (already calculated to ensure forward progression)
     */
    private static void checkForCompilationIssues(GDKGameLobbyController lobbyController, 
                                                  StartupWindowManager windowManager, 
                                                  int totalSteps,
                                                  int step) {
        // Update progress window with the calculated step
        SwingUtilities.invokeLater(() -> {
            try {
                windowManager.updateProgress(step, "Checking for compilation issues...");
            } catch (Exception e) {
                Logging.error("Error updating progress for compilation check: " + e.getMessage());
            }
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Checking for compilation issues...' message");
        
        // Check compilation issues
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    lobbyController.checkStartupCompilationFailures();
                }
            } catch (Exception e) {
                Logging.error("Error checking compilation issues: " + e.getMessage());
            }
        });
    }
    
    /**
     * Marks the startup process as complete in the progress window.
     * This must run on the Swing Event Dispatch Thread (EDT) since
     * StartupWindowManager is a Swing component.
     * 
     * @param windowManager The progress window to update (Swing component)
     * @param totalSteps Total number of progress steps
     * @param completeStep The step number to use for "Startup complete" (already calculated to ensure forward progression)
     */
    private static void markStartupComplete(StartupWindowManager windowManager, int totalSteps, int completeStep) {
        // Update with the calculated step for "Startup complete"
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(completeStep, "Startup complete");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Startup complete' message");
        
        // Final step: use the next consecutive step for "Ready!" to ensure smooth progression
        // Use totalSteps to ensure it reaches 100%
        final int readyStep = Math.min(completeStep + 1, totalSteps); // Cap at totalSteps (the maximum)
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(readyStep, "Ready!");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Ready!' message");
    }

    /**
     * Loads game modules with progress updates.
     * Called from the module loading thread.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param totalSteps The total number of steps in the startup process
     * @return The current step number after module loading completes
     */
    private static int loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        // Start at step 3 because:
        // - Step 0: "Starting GDK application..."
        // - Step 1: "Loading user interface..."
        // - Step 2: "Starting module loading..."
        // - Step 3+: Module loading steps
        ModuleLoadingProgressManager progressManager = new ModuleLoadingProgressManager(windowManager, 3, totalSteps);
        
        try {
            Logging.info("Starting module loading process with " + totalSteps + " total steps");
            
            // Initialize and check build status
            ModuleLoadingSteps.initializeModuleLoading(progressManager);
            
            // Discover modules
            progressManager.updateProgressWithDelay("Discovering modules...", 
                "After 'Discovering modules...' message");
            ModuleDiscoverySteps.DiscoveryResult discoveryResult = 
                ModuleDiscoverySteps.discover();
            
            // Handle discovery errors
            if (!discoveryResult.isSuccess()) {
                progressManager.updateProgress(discoveryResult.getErrorMessage());
                StartupDelayUtil.addDevelopmentDelay("After discovery error");
            } else if (discoveryResult.getValidModuleDirectories().isEmpty()) {
                Logging.info("No modules discovered - continuing without modules");
            } else {
                StartupDelayUtil.addDevelopmentDelay("After module discovery completed - found " + 
                    discoveryResult.getValidModuleDirectories().size() + " modules");
                
                // Load modules
                ModuleLoadingSteps.executeLoading(progressManager, discoveryResult, totalSteps);
            }
            
            // Finalize
            ModuleLoadingSteps.finalizeModuleLoading(progressManager);
            
            // Return the current step after finalization
            return progressManager.getCurrentStep();
            
        } catch (Exception e) {
            handleModuleLoadingException(progressManager, e);
            // Return current step even on error
            return progressManager.getCurrentStep();
        }
    }
    
    /**
     * Handles exceptions during module loading.
     * 
     * @param progressManager The progress manager
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(ModuleLoadingProgressManager progressManager,
                                                    Exception e) {
        Logging.error("Critical error during module loading: " + e.getMessage(), e);
        e.printStackTrace();
        progressManager.updateProgress("Error during module loading - continuing...");
        StartupDelayUtil.addDevelopmentDelay("After 'Error during module loading' message");
    }
}

