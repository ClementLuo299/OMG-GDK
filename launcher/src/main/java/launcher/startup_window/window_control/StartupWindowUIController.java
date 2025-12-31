package launcher.startup_window.window_control;

import launcher.startup_window.StartupWindow;
import javax.swing.SwingUtilities;

/**
 * Controller for updating UI components in the startup window.
 * This class is responsible for updating progress bars, labels, and other UI elements
 * in a thread-safe manner using the Event Dispatch Thread.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class StartupWindowUIController {
    
    /** The startup window containing the UI components to update. */
    private final StartupWindow progressWindow;
    
    /**
     * Constructs a new StartupWindowUIController.
     * 
     * @param progressWindow The startup window containing UI components
     */
    public StartupWindowUIController(StartupWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * Updates both the progress bar styling and percentage label.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setSmoothProgress(double progress) {
        // Clamp the progress value between 0.0 and 1.0
        double clampedProgress = Math.max(0.0, Math.min(1.0, progress));

        // Update the progress bar styling
        if (progressWindow.getProgressBarStyling() != null) {
            progressWindow.getProgressBarStyling().setSmoothProgress(clampedProgress);
        }
        
        // Update the percentage label
        if (progressWindow.getPercentageLabel() != null) {
            SwingUtilities.invokeLater(() -> {
                int percentage = (int) Math.round(clampedProgress * 100);
                progressWindow.getPercentageLabel().setText(percentage + "%");
            });
        }
        
        // Repaint the progress bar
        progressWindow.getProgressBar().repaint();
    }
    
    /**
     * Updates the status text displayed to the user.
     * 
     * @param text The text to display
     */
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

