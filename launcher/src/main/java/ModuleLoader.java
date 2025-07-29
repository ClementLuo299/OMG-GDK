import gdk.GameModule;
import gdk.Logging;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for discovering and loading game modules.
 * Provides functionality to scan directories for game modules and load them dynamically.
 * Supports compiled classes for development.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class ModuleLoader {
    
    // ==================== PUBLIC API ====================
    
    /**
     * Discovers game modules in the specified directory.
     * Scans for compiled classes and attempts to load GameModule implementations.
     * 
     * @param modulesDir The directory to scan for modules
     * @return List of discovered game modules
     * @throws Exception if there's an error during discovery
     */
    public static List<GameModule> discoverModules(String modulesDir) throws Exception {
        validateModulesDirectory(modulesDir);
        
        File dir = new File(modulesDir);
        Logging.info("🔍 Starting module discovery in: " + modulesDir);
        
        // Load from compiled classes (development mode)
        List<GameModule> modules = discoverModulesFromClasses(dir);
        Logging.info("📦 Found " + modules.size() + " module(s) from compiled classes");
        
        Logging.info("✅ Total modules discovered: " + modules.size());
        return modules;
    }
    
    /**
     * Validates a game module.
     * @param module The module to validate
     * @return true if the module is valid
     */
    public static boolean validateModule(GameModule module) {
        if (module == null) {
            return false;
        }
        
        try {
            String className = module.getClass().getSimpleName();
            String packageName = module.getClass().getPackageName();
            
            System.out.println("Validating module: " + className + " from package: " + packageName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Module validation failed: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Validates that the modules directory exists and is a directory
     */
    private static void validateModulesDirectory(String modulesDir) {
        File dir = new File(modulesDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Modules directory does not exist: " + modulesDir);
        }
    }
    
    // ==================== CLASS-BASED MODULE DISCOVERY ====================
    
    /**
     * Discovers game modules from compiled classes (development mode).
     * 
     * @param modulesDir The modules directory
     * @return List of discovered game modules
     */
    private static List<GameModule> discoverModulesFromClasses(File modulesDir) {
        List<GameModule> modules = new ArrayList<>();
        
        File[] subdirs = modulesDir.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                try {
                    GameModule module = loadModuleFromClasses(subdir);
                    if (module != null) {
                        modules.add(module);
                    }
                } catch (Exception e) {
                    Logging.error("Failed to load module from classes in " + subdir.getName() + ": " + e.getMessage(), e);
                }
            }
        }
        
        return modules;
    }
    
    /**
     * Loads a game module from compiled classes.
     * 
     * @param moduleDir The module directory containing compiled classes
     * @return The loaded game module, or null if loading fails
     */
    private static GameModule loadModuleFromClasses(File moduleDir) {
        try {
            // First check if source code exists (fast check)
            if (!hasMainSourceFile(moduleDir)) {
                return null; // Skip if no source code
            }
            
            File classesDir = findClassesDirectory(moduleDir);
            if (classesDir == null) {
                return null; // No compiled classes, skip silently
            }
            
            String mainClassName = findMainClassInDirectory(classesDir);
            if (mainClassName == null) {
                return null; // No Main class, skip silently
            }
            
            // Try to create the module instance - this validates it implements GameModule
            GameModule module = createModuleInstance(classesDir, mainClassName);
            if (module == null) {
                Logging.info("Module " + moduleDir.getName() + " has Main class but doesn't implement GameModule interface");
                return null; // Main class exists but doesn't implement GameModule
            }
            
            return module;
            
        } catch (Exception e) {
            Logging.error("Error loading module from classes in " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Checks if a module has a Main.java source file
     */
    private static boolean hasMainSourceFile(File moduleDir) {
        // Check for Main.java directly in src/main/java/ (standardized structure)
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        return mainJavaFile.exists();
    }
    
    /**
     * Finds the classes directory in a module directory
     */
    private static File findClassesDirectory(File moduleDir) {
        File classesDir = new File(moduleDir, "target/classes");
        if (!classesDir.exists() || !classesDir.isDirectory()) {
            return null;
        }
        return classesDir;
    }
    
    /**
     * Creates a module instance from a classes directory
     */
    private static GameModule createModuleInstance(File classesDir, String mainClassName) throws Exception {
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{classesDir.toURI().toURL()},
            ModuleLoader.class.getClassLoader()
        );
        
        Class<?> moduleClass = classLoader.loadClass(mainClassName);
        
        if (GameModule.class.isAssignableFrom(moduleClass)) {
            // Validate that the module implements all required methods
            if (validateGameModuleMethods(moduleClass)) {
                Object instance = moduleClass.getDeclaredConstructor().newInstance();
                return (GameModule) instance;
            } else {
                Logging.info("Module " + mainClassName + " implements GameModule but is missing required methods");
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Validates that a GameModule class implements all required methods
     * @param moduleClass The class to validate
     * @return true if all required methods are implemented
     */
    private static boolean validateGameModuleMethods(Class<?> moduleClass) {
        try {
            // Check for required methods
            boolean hasLaunchGame = hasMethod(moduleClass, "launchGame", javafx.stage.Stage.class);
            boolean hasStopGame = hasMethod(moduleClass, "stopGame");
            boolean hasHandleMessage = hasMethod(moduleClass, "handleMessage", java.util.Map.class);
            boolean hasGetMetadata = hasMethod(moduleClass, "getMetadata");
            
            // All methods must be present for a valid game module
            return hasLaunchGame && hasStopGame && hasHandleMessage && hasGetMetadata;
            
        } catch (Exception e) {
            Logging.error("Error validating GameModule methods: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if a class has a specific method
     * @param clazz The class to check
     * @param methodName The method name
     * @param parameterTypes The parameter types (can be empty for no parameters)
     * @return true if the method exists
     */
    private static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    // ==================== CLASS DISCOVERY ====================
    
    /**
     * Finds the main class in a compiled classes directory.
     * 
     * @param classesDir The classes directory
     * @return The main class name, or null if not found
     */
    private static String findMainClassInDirectory(File classesDir) {
        return findMainClassRecursively(classesDir, "");
    }
    
    /**
     * Finds the Main class in the classes directory.
     * 
     * @param classesDir The classes directory to search
     * @param packagePath The current package path (unused, kept for compatibility)
     * @return The class name, or null if not found
     */
    private static String findMainClassRecursively(File classesDir, String packagePath) {
        File mainClassFile = new File(classesDir, "Main.class");
        return mainClassFile.exists() ? "Main" : null;
    }
} 