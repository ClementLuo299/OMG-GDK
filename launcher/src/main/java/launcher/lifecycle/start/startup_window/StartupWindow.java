package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import launcher.lifecycle.start.startup_window.component_construction.*;
import launcher.lifecycle.start.startup_window.styling.ProgressBarStyling;

/**
 * Startup progress window that is displayed before JavaFX starts, using Swing for immediate display.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited December 26, 2025 
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
    
    /**
     * Gets the progress bar component.
     * 
     * @return The progress bar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    /**
     * Gets the percentage label component.
     * 
     * @return The percentage label
     */
    public JLabel getPercentageLabel() {
        return percentageLabel;
    }
    
    /**
     * Gets the status label component.
     * 
     * @return The status label
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }
    
    /**
     * Gets the progress bar styling component.
     * 
     * @return The progress bar styling
     */
    public ProgressBarStyling getProgressBarStyling() {
        return progressBarStyling;
    }
    
    /**
     * Gets the total number of steps.
     * 
     * @return The total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }
}
