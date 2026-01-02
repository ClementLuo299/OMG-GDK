package launcher.ui_areas.startup_window;

import gdk.internal.Logging;
import launcher.ui_areas.startup_window.build.StartupWindowBuilder;
import launcher.ui_areas.startup_window.build.components.loading_spinner.LoadingSpinner;
import launcher.ui_areas.startup_window.build.components.loading_spinner.LoadingSpinnerController;
import launcher.ui_areas.startup_window.window_control.ShowStartupWindow;
import launcher.ui_areas.startup_window.window_control.HideStartupWindow;

import javax.swing.*;

/**
 * Startup loading window that is displayed before JavaFX starts, using Swing for immediate display.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class StartupWindow {
    
    // The main frame of the startup window
    public final JFrame progressFrame;

    // The animated loading spinner component
    public final LoadingSpinner spinner;
    
    // The controller that manages the spinner animation
    public final LoadingSpinnerController spinnerController;
    
    /**
     * Initialize the startup loading window.
     * This constructor is public to allow StartupWindowBuilder to create instances.
     * 
     * @param frame The JFrame for the window
     * @param spinner The loading spinner component
     * @param spinnerController The controller that manages the spinner animation
     */
    public StartupWindow(JFrame frame, LoadingSpinner spinner, LoadingSpinnerController spinnerController) {
        this.progressFrame = frame;
        this.spinner = spinner;
        this.spinnerController = spinnerController;
    }
    
    /**
     * Creates a StartupWindow and shows it immediately.
     * Handles creation on the Event Dispatch Thread (required for Swing).
     * 
     * @return A new StartupWindow instance with the window already visible
     */
    public static StartupWindow show() {
        StartupWindow window;

        // Create the window on the EDT
        try {

            if (SwingUtilities.isEventDispatchThread()) {
                // Already on EDT - create directly
                window = StartupWindowBuilder.build();
            }

            else {
                // Not on EDT - dispatch to EDT and wait for completion
                // We need the startup window to be final for us to use invokeAndWait.
                // Thus, we create an array of size 1 as a workaround.
                final StartupWindow[] windowRef = new StartupWindow[1];

                SwingUtilities.invokeAndWait(() -> {
                    try {
                        windowRef[0] = StartupWindowBuilder.build();
                    } catch (Exception e) {
                        Logging.error("Error creating startup window: " + e.getMessage(), e);
                        throw new RuntimeException("Failed to create startup window", e);
                    }
                });

                // Capture the resulting startup window
                window = windowRef[0];
            }

        } catch (Exception e) {
            // Handle both direct creation failures and EDT dispatch failures
            Logging.error("Error creating startup window: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create startup window", e);
        }

        // Display the window
        ShowStartupWindow.show(window);

        return window;
    }
    
    /**
     * Hides and disposes the loading window.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        HideStartupWindow.hide(this);
    }
}
