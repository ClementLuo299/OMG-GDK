package com.utils.error_handling;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.gdk.shared.utils.error_handling.Logging;

/**
 * Safe execution utility for the application.
 * Provides methods for executing code with error handling.
 * 
 * @authors Clement Luo
 * @date June 22, 2025
 * @edited June 24, 2025
 * @since 1.0
 * @version 1.0
 */
public final class SafeExecute {

    /**
     * Default executor service for async operations.
     */
    private static final ExecutorService defaultExecutor = Executors.newCachedThreadPool();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SafeExecute() { throw new UnsupportedOperationException("Utility class cannot be instantiated"); }

    //region ==================== BASIC SAFE EXECUTION ====================

    /**
     * Executes a task safely, handling any exceptions that occur.
     * If an exception occurs, it's handled as a non-critical error and null is returned.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T execute(Callable<T> task, String errorMessage) {
        try {
            return task.call();
        } catch (Exception e) {
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    /**
     * Executes a task safely, handling any exceptions that occur.
     * If an exception occurs, it's handled as a non-critical error.
     *
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     */
    public static void execute(Runnable task, String errorMessage) {
        execute(() -> {
            task.run();
            return null;
        }, errorMessage);
    }

    //endregion

    //region ==================== SAFE EXECUTION WITH DEFAULTS ====================

    /**
     * Executes a task safely with a default value if the task fails.
     * If an exception occurs, it's handled as a non-critical error and the default value is returned.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param defaultValue the default value to return if the task fails
     * @return the result of the task, or the default value if an exception occurred
     */
    public static <T> T executeWithDefault(Callable<T> task, String errorMessage, T defaultValue) {
        try {
            return task.call();
        } catch (Exception e) {
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return defaultValue;
        }
    }

    /**
     * Executes a task safely with a default supplier if the task fails.
     * If an exception occurs, it's handled as a non-critical error and the default supplier is called.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param defaultSupplier the supplier to call if the task fails
     * @return the result of the task, or the result of the default supplier if an exception occurred
     */
    public static <T> T executeWithDefault(Callable<T> task, String errorMessage, Supplier<T> defaultSupplier) {
        try {
            return task.call();
        } catch (Exception e) {
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return defaultSupplier.get();
        }
    }

    //endregion

    //region ==================== UNCHECKED EXECUTION ====================

