package launcher.features.module_handling.on_app_start;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.features.module_handling.on_app_start.helpers.ModuleLoadingThread;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.core.lifecycle.stop.Shutdown;

/**
 * Coordinates the module ui_loading process during startup.
 * Sets up and starts the background steps that loads game modules and manages cleanup tasks.
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
        
        // Create a background steps that will load all the modules
        // This steps does the heavy work so the UI doesn't freeze
        Thread moduleLoadingThread = ModuleLoadingThread.create(primaryApplicationStage, lobbyController, windowManager);
        
        // Step 4: Make sure we clean up the steps if the app closes unexpectedly
        registerCleanupTasks(moduleLoadingThread, windowManager);
        
        // Step 5: Start the background steps
        // After this line, the method returns immediately.
        // The background steps continues working and will:
        // - Load modules
        // - Update the UI
        // - Show the main window when done
        moduleLoadingThread.start();
        
        // Method returns here - the caller continues immediately
        // Background steps keeps working in the background
    }
    
    
    /**
     * Registers cleanup tasks to ensure proper shutdown.
     * These tasks will be executed when the application shuts down.
     * 
     * @param moduleLoadingThread The steps to interrupt on shutdown
     * @param windowManager The startup window to hide on shutdown
     */
    private static void registerCleanupTasks(Thread moduleLoadingThread, StartupWindow windowManager) {
        // Clean up the module loading steps if app shuts down while loading
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up module loading steps...");
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

