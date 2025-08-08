package launcher.lifecycle.start;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages the running, displaying, and lifecycle of the startup progress window.
 * This class handles showing, hiding, updating progress, and managing animations
 * for the startup window after it has been created.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public class StartupWindowManager {
    
    private final PreStartupProgressWindow progressWindow;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private int totalSteps = 15;
    
    // Animation support
    private Timer animationTimer;
    private String fullMessage = "";
    private int currentCharIndex = 0;
    private boolean isAnimating = false;
    
    // Progress bar animation
    private Timer progressAnimationTimer;
    private float shimmerOffset = 0.0f;
    
    /**
     * Create a new startup window manager with the given progress window.
     * 
     * @param progressWindow The progress window to manage
     */
    public StartupWindowManager(PreStartupProgressWindow progressWindow) {
        this.progressWindow = progressWindow;
    }
    
    /**
     * Set the total number of steps for progress tracking.
     * 
     * @param totalSteps The total number of steps
     */
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        progressWindow.setTotalSteps(totalSteps);
    }
    
    /**
     * Show the progress window.
     */
    public void show() {
        System.out.println("🎬 Showing pre-startup progress window");
        progressWindow.show();
        startAnimations();
    }
    
    /**
     * Hide the progress window and clean up resources.
     */
    public void hide() {
        System.out.println("🏁 Hiding pre-startup progress window");
        stopAnimations();
        progressWindow.hide();
    }
    
    /**
     * Update the progress with current step and message.
     * 
     * @param step The current step number
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        currentStep.set(step);
        progressWindow.updateProgress(step, message);
        
        // Start text animation for the new message
        startTextAnimation(message);
    }
    
    /**
     * Update the progress with current step, total steps, and message.
     * 
     * @param step The current step number
     * @param message The status message to display
     * @param totalSteps The total number of steps
     */
    public void updateProgress(int step, String message, int totalSteps) {
        this.totalSteps = totalSteps;
        progressWindow.setTotalSteps(totalSteps);
        updateProgress(step, message);
    }
    
    /**
     * Start all animations (text and progress bar).
     */
    private void startAnimations() {
        startTextAnimation("");
        startProgressBarAnimation();
    }
    
    /**
     * Stop all animations and clean up timers.
     */
    private void stopAnimations() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
            progressAnimationTimer = null;
        }
        
        isAnimating = false;
    }
    
    /**
     * Start text animation with dots effect.
     * 
     * @param baseMessage The base message to animate
     */
    private void startTextAnimation(String baseMessage) {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        fullMessage = baseMessage;
        currentCharIndex = 0;
        isAnimating = true;
        
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isAnimating) {
                    return;
                }
                
                currentCharIndex++;
                if (currentCharIndex > 3) {
                    currentCharIndex = 0;
                }
                
                SwingUtilities.invokeLater(() -> {
                    String animatedMessage = fullMessage + ".".repeat(currentCharIndex);
                    progressWindow.updateStatusText(animatedMessage);
                });
            }
        }, 0, 500); // Update every 500ms
    }
    
    /**
     * Start progress bar shimmer animation.
     */
    private void startProgressBarAnimation() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
        }
        
        progressAnimationTimer = new Timer();
        progressAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shimmerOffset += 0.1f;
                if (shimmerOffset > 1.0f) {
                    shimmerOffset = 0.0f;
                }
                
                SwingUtilities.invokeLater(() -> {
                    // Update the shimmer offset in the styling UI
                    if (progressWindow.getProgressBarStyling() != null) {
                        progressWindow.getProgressBarStyling().setShimmerOffset(shimmerOffset);
                    }
                    progressWindow.repaintProgressBar();
                });
            }
        }, 0, 50); // 20 FPS animation
    }
    
    /**
     * Get the current step number.
     * 
     * @return The current step
     */
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    /**
     * Get the total number of steps.
     * 
     * @return The total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }
    
    /**
     * Check if the window is currently visible.
     * 
     * @return true if the window is visible, false otherwise
     */
    public boolean isVisible() {
        return progressWindow.isVisible();
    }
} 