package launcher.features.module_handling.load_modules.helpers.module_loading_steps;

import gdk.internal.Logging;

import java.io.File;
import java.net.URLClassLoader;
import javafx.application.Platform;

/**
 * Helper class for load_modules the Main class from bytecode into memory.
 * 
 * <p>This class handles the actual class load_modules process including JavaFX platform
 * checks, error handling, and diagnostics for class load_modules failures.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class LoadMainClass {
    
    private LoadMainClass() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads the Main class from bytecode into memory.
     * 
     * <p>This method handles the actual class load_modules process:
     * <ul>
     *   <li>Verifies JavaFX Platform is accessible</li>
     *   <li>Uses the ClassLoader to load the Main class from bytecode</li>
     *   <li>Handles all class load_modules errors with detailed diagnostics</li>
     * </ul>
     * 
     * @param classLoader The URLClassLoader to use for load_modules the class
     * @param moduleDir The module directory (for diagnostics)
     * @param moduleName The name of the module (for logging)
     * @param startTime The start time of the load_modules process (for elapsed time logging)
     * @return The loaded Class<?> object, or null if load_modules failed
     */
    public static Class<?> load(URLClassLoader classLoader, File moduleDir, String moduleName, long startTime) {
        // This is where the actual class load_modules happens:
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
        
        try {
            // Actually load the class - this is where most errors occur
            Logging.info("   Attempting to load class 'Main'...");
            long classLoadStart = System.currentTimeMillis();
            Class<?> mainClass = classLoader.loadClass("Main");
            long classLoadTime = System.currentTimeMillis() - classLoadStart;
            Logging.info("✅ Main class loaded successfully for module: " + moduleName + " (took " + classLoadTime + "ms)");
            Logging.info("   Class name: " + mainClass.getName());
            Logging.info("   Class loader: " + mainClass.getClassLoader().getClass().getName());
            return mainClass;
            
        } catch (ClassNotFoundException e) {
            // The Main.class file doesn't exist or can't be found by the ClassLoader
            // This usually means load_modules failed or the file is in the wrong location
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
            // This happens when static code in the class fails during load_modules
            Logging.error("❌ Static initializer error for module " + moduleName + ": " + e.getMessage(), e);
            Logging.error("   This means the class has a static initializer that failed");
            if (e.getException() != null) {
                Logging.error("   Caused by: " + e.getException().getMessage(), e.getException());
            }
            e.printStackTrace();
            return null;
            
        } catch (Exception e) {
            // Catch-all for any other unexpected errors during class load_modules
            Logging.error("❌ Error load_modules Main class for module " + moduleName + ": " + e.getMessage(), e);
            Logging.error("   Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }
}

