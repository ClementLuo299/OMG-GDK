package launcher.ui_areas.lobby.lifecycle.init;

import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.lifecycle.init.helpers.ModuleLoadingThread;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.core.lifecycle.stop.Shutdown;

/**
 * Coordinates the module loading process during startup.
 * Sets up and starts the background thread that loads game modules and manages cleanup tasks.
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
        Logging.info("Starting module loading process...");
        
        // Wait a tiny bit to make sure the window is fully visible
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create a background thread that will load all the modules
        // This thread does the heavy work so the UI doesn't freeze
        Thread moduleLoadingThread = ModuleLoadingThread.create(primaryApplicationStage, lobbyController, windowManager);
        
        // Make sure we clean up the thread if the app closes unexpectedly
        registerCleanupTasks(moduleLoadingThread, windowManager);
        
        // Start the background thread
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
     * @param windowManager The startup window to hide on shutdown
     */
    private static void registerCleanupTasks(Thread moduleLoadingThread, StartupWindow windowManager) {
        // Clean up the module loading thread if app shuts down while loading
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up module loading thread...");
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

