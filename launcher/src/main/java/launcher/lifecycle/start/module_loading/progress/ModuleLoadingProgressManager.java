package launcher.lifecycle.start.module_loading.progress;

import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;

import javax.swing.SwingUtilities;

/**
 * Handles progress tracking and updates for the module loading process.
 * Manages step counting and progress window updates on the Swing EDT.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingProgressManager {
    
    private final StartupWindowManager windowManager;
    private int currentStep;
    
    /**
     * Creates a new progress manager.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param initialStep The starting step number (typically 1)
     */
    public ModuleLoadingProgressManager(StartupWindowManager windowManager, int initialStep) {
        this.windowManager = windowManager;
        this.currentStep = initialStep;
    }
    
    /**
     * Updates progress with a message and increments the step counter.
     * 
     * @param message The progress message to display
     * @return The step number that was used
     */
    public int updateProgress(String message) {
        int step = currentStep++;
        updateProgress(step, message);
        return step;
    }
    
    /**
     * Updates progress with a message at a specific step without incrementing.
     * 
     * @param step The step number to update
     * @param message The progress message to display
     */
    public void updateProgress(int step, String message) {
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(step, message);
        });
    }
    
    /**
     * Gets the current step number.
     * 
     * @return The current step number
     */
    public int getCurrentStep() {
        return currentStep;
    }
    
    /**
     * Increments the step counter and returns the new value.
     * 
     * @return The incremented step number
     */
    public int incrementStep() {
        return currentStep++;
    }
    
    /**
     * Sets the current step to a specific value.
     * 
     * @param step The step number to set
     */
    public void setCurrentStep(int step) {
        this.currentStep = step;
    }
    
    /**
     * Updates progress and adds a development delay.
     * 
     * @param message The progress message
     * @param delayMessage The delay message for debugging
     * @return The step number that was used
     */
    public int updateProgressWithDelay(String message, String delayMessage) {
        int step = updateProgress(message);
        StartupDelayUtil.addDevelopmentDelay(delayMessage);
        return step;
    }
}

