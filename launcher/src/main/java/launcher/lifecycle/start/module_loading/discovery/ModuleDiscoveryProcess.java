package launcher.lifecycle.start.module_loading.discovery;

import gdk.internal.Logging;
import launcher.utils.module.ModuleDiscovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes the actual module discovery process.
 * Responsible for discovering valid modules and handling discovery errors.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleDiscoveryProcess {
    
    private ModuleDiscoveryProcess() {}
    
    /**
     * Performs module discovery on the validated modules directory.
     * 
     * @param modulesDirectory The validated modules directory
     * @param modulesDirectoryPath The path to the modules directory
     * @return List of valid module directories
     */
    public static List<File> findModules(File modulesDirectory, String modulesDirectoryPath) {
        List<File> validModuleDirectories = new ArrayList<>();
        
        try {
            // Scan the modules directory and validate each subdirectory as a module
            Logging.info("Starting module discovery...");
            validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
            Logging.info("Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
            
        } catch (Exception e) {
            // If discovery fails (e.g., file system error), log and run diagnostics
            Logging.error("Module discovery failed: " + e.getMessage(), e);
            e.printStackTrace();
            ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        }
        
        // Return list of valid module directories (empty if none found or on error)
        return validModuleDirectories;
    }
    
    /**
     * Runs diagnostics when no modules are found.
     * 
     * @param modulesDirectory The modules directory
     * @param modulesDirectoryPath The path to the modules directory
     */
    public static void runEmptyDiscoveryDiagnostics(File modulesDirectory, String modulesDirectoryPath) {
        // Run comprehensive diagnostics to help identify why no modules were found
        Logging.warning("No valid modules found - running diagnostics...");
        ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        
        // Check if modules exist but just need to be compiled
        Logging.info("Checking module compilation status...");
        ModuleDiscovery.reportModuleCompilationStatus(modulesDirectory);
    }
}

