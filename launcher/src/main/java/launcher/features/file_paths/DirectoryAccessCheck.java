package launcher.features.file_paths;

import gdk.internal.Logging;

import java.io.File;

/**
 * Utility class for checking directory accessibility.
 * 
 * <p>This class provides methods for testing directory access permissions
 * and listing capabilities. It performs basic accessibility checks including
 * existence, directory type, readability, and listing capability.
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
     * <p>This method performs basic accessibility checks including existence,
     * directory type, readability, and listing capability. This can help identify
     * if the issue is with file system access.
     * 
     * @param directoryPath The path to the directory to check
     * @return true if the directory is accessible, false otherwise
     */
    public static boolean checkAccess(String directoryPath) {
        try {
            Logging.info("Testing directory access: " + directoryPath);
            
            File directory = new File(directoryPath);
            
            if (!directory.exists()) {
                Logging.warning("Directory does not exist: " + directoryPath);
                return false;
            }
            
            if (!directory.isDirectory()) {
                Logging.warning("Path exists but is not a directory: " + directoryPath);
                return false;
            }
            
            if (!directory.canRead()) {
                Logging.warning("Directory is not readable: " + directoryPath);
                return false;
            }
            
            Logging.info("Directory is accessible and readable");
            
            // Try to list contents
            long startTime = System.currentTimeMillis();
            File[] contents = directory.listFiles();
            long listTime = System.currentTimeMillis() - startTime;
            
            if (contents == null) {
                Logging.warning("Cannot list directory contents (null returned)");
                return false;
            }
            
            Logging.info("Directory listing successful in " + listTime + "ms. Found " + contents.length + " items");
            
            // List first few items for debugging
            for (int i = 0; i < Math.min(5, contents.length); i++) {
                File item = contents[i];
                Logging.info("Item " + i + ": " + item.getName() + " (dir: " + item.isDirectory() + ")");
            }
            
            return true;
            
        } catch (Exception e) {
            Logging.error("Error testing directory access: " + e.getMessage(), e);
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
        File directory = new File(directoryPath);
        return directory.exists() && directory.isDirectory() && directory.canRead();
    }
}

