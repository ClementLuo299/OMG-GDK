package launcher.ui_areas.startup_window.loading_spinner;

import launcher.ui_areas.startup_window.styling_theme.LoadingSpinnerStyle;

import javax.swing.Timer;

/**
 * Controls the animation of a LoadingSpinner component.
 * 
 * Manages the rotation animation by updating the spinner's rotation angle
 * and triggering repaints. Handles starting and stopping the animation.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public final class LoadingSpinnerAnimator {
    
    /** The spinner component this controller manages */
    private final LoadingSpinner spinner;
    
    /** Current rotation angle in degrees (0-360) */
    private double rotationAngle = 0.0;
    
    /** Timer that drives the animation */
    private Timer animationTimer;
    
    /**
     * Creates a new controller for the given spinner.
     * 
     * @param spinner The spinner component to control
     */
    public LoadingSpinnerAnimator(LoadingSpinner spinner) {
        this.spinner = spinner;
        initializeTimer();
    }
    
    /**
     * Starts the spinner animation.
     * 
     * Begins the continuous rotation animation. Safe to call multiple times;
     * will only start if not already running.
     */
    public void start() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
    
    /**
     * Stops the spinner animation.
     * 
     * Halts the rotation animation. Safe to call multiple times;
     * will only stop if currently running.
     */
    public void stop() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    /**
     * Initializes the animation timer.
     *
     * Creates a timer that updates the rotation angle and triggers repaints
     * at the configured frame rate.
     */
    private void initializeTimer() {
        animationTimer = new Timer(LoadingSpinnerStyle.ANIMATION_DELAY_MS, e -> {
            // Rotate by the configured increment per frame
            rotationAngle += LoadingSpinnerStyle.ROTATION_INCREMENT;
            if (rotationAngle >= 360.0) {
                rotationAngle -= 360.0; // Keep angle in 0-360 range
            }

            // Update the spinner's rotation angle and trigger repaint
            spinner.setRotationAngle(rotationAngle);
            spinner.repaint();
        });
    }
}

