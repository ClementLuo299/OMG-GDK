import gdk.GameModule;
import gdk.Logging;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Utility class for discovering and loading game modules.
 * Provides functionality to scan directories for game modules and load them dynamically.
 * Supports both JAR files (production) and compiled classes (development).
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class ModuleLoader {
    
    // ==================== PUBLIC API ====================
    
    /**
     * Discovers game modules in the specified directory.
     * Scans for JAR files and compiled classes, attempts to load GameModule implementations.
     * 
     * @param modulesDir The directory to scan for modules
     * @return List of discovered game modules
     * @throws Exception if there's an error during discovery
     */
    public static List<GameModule> discoverModules(String modulesDir) throws Exception {
        validateModulesDirectory(modulesDir);
        
        List<GameModule> modules = new ArrayList<>();
        File dir = new File(modulesDir);
        
        Logging.info("🔍 Starting module discovery in: " + modulesDir);
        
        // First, try to load from compiled classes (development mode - prioritized)
        List<GameModule> classModules = discoverModulesFromClasses(dir);
        Logging.info("📦 Found " + classModules.size() + " module(s) from compiled classes");
        modules.addAll(classModules);
        
        // Only load from JAR files if no modules found from classes (fallback for speed)
        if (classModules.isEmpty()) {
            List<GameModule> jarModules = discoverModulesFromJars(dir);
            Logging.info("📦 Found " + jarModules.size() + " module(s) from JAR files (fallback)");
            modules.addAll(jarModules);
        } else {
            Logging.info("⏭️ Skipping JAR discovery (using compiled classes)");
        }
        
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
        // Check for Main.java directly in src/main/java/ (simplified structure)
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        if (mainJavaFile.exists() && !isFileCommentedOut(mainJavaFile)) {
            return true;
        }
        
        // Check for Main.java in any subdirectory of src/main/java/ (legacy support)
        File srcDir = new File(moduleDir, "src/main/java");
        if (srcDir.exists()) {
            File[] subdirs = srcDir.listFiles(File::isDirectory);
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    File mainInSubdir = new File(subdir, "Main.java");
                    if (mainInSubdir.exists() && !isFileCommentedOut(mainInSubdir)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if a file is effectively commented out (all content is comments or empty)
     * @param file The file to check
     * @return true if the file is commented out
     */
    private static boolean isFileCommentedOut(File file) {
        try {
            if (!file.exists()) return true;
            
            String content = java.nio.file.Files.readString(file.toPath()).trim();
            if (content.isEmpty()) return true;
            
            // Check if all non-empty lines are comments
            String[] lines = content.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                    return false; // Found non-comment content
                }
            }
            return true; // All lines are comments or empty
        } catch (Exception e) {
            return true; // If we can't read it, assume it's disabled
        }
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
     * Recursively searches for a Main class in the classes directory.
     * 
     * @param classesDir The classes directory to search
     * @param packagePath The current package path
     * @return The fully qualified class name, or null if not found
     */
    private static String findMainClassRecursively(File classesDir, String packagePath) {
        File[] files = classesDir.listFiles();
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packagePath.isEmpty() ? file.getName() : packagePath + "." + file.getName();
                String result = findMainClassRecursively(file, subPackage);
                if (result != null) {
                    return result;
                }
            } else if (file.getName().equals("Main.class")) {
                return packagePath.isEmpty() ? "Main" : packagePath + ".Main";
            }
        }
        
        return null;
    }
    
    // ==================== JAR-BASED MODULE DISCOVERY ====================
    
    /**
     * Discovers game modules from JAR files (production mode).
     * 
     * @param modulesDir The modules directory
     * @return List of discovered game modules
     */
    private static List<GameModule> discoverModulesFromJars(File modulesDir) {
        List<GameModule> modules = new ArrayList<>();
        
        File[] jarFiles = modulesDir.listFiles((file, name) -> name.endsWith(".jar"));
        
        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                try {
                    GameModule module = loadModuleFromJar(jarFile);
                    if (module != null) {
                        modules.add(module);
                        Logging.info("📦 Loaded game module: " + module.getGameName() + " (" + module.getClass().getSimpleName() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load module from " + jarFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        return modules;
    }
    
    /**
     * Loads a game module from a JAR file.
     * 
     * @param jarFile The JAR file to load
     * @return The loaded game module, or null if loading fails
     * @throws Exception if there's an error during loading
     */
    private static GameModule loadModuleFromJar(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            String mainClass = findMainClassInJar(jar);
            
            if (mainClass != null) {
                return createModuleInstanceFromJar(jarFile, mainClass);
            }
        }
        
        return null;
    }
    
    /**
     * Creates a module instance from a JAR file
     */
    private static GameModule createModuleInstanceFromJar(File jarFile, String mainClass) throws Exception {
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarFile.toURI().toURL()},
            ModuleLoader.class.getClassLoader()
        );
        
        Class<?> moduleClass = classLoader.loadClass(mainClass);
        
        if (GameModule.class.isAssignableFrom(moduleClass)) {
            // Validate that the module implements all required methods
            if (validateGameModuleMethods(moduleClass)) {
                Object instance = moduleClass.getDeclaredConstructor().newInstance();
                return (GameModule) instance;
            } else {
                Logging.info("JAR module " + mainClass + " implements GameModule but is missing required methods");
                return null;
            }
        }
        
        return null;
    }
    
    // ==================== JAR CLASS DISCOVERY ====================
    
    /**
     * Finds the main class in a JAR file
     */
    private static String findMainClassInJar(JarFile jarFile) {
        // Check manifest first
        String mainClass = findMainClassInManifest(jarFile);
        
        // If no main class in manifest, try to find GameModule implementations
        if (mainClass == null) {
            mainClass = findGameModuleClass(jarFile);
        }
        
        return mainClass;
    }
    
    /**
     * Finds main class in JAR manifest
     */
    private static String findMainClassInManifest(JarFile jarFile) {
        try {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue("Main-Class");
            }
        } catch (Exception e) {
            System.err.println("Error reading JAR manifest: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Finds a GameModule implementation class in a JAR file.
     * This is a simplified implementation - in a real system, you might want
     * to scan all classes in the JAR.
     * 
     * @param jarFile The JAR file to scan
     * @return The name of a GameModule class, or null if not found
     */
    private static String findGameModuleClass(JarFile jarFile) {
        String[] commonPatterns = {
            "Main",
            "GameModule",
            "Game",
            "Module"
        };
        
        for (String pattern : commonPatterns) {
            if (jarFile.getEntry(pattern + ".class") != null) {
                return pattern;
            }
        }
        
        return null;
    }
} 