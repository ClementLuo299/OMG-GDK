package launcher.core.lifecycle.start.startup_window.progress;

/**
 * Estimates the duration of startup steps based on message content.
 * Uses heuristics to predict how long each type of operation might take.
 * Accounts for development delays that occur after progress updates.
 * Returns conservative (underestimated) values to ensure animations complete early.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class DurationEstimator {
    
    /** Development delay time (added after most progress updates). */
    private static final long DEVELOPMENT_DELAY_MS = 1500;
    
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

