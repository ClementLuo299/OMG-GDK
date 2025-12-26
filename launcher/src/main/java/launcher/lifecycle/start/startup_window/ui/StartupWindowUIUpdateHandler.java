package launcher.lifecycle.start.startup_window.ui;

import launcher.lifecycle.start.startup_window.StartupWindow;
import javax.swing.SwingUtilities;

/**
 * Handles UI component updates for the startup window.
 * This class is responsible for updating progress bars, labels, and other UI elements
 * in a thread-safe manner using the Event Dispatch Thread.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @since Beta 1.0
 */
public class StartupWindowUIUpdateHandler implements UIUpdateHandler {
    
    /** The startup window containing the UI components to update. */
    private final StartupWindow progressWindow;
    
    /**
     * Constructs a new StartupWindowUIUpdateHandler.
     * 
     * @param progressWindow The startup window containing UI components
     */
    public StartupWindowUIUpdateHandler(StartupWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * Updates both the progress bar styling and percentage label.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    @Override
    public void setSmoothProgress(double progress) {
        double clampedProgress = Math.max(0.0, Math.min(1.0, progress));
        if (progressWindow.getProgressBarStyling() != null) {
            progressWindow.getProgressBarStyling().setSmoothProgress(clampedProgress);
        }
        
        if (progressWindow.getPercentageLabel() != null) {
            SwingUtilities.invokeLater(() -> {
                int percentage = (int) Math.round(clampedProgress * 100);
                progressWindow.getPercentageLabel().setText(percentage + "%");
            });
        }
        
        progressWindow.getProgressBar().repaint();
    }
    
    /**
     * Updates the status text displayed to the user.
     * 
     * @param text The text to display
     */
    @Override
    public void updateStatusText(String text) {
        if (progressWindow.getStatusLabel() != null) {
            SwingUtilities.invokeLater(() -> {
                progressWindow.getStatusLabel().setText(text);
            });
        }
    }
    
    /**
     * Updates the progress bar string with step information.
     * 
     * @param step The current step number
     * @param totalSteps The total number of steps
     */
    public void updateProgressBarString(int step, int totalSteps) {
        SwingUtilities.invokeLater(() -> {
            progressWindow.getProgressBar().setString(step + "/" + totalSteps + " (" + (step * 100 / totalSteps) + "%)");
        });
    }
}

