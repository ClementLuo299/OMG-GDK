package launcher.ui_areas.startup_window.window_control;

import gdk.internal.Logging;
import launcher.core.lifecycle.stop.Shutdown;
import launcher.ui_areas.startup_window.StartupWindow;
import javax.swing.SwingUtilities;

/**
 * Handles hiding the startup window.
 * Ensures the window is hidden on the Event Dispatch Thread and registers cleanup tasks.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public final class HideStartupWindow {
    
    private HideStartupWindow() {
        throw new AssertionError("Hide should not be instantiated");
    }
    
    /**
     * Hides and disposes the startup window.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     * 
     * @param window The startup window to hide
     */
    public static void hide(StartupWindow window) {

        // Hide and dispose the window on the EDT
        if (SwingUtilities.isEventDispatchThread()) {
            window.spinController.stop();
            window.progressFrame.setVisible(false);
            window.progressFrame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                window.spinController.stop();
                window.progressFrame.setVisible(false);
                window.progressFrame.dispose();
            });
        }

        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindow resources");
            try {
                if (window.progressFrame != null) {
                    window.progressFrame.dispose();
                }
                Logging.info("StartupWindow cleanup completed");
            } catch (Exception e) {
                Logging.error("Error during StartupWindow cleanup: " + e.getMessage(), e);
            }
        });
    }
}

