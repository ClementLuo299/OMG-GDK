package launcher.features.module_handling.load_modules.helpers.module_loading_steps;

import gdk.internal.Logging;
import launcher.features.module_handling.check_compilation_status.CheckCompilationNeeded;
import launcher.features.module_handling.compile_modules.CompileModule;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;

import java.io.File;

/**
 * Validates that a module is ready to be loaded.
 * 
 * <p>This class combines source validation and compile_modules status checking to ensure
 * a module has both valid source files and compiled classes ready for load_modules.
 * If compile_modules is needed, it automatically compiles the module before validation.
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
     *   <li>Checks if module needs compile_modules (via CheckCompilationNeeded)</li>
     *   <li>If compile_modules is needed, automatically compiles the module (via CompileModule)</li>
     *   <li>Verifies compile_modules succeeded and module is ready to load</li>
     * </ol>
     * 
     * <p>The compile_modules check verifies:
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
        
        // Check if module needs compile_modules
        if (CheckCompilationNeeded.needsCompilation(moduleDir)) {
            Logging.info("Module " + moduleName + " needs compile_modules - attempting to compile...");
            
            // Automatically compile the module
            boolean compilationSuccess = CompileModule.compile(moduleDir);
            
            if (!compilationSuccess) {
                Logging.error("Module " + moduleName + " compile_modules failed - not ready to load");
                return false;
            }
            
            Logging.info("Module " + moduleName + " compiled successfully");
            
            // Verify compile_modules succeeded by checking again
            if (CheckCompilationNeeded.needsCompilation(moduleDir)) {
                Logging.warning("Module " + moduleName + " still needs compile_modules after compile attempt");
                return false;
            }
        }
        
        return true;
    }
}

