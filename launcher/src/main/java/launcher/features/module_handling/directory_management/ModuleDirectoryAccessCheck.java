package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;

import java.io.File;

/**
 * Internal utility class for checking module directory accessibility.
 * 
 * <p>This class provides methods for testing directory access permissions
 * and listing capabilities. It performs basic accessibility checks including
 * existence, directory type, readability, and listing capability.
 * 
 * <p>This class is internal to the directory_management package. External code
 * should use {@link ModuleDirectoryManager} as the public API for directory
 * management operations.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDirectoryAccessCheck {
    
    private ModuleDirectoryAccessCheck() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Quick test to check if modules directory is accessible.
     * 
     * <p>This method performs basic accessibility checks including existence,
     * directory type, readability, and listing capability. This can help identify
     * if the issue is with file system access.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return true if the directory is accessible, false otherwise
     */
    public static boolean checkAccess(String modulesDirectoryPath) {
        try {
            Logging.info("Testing modules directory access: " + modulesDirectoryPath);
            
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.warning("Modules directory does not exist: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.isDirectory()) {
                Logging.warning("Path exists but is not a directory: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.canRead()) {
                Logging.warning("Modules directory is not readable: " + modulesDirectoryPath);
                return false;
            }
            
            Logging.info("Modules directory is accessible and readable");
            
            // Try to list contents
            long startTime = System.currentTimeMillis();
            File[] contents = modulesDirectory.listFiles();
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
            Logging.error("Error testing modules directory access: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if a module directory exists and is accessible.
     * 
     * @param modulePath The path to the module directory
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean moduleDirectoryExists(String modulePath) {
        File moduleDir = new File(modulePath);
        return moduleDir.exists() && moduleDir.isDirectory() && moduleDir.canRead();
    }
}

