package launcher.features.module_handling.discovery.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleFolderFilter;
import launcher.features.module_handling.main_module_directory.ModuleFolderFinder;

import java.io.File;

/**
 * Internal utility class for counting modules in the modules directory.
 * 
 * <p>This class provides methods for counting valid modules by scanning
 * the modules directory and validating module structures. This is a discovery
 * operation that determines how many valid modules exist.
 * 
 * <p>This class is internal to the module handling system. External code should use 
 * {@link launcher.features.module_handling.main_module_directory.ModuleFolderFinder}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleCounter {
    
    private ModuleCounter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Counts the number of valid modules in the modules directory.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return The number of valid modules found
     */
    public static int countValidModules(String modulesDirectoryPath) {

        // Number of valid modules found
        int validCount = 0;
        
        try {

            // Create File object from path string 
            File modulesDirectory = new File(modulesDirectoryPath);

            // Get the subdirectories of the modules directory
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0; // nothing to scan
            }
            
            for (File subdir : subdirs) {

                // Skip infrastructure and hidden directories that are not game modules
                if (ModuleFolderFilter.shouldSkip(subdir)) {
                    continue;
                }
                
                // Get the name of the module
                String moduleName = subdir.getName();
                
                // Only structural validity is checked here (not compilation)
                if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(subdir)) {
                    validCount++;
                    Logging.info("Valid module found: " + moduleName);
                } else {
                    Logging.info("Invalid module found: " + moduleName);
                }
            }
        } catch (Exception e) {
            Logging.error("Error counting valid modules: " + e.getMessage(), e);
        }
        
        return validCount;
    }
}

