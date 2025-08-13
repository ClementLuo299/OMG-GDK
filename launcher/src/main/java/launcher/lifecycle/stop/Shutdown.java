package launcher.lifecycle.stop;

import gdk.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles the shutdown process of the GDK application.
 * Ensures all resources are properly cleaned up and the application exits cleanly.
 * 
 * @author: Clement Luo
 * @date: August 6, 2025
 * @edited: August 12, 2025
 * @since: 1.0
 */
public class Shutdown {
    
    // Track resources that need cleanup
    private static final List<Runnable> cleanupTasks = new ArrayList<>();
    private static final List<ExecutorService> backgroundExecutors = new ArrayList<>();
    
    // Flag to prevent shutdown hook interference
    private static volatile boolean isShuttingDown = false;
    
    /**
     * Check if shutdown is currently in progress.
     * 
     * @return true if shutdown is in progress, false otherwise
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
    
    /**
     * Register a cleanup task to be executed during shutdown.
     * 
     * @param cleanupTask The cleanup task to register
     */
    public static void registerCleanupTask(Runnable cleanupTask) {
        if (cleanupTask != null && !isShuttingDown) {
            synchronized (cleanupTasks) {
                cleanupTasks.add(cleanupTask);
            }
        }
    }
    
    /**
     * Register a background executor service to be shut down during shutdown.
     * 
     * @param executor The executor service to register
     */
    public static void registerBackgroundExecutor(ExecutorService executor) {
        if (executor != null && !isShuttingDown) {
            synchronized (backgroundExecutors) {
                backgroundExecutors.add(executor);
            }
        }
    }
    
    /**
     * Execute the shutdown process.
     */
    public static void shutdown() {
        if (isShuttingDown) {
            Logging.info("🔄 Shutdown already in progress, skipping duplicate call");
            return;
        }
        
        isShuttingDown = true;
        Logging.info("🔄 Starting GDK application shutdown process...");
        
        try {
            // Step 1: Execute all registered cleanup tasks (create a copy to avoid concurrent modification)
            Logging.info("🧹 Executing cleanup tasks...");
            List<Runnable> tasksToExecute;
            synchronized (cleanupTasks) {
                tasksToExecute = new ArrayList<>(cleanupTasks);
                cleanupTasks.clear();
            }
            
            for (Runnable cleanupTask : tasksToExecute) {
                try {
                    if (cleanupTask != null) {
                        cleanupTask.run();
                    }
                } catch (Exception e) {
                    Logging.error("❌ Error during cleanup task: " + e.getMessage(), e);
                }
            }
            
            // Step 2: Shutdown background executor services (create a copy to avoid concurrent modification)
            Logging.info("🔄 Shutting down background executor services...");
            List<ExecutorService> executorsToShutdown;
            synchronized (backgroundExecutors) {
                executorsToShutdown = new ArrayList<>(backgroundExecutors);
                backgroundExecutors.clear();
            }
            
            for (ExecutorService executor : executorsToShutdown) {
                try {
                    if (executor != null && !executor.isShutdown()) {
                        executor.shutdown();
                        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                            Logging.warning("⚠️ Force shutting down executor service");
                            executor.shutdownNow();
                        }
                    }
                } catch (Exception e) {
                    Logging.error("❌ Error shutting down executor: " + e.getMessage(), e);
                    try {
                        if (executor != null) {
                            executor.shutdownNow();
                        }
                    } catch (Exception ignored) {}
                }
            }
            
            // Step 3: Simple exit - no complex threading during shutdown
            Logging.info("🚪 GDK application shutdown completed - exiting");
            System.exit(0);
            
        } catch (Exception e) {
            Logging.error("💥 Critical error during shutdown: " + e.getMessage(), e);
            // Force exit even if shutdown fails
            System.exit(1);
        }
    }
    
    /**
     * Force immediate shutdown (emergency exit).
     */
    public static void forceShutdown() {
        if (isShuttingDown) {
            Logging.info("🔄 Normal shutdown already in progress, skipping force shutdown");
            return;
        }
        
        Logging.warning("🚨 Force shutdown initiated");
        try {
            // Shutdown all executors immediately
            synchronized (backgroundExecutors) {
                for (ExecutorService executor : backgroundExecutors) {
                    try {
                        if (executor != null) {
                            executor.shutdownNow();
                        }
                    } catch (Exception ignored) {}
                }
                backgroundExecutors.clear();
            }
            
            // Clear cleanup tasks
            synchronized (cleanupTasks) {
                cleanupTasks.clear();
            }
            
            // Force exit
            System.exit(0);
        } catch (Exception e) {
            Logging.error("💥 Error during force shutdown: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
