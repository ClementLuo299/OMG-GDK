package launcher.lifecycle.start.startup_window.lifecycle;

import launcher.lifecycle.start.startup_window.StartupWindow;
import launcher.lifecycle.stop.Shutdown;
import gdk.internal.Logging;
import launcher.lifecycle.start.startup_window.animation.SmoothProgressAnimationController;

/**
 * Manages the lifecycle of the startup window including showing, hiding, and cleanup.
 * This class handles window visibility and ensures proper resource cleanup on shutdown.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @since Beta 1.0
 */
public class WindowLifecycleManager {
    
    /** The startup window to manage. */
    private final StartupWindow progressWindow;
    
    /** Controller for smooth progress bar animation between steps. */
    private final SmoothProgressAnimationController smoothProgressAnimationController;
    
    /**
     * Constructs a new WindowLifecycleManager.
     * 
     * @param progressWindow The startup window to manage
     * @param smoothProgressAnimationController The smooth progress animation controller
     */
    public WindowLifecycleManager(
            StartupWindow progressWindow,
            SmoothProgressAnimationController smoothProgressAnimationController) {
        this.progressWindow = progressWindow;
        this.smoothProgressAnimationController = smoothProgressAnimationController;
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
        smoothProgressAnimationController.stop();
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up WindowLifecycleManager resources");
            try {
                // Ensure animations are stopped (redundant but safe - already stopped above)
                smoothProgressAnimationController.stop();
                
                // Dispose of the progress window
                if (progressWindow != null) {
                    progressWindow.hide();
                }
                
                Logging.info("WindowLifecycleManager cleanup completed");
            } catch (Exception e) {
                Logging.error("Error during WindowLifecycleManager cleanup: " + e.getMessage(), e);
            }
        });
    }
}

