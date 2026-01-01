package launcher.core.lifecycle.stop.helpers;

import gdk.internal.Logging;

import java.util.List;

/**
 * Executes cleanup tasks during shutdown.
 * Responsible for running registered cleanup tasks and handling execution errors.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class CleanupTaskExecutor {
    
    private CleanupTaskExecutor() {}
    
    /**
     * Executes all registered cleanup tasks.
     * 
     * @param cleanupTasks List of cleanup tasks to execute
     */
    public static void executeCleanupTasks(List<Runnable> cleanupTasks) {
        Logging.info("Executing cleanup tasks...");
        
        // Execute each cleanup task, handling errors gracefully
        for (Runnable cleanupTask : cleanupTasks) {
            try {
                if (cleanupTask != null) {
                    cleanupTask.run();
                }
            } catch (Exception e) {
                // Log errors but continue with other cleanup tasks
                Logging.error("Error during cleanup task: " + e.getMessage(), e);
            }
        }
    }
}

