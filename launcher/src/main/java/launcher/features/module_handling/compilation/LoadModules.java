package launcher.features.module_handling.compilation;

import gdk.api.GameModule;
import launcher.features.module_handling.compilation.helpers.ModuleLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Load modules given its/their directory.
 * 
 * <p>This class provides methods for loading modules from compiled classes.
 * It handles the complete module loading process including validation, class loading,
 * and instantiation.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module loading operations.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited January 3, 2026
 * @since 1.0
 */
public final class LoadModules {
    
    private LoadModules() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static final class ModuleLoadResult {
        
        private final List<GameModule> loadedModules;
        private final List<String> compilationFailures;
        
        /**
         * Creates a new ModuleLoadResult.
         * 
         * @param loadedModules List of successfully loaded GameModule instances
         * @param compilationFailures List of module names that failed to compile or load
         */
        public ModuleLoadResult(List<GameModule> loadedModules, List<String> compilationFailures) {
            this.loadedModules = new ArrayList<>(loadedModules);
            this.compilationFailures = new ArrayList<>(compilationFailures);
        }
        
        /**
         * Gets the list of successfully loaded modules.
         * 
         * @return Unmodifiable list of loaded GameModule instances
         */
        public List<GameModule> getLoadedModules() {
            return Collections.unmodifiableList(loadedModules);
        }
        
        /**
         * Gets the list of compilation failures.
         * 
         * @return Unmodifiable list of module names that failed to compile or load
         */
        public List<String> getCompilationFailures() {
            return Collections.unmodifiableList(compilationFailures);
        }
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
     * @return ModuleLoadResult containing loaded modules and compilation failures
     */
    public static ModuleLoadResult loadModules(List<File> moduleDirectories) {
        return ModuleLoader.loadModules(moduleDirectories);
    }
}

