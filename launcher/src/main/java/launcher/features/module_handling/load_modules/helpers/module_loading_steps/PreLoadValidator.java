package launcher.features.module_handling.loading.helpers.module_loading_steps;

import gdk.internal.Logging;
import launcher.features.module_handling.check_compilation_status.CheckCompilationNeeded;
import launcher.features.module_handling.compilation.CompileModule;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;

import java.io.File;

/**
 * Validates that a module is ready to be loaded.
 * 
 * <p>This class combines source validation and compilation status checking to ensure
 * a module has both valid source files and compiled classes ready for loading.
 * If compilation is needed, it automatically compiles the module before validation.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class PreLoadValidator {
    
    private PreLoadValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that a module is ready to be loaded.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates source files are valid (via ModuleSourceValidator)</li>
     *   <li>Checks if module needs compilation (via CheckCompilationNeeded)</li>
     *   <li>If compilation is needed, automatically compiles the module (via CompileModule)</li>
     *   <li>Verifies compilation succeeded and module is ready to load</li>
     * </ol>
     * 
     * <p>The compilation check verifies:
     * <ul>
     *   <li>Compiled classes directory exists</li>
     *   <li>Main.class file exists</li>
     *   <li>Source files are not newer than compiled classes</li>
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
        
        // Check if module needs compilation
        if (CheckCompilationNeeded.needsCompilation(moduleDir)) {
            Logging.info("Module " + moduleName + " needs compilation - attempting to compile...");
            
            // Automatically compile the module
            boolean compilationSuccess = CompileModule.compile(moduleDir);
            
            if (!compilationSuccess) {
                Logging.error("Module " + moduleName + " compilation failed - not ready to load");
                return false;
            }
            
            Logging.info("Module " + moduleName + " compiled successfully");
            
            // Verify compilation succeeded by checking again
            if (CheckCompilationNeeded.needsCompilation(moduleDir)) {
                Logging.warning("Module " + moduleName + " still needs compilation after compile attempt");
                return false;
            }
        }
        
        return true;
    }
}

