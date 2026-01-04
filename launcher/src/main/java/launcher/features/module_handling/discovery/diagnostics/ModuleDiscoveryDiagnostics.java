package launcher.features.module_handling.discovery.diagnostics;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.diagnostics.helpers.*;

import java.io.File;

/**
 * Runs diagnostics for module detection.
 * 
 * <p>This class orchestrates comprehensive diagnostics to check why no modules are being detected.
 * All diagnostic steps are delegated to helper classes.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDiscoveryDiagnostics {
    
    private ModuleDiscoveryDiagnostics() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Comprehensive diagnostic method to check why modules are not being detected.
     * 
     * <p>This method performs extensive diagnostics including:
     * <ul>
     *   <li>Checking current working directory</li>
     *   <li>Verifying modules directory existence and accessibility</li>
     *   <li>Listing all contents</li>
     *   <li>Validating each potential module's structure</li>
     *   <li>Checking compilation status</li>
     * </ul>
     * 
     * <p>This will help identify file system, path, or module_code_validation issues.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     */
    public static void runModuleDetectionDiagnostics(String modulesDirectoryPath) {
        Logging.info("=== MODULE DETECTION DIAGNOSTICS ===");
        
        try {
            // Step 1: Check current working directory
            CheckWorkingDirectory.check();
            
            // Step 2: Check and resolve modules directory path
            File modulesDirectory = CheckModulesDirectoryPath.check(modulesDirectoryPath);
            
            // Step 3: Check if directory exists and is accessible
            if (!CheckDirectoryAccess.check(modulesDirectory)) {
                // If directory doesn't exist, search common locations
                SearchCommonLocationsForModuleDirectory.search();
                Logging.info("=== END DIAGNOSTICS ===");
                return;
            }
            
            // Step 4: List all directory contents
            File[] allContents = ListModuleDirectoryContents.list(modulesDirectory);
            if (allContents == null) {
                Logging.info("=== END DIAGNOSTICS ===");
                return;
            }
            
            // Step 5: Validate module directories
            ValidateModuleDirectories.validate(modulesDirectory);
            
            // Step 6: Check compilation status
            ValidateModuleDirectories.checkCompilationStatus(modulesDirectory);
            
        } catch (Exception e) {
            Logging.error("Error during diagnostics: " + e.getMessage(), e);
        }
        
        Logging.info("=== END DIAGNOSTICS ===");
    }
}

