package launcher.module_handling.progress;

import launcher.startup_window.StartupWindowManager;

import javax.swing.SwingUtilities;

/**
 * Handles progress tracking and updates for the module loading process.
 * Manages step counting and progress window updates on the Swing EDT.
 * Distributes steps evenly across the available range to ensure smooth progress bar movement.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingProgressManager {
    
    private final StartupWindowManager windowManager;
    private final int startStep; // First step available for module loading (typically 2)
    private final int endStep; // Last step available for module loading (totalSteps - 4, to reserve last 4 steps)
    private int stepIndex; // Current index in the sequence of module loading steps (0-based)
    
    /**
     * Creates a new progress manager that increments steps sequentially.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param initialStep The starting step number (typically 2)
     * @param totalSteps The total number of steps in the startup process
     */
    public ModuleLoadingProgressManager(StartupWindowManager windowManager, int initialStep, int totalSteps) {
        this.windowManager = windowManager;
        this.startStep = initialStep;
        // Reserve last 3 steps for final messages: compilation (totalSteps-2), complete (totalSteps-1), ready (totalSteps)
        // This ensures consecutive steps at the end and "Ready!" reaches 100%
        this.endStep = totalSteps - 3; // Reserve last 3 steps
        this.stepIndex = 0;
    }
    
    /**
     * Updates progress with a message and increments to the next step.
     * Each step is unique and increments sequentially to ensure the bar moves once per step.
     * 
     * @param message The progress message to display
     * @return The step number that was used
     */
    public int updateProgress(String message) {
        // Simple increment: each step gets a unique number
        // This ensures the bar moves once per step
        int step = startStep + stepIndex;
        // Only cap if we would exceed endStep, but allow going up to endStep
        if (step > endStep) {
            step = endStep;
        }
        stepIndex++;
        updateProgressDirect(step, message);
        return step;
    }
    
    /**
     * Gets the current step number (the last step that was used).
     * 
     * @return The current step number
     */
    public int getCurrentStep() {
        if (stepIndex == 0) {
            return startStep;
        }
        int step = startStep + (stepIndex - 1);
        return Math.min(step, endStep);
    }
    
    /**
     * Internal method to update progress without modifying step index.
     * 
     * @param step The step number to update
     * @param message The progress message to display
     */
    private void updateProgressDirect(int step, String message) {
        final int finalStep = step; // Make final for lambda
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(finalStep, message);
        });
    }
}

