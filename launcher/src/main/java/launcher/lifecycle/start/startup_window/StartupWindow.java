package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import launcher.lifecycle.start.startup_window.initialization.*;
import launcher.lifecycle.start.startup_window.styling.ProgressBarStyling;

/**
 * Startup progress window that is displayed before JavaFX starts, using Swing for immediate display.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited December 24, 2025 
 * @since Beta 1.0
 */
public class StartupWindow {
    
    // Swing components for the pre-startup progress window UI
    final JFrame progressFrame;
    final JProgressBar progressBar;
    final JLabel percentageLabel;
    final JLabel statusLabel;
    final ProgressBarStyling progressBarStyling;
    
    // Total steps for the progress bar
    final int totalSteps; 
    
    /**
     * Initialize the startup progress window
     * 
     * @param totalSteps The total number of steps for the progress bar
     */
    public StartupWindow(int totalSteps) {
        this.totalSteps = totalSteps;
        
        StartupWindowInitializer.InitializationResult result = 
            StartupWindowInitializer.initialize(totalSteps);
        
        this.progressFrame = result.frame;
        this.progressBar = result.progressBar;
        this.percentageLabel = result.percentageLabel;
        this.statusLabel = result.statusLabel;
        this.progressBarStyling = result.progressBarStyling;
    }
    
    /**
     * Shows the progress window.
     */
    public void show() {
        // Show the progress window on the Event Dispatch Thread
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(true);
            });
        }
    }
    
    /**
     * Hides and disposes the progress window.
     */
    public void hide() {
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(false);
            progressFrame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(false);
                progressFrame.dispose();
            });
        }
    }
}
