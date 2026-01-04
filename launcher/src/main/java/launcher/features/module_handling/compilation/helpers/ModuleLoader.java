package launcher.features.module_handling.compilation.helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.module_code_validation.ModuleValidator;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

/**
 * Helper class for loading modules from compiled classes.
 * 
 * @author Clement Luo
 * @date January 3, 2026
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
        String moduleName = moduleDir.getName();
        Logging.info("Loading module from: " + moduleName);
        Logging.info("   Current thread: " + Thread.currentThread().getName());
        Logging.info("   Is JavaFX thread: " + Platform.isFxApplicationThread());
        
        // Add timeout protection for individual module loading
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 second timeout for individual module loading (increased for JavaFX initialization)
        
        try {
            // Validate source files first
            if (!ModuleValidator.isValidModule(moduleDir)) {
                Logging.info("Module " + moduleName + " has invalid structure");
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Check if compiled classes exist
            File targetClassesDir = new File(moduleDir, "target/classes");
            if (!targetClassesDir.exists()) {
                Logging.info("Module " + moduleName + " missing compiled classes - recompilation needed");
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Verify Main.class exists
            File mainClassFile = new File(targetClassesDir, "Main.class");
            if (!mainClassFile.exists()) {
                Logging.info("Main.class missing in target");
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Create class loader with necessary dependencies
            Logging.info("🔧 Creating classloader for module: " + moduleName);
            URLClassLoader classLoader = ModuleClassLoaderFactory.create(moduleDir);
            Logging.info("✅ Classloader created successfully for module: " + moduleName);
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Load and validate the Main class
            Logging.info("📥 Loading Main class for module: " + moduleName);
            Logging.info("   Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
            
            // Ensure JavaFX Platform is initialized before loading JavaFX-dependent classes
            Logging.info("   JavaFX Platform check - on FX thread: " + Platform.isFxApplicationThread());
            try {
                // Verify JavaFX is accessible
                Platform.isFxApplicationThread(); // This will throw if JavaFX is not initialized
            } catch (Exception javafxCheck) {
                Logging.warning("   JavaFX Platform might not be initialized: " + javafxCheck.getMessage());
            }
            
            Class<?> mainClass;
            try {
                Logging.info("   Attempting to load class 'Main'...");
                long classLoadStart = System.currentTimeMillis();
                mainClass = classLoader.loadClass("Main");
                long classLoadTime = System.currentTimeMillis() - classLoadStart;
                Logging.info("✅ Main class loaded successfully for module: " + moduleName + " (took " + classLoadTime + "ms)");
                Logging.info("   Class name: " + mainClass.getName());
                Logging.info("   Class loader: " + mainClass.getClassLoader().getClass().getName());
            } catch (ClassNotFoundException e) {
                Logging.error("❌ Main class not found for module " + moduleName + ": " + e.getMessage(), e);
                // Try to list available classes in the target/classes directory
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
                Logging.error("❌ Class definition not found for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   This usually means a dependency is missing from the classpath");
                e.printStackTrace();
                return null;
            } catch (ExceptionInInitializerError e) {
                Logging.error("❌ Static initializer error for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   This means the class has a static initializer that failed");
                if (e.getException() != null) {
                    Logging.error("   Caused by: " + e.getException().getMessage(), e.getException());
                }
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                Logging.error("❌ Error loading Main class for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   Exception type: " + e.getClass().getName());
                e.printStackTrace();
                return null;
            }
            
            if (!ClassValidator.isValidMainClass(mainClass)) {
                Logging.info("Main class module_code_validation failed for " + moduleName);
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Create GameModule instance by instantiating the Main class
            if (GameModule.class.isAssignableFrom(mainClass)) {
                Logging.info("🎯 Instantiating Main class for module: " + moduleName);
                try {
                    GameModule module = (GameModule) mainClass.getDeclaredConstructor().newInstance();
                    Logging.info("✅ Instance created for module: " + moduleName);
                    
                    String gameName = module.getMetadata().getGameName();
                    Logging.info("✅ Successfully loaded module: " + moduleName + " (Game: " + gameName + ")");
                    return module;
                } catch (Exception instantiationError) {
                    Logging.error("❌ Failed to instantiate Main class for module " + moduleName + ": " + 
                        instantiationError.getMessage(), instantiationError);
                    instantiationError.printStackTrace();
                    return null;
                }
            } else {
                Logging.warning("Main class does not implement GameModule interface for module: " + moduleName);
                return null;
            }
            
        } catch (Exception e) {
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
     * module availability.
     * 
     * @param moduleDirectories List of module directories to load
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> loadModules(List<File> moduleDirectories) {
        List<GameModule> loadedModules = new ArrayList<>();
        
        // Add timeout protection for module loading
        long startTime = System.currentTimeMillis();
        long timeout = 15000; // 15 second timeout for all module loading
        
        for (File moduleDir : moduleDirectories) {
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout reached, stopping module loading");
                break;
            }
            
            try {
                GameModule module = loadModule(moduleDir);
                if (module != null) {
                    loadedModules.add(module);
                    Logging.info("✅ Module added to loaded list: " + moduleDir.getName());
                } else {
                    Logging.warning("⚠️ Module load returned null: " + moduleDir.getName() + 
                        " (check logs above for details)");
                }
            } catch (Exception e) {
                Logging.error("❌ Exception while loading module " + moduleDir.getName() + ": " + e.getMessage(), e);
                // Continue with other modules instead of failing completely
            }
        }
        
        Logging.info("Module loading completed. Successfully loaded " + loadedModules.size() + " modules");
        return loadedModules;
    }
}

