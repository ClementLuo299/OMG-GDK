package launcher.lifecycle.start.startup_window.animation;

import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;
import launcher.lifecycle.start.startup_window.IStartupWindow;

/**
 * Controls the shimmer/shine animation effect on the progress bar.
 * Creates a continuous animation that cycles the shimmer offset from 0.0 to 1.0.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class ProgressBarAnimationController {
    
    /** Timer that controls the progress bar shimmer/shine animation effect. */
    private Timer progressAnimationTimer;
    
    /** Offset value for the shimmer animation effect on the progress bar (0.0 to 1.0). */
    private float shimmerOffset = 0.0f;
    
    /** The progress window that contains the progress bar to animate. */
    private final launcher.lifecycle.start.startup_window.IStartupWindow progressWindow;
    
    /**
     * Constructs a new ProgressBarAnimationController.
     * 
     * @param progressWindow The progress window containing the progress bar to animate
     */
    public ProgressBarAnimationController(launcher.lifecycle.start.startup_window.IStartupWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Starts the shimmer/shine animation effect on the progress bar.
     * Creates a continuous animation that cycles the shimmer offset from 0.0 to 1.0.
     */
    public void start() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
        }
        progressAnimationTimer = new Timer();
        progressAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            private boolean isMovingForward = true;
            
            @Override
            public void run() {
                // Animate back and forth between 0.0 and 1.0
                if (isMovingForward) {
                    shimmerOffset += 0.01f;
                    if (shimmerOffset >= 1.0f) {
                        shimmerOffset = 1.0f;
                        isMovingForward = false;
                    }
                } else {
                    shimmerOffset -= 0.01f;
                    if (shimmerOffset <= 0.0f) {
                        shimmerOffset = 0.0f;
                        isMovingForward = true;
                    }
                }
                
                SwingUtilities.invokeLater(() -> {
                    // Shimmer animation disabled - no longer updating shimmer offset
                    // progressWindow.getProgressBarStyling().setShimmerOffset(shimmerOffset);
                    // Repainting not needed since shimmer is disabled
                    // progressWindow.repaintProgressBar();
                });
            }
        }, 0, 16); // ~60 FPS for smooth color animation
    }
    
    /**
     * Stops the progress bar animation and cleans up the timer.
     */
    public void stop() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
            progressAnimationTimer = null;
        }
    }
}

