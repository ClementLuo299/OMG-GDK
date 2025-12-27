package launcher.lifecycle.start.startup_window;

import gdk.internal.Logging;
import launcher.lifecycle.start.startup_window.animation.BarAnimationController;
import launcher.lifecycle.start.startup_window.progress.ProgressTracker;
import launcher.lifecycle.start.startup_window.progress.DurationEstimator;
import launcher.lifecycle.start.startup_window.ui.StartupWindowUIUpdateHandler;
import launcher.lifecycle.start.startup_window.window_control.WindowController;
import launcher.lifecycle.start.startup_window.progress.ProgressUpdateController;
import launcher.utils.module.ModuleDiscovery;

import javax.swing.SwingUtilities;

/**
 * Coordinates the startup progress window system.
 * This class acts as a facade/coordinator that delegates to specialized components
 * for window lifecycle, progress updates, UI handling, and step calculation.
 * 
 * @authors Clement Luo
 * @date August 12, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class StartupWindowManager {
    
    /** Controls window visibility and lifecycle (show/hide/cleanup). */
    private final WindowController windowController;
    
    /** Controls progress updates. */
    private final ProgressUpdateController progressUpdateController;
    
    /**
     * Constructs a new StartupWindowManager with the specified progress window and total steps.
     * 
     * @param progressWindow The progress window to manage
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    private StartupWindowManager(StartupWindow progressWindow, int totalSteps) {
        
        // Create shared components
        ProgressTracker progressTracker = new ProgressTracker(totalSteps);
        DurationEstimator durationEstimator = new DurationEstimator();
        StartupWindowUIUpdateHandler uiUpdateHandler = new StartupWindowUIUpdateHandler(progressWindow);
        
        // Create animation controllers
        BarAnimationController barAnimationController = 
            new BarAnimationController(uiUpdateHandler);
        
        // Create specialized managers
        this.windowController = new WindowController(
            progressWindow,
            barAnimationController
        );
        
        this.progressUpdateController = new ProgressUpdateController(
            progressTracker,
            durationEstimator,
            barAnimationController,
            uiUpdateHandler,
            totalSteps
        );
    }
    
    /**
     * Creates a StartupWindowManager with automatically calculated total steps.
     * Shows the window immediately with an estimated step count, then calculates
     * the actual steps in the background and updates the window.
     * 
     * @return A new StartupWindowManager instance with the window already visible
     */
    public static StartupWindowManager createAndShow() {
        // Use an estimated step count to show window immediately (actual count calculated later)
        final int estimatedSteps = 15;
        
        // Create the window and manager on the Event Dispatch Thread (required for Swing)
        final StartupWindow[] windowRef = new StartupWindow[1];
        final StartupWindowManager[] managerRef = new StartupWindowManager[1];
        
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // Already on EDT - create components directly
                windowRef[0] = new StartupWindow(estimatedSteps);
                managerRef[0] = new StartupWindowManager(windowRef[0], estimatedSteps);
            } else {
                // Not on EDT - dispatch to EDT and wait for completion
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        windowRef[0] = new StartupWindow(estimatedSteps);
                        managerRef[0] = new StartupWindowManager(windowRef[0], estimatedSteps);
                    } catch (Exception e) {
                        Logging.error("Error creating startup window: " + e.getMessage(), e);
                        throw new RuntimeException("Failed to create startup window", e);
                    }
                });
            }
        } catch (Exception e) {
            // Handle both direct creation failures and EDT dispatch failures
            Logging.error("Error creating startup window: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create startup window", e);
        }
        
        // Get the manager
        StartupWindowManager manager = managerRef[0];

        // Display the window and initialize progress
        manager.windowController.show();
        manager.progressUpdateController.resetToStep(0, estimatedSteps);
        manager.updateProgress(0, "Starting GDK Application");
        
        // Calculate actual step count in background (doesn't block window display)
        new Thread(() -> {
            final int actualSteps = ModuleDiscovery.calculateTotalSteps();
            if (actualSteps != estimatedSteps) {
                Logging.info("Calculated total steps: " + actualSteps + " (estimated: " + estimatedSteps + ")");
                // Note: The progress will continue with the estimated total for simplicity
            }
        }, "StartupWindow-StepCalculator").start();
        
        return manager;
    }

    /**
     * Hides the startup progress window and stops all animations.
     * Also registers cleanup tasks with the shutdown system to ensure proper resource cleanup.
     */
    public void hide() {
        windowController.hide();
    }
    
    /**
     * Gets the total number of steps in the startup process.
     * This value is calculated once at creation and never changes.
     * 
     * @return The total number of steps
     */
    public int getTotalSteps() {
        return progressUpdateController.getTotalSteps();
    }

    /**
     * Updates the progress display with the current step and status message.
     * 
     * @param step The current step number (starts at 0)
     * @param message The status message to display
     */
    public void updateProgress(int step, String message) {
        progressUpdateController.updateProgress(step, message);
    }
} 