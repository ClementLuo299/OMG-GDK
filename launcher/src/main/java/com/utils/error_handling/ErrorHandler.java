package com.utils.error_handling;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import com.utils.error_handling.enums.ErrorCategory;
import com.utils.error_handling.enums.ErrorSeverity;
import com.gdk.shared.utils.error_handling.Logging;

/**
 * Core error handling utility for the application.
 * Provides methods for handling critical and non-critical errors with user feedback,
 * error categorization, recovery strategies, and comprehensive error tracking.
 * 
 * @authors Clement Luo
 * @date May 22, 2025
 * @edited June 24, 2025
 * @since 1.0
 */
public final class ErrorHandler {

    /**
     * Tells the error handler that the application is shutting down.
     * Stops the application from handling errors during shutdown.
     */
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    /**
     * Error counter for tracking error frequency.
     */
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ErrorHandler() { throw new UnsupportedOperationException("Utility class cannot be instantiated"); }

    //region ==================== CORE ERROR HANDLING METHODS ====================

    /**
     * Handles a non-critical error that doesn't require application termination.
     * Logs the error, shows a warning dialog to the user.
     *
     * @param throwable the exception that occurred
     * @param userMessage a user-friendly message describing the error
     */
    public static void handleNonCriticalError(Throwable throwable, String userMessage) {
        handleError(throwable, userMessage, ErrorCategory.UNKNOWN, ErrorSeverity.MEDIUM, false);
    }

    /**
     * Handles a non-critical error with categorization.
     *
     * @param throwable the exception that occurred
     * @param userMessage a user-friendly message describing the error
     * @param category the error category
     * @param severity the error severity
     */
    public static void handleNonCriticalError(Throwable throwable, String userMessage, 
                                            ErrorCategory category, ErrorSeverity severity) {
        handleError(throwable, userMessage, category, severity, false);
    }

    /**
     * Handles a critical error that requires immediate application termination.
     * This method exits the application.
     *
     * @param throwable the exception that occurred
     * @param userMessage a user-friendly message describing the error
     */
    public static void handleCriticalError(Throwable throwable, String userMessage) {
        handleError(throwable, userMessage, ErrorCategory.UNKNOWN, ErrorSeverity.CRITICAL, true);
    }

    /**
     * Handles a critical error with categorization.
     *
     * @param throwable the exception that occurred
     * @param userMessage a user-friendly message describing the error
     * @param category the error category
     * @param severity the error severity
     */
    public static void handleCriticalError(Throwable throwable, String userMessage, 
                                         ErrorCategory category, ErrorSeverity severity) {
        handleError(throwable, userMessage, category, severity, true);
    }

