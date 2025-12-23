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
    // Animation Controllers
    // ============================================================================
    
    /** Controller for text animation (animated dots appearing after status messages). */
    private final TextAnimationController textAnimationController;
    
    /** Controller for progress bar shimmer/shine animation effect. */
    private final ProgressBarAnimationController progressBarAnimationController;
    
    /**
     * Constructs a new StartupWindowManager with the specified progress window and total steps.
     * 
     * @param progressWindow The progress window to manage
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    private StartupWindowManager(PreStartupProgressWindow progressWindow, int totalSteps) {
        this.progressWindow = progressWindow;
        this.totalSteps = totalSteps;
        this.textAnimationController = new TextAnimationController(progressWindow);
        this.progressBarAnimationController = new ProgressBarAnimationController(progressWindow);
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
        textAnimationController.start(message);
    }
    
    /**
     * Starts all animations (text animation and progress bar shimmer).
     * Called when the window is shown.
     */
    private void startAnimations() {
        textAnimationController.start("");
        progressBarAnimationController.start();
    }
    
    /**
     * Stops all animations and cleans up animation timers.
     * Called when the window is hidden or during cleanup.
     */
    private void stopAnimations() {
        textAnimationController.stop();
        progressBarAnimationController.stop();
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