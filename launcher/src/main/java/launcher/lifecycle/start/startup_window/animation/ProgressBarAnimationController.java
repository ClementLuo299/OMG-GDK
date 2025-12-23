package launcher.lifecycle.start.startup_window.animation;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import launcher.lifecycle.start.startup_window.PreStartupProgressWindow;

/**
 * Controls the shimmer/shine animation effect on the progress bar.
 * Creates a continuous animation that cycles the shimmer offset from 0.0 to 1.0.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public class ProgressBarAnimationController {
    
    /** Timer that controls the progress bar shimmer/shine animation effect. */
    private Timer progressAnimationTimer;
    
    /** Offset value for the shimmer animation effect on the progress bar (0.0 to 1.0). */
    private float shimmerOffset = 0.0f;
    
    /** The progress window that contains the progress bar to animate. */
    private final PreStartupProgressWindow progressWindow;
    
    /**
     * Constructs a new ProgressBarAnimationController.
     * 
     * @param progressWindow The progress window containing the progress bar to animate
     */
    public ProgressBarAnimationController(PreStartupProgressWindow progressWindow) {
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
            @Override
            public void run() {
                shimmerOffset += 0.008f; // Slower, smoother shimmer movement
                if (shimmerOffset > 1.0f) {
                    shimmerOffset = 0.0f;
                }
                SwingUtilities.invokeLater(() -> {
                    if (progressWindow.getProgressBarStyling() != null) {
                        progressWindow.getProgressBarStyling().setShimmerOffset(shimmerOffset);
                    }
                    progressWindow.repaintProgressBar();
                });
            }
        }, 0, 16); // ~60 FPS for smooth shimmer animation
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

