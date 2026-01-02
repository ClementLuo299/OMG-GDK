package launcher.features.module_handling.startup;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.features.module_handling.StartupWorkflow;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindowManager;
import launcher.features.development_features.StartupDelayUtil;
import launcher.core.ui_features.ui_loading.stage.StartupWindowToMainStageTransition;

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
     * @param windowManager The startup window manager
     * @return The configured but not-yet-started thread
     */
    public static Thread create(Stage primaryApplicationStage,
                                GDKGameLobbyController lobbyController, 
                                StartupWindowManager windowManager) {

        return new Thread(() -> {
            try {
                Logging.info("Starting module ui_loading on background thread");
                
                // Phase 1: Load all game modules
                StartupWorkflow.executeWorkflow(windowManager);
                
                // Phase 2: Check for compilation issues
                CompilationChecker.checkForCompilationIssues(lobbyController, windowManager);
                StartupDelayUtil.addDevelopmentDelay("After checking for compilation issues");
                
                // Phase 3: Update UI with loaded games
                ModuleUIUpdater.updateUIWithLoadedGames(lobbyController);
                StartupDelayUtil.addDevelopmentDelay("After refreshing game modules");
                
                // Phase 4: Startup complete
                StartupDelayUtil.addDevelopmentDelay("After startup complete and ready");
                
                // Phase 5: Show main stage and hide startup window
                Logging.info("All startup tasks complete - showing main stage");
                StartupWindowToMainStageTransition.showMainStage(primaryApplicationStage, windowManager);
                
                Logging.info("Module ui_loading thread completed successfully");
                
            } catch (Exception e) {
                handleModuleLoadingError(e, primaryApplicationStage, windowManager);
            }
        });
    }
    
    /**
     * Handles errors that occur during module ui_loading.
     * Attempts to show the main stage even if errors occurred.
     * 
     * @param error The exception that occurred
     * @param primaryApplicationStage The primary stage to show
     * @param windowManager The startup window manager
     */
    private static void handleModuleLoadingError(Exception error, 
                                                 Stage primaryApplicationStage, 
                                                 StartupWindowManager windowManager) {
        Logging.error("Critical error in module ui_loading thread: " + error.getMessage(), error);
        
        // Even on error, try to show the main stage
        try {
            StartupWindowToMainStageTransition.showMainStage(primaryApplicationStage, windowManager);
        } catch (Exception showError) {
            Logging.error("Failed to show main stage after error: " + showError.getMessage());
        }
    }
    
}

