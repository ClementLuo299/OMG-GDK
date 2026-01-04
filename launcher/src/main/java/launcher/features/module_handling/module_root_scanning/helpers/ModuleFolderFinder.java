package launcher.features.module_handling.discovery.module_root_scanning.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.module_root_scanning.ScanForModuleFolders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility class for finding module directories.
 * 
 * <p>This class scans the modules directory and finds all possible module folders,
 * filtering out infrastructure directories (like "target", ".git") and hidden directories.
 * 
 * <p>This class is internal to the module_root_scanning package. External code should use
 * {@link ScanForModuleFolders}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleFolderFinder {
    
    private ModuleFolderFinder() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Finds all possible module folders within the given modules directory.
     * 
     * <p>This method scans the directory and returns all subdirectories that could
     * be modules, filtering out infrastructure and hidden directories.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return List of all possible module folders (excluding infrastructure directories)
     */
    public static List<File> findModuleFolders(String modulesDirectoryPath) {
        List<File> allModules = new ArrayList<>();
        
        try {
            // Create File object from path string
            File modulesDirectory = new File(modulesDirectoryPath);
            
            // Get list of subdirectories and stop if there are none
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return allModules;
            }

            // Check each subdirectory and apply filter
            for (File subdir : subdirs) {
                // Skip infrastructure and hidden directories
                if (ModuleFolderFilter.shouldSkip(subdir)) {
                    continue;
                }

                // Add if it passes the filter
                allModules.add(subdir);
            }
        } catch (Exception e) {
            Logging.error("Error finding module folders: " + e.getMessage(), e);
        }

        return allModules;
    }
}

