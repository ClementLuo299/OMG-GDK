package launcher.features.module_handling.compilation;

import gdk.api.GameModule;
import launcher.features.module_handling.compilation.helpers.ModuleLoader;

import java.io.File;
import java.util.List;

/**
 * Public API for module compilation and loading operations.
 * 
 * <p>This class provides methods for loading modules from compiled classes.
 * 
 * <p>For other compilation-related operations:
 * <ul>
 *   <li>Checking if compilation is needed: use {@link CheckCompilationNeeded}</li>
 *   <li>Checking for compilation failures: use {@link CompilationFailures}</li>
 * </ul>
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for compilation and loading operations.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited January 3, 2026
 * @since 1.0
 */
public final class ModuleCompiler {
    
    private ModuleCompiler() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - MODULE LOADING ====================
    
    /**
     * Loads a module from its compiled classes.
     * 
     * <p>This method performs the complete module loading process:
     * <ol>
     *   <li>Validates module structure</li>
     *   <li>Checks for compiled classes</li>
     *   <li>Creates a class loader with dependencies</li>
     *   <li>Loads and validates the Main class</li>
     *   <li>Instantiates the GameModule</li>
     * </ol>
     * 
     * <p>Includes timeout protection and extensive error handling for JavaFX
     * initialization issues.
     * 
     * @param moduleDir The module directory
     * @return The loaded GameModule instance, or null if loading failed
     */
    public static GameModule loadModule(File moduleDir) {
        return ModuleLoader.loadModule(moduleDir);
    }
    
    /**
     * Loads multiple modules from their compiled classes.
     * 
     * <p>This method loads multiple modules sequentially with timeout protection.
     * It continues loading other modules even if one fails, ensuring maximum
     * module availability.
     * 
     * @param moduleDirectories List of module directories to load
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> loadModules(List<File> moduleDirectories) {
        return ModuleLoader.loadModules(moduleDirectories);
    }
}

