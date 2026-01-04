package launcher.features.module_handling.module_target_validation;

import gdk.internal.Logging;
import launcher.features.module_handling.module_source_validation.ModuleValidator;

import java.io.File;

/**
 * Validates that a module is ready to be loaded.
 * 
 * <p>This class combines source validation and target validation to ensure
 * a module has both valid source files and compiled classes ready for loading.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleLoadValidator {
    
    private ModuleLoadValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that a module is ready to be loaded.
     * 
     * <p>This method checks:
     * <ul>
     *   <li>Source files are valid (via ModuleValidator)</li>
     *   <li>Compiled classes exist and are ready (via CheckForCompiledClasses)</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the module is ready to load, false otherwise
     */
    public static boolean isReadyToLoad(File moduleDir) {
        String moduleName = moduleDir.getName();
        
        // Validate source files first
        if (!ModuleValidator.isValidModule(moduleDir)) {
            Logging.info("Module " + moduleName + " has invalid structure");
            return false;
        }
        
        // Check if compiled classes exist
        if (!CheckForCompiledClasses.hasCompiledClasses(moduleDir)) {
            return false;
        }
        
        return true;
    }
}

