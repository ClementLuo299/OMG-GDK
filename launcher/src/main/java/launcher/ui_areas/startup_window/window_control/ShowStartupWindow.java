package launcher.ui_areas.startup_window.window_control;

import launcher.ui_areas.startup_window.StartupWindow;
import javax.swing.SwingUtilities;

/**
 * Handles showing the startup window.
 * Ensures the window is displayed on the Event Dispatch Thread.
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
public final class ShowStartupWindow {
    
    private ShowStartupWindow() {
        throw new AssertionError("Show should not be instantiated");
    }
    
    /**
     * Shows the startup window.
     * Ensures the window is displayed on the Event Dispatch Thread.
     * Starts the spinner only after the window is fully visible and painted.
     * 
     * @param window The startup window to show
     */
    public static void show(StartupWindow window) {
        // Show startup window on the EDT
        if (SwingUtilities.isEventDispatchThread()) {
            window.progressFrame.setVisible(true);
            // Ensure the window is painted before starting the spinner
            window.progressFrame.validate();
            window.progressFrame.repaint();
            // Use invokeLater to ui_initialization spinner after the current paint cycle completes
            SwingUtilities.invokeLater(() -> {
                window.spinController.start();
            });
        }
        else {
            SwingUtilities.invokeLater(() -> {
                window.progressFrame.setVisible(true);
                window.progressFrame.validate();
                window.progressFrame.repaint();
                // Start spinner after the window is painted
                SwingUtilities.invokeLater(() -> {
                    window.spinController.start();
                });
            });
        }
    }
}

