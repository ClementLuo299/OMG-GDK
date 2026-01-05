package launcher.features.module_handling.load_modules.helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;
import launcher.features.module_handling.load_modules.helpers.steps.ClassLoaderCreator;
import launcher.features.module_handling.load_modules.helpers.steps.LoadGameModuleFromMain;
import launcher.features.module_handling.load_modules.helpers.steps.LoadMainClassFromBytecode;
import launcher.features.module_handling.load_modules.helpers.steps.PreLoadValidation;
import launcher.features.module_handling.module_target_validation.ModuleTargetValidator;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;

/**
 * Given a directory, we load the game module given its compiled code.
 * 
 * <p>This class handles the complete process of load_modules a game module from its
 * compiled bytecode into a usable GameModule instance. It performs validation,
 * class load_modules, and instantiation with comprehensive error handling.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class ModuleLoadingProcess {
    
    private ModuleLoadingProcess() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads a module from its compiled classes.
     * 
     * <p>This method performs the complete module load_modules process:
     * <ol>
     *   <li><b>Pre-validation:</b> Checks that source files are valid and compiled classes exist</li>
     *   <li><b>ClassLoader creation:</b> Creates a URLClassLoader with module dependencies</li>
     *   <li><b>Class load_modules:</b> Loads the Main class from bytecode into memory</li>
     *   <li><b>Post-validation:</b> Verifies the loaded class implements GameModule interface</li>
     *   <li><b>Instantiation:</b> Creates an instance of the Main class as a GameModule</li>
     * </ol>
     * 
     * <p>Includes timeout protection (30 seconds per module) and extensive error handling
     * for common issues like missing dependencies, JavaFX initialization problems, and
     * class load_modules failures.
     * 
     * @param moduleDir The module directory (e.g., modules/tictactoe/)
     * @return The loaded GameModule instance, or null if load_modules failed
     */
    public static GameModule loadModule(File moduleDir) {

        // ========================================================================
        // STEP 1: Initialize and log module load_modules attempt
        // ========================================================================
        String moduleName = moduleDir.getName();
        Logging.info("Loading module from: " + moduleName);
        Logging.info("   Current thread: " + Thread.currentThread().getName());
        Logging.info("   Is JavaFX thread: " + Platform.isFxApplicationThread());
        
        // Set up timeout protection to prevent hanging on problematic modules
        // 30 seconds should be enough for normal load_modules, but allows for JavaFX initialization delays
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 second timeout for individual module load_modules
        
        try {
            // ========================================================================
            // STEP 2: Pre-load validation - ensure module is ready to load
            // ========================================================================
            // This checks:
            //   - Source files are valid (Main.java, Metadata.java exist and have correct structure)
            //   - Compiled classes exist (target/classes/Main.class exists)
            // If either check fails, we can't load the module, so return early
            if (!PreLoadValidation.preLoadCheck(moduleDir)) {
                Logging.info("Module " + moduleName + " failed pre-load validation");
                return null;
            }
            
            // Check timeout after validation (should be fast, but check anyway)
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module load_modules timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 3: Create ClassLoader for the module
            // ========================================================================
            // The ClassLoader is responsible for:
            //   - Finding the compiled .class files in target/classes/
            //   - Loading dependencies (GDK, JavaFX, etc.) from the classpath
            //   - Resolving class references when load_modules the Main class
            // ModuleClassLoaderFactory creates a URLClassLoader with all necessary paths
            Logging.info("🔧 Creating classloader for module: " + moduleName);
            URLClassLoader classLoader = ClassLoaderCreator.create(moduleDir);
            Logging.info("✅ Classloader created successfully for module: " + moduleName);
            
            // Check timeout after ClassLoader creation
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module load_modules timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 4: Load the Main class from bytecode into memory
            // ========================================================================
            Class<?> mainClass = LoadMainClassFromBytecode.load(classLoader, moduleDir, moduleName, startTime);
            if (mainClass == null) {
                return null; // Error already logged in LoadMainClass
            }
            
            // Check timeout after class load_modules
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module load_modules timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 5: Post-load validation - verify the loaded class is valid
            // ========================================================================
            if (!ModuleTargetValidator.postLoadCheck(mainClass)) {
                Logging.info("Main class validation failed for " + moduleName + " - does not implement GameModule");
                return null;
            }
            
            // Check timeout after validation
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module load_modules timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 6: Instantiate the GameModule
            // ========================================================================
            return LoadGameModuleFromMain.load(mainClass, moduleName);
            
        } catch (Exception e) {
            // Catch-all for any unexpected errors in the entire load_modules process
            Logging.error("❌ Error load_modules module " + moduleName + ": " + e.getMessage(), e);
            Logging.error("   Module directory: " + moduleDir.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * Loads multiple modules from their compiled classes.
     * 
     * <p>This method loads multiple modules sequentially with timeout protection.
     * It continues load_modules other modules even if one fails, ensuring maximum
     * module availability. This is a "best effort" approach - we want to load
     * as many modules as possible, even if some fail.
     * 
     * <p>The method:
     * <ul>
     *   <li>Iterates through each module directory</li>
     *   <li>Calls loadModule() for each one</li>
     *   <li>Collects successful loads and failures separately</li>
     *   <li>Continues even if individual modules fail</li>
     *   <li>Has a global timeout (15 seconds total for all modules)</li>
     * </ul>
     * 
     * @param moduleDirectories List of module directories to load (e.g., [modules/tictactoe/, modules/chatroom/])
     * @return ModuleLoadResult containing:
     *         - List of successfully loaded GameModule instances
     *         - List of module names that failed to load
     */
    public static LoadModules.ModuleLoadResult loadModules(List<File> moduleDirectories) {
        List<GameModule> loadedModules = new ArrayList<>();
        Set<String> failures = new HashSet<>();
        
        // Global timeout for load_modules ALL modules (15 seconds total)
        // This prevents the entire load_modules process from hanging if modules are problematic
        long startTime = System.currentTimeMillis();
        long timeout = 15000; // 15 second timeout for all module load_modules
        
        // Process each module directory
        for (File moduleDir : moduleDirectories) {
            // Check if we've exceeded the global timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module load_modules timeout reached, stopping module load_modules");
                break;
            }
            
            String moduleName = moduleDir.getName();
            try {
                // Attempt to load this module (see loadModule() for detailed process)
                GameModule module = loadModule(moduleDir);
                
                if (module != null) {
                    // Success! Add to the loaded modules list
                    loadedModules.add(module);
                    Logging.info("✅ Module added to loaded list: " + moduleName);
                } else {
                    // Module failed to load (loadModule returned null)
                    // This could be due to validation failure, class load_modules error, etc.
                    // We continue with other modules instead of stopping
                    Logging.warning("⚠️ Module load returned null: " + moduleName + 
                        " (check logs above for details)");
                    failures.add(moduleName);
                }
            } catch (Exception e) {
                // Unexpected exception during load_modules (shouldn't happen, but catch it anyway)
                // This is different from loadModule returning null - this is an actual exception
                Logging.error("❌ Exception while load_modules module " + moduleName + ": " + e.getMessage(), e);
                failures.add(moduleName);
                // Continue with other modules instead of failing completely
            }
        }
        
        // Log summary of load_modules results
        Logging.info("Module load_modules completed. Successfully loaded " + loadedModules.size() + " modules");
        if (!failures.isEmpty()) {
            Logging.info("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
        }
        
        // Return both successful loads and failures so caller can handle them appropriately
        return new LoadModules.ModuleLoadResult(loadedModules, new ArrayList<>(failures));
    }
}

