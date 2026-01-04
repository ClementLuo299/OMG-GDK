package launcher.features.module_handling.compilation.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;
import launcher.features.module_handling.module_target_validation.ModuleTargetValidator;

import java.io.File;

/**
 * Validates that a module is ready to be loaded.
 * 
 * <p>This class combines source validation and target validation to ensure
 * a module has both valid source files and compiled classes ready for loading.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleValidator {
    
    private ModuleValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that a module is ready to be loaded.
     * 
     * <p>This method checks:
     * <ul>
     *   <li>Source files are valid (via ModuleSourceValidator)</li>
     *   <li>Compiled classes exist and are ready (via ModuleTargetValidator)</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the module is ready to load, false otherwise
     */
    public static boolean preLoadCheck(File moduleDir) {
        String moduleName = moduleDir.getName();
        
        // Validate source files first
        if (!ModuleSourceValidator.isValidModule(moduleDir)) {
            Logging.info("Module " + moduleName + " has invalid structure");
            return false;
        }
        
        // Check if compiled classes exist
        if (!ModuleTargetValidator.preLoadCheck(moduleDir)) {
            return false;
        }
        
        return true;
    }
}

