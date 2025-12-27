package launcher.lifecycle.start.startup_window.progress;
import launcher.lifecycle.start.startup_window.animation.BarAnimationController;
import launcher.lifecycle.start.startup_window.window_control.StartupWindowUIController;

/**
 * Controls progress updates for the startup window.
 * This class orchestrates progress tracking, duration estimation, animation,
 * and UI updates when the progress changes.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class ProgressUpdateController {
    
    /** Tracks progress state (current step and total steps). */
    private final ProgressTracker progressTracker;
    
    /** Estimates step durations based on message content. */
    private final DurationEstimator durationEstimator;
    
    /** Controller for smooth progress bar animation between steps. */
    private final BarAnimationController barAnimationController;
    
    /** Controller for UI component updates. */
    private final StartupWindowUIController uiController;
    
    /** Total steps for the startup process. */
    private final int totalSteps;
    
    /**
     * Constructs a new ProgressUpdateController.
     * 
     * @param progressTracker The progress tracker for state management
     * @param durationEstimator The duration estimator for step duration estimation
     * @param barAnimationController The progress bar animation controller
     * @param uiController The UI controller for updating UI components
     * @param totalSteps The total number of steps
     */
    public ProgressUpdateController(
            ProgressTracker progressTracker,
            DurationEstimator durationEstimator,
            BarAnimationController barAnimationController,
            StartupWindowUIController uiController,
            int totalSteps) {
        this.progressTracker = progressTracker;
        this.durationEstimator = durationEstimator;
        this.barAnimationController = barAnimationController;
        this.uiController = uiController;
        this.totalSteps = totalSteps;
    }
    
    /**
     * Updates the progress display with the current step and status message.
     * Coordinates progress tracking, UI updates, duration estimation, and animation.
     * 
     * @param step The current step number (starts at 0)
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        // Update the current step
        progressTracker.setCurrentStep(step);

        // Update the progress bar string (but let smooth animation handle the visual progress)
        uiController.updateProgressBarString(step, totalSteps);
        
        // Update the status text
        uiController.updateStatusText(message);

        // Estimate the duration of the step and start the smooth animation toward the target step
        // The smooth animation will handle updating the visual progress bar
        long estimatedDuration = durationEstimator.estimateDuration(message);
        barAnimationController.animateToStep(step, progressTracker.getTotalSteps(), estimatedDuration);
    }
    
    /**
     * Gets the total number of steps in the startup process.
     * 
     * @return The total number of steps
     */
    public int getTotalSteps() {
        return progressTracker.getTotalSteps();
    }
    
    /**
     * Resets the progress animation to a specific step.
     * 
     * @param step The step to reset to (0-based)
     * @param totalSteps The total number of steps
     */
    public void resetToStep(int step, int totalSteps) {
        barAnimationController.resetToStep(step, totalSteps);
    }
}

