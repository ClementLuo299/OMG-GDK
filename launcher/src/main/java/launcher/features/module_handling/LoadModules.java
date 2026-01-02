package launcher.features.module_handling;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.features.module_handling.startup.ModuleLoadingThread;
import launcher.ui_areas.startup_window.StartupWindowManager;
import launcher.core.lifecycle.stop.Shutdown;
import launcher.features.development_features.StartupDelayUtil;

/**
 * Coordinates the module ui_loading process during startup.
 * Sets up and starts the background thread that loads game modules and manages cleanup tasks.
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public final class LoadModules {

    private LoadModules() {}

    /**
     * Loads game modules in the background and prepares the UI.
     * 
     * @param primaryApplicationStage The main window (hidden until modules are loaded)
     * @param lobbyController The UI controller that will show the list of games
     * @param windowManager The startup progress window (visible during ui_loading)
     */
    public static void load(Stage primaryApplicationStage, GDKGameLobbyController lobbyController, StartupWindowManager windowManager) {
        Logging.info("Starting module ui_loading process...");
        StartupDelayUtil.addDevelopmentDelay("After 'Starting module ui_loading' message");
        
        // Wait a tiny bit to make sure the window is fully visible
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create a background thread that will load all the modules
        // This thread does the heavy work so the UI doesn't freeze
        Thread moduleLoadingThread = ModuleLoadingThread.create(primaryApplicationStage, lobbyController, windowManager);
        
        // Step 4: Make sure we clean up the thread if the app closes unexpectedly
        registerCleanupTasks(moduleLoadingThread, windowManager);
        
        // Step 5: Start the background thread
        // After this line, the method returns immediately.
        // The background thread continues working and will:
        // - Load modules
        // - Update the UI
        // - Show the main window when done
        moduleLoadingThread.start();
        
        // Method returns here - the caller continues immediately
        // Background thread keeps working in the background
    }
    
    
    /**
     * Registers cleanup tasks to ensure proper shutdown.
     * These tasks will be executed when the application shuts down.
     * 
     * @param moduleLoadingThread The thread to interrupt on shutdown
     * @param windowManager The window manager to hide on shutdown
     */
    private static void registerCleanupTasks(Thread moduleLoadingThread, StartupWindowManager windowManager) {
        // Clean up the module ui_loading thread if app shuts down while ui_loading
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up module ui_loading thread...");
            if (moduleLoadingThread.isAlive()) {
                moduleLoadingThread.interrupt();
            }
        });
        
        // Clean up the startup window manager
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindowManager...");
            if (windowManager != null) {
                windowManager.hide();
            }
        });
    }
}

