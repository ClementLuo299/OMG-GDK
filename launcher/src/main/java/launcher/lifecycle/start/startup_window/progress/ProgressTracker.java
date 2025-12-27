package launcher.lifecycle.start.startup_window.progress;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks progress state for the startup process.
 * Manages the current step and total steps in a thread-safe manner.
 * Also estimates step durations based on message content.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class ProgressTracker {
    
    /** The current step number in the startup process (thread-safe). */
    private final AtomicInteger currentStep = new AtomicInteger(0);
    
    /** The total number of steps in the startup process. Calculated once at creation and never changes. */
    private final int totalSteps;
    
    /** Development delay time (added after most progress updates). */
    private static final long DEVELOPMENT_DELAY_MS = 1500;
    
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
    
    /**
     * Estimates the duration of a step based on the message content.
     * Uses heuristics to predict how long each type of operation might take.
     * Accounts for development delays (1500ms) that occur after progress updates.
     * Returns conservative (underestimated) values to ensure animation completes early.
     * 
     * @param message The status message for the step
     * @return Estimated duration in milliseconds (underestimated)
     */
    public long estimateDuration(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Base operation time estimates
        long baseDuration;
        
        // Quick operations (UI updates, simple state changes)
        if (lowerMessage.contains("starting") || lowerMessage.contains("ready") || 
            lowerMessage.contains("complete") || lowerMessage.contains("loading user interface")) {
            baseDuration = 500; // ~500ms (increased from 300ms)
        }
        // Module discovery operations
        else if (lowerMessage.contains("discovering") || lowerMessage.contains("discovery")) {
            baseDuration = 1200; // ~1.2 seconds (increased from 800ms)
        }
        // Compilation/build operations (usually slower)
        else if (lowerMessage.contains("compil") || lowerMessage.contains("build") || 
                 lowerMessage.contains("building")) {
            baseDuration = 2800; // ~2.8 seconds (increased from 2000ms)
        }
        // Loading/processing operations
        else if (lowerMessage.contains("loading") || lowerMessage.contains("processing") || 
                 lowerMessage.contains("preparing") || lowerMessage.contains("initializing")) {
            baseDuration = 1500; // ~1.5 seconds (increased from 1000ms)
        }
        // Checking operations
        else if (lowerMessage.contains("checking") || lowerMessage.contains("validating")) {
            baseDuration = 900; // ~900ms (increased from 600ms)
        }
        // Default for unknown operations (conservative estimate)
        else {
            baseDuration = 800; // ~800ms default (increased from 500ms)
        }
        
        // Add development delay time since delays occur after progress updates
        // Most steps have development delays, so account for them in the estimate
        return baseDuration + DEVELOPMENT_DELAY_MS;
    }
}

