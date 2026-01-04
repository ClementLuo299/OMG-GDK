package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility class for operations on the modules directory.
 * 
 * <p>This class handles directory-level operations such as:
 * <ul>
 *   <li>Scanning the modules directory for module subdirectories</li>
 *   <li>Counting modules in the directory</li>
 * </ul>
 * 
 * <p>This class is package-private. External code should use {@link ModuleDirectoryValidator}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDirectoryUtil {
    
    private ModuleDirectoryUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Gets a list of valid module directories for processing.
     * 
     * <p>This method scans the modules directory and returns a list of directories
     * that pass structural validation. It includes timeout protection for file
     * operations and filters out infrastructure directories.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return List of valid module directories
     */
    public static List<File> getAllValidModuleDirectories(String modulesDirectoryPath) {
        List<File> validModules = new ArrayList<>();
        
        try {
            // Create File object from path string to enable file system operations
            File modulesDirectory = new File(modulesDirectoryPath);
            Logging.info("Starting module discovery in: " + modulesDirectory.getAbsolutePath());
            
            // Add timeout protection for file operations
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // Reduced to 5 second timeout for faster failure detection
            
            // Use file operations to discover subdirectories - this is necessary because:
            // 1. We need to read the filesystem to see what module directories actually exist
            // 2. There's no registry or database of modules - discovery happens by scanning
            // 3. listFiles(File::isDirectory) filters to only directories, excluding files
            // 4. Returns File objects needed for validation and further operations
            Logging.info("Attempting to list directory contents...");
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Directory listing timeout reached");
                return validModules;
            }
            
            if (subdirs == null) {
                Logging.info("No subdirectories found in modules directory");
                return validModules; // empty if nothing to scan
            }
            
            Logging.info("Found " + subdirs.length + " subdirectories to check");
            
            for (File subdir : subdirs) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > timeout) {
                    Logging.warning("Module discovery timeout reached, stopping discovery");
                    break;
                }
                
                // Filter out infrastructure and hidden directories
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
                    String moduleName = subdir.getName();
                    Logging.info("Skipping internal directory: " + moduleName);
                    continue;
                }
                
                String moduleName = subdir.getName();
                Logging.info("Checking module: " + moduleName);
                
                try {
                    Logging.info("Validating module structure for: " + moduleName);
                    // Collect only those passing structural checks
                    if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(subdir)) {
                        validModules.add(subdir);
                        Logging.info("Valid module found: " + moduleName);
                    } else {
                        Logging.info("Invalid module structure: " + moduleName);
                    }
                } catch (Exception e) {
                    Logging.error("Error validating module " + moduleName + ": " + e.getMessage());
                    // Continue with other modules instead of failing completely
                }
            }
            
            Logging.info("Module discovery completed. Found " + validModules.size() + " valid modules");
            
        } catch (Exception e) {
            Logging.error("Error getting valid module directories: " + e.getMessage(), e);
        }
        
        return validModules;
    }
    
    /**
     * Gets the list of all module directories (valid or invalid).
     * 
     * <p>This method returns all directories in the modules directory, regardless
     * of validation status. Useful for diagnostics or bulk operations before validation.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return List of all module directories (excluding infrastructure directories)
     */
    public static List<File> getAllModuleDirectories(String modulesDirectoryPath) {
        List<File> allModules = new ArrayList<>();
        
        try {
            // Create File object from path string
            File modulesDirectory = new File(modulesDirectoryPath);
            
            // Use file operations to discover subdirectories - this is necessary because:
            // 1. We need to read the filesystem to see what module directories actually exist
            // 2. There's no registry or database of modules - discovery happens by scanning
            // 3. listFiles(File::isDirectory) filters to only directories, excluding files
            // 4. Returns File objects needed for further operations
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return allModules;
            }
            
            for (File subdir : subdirs) {
                // Skip infrastructure and hidden directories
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
                    continue;
                }
                
                allModules.add(subdir);
            }
        } catch (Exception e) {
            Logging.error("Error getting all module directories: " + e.getMessage(), e);
        }
        
        return allModules;
    }
}