    /**
     * Core error handling method with full categorization and context.
     *
     * @param throwable the exception that occurred
     * @param userMessage a user-friendly message describing the error
     * @param category the error category
     * @param severity the error severity
     * @param isCritical whether this is a critical error
     */
    private static void handleError(Throwable throwable, String userMessage, 
                                  ErrorCategory category, ErrorSeverity severity, boolean isCritical) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring error during shutdown: " + userMessage);
            return;
        }

        // Increment error counter
        int currentErrorCount = errorCount.incrementAndGet();

        // Set MDC context for better error tracking
        Map<String, String> errorContext = new HashMap<>();
        errorContext.put("errorCount", String.valueOf(currentErrorCount));
        errorContext.put("errorCategory", category.name());
        errorContext.put("errorSeverity", severity.name());
        errorContext.put("isCritical", String.valueOf(isCritical));

        // Log the error with context
        if (isCritical) {
            Logging.error(userMessage, throwable);
        } else {
            Logging.warning(userMessage + ": " + throwable.getMessage());
        }

        // Show appropriate dialog based on severity and criticality
        if (isCritical) {
            Dialog.showErrorAndExit(category.getDisplayName(), userMessage, throwable);
        } else {
            switch (severity) {
                case LOW:
                    Logging.info("Low severity error handled: " + userMessage);
                    break;
                case MEDIUM:
                    Dialog.showWarning(category.getDisplayName(), userMessage, throwable);
                    break;
                case HIGH:
                    Dialog.showError(category.getDisplayName(), userMessage, throwable);
                    break;
                case CRITICAL:
                    Dialog.showError(category.getDisplayName(), userMessage, throwable);
                    break;
            }
        }
        
        // Removed Logging.clearMDC() call
    }

    //endregion

    //region ==================== ERROR RECOVERY METHODS ====================

    /**
     * Attempts to recover from an error using a recovery strategy.
     *
     * @param <T> the return type
     * @param operation the operation that failed
     * @param errorMessage the error message
     * @param recoveryStrategy the recovery strategy to attempt
     * @param maxRetries the maximum number of retry attempts
     * @return the result of the operation or recovery, or null if all attempts failed
     */
    public static <T> T handleWithRecovery(Supplier<T> operation, String errorMessage, 
                                         Supplier<T> recoveryStrategy, int maxRetries) {
        // Try the primary operation first
        T result = safeExecute(operation, errorMessage);
        if (result != null) {
            return result;
        }

        // If primary operation failed, try recovery strategy with retries
        return SafeExecute.executeWithRetry(() -> recoveryStrategy.get(), 
            "Recovery operation failed: " + errorMessage, maxRetries);
    }

    /**
     * Handles an error with a fallback operation.
     *
     * @param <T> the return type
     * @param primaryOperation the primary operation
     * @param fallbackOperation the fallback operation
     * @param errorMessage the error message
     * @return the result of the primary operation or fallback
     */
    public static <T> T handleWithFallback(Supplier<T> primaryOperation, 
                                         Supplier<T> fallbackOperation, 
                                         String errorMessage) {
        // Try primary operation first
        T result = safeExecute(primaryOperation, errorMessage);
        if (result != null) {
            return result;
        }

        // If primary failed, try fallback
        return safeExecute(fallbackOperation, errorMessage + " (fallback)");
    }

    /**
     * Handles an error with user confirmation for recovery.
     *
     * @param <T> the return type
     * @param operation the operation that failed
     * @param errorMessage the error message
     * @param recoveryMessage the message to show for recovery confirmation
     * @param recoveryOperation the recovery operation
     * @return the result of the operation or recovery, or null if user declined
     */
    public static <T> T handleWithUserRecovery(Supplier<T> operation, String errorMessage, 
                                             String recoveryMessage, Supplier<T> recoveryOperation) {
        // Try the primary operation
        T result = safeExecute(operation, errorMessage);
        if (result != null) {
            return result;
        }

        // If primary operation failed, ask user for recovery
        boolean shouldRecover = Dialog.showConfirmation("Recovery", recoveryMessage);
        if (shouldRecover) {
            return safeExecute(recoveryOperation, "Recovery operation failed");
        }
        return null;
    }

    //endregion

    //region ==================== ERROR REPORTING AND ANALYTICS ====================

    /**
     * Reports an error for analytics and monitoring.
     *
     * @param throwable the exception that occurred
     * @param context additional context information
     */
    public static void reportError(Throwable throwable, Map<String, String> context) {
        Logging.error("Error reported for analytics", throwable);
        // Here you could add integration with error reporting services
        // like Sentry, LogRocket, or custom analytics
    }

    /**
     * Gets the current error count.
     *
     * @return the total number of errors handled
     */
    public static int getErrorCount() {
        return errorCount.get();
    }

    /**
     * Resets the error counter.
     */
    public static void resetErrorCount() {
        errorCount.set(0);
    }

    /**
     * Checks if the error count exceeds a threshold.
     *
     * @param threshold the error threshold
     * @return true if error count exceeds threshold
     */
    public static boolean isErrorThresholdExceeded(int threshold) {
        return errorCount.get() >= threshold;
    }

    //endregion

    //region ==================== VALIDATION ERROR HANDLING ====================

    /**
     * Handles validation errors with field-specific information.
     *
     * @param field the field that failed validation
     * @param value the invalid value
     * @param reason the reason for validation failure
     */
    public static void handleValidationError(String field, String value, String reason) {
        String userMessage = String.format("Invalid value for '%s': %s. %s", field, value, reason);
        
        Logging.warning(field + " validation failed: " + value + " (" + reason + ")");
        Dialog.showWarning("Validation Error", userMessage, null);
    }

    /**
     * Handles multiple validation errors at once.
     *
     * @param validationErrors list of validation error details
     */
    public static void handleValidationErrors(List<ValidationError> validationErrors) {
        if (validationErrors.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder("Multiple validation errors occurred:\n");
        for (ValidationError error : validationErrors) {
            message.append(String.format("• %s: %s (%s)\n", 
                error.getField(), error.getValue(), error.getReason()));
        }

        Logging.warning("Multiple validation errors: " + message.toString());
        Dialog.showWarning("Validation Errors", message.toString(), null);
    }

    /**
     * Validation error data class.
     */
    public static class ValidationError {
        private final String field;
        private final String value;
        private final String reason;

        public ValidationError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public String getField() { return field; }
        public String getValue() { return value; }
        public String getReason() { return reason; }
    }

    //endregion

    //region ==================== SHUTDOWN MANAGEMENT ====================

    /**
     * Marks the application as shutting down to prevent error handling during shutdown.
     */
    public static void markShuttingDown() {
        isShuttingDown.set(true);
        Dialog.markShuttingDown();
    }

    /**
     * Checks if the application is currently shutting down.
     *
     * @return true if the application is shutting down, false otherwise
     */
    public static boolean isShuttingDown() {
        return isShuttingDown.get();
    }

    //endregion

    //region ==================== UTILITY METHODS ====================

    /**
     * Rethrows a checked exception as an unchecked exception.
     * This is a utility method for converting checked exceptions to unchecked ones.
     *
     * @param <E> the type of the exception
     * @param exception the exception to rethrow
     * @return never returns, always throws
     * @throws E the exception that was passed in
     */
    public static <E extends Exception> RuntimeException rethrow(E exception) throws E {
        throw exception;
    }

    /**
     * Safely executes an operation with error handling.
     *
     * @param <T> the return type
     * @param operation the operation to execute
     * @param errorMessage the error message if operation fails
     * @return the result of the operation, or null if it fails
     */
    public static <T> T safeExecute(Supplier<T> operation, String errorMessage) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleNonCriticalError(e, errorMessage);
            return null;
        }
    }

    /**
     * Safely executes an operation with error handling and default value.
     *
     * @param <T> the return type
     * @param operation the operation to execute
     * @param errorMessage the error message if operation fails
     * @param defaultValue the default value to return if operation fails
     * @return the result of the operation, or the default value if it fails
     */
    public static <T> T safeExecute(Supplier<T> operation, String errorMessage, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleNonCriticalError(e, errorMessage);
            return defaultValue;
        }
    }

    /**
     * Safely executes an operation with timeout.
     *
     * @param <T> the return type
     * @param operation the operation to execute
     * @param errorMessage the error message if operation fails
     * @param timeoutMs the timeout in milliseconds
     * @return the result of the operation, or null if it fails or times out
     */
    public static <T> T safeExecuteWithTimeout(Supplier<T> operation, String errorMessage, long timeoutMs) {
        return SafeExecute.executeWithTimeout(() -> operation.get(), errorMessage, timeoutMs);
    }

    //endregion
}