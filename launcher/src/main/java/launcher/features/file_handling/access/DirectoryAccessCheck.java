package launcher.features.file_handling.access;

import launcher.features.file_handling.access.existence.DirectoryValidator;
import java.io.File;

/**
 * Utility class for checking directory accessibility.
 * 
 * <p>This class orchestrates directory access checks by coordinating
 * validation, listing, and logging operations. It delegates to specialized
 * classes for specific functionality:
 * <ul>
 *   <li>{@link launcher.features.file_handling.access.existence.DirectoryValidator} - Basic directory validation</li>
 *   <li>{@link DirectoryLister} - Directory listing capability</li>
 *   <li>{@link DirectoryAccessLogger} - Logging and debugging output</li>
 * </ul>
 * 
 * <p>This is a general-purpose utility that can be used anywhere directory
 * access validation is needed.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class DirectoryAccessCheck {
    
    private DirectoryAccessCheck() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory is accessible.
     * 
     * <p>This method performs comprehensive accessibility checks including existence,
     * directory type, readability, and listing capability. This can help identify
     * if the issue is with file system access.
     * 
     * @param directoryPath The path to the directory to check
     * @return true if the directory is accessible, false otherwise
     */
    public static boolean checkAccess(String directoryPath) {
        try {
            DirectoryAccessLogger.logAccessTestStart(directoryPath);
            
            File directory = new File(directoryPath);
            
            // Check existence
            if (!DirectoryValidator.exists(directory)) {
                DirectoryAccessLogger.logDirectoryNotFound(directoryPath);
                return false;
            }
            
            // Check if it's a directory
            if (!DirectoryValidator.isDirectory(directory)) {
                DirectoryAccessLogger.logNotADirectory(directoryPath);
                return false;
            }
            
            // Check readability
            if (!DirectoryValidator.isReadable(directory)) {
                DirectoryAccessLogger.logNotReadable(directoryPath);
                return false;
            }
            
            DirectoryAccessLogger.logAccessible();
            
            // Try to list contents
            DirectoryLister.ListingResult result = DirectoryLister.listContents(directory);
            
            if (!result.isSuccess()) {
                DirectoryAccessLogger.logListingFailed();
                return false;
            }
            
            File[] contents = result.getContents();
            DirectoryAccessLogger.logListingSuccess(result.getListTimeMs(), contents.length);
            
            // List first few items for debugging
            DirectoryAccessLogger.logFirstItems(contents, 5);
            
            return true;
            
        } catch (Exception e) {
            DirectoryAccessLogger.logError(e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if a directory exists and is accessible.
     * 
     * <p>This is a lightweight check that verifies the directory exists,
     * is actually a directory, and is readable. It does not attempt to list
     * the directory contents.
     * 
     * @param directoryPath The path to the directory to check
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean exists(String directoryPath) {
        return DirectoryValidator.exists(directoryPath);
    }
}

