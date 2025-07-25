package gdk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the GDK.
 * Provides basic logging functionality without external dependencies.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class Logging {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Logs an informational message.
     * @param message The message to log
     */
    public static void info(String message) {
        log("INFO", message, null);
    }
    
    /**
     * Logs a warning message.
     * @param message The message to log
     */
    public static void warning(String message) {
        log("WARN", message, null);
    }
    
    /**
     * Logs an error message.
     * @param message The message to log
     */
    public static void error(String message) {
        log("ERROR", message, null);
    }
    
    /**
     * Logs an error message with an exception.
     * @param message The message to log
     * @param exception The exception to log
     */
    public static void error(String message, Throwable exception) {
        log("ERROR", message, exception);
    }
    
    /**
     * Logs a debug message.
     * @param message The message to log
     */
    public static void debug(String message) {
        log("DEBUG", message, null);
    }
    
    /**
     * Internal logging method.
     * @param level The log level
     * @param message The message to log
     * @param exception The exception to log (can be null)
     */
    private static void log(String level, String message, Throwable exception) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String threadName = Thread.currentThread().getName();
        
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("[").append(timestamp).append("] "); // Timestamp
        logEntry.append("[").append(threadName).append("] "); // Thread name
        logEntry.append("[").append(level).append("] "); // Log level
        logEntry.append(message); // Message
        
        System.out.println(logEntry.toString());
        
        if (exception != null) {
            System.err.println("Exception: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
} 