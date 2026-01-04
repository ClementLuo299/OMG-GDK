package launcher.features.module_handling.module_target_validation;

import launcher.features.module_handling.module_target_validation.helpers.CheckForCompiledClasses;
import launcher.features.module_handling.module_target_validation.helpers.ClassValidator;

import java.io.File;

/**
 * Checks if a module has valid compiled classes ready for loading.
 * 
 * <p>This class provides a single method to check if a module has valid compiled classes.
 * All other module_target_validation logic is delegated to helper classes.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module target validation operations.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleTargetValidator {
    
    private ModuleTargetValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a module has valid compiled classes ready for loading.
     * 
     * <p>Validity is determined by:
     * <ul>
     *   <li>Presence of compiled classes in target/classes directory</li>
     *   <li>Main.class exists in target/classes</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the module has valid compiled classes, false otherwise
     */
    public static boolean preLoadCheck(File moduleDir) {
        return CheckForCompiledClasses.hasCompiledClasses(moduleDir);
    }
    
    /**
     * Validates that a loaded Main class implements the GameModule interface.
     * 
     * @param mainClass The Main class to validate
     * @return true if the class implements GameModule, false otherwise
     */
    public static boolean postLoadCheck(Class<?> mainClass) {
        return ClassValidator.isValidMainClass(mainClass);
    }
}

