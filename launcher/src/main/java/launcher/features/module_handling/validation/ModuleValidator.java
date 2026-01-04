package launcher.features.module_handling.validation;

import gdk.internal.Logging;
import launcher.features.module_handling.validation.helpers.MainJavaFileValidator;
import launcher.features.module_handling.validation.helpers.MetadataJavaFileValidator;
import launcher.features.module_handling.validation.helpers.ModuleCompilationChecker;
import launcher.features.module_handling.validation.helpers.ModuleDiagnostics;
import launcher.features.module_handling.validation.helpers.ModuleStructureValidator;

import java.io.File;

/**
 * Public API for module validation.
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
            // Check for required files
            if (!ModuleStructureValidator.hasRequiredFiles(moduleDir)) {
                return false;
            }
            
            // Validate Main.java content
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            if (!MainJavaFileValidator.isValid(mainJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Main.java");
                return false;
            }
            
            // Validate Metadata.java content
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            if (!MetadataJavaFileValidator.isValid(metadataJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Metadata.java");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Logging.error("Error validating module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== DEPRECATED METHODS - FOR BACKWARD COMPATIBILITY ====================
    
    /**
     * @deprecated Use {@link #isValidModule(File)} instead.
     */
    @Deprecated
    public static boolean isValidModuleStructure(File moduleDir) {
        return isValidModule(moduleDir);
    }
    
    /**
     * @deprecated Use {@link launcher.features.module_handling.validation.helpers.ModuleCompilationChecker#needsCompilation(File)} instead.
     */
    @Deprecated
    public static boolean moduleNeedsCompilation(File moduleDir) {
        return ModuleCompilationChecker.needsCompilation(moduleDir);
    }
    
    /**
     * @deprecated Use {@link launcher.features.module_handling.validation.helpers.ModuleDiagnostics#diagnoseModuleDetectionIssues(String)} instead.
     */
    @Deprecated
    public static void diagnoseModuleDetectionIssues(String modulesDirectoryPath) {
        ModuleDiagnostics.diagnoseModuleDetectionIssues(modulesDirectoryPath);
    }
}

