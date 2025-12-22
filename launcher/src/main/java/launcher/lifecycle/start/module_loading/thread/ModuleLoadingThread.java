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
                loadModulesWithProgress(windowManager, totalSteps);
                Logging.info("Module loading completed");
                StartupDelayUtil.addDevelopmentDelay("After module loading process completed");
                
                // PHASE 2: Check for compilation issues 
                checkForCompilationIssues(lobbyController, windowManager, totalSteps);
                StartupDelayUtil.addDevelopmentDelay("After checking for compilation issues");
                
                // PHASE 3: Update UI with loaded games
                updateUIWithLoadedGames(lobbyController);
                StartupDelayUtil.addDevelopmentDelay("After refreshing game modules");
                
                // PHASE 4: Mark startup as complete
                markStartupComplete(windowManager, totalSteps);
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
     */
    private static void checkForCompilationIssues(GDKGameLobbyController lobbyController, 
                                                  StartupWindowManager windowManager, 
                                                  int totalSteps) {
        // Update progress window
        SwingUtilities.invokeLater(() -> {
            try {
                windowManager.updateProgress(totalSteps - 2, "Checking for compilation issues...");
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
     */
    private static void markStartupComplete(StartupWindowManager windowManager, int totalSteps) {
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(totalSteps - 1, "Startup complete");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Startup complete' message");
        
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(totalSteps, "Ready!");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Ready!' message");
    }

    /**
     * Loads game modules with progress updates.
     * Called from the module loading thread.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param totalSteps The total number of steps in the startup process
     */
    private static void loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        ModuleLoadingProgressManager progressManager = new ModuleLoadingProgressManager(windowManager, 1);
        
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
            
        } catch (Exception e) {
            handleModuleLoadingException(progressManager, e);
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

