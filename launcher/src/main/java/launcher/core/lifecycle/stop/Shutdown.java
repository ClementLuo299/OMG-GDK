package launcher.core.lifecycle.stop;

import gdk.internal.Logging;

import java.util.concurrent.ExecutorService;
import java.util.List;

/**
 * Orchestrates the shutdown process of the GDK application.
 * Coordinates cleanup task execution, executor shutdown, and application exit.
 * 
 * @author Clement Luo
 * @date August 6, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class Shutdown {
    
    // Flag to prevent shutdown hook interference
    private static volatile boolean isShuttingDown = false;
    
    private Shutdown() {}
    
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
        ShutdownTaskRegistry.registerCleanupTask(cleanupTask, isShuttingDown);
    }
    
    /**
     * Register a background executor service to be shut down during shutdown.
     * 
     * @param executor The executor service to register
     */
    public static void registerBackgroundExecutor(ExecutorService executor) {
        ShutdownTaskRegistry.registerBackgroundExecutor(executor, isShuttingDown);
    }
    
    /**
     * Execute the shutdown process.
     */
    public static void shutdown() {
        // Step 1: Check if shutdown is already in progress
        if (isShuttingDown) {
            Logging.info("Shutdown already in progress, skipping duplicate call");
            return;
        }
        
        isShuttingDown = true;
        Logging.info("Starting GDK application shutdown process...");
        
        try {
            // Step 2: Get all registered resources and clear the registry
            List<Runnable> cleanupTasks = ShutdownTaskRegistry.getAndClearCleanupTasks();
            List<ExecutorService> executors = ShutdownTaskRegistry.getAndClearExecutors();
            
            // Step 3: Execute all cleanup tasks
            CleanupTaskExecutor.executeCleanupTasks(cleanupTasks);
            
            // Step 4: Shutdown all executor services
            ExecutorServiceShutdown.shutdownExecutors(executors);
            
            // Step 5: Exit the application
            Logging.info("GDK application shutdown completed - exiting");
            System.exit(0);
            
        } catch (Exception e) {
            // Handle critical errors during shutdown
            Logging.error("Critical error during shutdown: " + e.getMessage(), e);
            // Force exit even if shutdown fails
            System.exit(1);
        }
    }
    
    /**
     * Force immediate shutdown (emergency exit).
     */
    public static void forceShutdown() {
        // Step 1: Check if shutdown is already in progress
        if (isShuttingDown) {
            Logging.info("Normal shutdown already in progress, skipping force shutdown");
            return;
        }
        
        Logging.warning("Force shutdown initiated");
        try {
            // Step 2: Get all executors and force shutdown immediately
            List<ExecutorService> executors = ShutdownTaskRegistry.getAndClearExecutors();
            ExecutorServiceShutdown.forceShutdownExecutors(executors);
            
            // Step 3: Clear all registered resources without executing cleanup tasks
            ShutdownTaskRegistry.clearAll();
            
            // Step 4: Force exit immediately
            System.exit(0);
        } catch (Exception e) {
            Logging.error("Error during force shutdown: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
