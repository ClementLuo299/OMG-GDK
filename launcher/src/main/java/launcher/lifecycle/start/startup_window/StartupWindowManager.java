package launcher.lifecycle.start.startup_window;

import javax.swing.*;
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
    
    /** Tracks progress state (current step and total steps). */
    private final ProgressTracker progressTracker;
    
    // ============================================================================
    // Animation Controllers
    // ============================================================================
    
    /** Controller for progress bar shimmer/shine animation effect. */
    private final ProgressBarAnimationController progressBarAnimationController;
    
    /** Controller for smooth progress bar animation between steps. */
    private final SmoothProgressAnimationController smoothProgressAnimationController;
    
    /**
     * Constructs a new StartupWindowManager with the specified progress window and total steps.
     * 
     * @param progressWindow The progress window to manage
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    private StartupWindowManager(PreStartupProgressWindow progressWindow, int totalSteps) {
        this.progressWindow = progressWindow;
        this.progressTracker = new ProgressTracker(totalSteps);
        this.progressBarAnimationController = new ProgressBarAnimationController(progressWindow);
        this.smoothProgressAnimationController = new SmoothProgressAnimationController(progressWindow);
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
        // Initialize smooth progress to 0
        manager.initializeSmoothProgress(0, totalSteps);
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
        return progressTracker.getTotalSteps();
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
            Logging.info("Cleaning up StartupWindowManager resources");
            try {
                // Ensure animations are stopped
                stopAnimations();
                
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
     * Initializes the smooth progress animation to a specific step.
     * 
     * @param step The initial step value (0-based)
     * @param totalSteps The total number of steps
     */
    private void initializeSmoothProgress(int step, int totalSteps) {
        smoothProgressAnimationController.resetToStep(step, totalSteps);
    }
    
    /**
     * Starts all animations (progress bar shimmer and smooth progress animation).
     * Called when the window is shown.
     */
    private void startAnimations() {
        progressBarAnimationController.start();
    }
    
    /**
     * Stops all animations and cleans up animation timers.
     * Called when the window is hidden or during cleanup.
     */
    private void stopAnimations() {
        progressBarAnimationController.stop();
        smoothProgressAnimationController.stop();
    }

    /**
     * Updates the progress display with the current step and status message.
     * Uses smooth animation to transition between steps instead of jumping.
     * Estimates the step duration based on the message content.
     * 
     * @param step The current step number (0-based)
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        long estimatedDuration = estimateStepDuration(message);
        updateProgress(step, message, estimatedDuration);
    }
    
    /**
     * Updates the progress display with the current step, status message, and estimated duration.
     * Uses smooth animation timed to complete in the estimated duration (underestimated by 20%).
     * 
     * @param step The current step number (0-based)
     * @param message The status message to display
     * @param estimatedDurationMs Estimated duration for this step in milliseconds
     */
    public void updateProgress(int step, String message, long estimatedDurationMs) {
        progressTracker.setCurrentStep(step);
        // Update the status message and step count display
        progressWindow.updateProgress(step, message);
        // Start smooth animation toward the target step with estimated duration
        smoothProgressAnimationController.animateToStep(step, progressTracker.getTotalSteps(), estimatedDurationMs);
    }
    
    /**
     * Estimates the duration of a step based on the message content.
     * Uses heuristics to predict how long each type of operation might take.
     * Accounts for development delays (1500ms) that occur after progress updates.
     * Returns conservative (underestimated) values to ensure animation completes early.
     * 
     * @param message The status message for the step
     * @return Estimated duration in milliseconds (underestimated)
     */
    private long estimateStepDuration(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Development delay time (added after most progress updates)
        final long DEVELOPMENT_DELAY_MS = 1500;
        
        // Base operation time estimates
        long baseDuration;
        
        // Quick operations (UI updates, simple state changes)
        if (lowerMessage.contains("starting") || lowerMessage.contains("ready") || 
            lowerMessage.contains("complete") || lowerMessage.contains("loading user interface")) {
            baseDuration = 300; // ~300ms
        }
        // Module discovery operations
        else if (lowerMessage.contains("discovering") || lowerMessage.contains("discovery")) {
            baseDuration = 800; // ~800ms
        }
        // Compilation/build operations (usually slower)
        else if (lowerMessage.contains("compil") || lowerMessage.contains("build") || 
                 lowerMessage.contains("building")) {
            baseDuration = 2000; // ~2 seconds
        }
        // Loading/processing operations
        else if (lowerMessage.contains("loading") || lowerMessage.contains("processing") || 
                 lowerMessage.contains("preparing") || lowerMessage.contains("initializing")) {
            baseDuration = 1000; // ~1 second
        }
        // Checking operations
        else if (lowerMessage.contains("checking") || lowerMessage.contains("validating")) {
            baseDuration = 600; // ~600ms
        }
        // Default for unknown operations (conservative estimate)
        else {
            baseDuration = 500; // ~500ms default
        }
        
        // Add development delay time since delays occur after progress updates
        // Most steps have development delays, so account for them in the estimate
        return baseDuration + DEVELOPMENT_DELAY_MS;
    }
} 