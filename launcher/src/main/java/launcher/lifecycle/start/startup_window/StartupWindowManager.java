package launcher.lifecycle.start.startup_window;

import launcher.utils.module.ModuleDiscovery;
import launcher.lifecycle.stop.Shutdown;
import gdk.internal.Logging;
import launcher.lifecycle.start.startup_window.animation.ProgressBarAnimationController;
import launcher.lifecycle.start.startup_window.animation.SmoothProgressAnimationController;
import launcher.lifecycle.start.startup_window.tracking.ProgressTracker;
import launcher.lifecycle.start.startup_window.estimation.StepDurationEstimator;

import javax.swing.SwingUtilities;

/**
 * Manages the running, displaying, and lifecycle of the startup progress window.
 * This class handles showing, hiding, updating progress, and managing animations
 * for the startup window after it has been created.
 * 
 * @authors Clement Luo
 * @date August 12, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class StartupWindowManager {
    
    /** The underlying progress window that displays startup progress to the user. */
    private final StartupWindow progressWindow;
    
    /** Tracks progress state (current step and total steps). */
    private final ProgressTracker progressTracker;
    
    // ============================================================================
    // Animation Controllers
    // ============================================================================
    
    /** Controller for progress bar shimmer/shine animation effect. */
    private final ProgressBarAnimationController progressBarAnimationController;
    
    /** Controller for smooth progress bar animation between steps. */
    private final SmoothProgressAnimationController smoothProgressAnimationController;
    
    /** Estimates step durations based on message content. */
    private final StepDurationEstimator stepDurationEstimator;
    
    /**
     * Constructs a new StartupWindowManager with the specified progress window and total steps.
     * 
     * @param progressWindow The progress window to manage
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    private StartupWindowManager(StartupWindow progressWindow, int totalSteps) {
        this.progressWindow = progressWindow;
        this.progressTracker = new ProgressTracker(totalSteps);
        this.progressBarAnimationController = new ProgressBarAnimationController(progressWindow);
        this.smoothProgressAnimationController = new SmoothProgressAnimationController(progressWindow);
        this.stepDurationEstimator = new StepDurationEstimator();
    }

    /**
     * Calculates the total steps for the startup process.
     * 
     * @return The total number of steps, or 15 as a fallback if calculation fails
     */
    private static int calculateTotalSteps() {
        try {
            int steps = ModuleDiscovery.calculateTotalSteps();
            Logging.info("Calculated total steps: " + steps);
            return steps;
        } catch (Exception e) {
            Logging.error("Error calculating total steps: " + e.getMessage() + ", using default 15");
            return 15; // Fallback to default on error
        }
    }
    
    /**
     * Creates a StartupWindowManager with automatically calculated total steps.
     * Calculates the total steps synchronously before displaying the window to ensure
     * accurate progress tracking from the start.
     * 
     * @return A new StartupWindowManager instance with the window already visible
     */
    public static StartupWindowManager show() {
        // Calculate total steps before creating the window to ensure accuracy
        final int totalSteps = calculateTotalSteps();
        
        // Create the window and manager on the EDT (Swing components must be created on EDT)
        final StartupWindow[] windowRef = new StartupWindow[1];
        final StartupWindowManager[] managerRef = new StartupWindowManager[1];
        
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // Already on EDT, create directly
                windowRef[0] = new StartupWindow(totalSteps);
                managerRef[0] = new StartupWindowManager(windowRef[0], totalSteps);
            } else {
                // Not on EDT, use invokeAndWait to create on EDT
                SwingUtilities.invokeAndWait(() -> {
                    windowRef[0] = new StartupWindow(totalSteps);
                    managerRef[0] = new StartupWindowManager(windowRef[0], totalSteps);
                });
            }
        } catch (Exception e) {
            Logging.error("Error creating startup window: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create startup window", e);
        }
        
        StartupWindow window = windowRef[0];
        StartupWindowManager manager = managerRef[0];

        // Initialize smooth progress to 0 and show the window
        manager.smoothProgressAnimationController.resetToStep(0, totalSteps);
        window.show();
        // Shimmer animation disabled - removed for static progress bar
        // manager.progressBarAnimationController.start();
        manager.updateProgress(0, "Starting GDK application...");
        
        return manager;
    }

    /**
     * Hides the startup progress window and stops all animations.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        // Stop all animations
        progressBarAnimationController.stop();
        smoothProgressAnimationController.stop();
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("Cleaning up StartupWindowManager resources");
            try {
                // Ensure animations are stopped (redundant but safe - already stopped above)
                progressBarAnimationController.stop();
                smoothProgressAnimationController.stop();
                
                // Dispose of the progress window
                if (progressWindow != null) {
                    progressWindow.hide();
                }
                
                Logging.info("StartupWindowManager cleanup completed");
            } catch (Exception e) {
                Logging.error("Error during StartupWindowManager cleanup: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Gets the total number of steps in the startup process.
     * This value is calculated once at creation and never changes.
     * 
     * @return The total number of steps
     */
    public int getTotalSteps() {
        return progressTracker.getTotalSteps();
    }

    /**
     * Updates the progress display with the current step and status message.
     * 
     * @param step The current step number (starts at 0)
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        // Update the current step
        progressTracker.setCurrentStep(step);

        // Update the progress window with the current step and status message
        progressWindow.updateProgress(step, message);

        // Estimate the duration of the step and start the smooth animation toward the target step
        long estimatedDuration = stepDurationEstimator.estimateDuration(message);
        smoothProgressAnimationController.animateToStep(step, progressTracker.getTotalSteps(), estimatedDuration);
    }
} 