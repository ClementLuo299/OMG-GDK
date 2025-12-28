package launcher.lifecycle.stop;

import gdk.internal.Logging;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles shutdown of executor services during application shutdown.
 * Responsible for gracefully shutting down executors with timeout handling.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ExecutorServiceShutdown {
    
    private ExecutorServiceShutdown() {}
    
    /**
     * Shuts down all executor services gracefully with timeout.
     * 
     * @param executors List of executor services to shut down
     */
    public static void shutdownExecutors(List<ExecutorService> executors) {
        Logging.info("Shutting down background executor services...");
        
        for (ExecutorService executor : executors) {
            try {
                if (executor != null && !executor.isShutdown()) {
                    // Attempt graceful shutdown with 2 second timeout
                    executor.shutdown();
                    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                        Logging.warning("Force shutting down executor service");
                        executor.shutdownNow();
                    }
                }
            } catch (Exception e) {
                // Log error and force shutdown as fallback
                Logging.error("Error shutting down executor: " + e.getMessage(), e);
                try {
                    if (executor != null) {
                        executor.shutdownNow();
                    }
                } catch (Exception ignored) {
                    // Ignore errors during force shutdown
                }
            }
        }
    }
    
    /**
     * Forcefully shuts down all executor services immediately.
     * Used for emergency shutdown scenarios.
     * 
     * @param executors List of executor services to force shutdown
     */
    public static void forceShutdownExecutors(List<ExecutorService> executors) {
        for (ExecutorService executor : executors) {
            try {
                if (executor != null) {
                    executor.shutdownNow();
                }
            } catch (Exception ignored) {
                // Ignore errors during force shutdown
            }
        }
    }
}

