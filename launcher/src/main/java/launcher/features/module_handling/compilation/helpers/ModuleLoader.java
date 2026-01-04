package launcher.features.module_handling.compilation.helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.LoadModules;
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
 * <p>This class handles the complete process of loading a game module from its
 * compiled bytecode into a usable GameModule instance. It performs validation,
 * class loading, and instantiation with comprehensive error handling.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleLoader {
    
    private ModuleLoader() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads a module from its compiled classes.
     * 
     * <p>This method performs the complete module loading process:
     * <ol>
     *   <li><b>Pre-validation:</b> Checks that source files are valid and compiled classes exist</li>
     *   <li><b>ClassLoader creation:</b> Creates a URLClassLoader with module dependencies</li>
     *   <li><b>Class loading:</b> Loads the Main class from bytecode into memory</li>
     *   <li><b>Post-validation:</b> Verifies the loaded class implements GameModule interface</li>
     *   <li><b>Instantiation:</b> Creates an instance of the Main class as a GameModule</li>
     * </ol>
     * 
     * <p>Includes timeout protection (30 seconds per module) and extensive error handling
     * for common issues like missing dependencies, JavaFX initialization problems, and
     * class loading failures.
     * 
     * @param moduleDir The module directory (e.g., modules/tictactoe/)
     * @return The loaded GameModule instance, or null if loading failed
     */
    public static GameModule loadModule(File moduleDir) {

        // ========================================================================
        // STEP 1: Initialize and log module loading attempt
        // ========================================================================
        String moduleName = moduleDir.getName();
        Logging.info("Loading module from: " + moduleName);
        Logging.info("   Current thread: " + Thread.currentThread().getName());
        Logging.info("   Is JavaFX thread: " + Platform.isFxApplicationThread());
        
        // Set up timeout protection to prevent hanging on problematic modules
        // 30 seconds should be enough for normal loading, but allows for JavaFX initialization delays
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 second timeout for individual module loading
        
        try {
            // ========================================================================
            // STEP 2: Pre-load validation - ensure module is ready to load
            // ========================================================================
            // This checks:
            //   - Source files are valid (Main.java, Metadata.java exist and have correct structure)
            //   - Compiled classes exist (target/classes/Main.class exists)
            // If either check fails, we can't load the module, so return early
            if (!ModuleValidator.preLoadCheck(moduleDir)) {
                Logging.info("Module " + moduleName + " failed pre-load validation");
                return null;
            }
            
            // Check timeout after validation (should be fast, but check anyway)
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 3: Create ClassLoader for the module
            // ========================================================================
            // The ClassLoader is responsible for:
            //   - Finding the compiled .class files in target/classes/
            //   - Loading dependencies (GDK, JavaFX, etc.) from the classpath
            //   - Resolving class references when loading the Main class
            // ModuleClassLoaderFactory creates a URLClassLoader with all necessary paths
            Logging.info("🔧 Creating classloader for module: " + moduleName);
            URLClassLoader classLoader = CreateClassLoader.create(moduleDir);
            Logging.info("✅ Classloader created successfully for module: " + moduleName);
            
            // Check timeout after ClassLoader creation
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 4: Load the Main class from bytecode into memory
            // ========================================================================
            // This is where the actual class loading happens:
            //   - The ClassLoader reads Main.class from disk
            //   - Parses the bytecode and creates a Class<?> object
            //   - Resolves any dependencies (other classes, interfaces, etc.)
            //   - Executes static initializers if present
            Logging.info("📥 Loading Main class for module: " + moduleName);
            Logging.info("   Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
            
            // JavaFX check: Some modules may use JavaFX, so we verify the platform is accessible
            // This helps diagnose issues if JavaFX isn't properly initialized
            Logging.info("   JavaFX Platform check - on FX thread: " + Platform.isFxApplicationThread());
            try {
                // This will throw if JavaFX is not initialized
                Platform.isFxApplicationThread();
            } catch (Exception javafxCheck) {
                Logging.warning("   JavaFX Platform might not be initialized: " + javafxCheck.getMessage());
            }
            
            Class<?> mainClass;
            try {
                // Actually load the class - this is where most errors occur
                Logging.info("   Attempting to load class 'Main'...");
                long classLoadStart = System.currentTimeMillis();
                mainClass = classLoader.loadClass("Main");
                long classLoadTime = System.currentTimeMillis() - classLoadStart;
                Logging.info("✅ Main class loaded successfully for module: " + moduleName + " (took " + classLoadTime + "ms)");
                Logging.info("   Class name: " + mainClass.getName());
                Logging.info("   Class loader: " + mainClass.getClassLoader().getClass().getName());
                
            } catch (ClassNotFoundException e) {
                // The Main.class file doesn't exist or can't be found by the ClassLoader
                // This usually means compilation failed or the file is in the wrong location
                Logging.error("❌ Main class not found for module " + moduleName + ": " + e.getMessage(), e);
                
                // Diagnostic: List what class files actually exist to help debug
                try {
                    File classesDir = new File(moduleDir, "target/classes");
                    if (classesDir.exists()) {
                        Logging.info("🔍 Checking for classes in: " + classesDir.getAbsolutePath());
                        java.nio.file.Files.walk(classesDir.toPath())
                            .filter(p -> p.toString().endsWith(".class"))
                            .forEach(p -> Logging.info("   Found class file: " + p.toString()));
                    }
                } catch (Exception listError) {
                    Logging.error("Error listing classes: " + listError.getMessage());
                }
                return null;
                
            } catch (NoClassDefFoundError e) {
                // A dependency of Main.class is missing from the classpath
                // This usually means a required library (like GDK or JavaFX) isn't available
                Logging.error("❌ Class definition not found for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   This usually means a dependency is missing from the classpath");
                e.printStackTrace();
                return null;
                
            } catch (ExceptionInInitializerError e) {
                // Main.class has a static initializer block that threw an exception
                // This happens when static code in the class fails during loading
                Logging.error("❌ Static initializer error for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   This means the class has a static initializer that failed");
                if (e.getException() != null) {
                    Logging.error("   Caused by: " + e.getException().getMessage(), e.getException());
                }
                e.printStackTrace();
                return null;
                
            } catch (Exception e) {
                // Catch-all for any other unexpected errors during class loading
                Logging.error("❌ Error loading Main class for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   Exception type: " + e.getClass().getName());
                e.printStackTrace();
                return null;
            }
            
            // ========================================================================
            // STEP 5: Post-load validation - verify the loaded class is valid
            // ========================================================================
            // Now that the class is loaded, we verify it implements the GameModule interface
            // This uses reflection to check if the class implements GameModule
            // We can't do this check before loading because we need the Class object
            if (!ModuleTargetValidator.postLoadCheck(mainClass)) {
                Logging.info("Main class validation failed for " + moduleName + " - does not implement GameModule");
                return null;
            }
            
            // Check timeout after class loading and validation
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // ========================================================================
            // STEP 6: Instantiate the GameModule
            // ========================================================================
            // Finally, create an instance of the Main class by calling its no-arg constructor
            // We can safely cast to GameModule because we validated it in step 5
            // This is where the module's constructor runs, so any initialization code executes here
            Logging.info("🎯 Instantiating Main class for module: " + moduleName);
            try {
                // Use reflection to call the no-argument constructor
                // This will fail if the class doesn't have a no-arg constructor or if
                // the constructor throws an exception
                GameModule module = (GameModule) mainClass.getDeclaredConstructor().newInstance();
                Logging.info("✅ Instance created for module: " + moduleName);
                
                // Verify we can access the module's metadata (sanity check)
                String gameName = module.getMetadata().getGameName();
                Logging.info("✅ Successfully loaded module: " + moduleName + " (Game: " + gameName + ")");
                return module;
                
            } catch (Exception instantiationError) {
                // Constructor failed - could be missing no-arg constructor, constructor threw exception, etc.
                Logging.error("❌ Failed to instantiate Main class for module " + moduleName + ": " + 
                    instantiationError.getMessage(), instantiationError);
                instantiationError.printStackTrace();
                return null;
            }
            
        } catch (Exception e) {
            // Catch-all for any unexpected errors in the entire loading process
            Logging.error("❌ Error loading module " + moduleName + ": " + e.getMessage(), e);
            Logging.error("   Module directory: " + moduleDir.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * Loads multiple modules from their compiled classes.
     * 
     * <p>This method loads multiple modules sequentially with timeout protection.
     * It continues loading other modules even if one fails, ensuring maximum
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
        
        // Global timeout for loading ALL modules (15 seconds total)
        // This prevents the entire loading process from hanging if modules are problematic
        long startTime = System.currentTimeMillis();
        long timeout = 15000; // 15 second timeout for all module loading
        
        // Process each module directory
        for (File moduleDir : moduleDirectories) {
            // Check if we've exceeded the global timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout reached, stopping module loading");
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
                    // This could be due to validation failure, class loading error, etc.
                    // We continue with other modules instead of stopping
                    Logging.warning("⚠️ Module load returned null: " + moduleName + 
                        " (check logs above for details)");
                    failures.add(moduleName);
                }
            } catch (Exception e) {
                // Unexpected exception during loading (shouldn't happen, but catch it anyway)
                // This is different from loadModule returning null - this is an actual exception
                Logging.error("❌ Exception while loading module " + moduleName + ": " + e.getMessage(), e);
                failures.add(moduleName);
                // Continue with other modules instead of failing completely
            }
        }
        
        // Log summary of loading results
        Logging.info("Module loading completed. Successfully loaded " + loadedModules.size() + " modules");
        if (!failures.isEmpty()) {
            Logging.info("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
        }
        
        // Return both successful loads and failures so caller can handle them appropriately
        return new LoadModules.ModuleLoadResult(loadedModules, new ArrayList<>(failures));
    }
}