    /**
     * Executes a task and rethrows any checked exceptions as runtime exceptions.
     * This is useful for converting checked exceptions to unchecked ones.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @return the result of the task
     * @throws RuntimeException if the task throws any exception
     */
    public static <T> T unchecked(ThrowingSupplier<T> task) {
        try {
            return task.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a task and rethrows any checked exceptions as runtime exceptions.
     * This is useful for converting checked exceptions to unchecked ones.
     *
     * @param task the task to execute
     * @throws RuntimeException if the task throws any exception
     */
    public static void unchecked(ThrowingRunnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region ==================== RETRY LOGIC ====================

    /**
     * Executes a task with retry logic using fixed delay.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param maxRetries the maximum number of retries
     * @param delayMs the delay between retries in milliseconds (optional, defaults to 1000ms)
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithRetry(Callable<T> task, String errorMessage, int maxRetries, long delayMs) {
        // Track the last exception for final error reporting
        Exception lastException = null;

        // Try the task up to maxRetries + 1 times (initial attempt + retries)
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                // Attempt to execute the task
                return task.call();
            } catch (Exception e) {
                // Store the exception for final error reporting
                lastException = e;
                
                // If we haven't exhausted all retries, try again
                if (attempt < maxRetries) {
                    // Log the failure and retry attempt
                    Logging.warning("Attempt " + (attempt + 1) + " failed, retrying in " + delayMs + "ms: " + e.getMessage());
                    try {
                        // Wait before retrying (fixed delay)
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        // Restore interrupt status and exit retry loop
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // All retries exhausted, handle the final error
        ErrorHandler.handleNonCriticalError(lastException, errorMessage + " (after " + (maxRetries + 1) + " attempts)");
        return null;
    }

    /**
     * Executes a task with retry logic using fixed delay (default 1000ms).
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param maxRetries the maximum number of retries
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithRetry(Callable<T> task, String errorMessage, int maxRetries) {
        return executeWithRetry(task, errorMessage, maxRetries, 1000); // Default 1 second delay
    }

    /**
     * Executes a task with retry logic using custom retry condition and fixed delay.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param shouldRetry the predicate to determine if a retry should occur
     * @param maxRetries the maximum number of retries
     * @param delayMs the delay between retries in milliseconds (optional, defaults to 1000ms)
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithRetry(Callable<T> task, String errorMessage, 
                                       Predicate<Exception> shouldRetry, int maxRetries, long delayMs) {
        // Track the last exception for final error reporting
        Exception lastException = null;

        // Try the task up to maxRetries + 1 times (initial attempt + retries)
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                // Attempt to execute the task
                return task.call();
            } catch (Exception e) {
                // Store the exception for final error reporting
                lastException = e;
                
                // Check if we should retry based on custom condition and retry count
                if (attempt < maxRetries && shouldRetry.test(e)) {
                    // Log the failure and retry attempt
                    Logging.warning("Attempt " + (attempt + 1) + " failed, retrying in " + delayMs + "ms: " + e.getMessage());
                    try {
                        // Wait before retrying (fixed delay)
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        // Restore interrupt status and exit retry loop
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // Either no more retries or condition says don't retry
                    break;
                }
            }
        }

        // All retries exhausted or condition not met, handle the final error
        ErrorHandler.handleNonCriticalError(lastException, errorMessage + " (after " + (maxRetries + 1) + " attempts)");
        return null;
    }

    /**
     * Executes a task with retry logic using custom retry condition and fixed delay (default 1000ms).
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param shouldRetry the predicate to determine if a retry should occur
     * @param maxRetries the maximum number of retries
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithRetry(Callable<T> task, String errorMessage, 
                                       Predicate<Exception> shouldRetry, int maxRetries) {
        return executeWithRetry(task, errorMessage, shouldRetry, maxRetries, 1000); // Default 1 second delay
    }

    //endregion

    //region ==================== TIMEOUT SUPPORT ====================

    /**
     * Executes a task with timeout support.
     * If the task takes longer than the specified timeout, it's considered failed.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param timeoutMs the timeout in milliseconds
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithTimeout(Callable<T> task, String errorMessage, long timeoutMs) {
        // Submit the task to the executor service for execution
        Future<T> future = defaultExecutor.submit(task);
        
        try {
            // Wait for the task to complete with the specified timeout
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Task exceeded the timeout limit
            future.cancel(true); // Cancel the running task
            ErrorHandler.handleNonCriticalError(e, errorMessage + " (timeout after " + timeoutMs + "ms)");
            return null;
        } catch (Exception e) {
            // Task failed with some other exception
            future.cancel(true); // Cancel the running task
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    /**
     * Executes a task with timeout support and a default value if the task fails.
     * If the task takes longer than the specified timeout, it's considered failed and the default value is returned.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param timeoutMs the timeout in milliseconds
     * @param defaultValue the default value to return if the task fails
     * @return the result of the task, or the default value if an exception occurred
     */
    public static <T> T executeWithTimeout(Callable<T> task, String errorMessage, 
                                         long timeoutMs, T defaultValue) {
        // Submit the task to the executor service for execution
        Future<T> future = defaultExecutor.submit(task);
        
        try {
            // Wait for the task to complete with the specified timeout
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Task exceeded the timeout limit
            future.cancel(true); // Cancel the running task
            ErrorHandler.handleNonCriticalError(e, errorMessage + " (timeout after " + timeoutMs + "ms)");
            return defaultValue; // Return the provided default value instead of null
        } catch (Exception e) {
            // Task failed with some other exception
            future.cancel(true); // Cancel the running task
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return defaultValue; // Return the provided default value instead of null
        }
    }

    //endregion

    //region ==================== ASYNC EXECUTION ====================

    /**
     * Executes a task asynchronously using CompletableFuture.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @return a CompletableFuture representing the task execution
     */
    public static <T> CompletableFuture<T> executeAsync(Callable<T> task, String errorMessage) {
        // Create an asynchronous task using CompletableFuture.supplyAsync
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Execute the task and return its result
                return task.call();
            } catch (Exception e) {
                // Handle any exceptions that occur during task execution
                ErrorHandler.handleNonCriticalError(e, errorMessage);
                return null; // Return null on failure
            }
        }, defaultExecutor); // Use the default thread pool for execution
    }

