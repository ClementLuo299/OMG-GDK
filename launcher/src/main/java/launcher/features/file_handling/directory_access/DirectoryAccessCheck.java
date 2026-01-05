package launcher.features.file_handling.directory_access;

import gdk.internal.Logging;
import launcher.features.file_handling.directory_existence.DirectoryExistenceCheck;
import java.io.File;

/**
 * Utility class for checking directory accessibility.
 * 
 * <p>This class orchestrates directory access checks by coordinating
 * validation and listing operations. It delegates to specialized
 * classes for specific functionality:
 * <ul>
 *   <li>{@link DirectoryExistenceCheck} - Basic directory validation</li>
 * </ul>
 * 
 * <p>This is a general-purpose utility that can be used anywhere directory
 * access validation is needed.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 4, 2026
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
            File directory = new File(directoryPath);
            
            // Validate basic directory properties
            if (!DirectoryExistenceCheck.validate(directory)) {
                Logging.warning("Directory is not accessible: " + directoryPath);
                return false;
            }
            
            // Try to list contents
            File[] contents = directory.listFiles();
            if (contents == null) {
                Logging.warning("Cannot list directory contents: " + directoryPath);
                return false;
            }
            
            Logging.info("Directory access verified: " + directoryPath + " (" + contents.length + " items)");
            return true;
            
        } catch (Exception e) {
            Logging.error("Error checking directory access: " + directoryPath + " - " + e.getMessage(), e);
            return false;
        }
    }
}

