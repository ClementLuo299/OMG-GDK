package launcher.ui_areas.lobby.lifecycle.init.helpers;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers.CompilationChecker;
import launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers.LoadModules;
import launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers.ModuleUIUpdater;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindow;
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
     * @param windowManager The startup window
     * @return The configured but not-yet-started thread
     */
    public static Thread create(Stage primaryApplicationStage,
                                GDKGameLobbyController lobbyController, 
                                StartupWindow windowManager) {

        return new Thread(() -> {
            try {
                Logging.info("Starting module loading on background thread");
                
                // Phase 1: Load all game modules
                LoadModules.loadModules();
                
                // Phase 2: Check for loading issues
                CompilationChecker.checkForCompilationIssues(lobbyController);
                
                // Phase 3: Update UI with loaded games
                ModuleUIUpdater.updateUIWithLoadedGames(lobbyController);
                
                // Phase 4: Show main stage and hide startup window
                Logging.info("All startup tasks complete - showing main stage");
                StartupWindowToMainStageTransition.showMainStage(primaryApplicationStage, windowManager);
                
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
     * @param windowManager The startup window
     */
    private static void handleModuleLoadingError(Exception error, 
                                                 Stage primaryApplicationStage, 
                                                 StartupWindow windowManager) {
        Logging.error("Critical error in module loading thread: " + error.getMessage(), error);
        
        // Even on error, try to show the main stage
        try {
            StartupWindowToMainStageTransition.showMainStage(primaryApplicationStage, windowManager);
        } catch (Exception showError) {
            Logging.error("Failed to show main stage after error: " + showError.getMessage());
        }
    }
    
}

