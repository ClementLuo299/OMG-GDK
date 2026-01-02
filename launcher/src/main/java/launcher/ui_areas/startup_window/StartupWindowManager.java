package launcher.ui_areas.startup_window;

import gdk.internal.Logging;
import launcher.ui_areas.startup_window.window_control.WindowController;

import javax.swing.SwingUtilities;

/**
 * Coordinates the startup loading window system.
 * Manages window lifecycle (show/hide).
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class StartupWindowManager {
    
    /** Controls window visibility and lifecycle (show/hide/cleanup). */
    private final WindowController windowController;
    
    /**
     * Constructs a new StartupWindowManager with the specified loading window.
     * 
     * @param progressWindow The loading window to manage
     */
    private StartupWindowManager(StartupWindow progressWindow) {
        this.windowController = new WindowController(progressWindow);
    }
    
    /**
     * Creates a StartupWindowManager and shows the window immediately.
     * 
     * @return A new StartupWindowManager instance with the window already visible
     */
    public static StartupWindowManager createAndShow() {
        // Create the window and manager on the Event Dispatch Thread (required for Swing)
        final StartupWindow[] windowRef = new StartupWindow[1];
        final StartupWindowManager[] managerRef = new StartupWindowManager[1];
        
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // Already on EDT - create components directly
                windowRef[0] = new StartupWindow();
                managerRef[0] = new StartupWindowManager(windowRef[0]);
            } else {
                // Not on EDT - dispatch to EDT and wait for completion
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        windowRef[0] = new StartupWindow();
                        managerRef[0] = new StartupWindowManager(windowRef[0]);
                    } catch (Exception e) {
                        Logging.error("Error creating startup window: " + e.getMessage(), e);
                        throw new RuntimeException("Failed to create startup window", e);
                    }
                });
            }
        } catch (Exception e) {
            // Handle both direct creation failures and EDT dispatch failures
            Logging.error("Error creating startup window: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create startup window", e);
        }
        
        // Get the manager
        StartupWindowManager manager = managerRef[0];

        // Display the window
        manager.windowController.show();
        
        return manager;
    }

    /**
     * Hides the startup loading window.
     */
    public void hide() {
        windowController.hide();
    }
} 