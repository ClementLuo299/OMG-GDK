package launcher.features.module_handling.discovery.diagnostics.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.module_source_validation.ModuleValidator;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;

import java.io.File;
import java.util.List;

/**
 * Helper class for validating module directories during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ValidateModuleDirectories {
    
    private ValidateModuleDirectories() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates all module directories found in the modules directory.
     * 
     * <p>This method uses ScanForModuleFolders to find potential module folders
     * (which already filters out infrastructure directories), then uses
     * ModuleValidator to check each one and logs the validation results
     * along with compilation status.
     * 
     * @param modulesDirectory The modules directory containing potential module folders
     */
    public static void validate(File modulesDirectory) {
        String modulesDirectoryPath = modulesDirectory.getAbsolutePath();
        List<File> moduleFolders = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
        
        if (moduleFolders.isEmpty()) {
            Logging.info("No module folders found in: " + modulesDirectoryPath);
            return;
        }
        
        Logging.info("Found " + moduleFolders.size() + " potential module folder(s):");
        for (File moduleFolder : moduleFolders) {
            String moduleName = moduleFolder.getName();
            Logging.info("Checking module folder: " + moduleName);
            
            // Validate using ModuleValidator (which already checks all required files)
            try {
                boolean isValid = ModuleValidator.isValidModule(moduleFolder);
                Logging.info("Module validation result for " + moduleName + ": " + (isValid ? "VALID" : "INVALID"));
                
                if (isValid) {
                    // Check compilation status
                    boolean needsCompilation = CheckCompilationNeeded.needsCompilation(moduleFolder);
                    Logging.info("Compilation needed: " + needsCompilation);
                    
                    if (needsCompilation) {
                        Logging.info("Run 'mvn compile' in modules/" + moduleName + " to compile");
                    }
                }
            } catch (Exception e) {
                Logging.error("Error validating module " + moduleName + ": " + e.getMessage());
            }
        }
    }
}

