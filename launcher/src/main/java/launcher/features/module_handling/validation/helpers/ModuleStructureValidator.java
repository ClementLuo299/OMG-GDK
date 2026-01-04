package launcher.features.module_handling.validation.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for validating module structure.
 * 
 * <p>This class validates that a directory contains the required files
 * and structure for a valid module.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleStructureValidator {
    
    private ModuleStructureValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that a directory has the required structure for a module.
     * 
     * @param moduleDir The module directory to validate
     * @return true if the directory has required files, false otherwise
     */
    public static boolean hasRequiredFiles(File moduleDir) {
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
        
        if (!mainJavaFile.exists() || !metadataJavaFile.exists()) {
            Logging.info("Module " + moduleDir.getName() + " missing required source files");
            return false;
        }
        
        return true;
    }
}

