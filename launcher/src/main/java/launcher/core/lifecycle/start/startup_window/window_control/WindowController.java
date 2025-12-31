package launcher.core.lifecycle.start.startup_window.window_control;

import launcher.core.lifecycle.start.startup_window.StartupWindow;
import launcher.core.lifecycle.stop.Shutdown;
import gdk.internal.Logging;
import launcher.core.lifecycle.start.startup_window.animation.BarAnimationController;

/**
 * Controls the visibility and lifecycle of a startup window instance.
 * Handles showing, hiding, and cleanup of the window and its associated resources.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class WindowController {
    
    /** The startup window to manage. */
    private final StartupWindow progressWindow;
    
    /** Controller for smooth progress bar animation between steps. */
    private final BarAnimationController barAnimationController;
    
    /**
     * Constructs a new WindowController.
     * 
     * @param progressWindow The startup window to manage
     * @param barAnimationController The progress bar animation controller
     */
    public WindowController(
            StartupWindow progressWindow,
            BarAnimationController barAnimationController) {
        this.progressWindow = progressWindow;
        this.barAnimationController = barAnimationController;
    }
    
    /**
     * Shows the startup window.
     */
    public void show() {
        progressWindow.show();
    }
    
    /**
     * Hides the startup window and stops all animations.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        // Stop all animations
        barAnimationController.stop();
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up WindowController resources");
            try {
                // Ensure animations are stopped (redundant but safe - already stopped above)
                barAnimationController.stop();
                
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

