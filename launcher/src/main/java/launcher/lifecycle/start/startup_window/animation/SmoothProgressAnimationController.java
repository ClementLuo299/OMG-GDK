package launcher.lifecycle.start.startup_window.animation;

import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;
import launcher.lifecycle.start.startup_window.StartupWindow;

/**
 * Controls smooth animation of the progress bar between steps.
 * Instead of jumping directly to the target step, this animates smoothly from
 * the current displayed value to the target value, optionally timed to match
 * predicted step duration.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @since Beta 1.0
 */
public class SmoothProgressAnimationController {
    
    /** Timer that controls the smooth progress animation. */
    private Timer animationTimer;
    
    /** The current displayed progress value (can be fractional for smooth animation). */
    private double currentDisplayedProgress = 0.0;
    
    /** The target progress value to animate toward. */
    private double targetProgress = 0.0;
    
    /** The starting progress value when current animation began. */
    private double startProgress = 0.0;
    
    /** The progress window that contains the progress bar to animate. */
    private final StartupWindow progressWindow;
    
    /** Frame time in milliseconds (target ~60 FPS). */
    private static final long FRAME_TIME_MS = 16;
    
    /** Time when the current animation started (milliseconds since epoch). */
    private long animationStartTime = 0;
    
    /** Duration for the current animation in milliseconds. */
    private long animationDurationMs = 0;
    
    /**
     * Constructs a new SmoothProgressAnimationController.
     * 
     * @param progressWindow The progress window containing the progress bar to animate
     */
    public SmoothProgressAnimationController(StartupWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Sets the target progress value and starts smooth animation toward it.
     * Uses a default animation duration if none is specified.
     * 
     * @param targetStep The target step value (0-based)
     * @param totalSteps The total number of steps
     */
    public void animateToStep(int targetStep, int totalSteps) {
        animateToStep(targetStep, totalSteps, 500); // Default 500ms animation
    }
    
    /**
     * Sets the target progress value and starts smooth animation toward it.
     * The animation will complete in the specified duration (underestimated to finish early).
     * 
     * @param targetStep The target step value (0-based)
     * @param totalSteps The total number of steps
     * @param estimatedDurationMs Estimated duration for this step in milliseconds (will be underestimated by 10% for safety)
     */
    public void animateToStep(int targetStep, int totalSteps, long estimatedDurationMs) {
        if (totalSteps == 0) {
            return;
        }
        
        // Convert step to progress percentage (0.0 to 1.0)
        double newTargetProgress = (double) targetStep / totalSteps;
        
        // If target hasn't changed, don't restart animation
        if (Math.abs(newTargetProgress - targetProgress) < 0.001) {
            return;
        }
        
        // Update target and start position
        this.startProgress = currentDisplayedProgress;
        this.targetProgress = newTargetProgress;
        
        // Calculate the progress increment for this step (distance to travel)
        double progressIncrement = Math.abs(newTargetProgress - startProgress);
        
        // Use 95% of estimated duration (reduced underestimation) to keep animation moving longer
        // This prevents the bar from stopping too early
        this.animationDurationMs = (long) (estimatedDurationMs * 0.95);
        
        // Ensure minimum duration based on progress increment to make movement visible
        // Increased minimums to prevent stopping
        long minDuration;
        if (progressIncrement < 0.005) {
            // For increments less than 0.5%, use at least 1500ms to ensure visibility
            minDuration = 1500;
        } else if (progressIncrement < 0.01) {
            // For increments 0.5-1%, use at least 1200ms
            minDuration = 1200;
        } else if (progressIncrement < 0.02) {
            // For increments 1-2%, use at least 1000ms
            minDuration = 1000;
        } else if (progressIncrement < 0.05) {
            // For increments 2-5%, use 800ms minimum
            minDuration = 800;
        } else {
            // For larger increments, use 600ms minimum
            minDuration = 600;
        }
        
        if (this.animationDurationMs < minDuration) {
            this.animationDurationMs = minDuration;
        }
        
        // Record when animation started
        this.animationStartTime = System.currentTimeMillis();
        
        // Start animation timer if not already running
        if (animationTimer == null) {
            startAnimationLoop();
        }
    }
    
    /**
     * Starts the animation loop that smoothly updates the progress bar.
     * Uses time-based interpolation to match the predicted duration.
     */
    private void startAnimationLoop() {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - animationStartTime;
                
                // Calculate progress based on elapsed time (0.0 to 1.0)
                double timeProgress = (double) elapsedTime / animationDurationMs;
                
                // Clamp to 1.0 to ensure we reach the target
                if (timeProgress >= 1.0) {
                    currentDisplayedProgress = targetProgress;
                } else {
                    // Use linear interpolation between start and target
                    // This ensures smooth animation that matches the predicted duration
                    currentDisplayedProgress = startProgress + (targetProgress - startProgress) * timeProgress;
                }
                
                // Update the progress bar on JavaFX Application Thread
                Platform.runLater(() -> {
                    progressWindow.setSmoothProgress(currentDisplayedProgress);
                });
                
                // If animation is complete, we can stop (but keep running for future animations)
                // The timer will keep running to handle new animations
            }
        }, 0, FRAME_TIME_MS); // ~60 FPS
    }
    
    /**
     * Stops the smooth progress animation and cleans up the timer.
     */
    public void stop() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
    }
    
    /**
     * Resets the displayed progress to the given value immediately (no animation).
     * 
     * @param step The step value to reset to (0-based)
     * @param totalSteps The total number of steps
     */
    public void resetToStep(int step, int totalSteps) {
        if (totalSteps == 0) {
            currentDisplayedProgress = 0.0;
            targetProgress = 0.0;
            startProgress = 0.0;
        } else {
            double progress = (double) step / totalSteps;
            currentDisplayedProgress = progress;
            targetProgress = progress;
            startProgress = progress;
        }
        animationStartTime = System.currentTimeMillis();
        animationDurationMs = 0;
    }
}

