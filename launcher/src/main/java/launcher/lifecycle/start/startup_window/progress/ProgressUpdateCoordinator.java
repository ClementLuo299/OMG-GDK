package launcher.lifecycle.start.startup_window.progress;
import launcher.lifecycle.start.startup_window.estimation.StepDurationEstimator;
import launcher.lifecycle.start.startup_window.animation.SmoothProgressAnimationController;
import launcher.lifecycle.start.startup_window.ui.StartupWindowUIUpdateHandler;

/**
 * Coordinates progress updates for the startup window.
 * This class orchestrates progress tracking, duration estimation, animation,
 * and UI updates when the progress changes.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @since Beta 1.0
 */
public class ProgressUpdateCoordinator {
    
    /** Tracks progress state (current step and total steps). */
    private final ProgressTracker progressTracker;
    
    /** Estimates step durations based on message content. */
    private final StepDurationEstimator stepDurationEstimator;
    
    /** Controller for smooth progress bar animation between steps. */
    private final SmoothProgressAnimationController smoothProgressAnimationController;
    
    /** Handler for UI component updates. */
    private final StartupWindowUIUpdateHandler uiUpdateHandler;
    
    /** Total steps for the startup process. */
    private final int totalSteps;
    
    /**
     * Constructs a new ProgressUpdateCoordinator.
     * 
     * @param progressTracker The progress tracker for state management
     * @param stepDurationEstimator The step duration estimator
     * @param smoothProgressAnimationController The smooth progress animation controller
     * @param uiUpdateHandler The UI update handler
     * @param totalSteps The total number of steps
     */
    public ProgressUpdateCoordinator(
            ProgressTracker progressTracker,
            StepDurationEstimator stepDurationEstimator,
            SmoothProgressAnimationController smoothProgressAnimationController,
            StartupWindowUIUpdateHandler uiUpdateHandler,
            int totalSteps) {
        this.progressTracker = progressTracker;
        this.stepDurationEstimator = stepDurationEstimator;
        this.smoothProgressAnimationController = smoothProgressAnimationController;
        this.uiUpdateHandler = uiUpdateHandler;
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
        uiUpdateHandler.updateProgressBarString(step, totalSteps);
        
        // Update the status text
        uiUpdateHandler.updateStatusText(message);

        // Estimate the duration of the step and start the smooth animation toward the target step
        // The smooth animation will handle updating the visual progress bar
        long estimatedDuration = stepDurationEstimator.estimateDuration(message);
        smoothProgressAnimationController.animateToStep(step, progressTracker.getTotalSteps(), estimatedDuration);
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
        smoothProgressAnimationController.resetToStep(step, totalSteps);
    }
}

