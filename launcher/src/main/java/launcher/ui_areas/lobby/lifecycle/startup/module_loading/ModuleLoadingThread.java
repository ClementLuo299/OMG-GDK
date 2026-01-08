package launcher.ui_areas.lobby.lifecycle.startup.module_loading;

import gdk.internal.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.core.lifecycle.stop.Shutdown;
import launcher.features.module_handling.ModuleDiscoveryAndLoading;
import launcher.features.module_handling.load_modules.LoadModules;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates the module loading process during startup.
 * Sets up and starts the background thread that loads game modules and manages cleanup tasks.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public final class ModuleLoadingThread {

    /** Temporary storage for loading failures from startup loading. */
    private static List<String> startupFailures = new ArrayList<>();

    private ModuleLoadingThread() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Starts the module loading process in the background.
     * 
     * @param primaryApplicationStage The main window (hidden until modules are loaded)
     * @param lobbyController The UI controller that will show the list of games
     * @param windowManager The startup window (visible during loading)
     */
    public static void start(Stage primaryApplicationStage, 
                            GDKGameLobbyController lobbyController, 
                            StartupWindow windowManager) {
        Logging.info("Starting module loading process...");
        
        // Wait a tiny bit to make sure the window is fully visible
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create and start background thread
        Thread moduleLoadingThread = new Thread(() -> {
            try {
                Logging.info("Starting module loading on background thread");
                
                // Phase 1: Discover and load all game modules
                LoadModules.ModuleLoadResult loadResult = ModuleDiscoveryAndLoading.discoverAndLoadAllModules();
                
                // Store failures for later reporting to UI
                startupFailures = loadResult.getCompilationFailures() != null 
                    ? new ArrayList<>(loadResult.getCompilationFailures()) 
                    : new ArrayList<>();
                
                // Phase 2: Check for loading issues
                Platform.runLater(() -> {
                    try {
                        lobbyController.reportStartupCompilationFailures(startupFailures);
                        startupFailures.clear();
                    } catch (Exception e) {
                        Logging.error("Error checking loading issues: " + e.getMessage());
                    }
                });
                
                // Phase 3: Update UI with loaded games
                Platform.runLater(() -> {
                    try {
                        lobbyController.refreshAvailableGameModulesFast();
                    } catch (Exception e) {
                        Logging.error("Error refreshing game modules: " + e.getMessage(), e);
                    }
                });
                
                // Phase 4: Show main stage and hide startup window
                Logging.info("All startup tasks complete - showing main stage");
                showMainStage(primaryApplicationStage, windowManager);
                
                Logging.info("Module loading thread completed successfully");
                
            } catch (Exception e) {
                Logging.error("Critical error in module loading thread: " + e.getMessage(), e);
                
                // Even on error, try to show the main stage
                showMainStage(primaryApplicationStage, windowManager);
            }
        });
        
        // Register cleanup tasks
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up module loading thread...");
            if (moduleLoadingThread.isAlive()) {
                moduleLoadingThread.interrupt();
            }
        });
        
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindow...");
            if (windowManager != null) {
                windowManager.hide();
            }
        });
        
        // Start the background thread
        moduleLoadingThread.start();
    }
    
    /**
     * Shows the main stage and hides the startup window.
     * 
     * @param primaryApplicationStage The primary stage to show
     * @param windowManager The startup window to hide
     */
    private static void showMainStage(Stage primaryApplicationStage, StartupWindow windowManager) {
        try {
            windowManager.hide();
            Platform.runLater(() -> {
                primaryApplicationStage.setOpacity(1.0);
                primaryApplicationStage.show();
            });
        } catch (Exception e) {
            Logging.error("Failed to show main stage: " + e.getMessage());
        }
    }
    
}