    /**
     * Executes a task asynchronously with timeout using CompletableFuture.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param timeoutMs the timeout in milliseconds
     * @return a CompletableFuture representing the task execution
     */
    public static <T> CompletableFuture<T> executeAsyncWithTimeout(Callable<T> task, 
                                                                 String errorMessage, 
                                                                 long timeoutMs) {
        // Create the base asynchronous task (same as executeAsync)
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                // Execute the task and return its result
                return task.call();
            } catch (Exception e) {
                // Handle any exceptions that occur during task execution
                ErrorHandler.handleNonCriticalError(e, errorMessage);
                return null; // Return null on failure
            }
        }, defaultExecutor); // Use the default thread pool for execution

        // Add timeout behavior to the future
        return future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        // Handle timeout exceptions specifically
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            ErrorHandler.handleNonCriticalError(throwable, 
                                errorMessage + " (timeout after " + timeoutMs + "ms)");
                        }
                        return null; // Return null for any exception (timeout or other)
                    });
    }

    //endregion

    //region ==================== CONDITIONAL EXECUTION ====================

    /**
     * Executes a task only if a condition is met, otherwise returns a default value.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param condition the condition that must be true for the task to execute
     * @param defaultValue the default value to return if the condition is false
     * @return the result of the task, or the default value if the condition is false
     */
    public static <T> T executeIf(Callable<T> task, String errorMessage, 
                                Supplier<Boolean> condition, T defaultValue) {
        // Check if the condition is met before executing the task
        if (condition.get()) {
            // Condition is true, execute the task safely
            return execute(task, errorMessage);
        } else {
            // Condition is false, return the default value without executing the task
            return defaultValue;
        }
    }

    /**
     * Executes a task with a fallback chain. If the primary task fails, tries the fallback tasks in order.
     *
     * @param <T> the return type of the task
     * @param primaryTask the primary task to execute
     * @param fallbackTasks the list of fallback tasks to try if the primary fails
     * @param errorMessage the error message to display if all tasks fail
     * @return the result of the first successful task, or null if all fail
     */
    public static <T> T executeWithFallback(Callable<T> primaryTask, 
                                          List<Callable<T>> fallbackTasks, 
                                          String errorMessage) {
        // Try the primary task first
        T result = execute(primaryTask, errorMessage);
        if (result != null) {
            // Primary task succeeded, return its result
            return result;
        }

        // Primary task failed, try each fallback task in order
        for (int i = 0; i < fallbackTasks.size(); i++) {
            Callable<T> fallbackTask = fallbackTasks.get(i);
            // Execute fallback task with numbered error message for identification
            result = execute(fallbackTask, errorMessage + " (fallback " + (i + 1) + ")");
            if (result != null) {
                // Fallback task succeeded, return its result
                return result;
            }
        }

        // All tasks failed, return null
        return null;
    }

    //endregion

    //region ==================== RESOURCE MANAGEMENT ====================

    /**
     * Executes a task with automatic resource cleanup.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param resource the resource to close after task execution
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithResource(Callable<T> task, String errorMessage, 
                                          AutoCloseable resource) {
        try {
            // Execute the task and get the result
            T result = task.call();
            // Close the resource after successful execution
            resource.close();
            return result;
        } catch (Exception e) {
            // Task failed, but we still need to close the resource
            try {
                resource.close();
            } catch (Exception closeException) {
                // Log warning if resource cleanup fails
                Logging.warning("Failed to close resource: " + closeException.getMessage());
            }
            // Handle the original task error
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    /**
     * Executes a task with multiple resource cleanup.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param resources the list of resources to close after task execution
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithResources(Callable<T> task, String errorMessage, 
                                           List<AutoCloseable> resources) {
        try {
            // Execute the task and get the result
            T result = task.call();
            // Close all resources after successful execution
            for (AutoCloseable resource : resources) {
                resource.close();
            }
            return result;
        } catch (Exception e) {
            // Task failed, but we still need to close all resources
            for (AutoCloseable resource : resources) {
                try {
                    resource.close();
                } catch (Exception closeException) {
                    // Log warning if any resource cleanup fails
                    Logging.warning("Failed to close resource: " + closeException.getMessage());
                }
            }
            // Handle the original task error
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    //endregion

    //region ==================== PERFORMANCE MONITORING ====================

    /**
     * Executes a task with performance logging.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param operationName the name of the operation for logging
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithMetrics(Callable<T> task, String errorMessage, 
                                         String operationName) {
        // Record start time for performance measurement
        long startTime = System.currentTimeMillis();
        try {
            // Execute the task and measure duration
            T result = task.call();
            long duration = System.currentTimeMillis() - startTime;
            // Log successful operation performance
            Logging.info("Operation '" + operationName + "' completed in " + duration + "ms");
            return result;
        } catch (Exception e) {
            // Calculate duration even for failed operations
            long duration = System.currentTimeMillis() - startTime;
            // Log failed operation with duration
            Logging.warning("Operation '" + operationName + "' failed after " + duration + "ms: " + e.getMessage());
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    /**
     * Executes a task with custom metrics collection.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param metricsConsumer the consumer to handle performance metrics
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithMetrics(Callable<T> task, String errorMessage, 
                                         Consumer<Long> metricsConsumer) {
        // Record start time for performance measurement
        long startTime = System.currentTimeMillis();
        try {
            // Execute the task and measure duration
            T result = task.call();
            long duration = System.currentTimeMillis() - startTime;
            // Pass duration to custom metrics consumer
            metricsConsumer.accept(duration);
            return result;
        } catch (Exception e) {
            // Calculate duration and pass to consumer even for failed operations
            long duration = System.currentTimeMillis() - startTime;
            metricsConsumer.accept(duration);
            ErrorHandler.handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    //endregion

    //region ==================== BATCH OPERATIONS ====================

    /**
     * Executes a batch of tasks safely.
     *
     * @param <T> the return type of the tasks
     * @param tasks the list of tasks to execute
     * @param errorMessage the error message to display if any task fails
     * @return a list of results, with null for failed tasks
     */
    public static <T> List<T> executeBatch(List<Callable<T>> tasks, String errorMessage) {
        // Initialize results list to store all task outcomes
        List<T> results = new ArrayList<>();
        
        // Execute each task in the batch sequentially
        for (int i = 0; i < tasks.size(); i++) {
            Callable<T> task = tasks.get(i);
            // Execute individual task with numbered error message for identification
            T result = execute(task, errorMessage + " (task " + (i + 1) + ")");
            results.add(result);
        }
        
        return results;
    }

    /**
     * Result class for batch operations with partial failure information.
     */
    public static class BatchResult<T> {
        // Store successful task results
        private final List<T> successfulResults;
        // Store exceptions from failed tasks
        private final List<Exception> failures;
        // Store indices of failed tasks for easy identification
        private final List<Integer> failedIndices;

        public BatchResult(List<T> successfulResults, List<Exception> failures, List<Integer> failedIndices) {
            this.successfulResults = successfulResults;
            this.failures = failures;
            this.failedIndices = failedIndices;
        }

        public List<T> getSuccessfulResults() { return successfulResults; }
        public List<Exception> getFailures() { return failures; }
        public List<Integer> getFailedIndices() { return failedIndices; }
        public boolean hasFailures() { return !failures.isEmpty(); }
        public int getSuccessCount() { return successfulResults.size(); }
        public int getFailureCount() { return failures.size(); }
    }

    /**
     * Executes a batch of tasks with detailed failure information.
     *
     * @param <T> the return type of the tasks
     * @param tasks the list of tasks to execute
     * @param errorMessage the error message to display if any task fails
     * @return a BatchResult containing successful results and failure information
     */
    public static <T> BatchResult<T> executeBatchWithPartialFailure(List<Callable<T>> tasks, 
                                                                  String errorMessage) {
        // Initialize collections to track different types of results
        List<T> successfulResults = new ArrayList<>();
        List<Exception> failures = new ArrayList<>();
        List<Integer> failedIndices = new ArrayList<>();

        // Process each task individually to track success/failure separately
        for (int i = 0; i < tasks.size(); i++) {
            Callable<T> task = tasks.get(i);
            try {
                // Attempt to execute the task
                T result = task.call();
                successfulResults.add(result);
            } catch (Exception e) {
                // Track failure details for comprehensive reporting
                failures.add(e);
                failedIndices.add(i);
                ErrorHandler.handleNonCriticalError(e, errorMessage + " (task " + (i + 1) + ")");
            }
        }

        // Return comprehensive batch result with success and failure information
        return new BatchResult<>(successfulResults, failures, failedIndices);
    }

    //endregion

    //region ==================== CONTEXT-AWARE EXECUTION ====================

    /**
     * Executes a task with context information for better error tracking.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param context the context information for the operation
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithContext(Callable<T> task, String errorMessage, 
                                         String context) {
        // Removed Logging.debugWithContext, Logging.executeWithMDC, and Logging.logWithMDC calls
        try {
            // Execute the task and log successful completion
            T result = task.call();
            Logging.info("Task completed successfully in context: " + context);
            return result;
        } catch (Exception e) {
            // Log detailed error with context information
            Logging.error("Task failed in context: " + context, e);
            ErrorHandler.handleNonCriticalError(e, errorMessage + " (context: " + context + ")");
            return null;
        }
    }

    /**
     * Executes a task with MDC (Mapped Diagnostic Context) support.
     *
     * @param <T> the return type of the task
     * @param task the task to execute
     * @param errorMessage the error message to display if the task fails
     * @param context the context map for MDC
     * @return the result of the task, or null if an exception occurred
     */
    public static <T> T executeWithMDC(Callable<T> task, String errorMessage, 
                                     Map<String, String> context) {
        try {
            // Execute the task (no MDC support in Logging)
            return task.call();
        } catch (Exception e) {
            // Log the error with context information
            Logging.error("Task failed with context: " + context, e);
            ErrorHandler.handleNonCriticalError(e, errorMessage + " (context: " + context + ")");
            return null;
        }
    }

    //endregion

    //region ==================== FUNCTIONAL INTERFACES ====================

    /**
     * A functional interface for suppliers that may throw checked exceptions.
     * This allows for cleaner exception handling in lambda expressions.
     *
     * @param <T> the type of the result
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        /**
         * Gets a result, potentially throwing an exception.
         *
         * @return the result
         * @throws Exception if an error occurs
         */
        T get() throws Exception;
    }

    /**
     * A functional interface for runnables that may throw checked exceptions.
     * This allows for cleaner exception handling in lambda expressions.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        /**
         * Runs the task, potentially throwing an exception.
         *
         * @throws Exception if an error occurs
         */
        void run() throws Exception;
    }

    //endregion

    //region ==================== UTILITY METHODS ====================

    /**
     * Shuts down the default executor service.
     * Should be called when the application is shutting down.
     */
    public static void shutdown() {
        // Initiate graceful shutdown of the executor service
        defaultExecutor.shutdown();
        try {
            // Wait up to 60 seconds for tasks to complete gracefully
            if (!defaultExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                // Force shutdown if graceful shutdown times out
                defaultExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Handle interruption during shutdown wait
            defaultExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    //endregion
}
