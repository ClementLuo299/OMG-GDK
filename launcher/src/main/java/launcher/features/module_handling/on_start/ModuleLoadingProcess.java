package launcher.features.module_handling.on_start;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.features.module_handling.on_start.helpers.ModuleLoadingThread;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.core.lifecycle.stop.Shutdown;

/**
 * Coordinates the module ui_loading process during startup.
 * Sets up and starts the background helpers that loads game modules and manages cleanup tasks.
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingProcess {

    private ModuleLoadingProcess() {}

    /**
     * Loads game modules in the background and prepares the UI.
     * 
     * @param primaryApplicationStage The main window (hidden until modules are loaded)
     * @param lobbyController The UI controller that will show the list of games
     * @param windowManager The startup window (visible during loading)
     */
    public static void start(Stage primaryApplicationStage, GDKGameLobbyController lobbyController, StartupWindow windowManager) {
        Logging.info("Starting module ui_loading process...");
        
        // Wait a tiny bit to make sure the window is fully visible
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create a background helpers that will load all the modules
        // This helpers does the heavy work so the UI doesn't freeze
        Thread moduleLoadingThread = ModuleLoadingThread.create(primaryApplicationStage, lobbyController, windowManager);
        
        // Step 4: Make sure we clean up the helpers if the app closes unexpectedly
        registerCleanupTasks(moduleLoadingThread, windowManager);
        
        // Step 5: Start the background helpers
        // After this line, the method returns immediately.
        // The background helpers continues working and will:
        // - Load modules
        // - Update the UI
        // - Show the main window when done
        moduleLoadingThread.start();
        
        // Method returns here - the caller continues immediately
        // Background helpers keeps working in the background
    }
    
    
    /**
     * Registers cleanup tasks to ensure proper shutdown.
     * These tasks will be executed when the application shuts down.
     * 
     * @param moduleLoadingThread The helpers to interrupt on shutdown
     * @param windowManager The startup window to hide on shutdown
     */
    private static void registerCleanupTasks(Thread moduleLoadingThread, StartupWindow windowManager) {
        // Clean up the module loading helpers if app shuts down while loading
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up module loading helpers...");
            if (moduleLoadingThread.isAlive()) {
                moduleLoadingThread.interrupt();
            }
        });
        
        // Clean up the startup window
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindow...");
            if (windowManager != null) {
                windowManager.hide();
            }
        });
    }
}

