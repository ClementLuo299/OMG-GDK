package launcher.features.module_handling.validation;

import gdk.internal.Logging;
import launcher.features.module_handling.validation.helpers.file_validation.ValidateMainFile;
import launcher.features.module_handling.validation.helpers.file_validation.ValidateMetadataFile;
import launcher.features.module_handling.validation.helpers.file_validation.CheckForRequiredFiles;

import java.io.File;

/**
 * Checks if a directory contains a valid module.
 * 
 * <p>This class provides a single method to check if a directory contains a valid module.
 * All other validation logic is delegated to helper classes.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module validation operations.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleValidator {
    
    private ModuleValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory contains a valid module.
     * 
     * <p>Validity is determined by:
     * <ul>
     *   <li>Presence of required source files (Main.java, Metadata.java)</li>
     *   <li>Minimal API signatures in those files</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the directory contains a valid module, false otherwise
     */
    public static boolean isValidModule(File moduleDir) {
        try {
            // Check for required file structure
            if (!CheckForRequiredFiles.hasRequiredFiles(moduleDir)) {
                return false;
            }
            
            // Validate Main.java content
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            if (!ValidateMainFile.isValid(mainJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Main.java");
                return false;
            }
            
            // Validate Metadata.java content
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            if (!ValidateMetadataFile.isValid(metadataJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Metadata.java");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Logging.error("Error validating module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
}

