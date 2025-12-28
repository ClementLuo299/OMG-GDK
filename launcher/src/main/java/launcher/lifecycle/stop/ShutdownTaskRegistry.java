package launcher.lifecycle.stop;

import java.util.concurrent.ExecutorService;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages registration and tracking of resources that need cleanup during shutdown.
 * Responsible for storing cleanup tasks and executor services.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ShutdownTaskRegistry {
    
    // Track resources that need cleanup
    private static final List<Runnable> cleanupTasks = new ArrayList<>();
    private static final List<ExecutorService> backgroundExecutors = new ArrayList<>();
    
    private ShutdownTaskRegistry() {}
    
    /**
     * Register a cleanup task to be executed during shutdown.
     * 
     * @param cleanupTask The cleanup task to register
     * @param isShuttingDown Flag indicating if shutdown is in progress
     */
    public static void registerCleanupTask(Runnable cleanupTask, boolean isShuttingDown) {
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
     * @param isShuttingDown Flag indicating if shutdown is in progress
     */
    public static void registerBackgroundExecutor(ExecutorService executor, boolean isShuttingDown) {
        if (executor != null && !isShuttingDown) {
            synchronized (backgroundExecutors) {
                backgroundExecutors.add(executor);
            }
        }
    }
    
    /**
     * Get all registered cleanup tasks and clear the registry.
     * 
     * @return A copy of all registered cleanup tasks
     */
    public static List<Runnable> getAndClearCleanupTasks() {
        synchronized (cleanupTasks) {
            List<Runnable> tasks = new ArrayList<>(cleanupTasks);
            cleanupTasks.clear();
            return tasks;
        }
    }
    
    /**
     * Get all registered executor services and clear the registry.
     * 
     * @return A copy of all registered executor services
     */
    public static List<ExecutorService> getAndClearExecutors() {
        synchronized (backgroundExecutors) {
            List<ExecutorService> executors = new ArrayList<>(backgroundExecutors);
            backgroundExecutors.clear();
            return executors;
        }
    }
    
    /**
     * Clear all registered resources without returning them.
     * Used for force shutdown scenarios.
     */
    public static void clearAll() {
        synchronized (cleanupTasks) {
            cleanupTasks.clear();
        }
        synchronized (backgroundExecutors) {
            backgroundExecutors.clear();
        }
    }
}

