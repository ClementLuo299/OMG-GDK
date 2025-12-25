package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import launcher.lifecycle.start.startup_window.initialization.*;
import launcher.lifecycle.start.startup_window.styling.ProgressBarStyling;

/**
 * Startup progress window that is displayed before JavaFX starts, using Swing for immediate display.
 * This window appears instantly when the application launches and shows progress
 * during the JavaFX initialization phase.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited December 24, 2025 
 * @since Beta 1.0
 */
public class StartupWindow {
    
    // Swing components for the pre-startup progress window UI
    private JFrame progressFrame;
    private JProgressBar progressBar;
    private JLabel percentageLabel;
    private JLabel statusLabel;
    
    // Progress tracking for the progress bar
    private final int totalSteps; // Total steps for the progress bar 
    
    // Smooth progress value for animation (0.0 to 1.0, can be fractional)
    private double smoothProgress = 0.0;
    
    // Progress bar styling reference
    private ProgressBarStyling progressBarStyling; 
    
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
     * Show the progress window
     */
    public void show() {
        System.out.println("Showing startup progress window");
        
        // Show on EDT to ensure thread safety
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(true);
            });
        }
    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        System.out.println("Hiding startup progress window");
        
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
     * Update progress and status
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(step);
            progressBar.setString(step + "/" + totalSteps + " (" + (step * 100 / totalSteps) + "%)");
            
            // Update percentage label
            int percentage = totalSteps > 0 ? (step * 100 / totalSteps) : 0;
            percentageLabel.setText(percentage + "%");
            
            statusLabel.setText(status);
            
            System.out.println("Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * This allows fractional progress values for smooth animation.
     * Also updates the percentage label to reflect the smooth progress.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setSmoothProgress(double progress) {
        smoothProgress = Math.max(0.0, Math.min(1.0, progress)); // Clamp to 0.0-1.0
        if (progressBarStyling != null) {
            progressBarStyling.setSmoothProgress(smoothProgress);
        }
        
        // Update percentage label with smooth progress
        if (percentageLabel != null) {
            int percentage = (int) Math.round(smoothProgress * 100);
            percentageLabel.setText(percentage + "%");
        }
        
        progressBar.repaint();
    }
    
    /**
     * Update the status text without animation.
     * 
     * @param text The text to display
     */
    public void updateStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }
    
    /**
     * Get the progress bar styling instance.
     * 
     * @return The progress bar styling instance
     */
    public ProgressBarStyling getProgressBarStyling() {
        return progressBarStyling;
    }
    
    /**
     * Repaint the progress bar.
     */
    public void repaintProgressBar() {
        if (progressBar != null) {
            progressBar.repaint();
        }
    }
    
}
