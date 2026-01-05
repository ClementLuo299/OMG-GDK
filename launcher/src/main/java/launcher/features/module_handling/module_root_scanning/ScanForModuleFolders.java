package launcher.features.module_handling.module_root_scanning;

import launcher.features.file_handling.directory_access.DirectoryAccessCheck;
import launcher.features.module_handling.module_root_scanning.helpers.ModuleFolderFinder;

import java.io.File;
import java.util.List;

/**
 * Finds the folders for individual game modules.
 * 
 * <p>This class handles the main modules directory. Given a path, it checks
 * if the directory is accessible and returns all possible module folders within it.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for directory management operations.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ScanForModuleFolders {
    
    private ScanForModuleFolders() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Gets all module directories from the given modules directory path.
     * 
     * <p>This method checks if the directory is accessible, then scans it and returns
     * all subdirectories that could be modules, filtering out infrastructure directories
     * (like "target", ".git") and hidden directories.
     * 
     * <p>If the directory is not accessible, returns an empty list.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return List of all possible module directories (excluding infrastructure directories),
     *         or empty list if the directory is not accessible
     */
    public static List<File> findModuleFolders(String modulesDirectoryPath) {
        // Check directory_access first
        if (!DirectoryAccessCheck.checkAccess(modulesDirectoryPath)) {
            return new java.util.ArrayList<>();
        }
        
        // If accessible, find and return all module folders
        return ModuleFolderFinder.findModuleFolders(modulesDirectoryPath);
    }
}

