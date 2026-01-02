package launcher.ui_areas.startup_window;

import gdk.internal.Logging;
import launcher.core.lifecycle.stop.Shutdown;
import launcher.ui_areas.startup_window.component_construction.StartupWindowBuilder;
import launcher.ui_areas.startup_window.component_construction.components.LoadingSpinner;

import javax.swing.*;

/**
 * Startup loading window that is displayed before JavaFX starts, using Swing for immediate display.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited January 2025 
 * @since Beta 1.0
 */
public class StartupWindow {
    
    // Swing components for the pre-startup loading window UI
    final JFrame progressFrame;
    final LoadingSpinner spinner;
    
    /**
     * Initialize the startup loading window
     */
    private StartupWindow() {
        StartupWindowBuilder.InitializationResult result =
            StartupWindowBuilder.build();
        
        this.progressFrame = result.frame;
        this.spinner = result.spinner;
    }
    
    /**
     * Creates a StartupWindow and shows it immediately.
     * Handles creation on the Event Dispatch Thread (required for Swing).
     * 
     * @return A new StartupWindow instance with the window already visible
     */
    public static StartupWindow createAndShow() {
        // Create the window on the Event Dispatch Thread (required for Swing)
        final StartupWindow[] windowRef = new StartupWindow[1];
        
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // Already on EDT - create directly
                windowRef[0] = new StartupWindow();
            } else {
                // Not on EDT - dispatch to EDT and wait for completion
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        windowRef[0] = new StartupWindow();
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
        
        // Get the window
        StartupWindow window = windowRef[0];

        // Display the window
        window.show();
        
        return window;
    }
    
    /**
     * Shows the loading window.
     */
    public void show() {
        // Show the loading window on the Event Dispatch Thread
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(true);
            spinner.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(true);
                spinner.start();
            });
        }
    }
    
    /**
     * Hides and disposes the loading window.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        if (SwingUtilities.isEventDispatchThread()) {
            spinner.stop();
            progressFrame.setVisible(false);
            progressFrame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                spinner.stop();
                progressFrame.setVisible(false);
                progressFrame.dispose();
            });
        }
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindow resources");
            try {
                // Dispose of the window
                if (progressFrame != null) {
                    progressFrame.dispose();
                }
                
                Logging.info("StartupWindow cleanup completed");
            } catch (Exception e) {
                Logging.error("Error during StartupWindow cleanup: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Gets the loading spinner component.
     * 
     * @return The loading spinner
     */
    public LoadingSpinner getSpinner() {
        return spinner;
    }
}
