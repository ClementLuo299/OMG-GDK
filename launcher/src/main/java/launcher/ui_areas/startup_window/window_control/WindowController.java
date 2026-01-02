package launcher.ui_areas.startup_window.window_control;

import launcher.ui_areas.startup_window.StartupWindow;
import launcher.core.lifecycle.stop.Shutdown;
import gdk.internal.Logging;

/**
 * Controls the visibility and lifecycle of a startup window instance.
 * Handles showing, hiding, and cleanup of the window and its associated resources.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class WindowController {
    
    /** The startup window to manage. */
    private final StartupWindow progressWindow;
    
    /**
     * Constructs a new WindowController.
     * 
     * @param progressWindow The startup window to manage
     */
    public WindowController(StartupWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Shows the startup window.
     */
    public void show() {
        progressWindow.show();
    }
    
    /**
     * Hides the startup window.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up WindowController resources");
            try {
                // Dispose of the progress window
                if (progressWindow != null) {
                    progressWindow.hide();
                }
                
                Logging.info("WindowController cleanup completed");
            } catch (Exception e) {
                Logging.error("Error during WindowController cleanup: " + e.getMessage(), e);
            }
        });
    }
}

