package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;
import launcher.utils.module.ModuleDiscovery;
import launcher.lifecycle.stop.Shutdown;
import gdk.internal.Logging;

/**
 * Manages the running, displaying, and lifecycle of the startup progress window.
 * This class handles showing, hiding, updating progress, and managing animations
 * for the startup window after it has been created.
 * 
 * @authors Clement Luo
 * @date August 12, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public class StartupWindowManager {
    
    /** The underlying progress window that displays startup progress to the user. */
    private final PreStartupProgressWindow progressWindow;
    
    /** The current step number in the startup process (thread-safe). */
    private final AtomicInteger currentStep = new AtomicInteger(0);
    
    /** The total number of steps in the startup process. Calculated once at creation and never changes. */
    private final int totalSteps;
    
    // ============================================================================
    // Animation Support
    // ============================================================================
    // Fields related to text animation (animated dots appearing after status messages)
    
    /** Timer that controls the text animation (dots appearing after messages). */
    private Timer animationTimer;
    
    /** The complete message text to display (before animation dots are added). */
    private String fullMessage = "";
    
    /** Current index for the animated dots (0-3 dots cycle). */
    private int currentCharIndex = 0;
    
    /** Flag indicating whether text animation is currently active. */
    private boolean isAnimating = false;
    
    // ============================================================================
    // Progress Bar Animation
    // ============================================================================
    // Fields related to the shimmer/shine animation effect on the progress bar
    
    /** Timer that controls the progress bar shimmer/shine animation effect. */
    private Timer progressAnimationTimer;
    
    /** Offset value for the shimmer animation effect on the progress bar (0.0 to 1.0). */
    private float shimmerOffset = 0.0f;
    
    /**
     * Constructs a new StartupWindowManager with the specified progress window.
     * 
     * @param progressWindow The progress window to manage
     */
    /**
     * Constructs a new StartupWindowManager with the specified progress window and total steps.
     * 
     * @param progressWindow The progress window to manage
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    private StartupWindowManager(PreStartupProgressWindow progressWindow, int totalSteps) {
        this.progressWindow = progressWindow;
        this.totalSteps = totalSteps;
    }

    /**
     * Creates a StartupWindowManager with automatically calculated total steps.
     * Calculates the total steps synchronously before displaying the window to ensure
     * accurate progress tracking from the start.
     * 
     * @return A new StartupWindowManager instance with the window already visible
     */
    public static StartupWindowManager create() {
        // Calculate total steps before creating the window to ensure accuracy
        int totalSteps;
        try {
            totalSteps = ModuleDiscovery.calculateTotalSteps();
            Logging.info("Calculated total steps: " + totalSteps);
        } catch (Exception e) {
            Logging.error("Error calculating total steps: " + e.getMessage() + ", using default 15");
            totalSteps = 15; // Fallback to default on error
        }
        
        PreStartupProgressWindow window = new PreStartupProgressWindow(totalSteps);
        StartupWindowManager manager = new StartupWindowManager(window, totalSteps);
        manager.show();
        manager.updateProgress(0, "Starting GDK application...");
        
        return manager;
    }
    
    /**
     * Gets the total number of steps in the startup process.
     * This value is calculated once at creation and never changes.
     * 
     * @return The total number of steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }
    
    /**
     * Shows the startup progress window and starts all animations.
     * This makes the window visible to the user and begins the text and progress bar animations.
     */
    public void show() {
        progressWindow.show();
        startAnimations();
    }
    
    /**
     * Hides the startup progress window and stops all animations.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        stopAnimations();
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up StartupWindowManager resources...");
            try {
                // Ensure animations are stopped
                stopAnimations();
                
                // Dispose of the progress window
                if (progressWindow != null) {
                    progressWindow.hide();
                }
                
                Logging.info("✅ StartupWindowManager cleanup completed");
            } catch (Exception e) {
                Logging.error("❌ Error during StartupWindowManager cleanup: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Updates the progress display with the current step and status message.
     * Ensures UI updates happen on the Event Dispatch Thread (EDT) for Swing components.
     * 
     * @param step The current step number (0-based)
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        // Ensure UI updates happen on the EDT for Swing components
        if (SwingUtilities.isEventDispatchThread()) {
            updateProgressInternal(step, message);
        } else {
            SwingUtilities.invokeLater(() -> updateProgressInternal(step, message));
        }
    }
    
    /**
     * Updates the progress display with a delay to slow down the progress bar animation.
     * This method schedules the progress update after a delay without blocking the UI.
     * 
     * @param step The current step number (0-based)
     * @param message The status message to display
     * @param delayMs The delay in milliseconds before updating the progress
     */
    public void updateProgressWithDelay(int step, String message, int delayMs) {
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProgress(step, message);
                delayTimer.cancel(); // Clean up the timer
            }
        }, delayMs);
    }
    
    /**
     * Internal method to update progress. Must be called on the Event Dispatch Thread.
     * Updates the current step, progress window, and starts text animation.
     * 
     * @param step The current step number (0-based)
     * @param message The status message to display
     */
    private void updateProgressInternal(int step, String message) {
        currentStep.set(step);
        progressWindow.updateProgress(step, message);
        startTextAnimation(message);
    }
    
    
    /**
     * Starts all animations (text animation and progress bar shimmer).
     * Called when the window is shown.
     */
    private void startAnimations() {
        startTextAnimation("");
        startProgressBarAnimation();
    }
    
    /**
     * Stops all animations and cleans up animation timers.
     * Called when the window is hidden or during cleanup.
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
     * Starts the text animation that cycles dots after the status message.
     * The animation cycles through 0-3 dots (., .., ..., then repeats).
     * 
     * @param baseMessage The base message text to animate (dots will be appended)
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
        }, 0, 500);
    }
    
    /**
     * Starts the shimmer/shine animation effect on the progress bar.
     * Creates a continuous animation that cycles the shimmer offset from 0.0 to 1.0.
     */
    private void startProgressBarAnimation() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
        }
        progressAnimationTimer = new Timer();
        progressAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shimmerOffset += 0.02f; // Much slower shimmer movement for development
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
        }, 0, 500); // Much slower animation (500ms instead of 200ms) for development
    }
    
    /**
     * Gets the current step number in the startup process.
     * 
     * @return The current step number (0-based)
     */
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    /**
     * Checks if the startup progress window is currently visible.
     * 
     * @return true if the window is visible, false otherwise
     */
    public boolean isVisible() {
        return progressWindow.isVisible();
    }
} 