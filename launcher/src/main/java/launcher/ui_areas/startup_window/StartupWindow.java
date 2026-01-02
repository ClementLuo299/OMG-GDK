package launcher.ui_areas.startup_window;

import javax.swing.*;
import launcher.ui_areas.startup_window.component_construction.StartupWindowBuilder;
import launcher.ui_areas.startup_window.component_construction.components.LoadingSpinner;

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
    public StartupWindow() {
        StartupWindowBuilder.InitializationResult result =
            StartupWindowBuilder.build();
        
        this.progressFrame = result.frame;
        this.spinner = result.spinner;
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
