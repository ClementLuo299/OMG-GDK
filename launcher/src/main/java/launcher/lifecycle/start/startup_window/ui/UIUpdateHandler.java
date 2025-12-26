package launcher.lifecycle.start.startup_window.ui;

/**
 * Interface for updating UI components in the startup window.
 * Provides a clean abstraction for animation controllers to update UI elements
 * without directly depending on the window implementation.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @since Beta 1.0
 */
public interface UIUpdateHandler {
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * Updates both the progress bar styling and percentage label.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    void setSmoothProgress(double progress);
    
    /**
     * Updates the status text displayed to the user.
     * 
     * @param text The text to display
     */
    void updateStatusText(String text);
}

