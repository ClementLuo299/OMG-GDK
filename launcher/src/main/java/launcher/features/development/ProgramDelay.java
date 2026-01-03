package launcher.features.development_features;

import gdk.internal.Logging;

/**
 * Utility class for adding delays during startup for debugging purposes.
 * 
 * <p>This class has a single responsibility: providing controlled delays
 * during application startup to aid in debugging and development.
 * 
 * <p>Delays are controlled by the ENABLE_DEVELOPMENT_DELAYS flag and are
 * disabled by default for normal operation.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class StartupDelayUtil {

    // ==================== CONSTANTS ====================
    
    /**
     * Flag to enable/disable development delays.
     * 
     * <p>When true, adds delays between startup steps for easier debugging.
     * Set to false for normal operation.
     */
    private static final boolean ENABLE_DEVELOPMENT_DELAYS = false;
    
    /**
     * Duration of the development delay in milliseconds.
     * 
     * <p>This is the fixed delay duration used when development delays are enabled.
     */
    private static final long DELAY_DURATION_MS = 5000; // 5 seconds
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private StartupDelayUtil() {
        throw new AssertionError("StartupDelayUtil should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Adds a development delay to slow down the startup process.
     * 
     * <p>This method adds a delay with logging to make it clear what's happening.
     * The delay is only executed if ENABLE_DEVELOPMENT_DELAYS is set to true.
     * 
     * <p>The delay duration is controlled by DELAY_DURATION_MS. If the thread is interrupted,
     * the interruption status is preserved.
     * 
     * @param reason The reason for the delay (for logging purposes)
     */
    public static void addDevelopmentDelay(String reason) {
        if (!ENABLE_DEVELOPMENT_DELAYS) {
            return; // Skip delay if disabled
        }
        
        final long startTime = System.currentTimeMillis();
        final long delaySeconds = DELAY_DURATION_MS / 1000;
        
        Logging.info("DEVELOPMENT DELAY: " + reason + " - waiting " + delaySeconds + " seconds...");
        
        // Add a delay for the specified duration
        try {
            // Sleep for the specified duration
            Thread.sleep(DELAY_DURATION_MS);
            final long actualDelay = System.currentTimeMillis() - startTime;
            Logging.info("Development delay completed for: " + reason + " (actual: " + actualDelay + "ms)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long actualDelay = System.currentTimeMillis() - startTime;
            Logging.warning("Development delay INTERRUPTED for: " + reason + " (was: " + actualDelay + "ms)");
        }
    }
}

