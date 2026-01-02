package launcher.ui_areas.startup_window.window_control;

import launcher.ui_areas.startup_window.StartupWindow;

/**
 * Controller for updating UI components in the startup window.
 * Simplified version - no longer updates progress since we only show a spinner.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class StartupWindowUIController {
    
    /**
     * Constructs a new StartupWindowUIController.
     * 
     * @param progressWindow The startup window containing UI components (kept for compatibility)
     */
    public StartupWindowUIController(StartupWindow progressWindow) {
        // No-op: spinner handles its own animation, no UI updates needed
    }
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * No-op since we no longer use progress bars.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setSmoothProgress(double progress) {
        // No-op: spinner handles its own animation
    }
    
    /**
     * Updates the status text displayed to the user.
     * No-op since we only show "Loading".
     * 
     * @param text The text to display
     */
    public void updateStatusText(String text) {
        // No-op: we only show "Loading" text
    }
    
    /**
     * Updates the progress bar string with step information.
     * No-op since we no longer use progress bars.
     * 
     * @param step The current step number
     * @param totalSteps The total number of steps
     */
    public void updateProgressBarString(int step, int totalSteps) {
        // No-op: we no longer use progress bars
    }
}

