package launcher.features.module_handling.main_module_directory.helpers;

import gdk.internal.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility class for finding module directories.
 * 
 * <p>This class scans the modules directory and finds all possible module folders,
 * filtering out infrastructure directories (like "target", ".git") and hidden directories.
 * 
 * <p>This class is internal to the main_module_directory package. External code should use
 * {@link launcher.features.module_handling.main_module_directory.ModuleFolderFinder}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDirectoryFinder {
    
    private ModuleDirectoryFinder() {
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
            Logging.error("Error finding module folders: " + e.getMessage(), e);
        }
        
        return allModules;
    }
}

