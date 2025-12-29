package launcher.utils;

import gdk.internal.Logging;

/**
 * Utility class for adding delays during startup for debugging purposes.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class StartupDelayUtil {

    /**
     * Flag to enable/disable development delays.
     * When true, adds delays between startup steps for easier debugging.
     * Set to false for normal operation.
     */
    private static final boolean ENABLE_DEVELOPMENT_DELAYS = false;

    private StartupDelayUtil() {}

    /**
     * Add a development delay to slow down the startup process so users can read each message.
     * This method adds a delay with logging to make it clear what's happening.
     * The delay is only executed if ENABLE_DEVELOPMENT_DELAYS is set to true.
     * 
     * @param reason The reason for the delay (for logging)
     */
    public static void addDevelopmentDelay(String reason) {
        if (!ENABLE_DEVELOPMENT_DELAYS) {
            return; // Skip delay if disabled
        }
        
        final long delayMs = 1500; // 1.5 seconds
        final long startTime = System.currentTimeMillis();
        
        Logging.info("DEVELOPMENT DELAY: " + reason + " - waiting 1.5 seconds...");
        
        try {
            Thread.sleep(delayMs);
            final long actualDelay = System.currentTimeMillis() - startTime;
            Logging.info("Development delay completed for: " + reason + " (actual: " + actualDelay + "ms)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final long actualDelay = System.currentTimeMillis() - startTime;
            Logging.warning("Development delay INTERRUPTED for: " + reason + " (was: " + actualDelay + "ms)");
        }
    }
}

