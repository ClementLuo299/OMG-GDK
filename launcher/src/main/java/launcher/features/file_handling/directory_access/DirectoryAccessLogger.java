package launcher.features.file_handling.access;

import gdk.internal.Logging;

import java.io.File;

/**
 * Provides logging functionality for directory access operations.
 * 
 * <p>This class handles all logging and debugging output related to
 * directory access checks, including validation results, listing operations,
 * and debugging information.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class DirectoryAccessLogger {
    
    private DirectoryAccessLogger() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Logs the start of a directory access test.
     * 
     * @param directoryPath The path being tested
     */
    public static void logAccessTestStart(String directoryPath) {
        Logging.info("Testing directory access: " + directoryPath);
    }
    
    /**
     * Logs that a directory does not exist.
     * 
     * @param directoryPath The path that doesn't exist
     */
    public static void logDirectoryNotFound(String directoryPath) {
        Logging.warning("Directory does not exist: " + directoryPath);
    }
    
    /**
     * Logs that a path exists but is not a directory.
     * 
     * @param directoryPath The path that is not a directory
     */
    public static void logNotADirectory(String directoryPath) {
        Logging.warning("Path exists but is not a directory: " + directoryPath);
    }
    
    /**
     * Logs that a directory is not readable.
     * 
     * @param directoryPath The path that is not readable
     */
    public static void logNotReadable(String directoryPath) {
        Logging.warning("Directory is not readable: " + directoryPath);
    }
    
    /**
     * Logs that a directory is accessible and readable.
     */
    public static void logAccessible() {
        Logging.info("Directory is accessible and readable");
    }
    
    /**
     * Logs that directory listing failed (returned null).
     */
    public static void logListingFailed() {
        Logging.warning("Cannot list directory contents (null returned)");
    }
    
    /**
     * Logs successful directory listing with timing and item count.
     * 
     * @param listTimeMs The time taken to list in milliseconds
     * @param itemCount The number of items found
     */
    public static void logListingSuccess(long listTimeMs, int itemCount) {
        Logging.info("Directory listing successful in " + listTimeMs + "ms. Found " + itemCount + " items");
    }
    
    /**
     * Logs the first few items in a directory for debugging.
     * 
     * @param contents The directory contents
     * @param maxItems The maximum number of items to log
     */
    public static void logFirstItems(File[] contents, int maxItems) {
        for (int i = 0; i < Math.min(maxItems, contents.length); i++) {
            File item = contents[i];
            Logging.info("Item " + i + ": " + item.getName() + " (dir: " + item.isDirectory() + ")");
        }
    }
    
    /**
     * Logs an error during directory access testing.
     * 
     * @param message The error message
     * @param exception The exception that occurred
     */
    public static void logError(String message, Exception exception) {
        Logging.error("Error testing directory access: " + message, exception);
    }
}

