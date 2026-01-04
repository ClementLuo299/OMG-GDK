package launcher.features.module_handling.discovery.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;

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
final class ModuleDiscoveryProcess {
    
    private ModuleDiscoveryProcess() {}
    
    /**
     * Performs module discovery by validating the provided module directories.
     * 
     * @param modulesDirectory The modules directory (for diagnostics)
     * @param modulesDirectoryPath The path to the modules directory (for diagnostics)
     * @param moduleDirectories List of module directories to validate
     * @return List of valid module directories
     */
    public static List<File> findModules(File modulesDirectory, String modulesDirectoryPath, List<File> moduleDirectories) {
        List<File> validModuleDirectories = new ArrayList<>();
        
        try {
            // Validate each folder to determine if it's a valid module
            Logging.info("Validating " + moduleDirectories.size() + " module folders...");
            for (File moduleFolder : moduleDirectories) {
                try {
                    if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(moduleFolder)) {
                        validModuleDirectories.add(moduleFolder);
                        Logging.info("Valid module found: " + moduleFolder.getName());
                    } else {
                        Logging.info("Invalid module structure: " + moduleFolder.getName());
                    }
                } catch (Exception e) {
                    Logging.error("Error validating module " + moduleFolder.getName() + ": " + e.getMessage());
                    // Continue with other modules instead of failing completely
                }
            }
            
            Logging.info("Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
            
        } catch (Exception e) {
            // If discovery fails (e.g., file system error), log and run diagnostics
            Logging.error("Module discovery failed: " + e.getMessage(), e);
            e.printStackTrace();
            launcher.features.module_handling.validation.ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        }
        
        // Return list of valid module directories (empty if none found or on error)
        return validModuleDirectories;
    }
    
    /**
     * Runs diagnostics when no modules are found.
     * 
     * @param modulesDirectory The modules directory
     * @param modulesDirectoryPath The file_paths to the modules directory
     */
    public static void runEmptyDiscoveryDiagnostics(File modulesDirectory, String modulesDirectoryPath) {
        // Run comprehensive diagnostics to help identify why no modules were found
        Logging.warning("No valid modules found - running diagnostics...");
        launcher.features.module_handling.validation.ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        
        // Check if modules exist but just need to be compiled
        Logging.info("Checking module compilation status...");
        ModuleCompiler.reportModuleCompilationStatus(modulesDirectory);
    }
}

