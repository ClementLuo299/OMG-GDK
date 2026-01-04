package launcher.features.module_handling.discovery.diagnostics.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.validation.ModuleValidator;
import launcher.features.module_handling.validation.helpers.CheckCompilationNeeded;

import java.io.File;

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
     * @param modulesDirectory The modules directory containing potential module folders
     */
    public static void validate(File modulesDirectory) {
        File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
        if (subdirs == null) {
            Logging.error("Cannot list subdirectories (null returned)");
            return;
        }
        
        Logging.info("Found " + subdirs.length + " subdirectories:");
        for (File subdir : subdirs) {
            String moduleName = subdir.getName();
            Logging.info("Checking subdirectory: " + moduleName);
            
            // Skip infrastructure and hidden directories
            String dirName = subdir.getName();
            if (dirName.equals("target") || dirName.startsWith(".")) {
                Logging.info("Skipping internal directory: " + moduleName);
                continue;
            }
            
            // Check module structure
            Logging.info("Validating module structure for: " + moduleName);
            
            // Check for required files
            File mainJavaFile = new File(subdir, "src/main/java/Main.java");
            File metadataJavaFile = new File(subdir, "src/main/java/Metadata.java");
            File targetClassesDir = new File(subdir, "target/classes");
            File mainClassFile = new File(targetClassesDir, "Main.class");
            
            Logging.info("Main.java exists: " + mainJavaFile.exists());
            Logging.info("Metadata.java exists: " + metadataJavaFile.exists());
            Logging.info("target/classes exists: " + targetClassesDir.exists());
            Logging.info("Main.class exists: " + mainClassFile.exists());
            
            // Try to validate the structure
            try {
                boolean isValid = ModuleValidator.isValidModule(subdir);
                Logging.info("Module structure validation result: " + isValid);
                
                if (isValid) {
                    Logging.info("Module " + moduleName + " is VALID!");
                    
                    // Check compilation status
                    boolean needsCompilation = CheckCompilationNeeded.needsCompilation(subdir);
                    Logging.info("Compilation needed: " + needsCompilation);
                    
                    if (needsCompilation) {
                        Logging.info("Run 'mvn compile' in modules/" + moduleName + " to compile");
                    }
                } else {
                    Logging.info("Module " + moduleName + " is INVALID");
                }
            } catch (Exception e) {
                Logging.error("Error validating module " + moduleName + ": " + e.getMessage());
            }
        }
    }
}

