package launcher.lifecycle.start.startup_window;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks progress state for the startup process.
 * Manages the current step and total steps in a thread-safe manner.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 22, 2025
 * @since Beta 1.0
 */
public class ProgressTracker {
    
    /** The current step number in the startup process (thread-safe). */
    private final AtomicInteger currentStep = new AtomicInteger(0);
    
    /** The total number of steps in the startup process. Calculated once at creation and never changes. */
    private final int totalSteps;
    
    /**
     * Constructs a new ProgressTracker with the specified total steps.
     * 
     * @param totalSteps The total number of steps (calculated once and never changes)
     */
    public ProgressTracker(int totalSteps) {
        this.totalSteps = totalSteps;
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
     * Gets the current step number in the startup process.
     * 
     * @return The current step number (0-based)
     */
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    /**
     * Sets the current step number.
     * 
     * @param step The step number to set (0-based)
     */
    public void setCurrentStep(int step) {
        currentStep.set(step);
    }
    
    /**
     * Calculates the progress percentage.
     * 
     * @return The progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (totalSteps == 0) {
            return 0;
        }
        return (int) ((currentStep.get() * 100.0) / totalSteps);
    }
}

