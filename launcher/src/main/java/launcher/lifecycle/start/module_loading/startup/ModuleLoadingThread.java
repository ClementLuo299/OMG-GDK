package launcher.lifecycle.start.module_loading.startup;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;
import launcher.lifecycle.start.StartupTransitionUtil;

/**
 * Creates and manages the background thread that loads game modules during startup.
 * Focuses on thread creation, error handling, and coordinating startup phases.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 27, 2025
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
                
                // Phase 1: Load all game modules
                int currentStep = StartupWorkflow.executeWorkflow(windowManager, totalSteps);
                
                // Phase 2: Check for compilation issues
                // Always move forward: ensure consecutive steps
                // Reserve last 3 steps: compilation (totalSteps-2), complete (totalSteps-1), ready (totalSteps)
                int compilationStep = Math.max(currentStep + 1, totalSteps - 2);
                compilationStep = Math.min(compilationStep, totalSteps - 2); // Cap at reserved step
                CompilationChecker.checkForCompilationIssues(lobbyController, windowManager, compilationStep);
                StartupDelayUtil.addDevelopmentDelay("After checking for compilation issues");
                
                // Phase 3: Update UI with loaded games
                ModuleUIUpdater.updateUIWithLoadedGames(lobbyController);
                StartupDelayUtil.addDevelopmentDelay("After refreshing game modules");
                
                // Phase 4: Mark startup as complete
                // Always move forward from compilation step - ensure consecutive steps
                int completeStep = compilationStep + 1;
                completeStep = Math.min(completeStep, totalSteps - 1); // Cap at reserved step
                StartupCompletionHandler.markStartupComplete(windowManager, totalSteps, completeStep);
                StartupDelayUtil.addDevelopmentDelay("After startup complete and ready");
                
                // Phase 5: Show main stage and hide startup window
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
    
}

