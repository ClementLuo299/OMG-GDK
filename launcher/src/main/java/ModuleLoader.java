import gdk.GameModule;
import gdk.Logging;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility class for discovering and loading game modules.
 * Provides functionality to scan directories for game modules and load them dynamically.
 * Supports source code analysis for development.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited August 4, 2025
 * @since 1.0
 */
public class ModuleLoader {
    
    // ==================== PUBLIC API ====================
    
    /**
     * Discovers game modules in the specified directory.
     * Scans for source code and attempts to load GameModule implementations.
     * 
     * @param modulesDir The directory to scan for modules
     * @return List of discovered game modules
     * @throws Exception if there's an error during discovery
     */
    public static List<GameModule> discoverModules(String modulesDir) throws Exception {
        validateModulesDirectory(modulesDir);
        
        File dir = new File(modulesDir);
        Logging.info("🔍 Starting module discovery in: " + modulesDir);
        
        // Load from source code (development mode)
        List<GameModule> modules = discoverModulesFromSource(dir);
        Logging.info("📦 Found " + modules.size() + " module(s) from source code");
        
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
    
    // ==================== SOURCE-BASED MODULE DISCOVERY ====================
    
    /**
     * Discovers game modules from source code (development mode).
     * 
     * @param modulesDir The modules directory
     * @return List of discovered game modules
     */
    private static List<GameModule> discoverModulesFromSource(File modulesDir) {
        List<GameModule> modules = new ArrayList<>();
        
        File[] subdirs = modulesDir.listFiles(File::isDirectory);
        Logging.info("🔍 Found " + (subdirs != null ? subdirs.length : 0) + " subdirectories in modules directory");
        
        if (subdirs != null) {
            for (File subdir : subdirs) {
                Logging.info("🔍 Checking subdirectory: " + subdir.getName());
                try {
                    GameModule module = loadModuleFromSource(subdir);
                    if (module != null) {
                        Logging.info("✅ Successfully loaded module: " + subdir.getName());
                        modules.add(module);
                    } else {
                        Logging.info("❌ Failed to load module from: " + subdir.getName());
                    }
                } catch (Exception e) {
                    Logging.error("Failed to load module from source in " + subdir.getName() + ": " + e.getMessage(), e);
                }
            }
        }
        
        Logging.info("📦 Total modules loaded: " + modules.size());
        return modules;
    }
    
    /**
     * Loads a game module from source code.
     * 
     * @param moduleDir The module directory containing source code
     * @return The loaded game module, or null if loading fails
     */
    private static GameModule loadModuleFromSource(File moduleDir) {
        Logging.info("🔍 Loading module from: " + moduleDir.getName());
        
        try {
            // Check if source code exists and is valid
            if (!hasValidMainSourceFile(moduleDir)) {
                Logging.info("❌ No valid Main.java source file found in " + moduleDir.getName());
                // Clean up compiled files since module is disabled
                cleanupCompiledFiles(moduleDir);
                return null; // Skip if no valid source code
            }
            Logging.info("✅ Found valid Main.java source file in " + moduleDir.getName());
            
            // Try to load from compiled classes if available (for instantiation)
            File classesDir = findClassesDirectory(moduleDir);
            if (classesDir != null) {
                Logging.info("✅ Found compiled classes in " + moduleDir.getName());
                
                // ONLY compile if source code has changed - this is the key optimization
                if (isSourceCodeNewerThanCompiled(moduleDir)) {
                    Logging.info("🔄 Source code changed in " + moduleDir.getName() + " - incremental compilation");
                    // Don't clean up - let Maven handle incremental compilation
                    classesDir = null; // Force recompilation
                } else {
                    // Check if all required classes are present
                    if (!areAllRequiredClassesPresent(classesDir)) {
                        Logging.warning("⚠️ Missing required classes in " + moduleDir.getName() + " - skipping module (no source changes detected)");
                        // Don't compile - just skip this module if classes are missing but source hasn't changed
                        return null;
                    } else {
                        String mainClassName = findMainClassInDirectory(classesDir);
                        if (mainClassName != null) {
                            Logging.info("✅ Found Main.class in " + moduleDir.getName());
                            
                            // Try to create the module instance - this validates it implements GameModule
                            try {
                                GameModule module = createModuleInstance(classesDir, mainClassName);
                                if (module != null) {
                                    Logging.info("✅ Successfully created GameModule instance for " + moduleDir.getName());
                                    return module;
                                } else {
                                    Logging.info("❌ Module " + moduleDir.getName() + " has Main class but doesn't implement GameModule interface");
                                    // Clean up compiled files since module is invalid
                                    cleanupCompiledFiles(moduleDir);
                                    return null;
                                }
                            } catch (ClassNotFoundException e) {
                                Logging.warning("⚠️ Missing required classes in " + moduleDir.getName() + " - skipping module (no source changes detected)");
                                // Don't compile - just skip this module if classes are missing but source hasn't changed
                                return null;
                            } catch (Exception e) {
                                Logging.warning("⚠️ Error loading module " + moduleDir.getName() + ": " + e.getMessage());
                                return null;
                            }
                        } else {
                            Logging.info("❌ No Main.class found in " + moduleDir.getName());
                            return null;
                        }
                    }
                }
            }
            
            // If we get here, either no classes directory or compilation is needed due to source changes
            if (classesDir == null) {
                Logging.info("🔨 Compiling module due to source changes: " + moduleDir.getName());
                // Try to compile the module automatically
                if (compileModule(moduleDir)) {
                    Logging.info("✅ Successfully compiled " + moduleDir.getName());
                    // Try to load again after compilation
                    classesDir = findClassesDirectory(moduleDir);
                    if (classesDir != null) {
                        Logging.info("✅ Found compiled classes in " + moduleDir.getName() + " after compilation");
                        
                        String mainClassName = findMainClassInDirectory(classesDir);
                        if (mainClassName != null) {
                            Logging.info("✅ Found Main.class in " + moduleDir.getName());
                            
                            // Try to create the module instance - this validates it implements GameModule
                            GameModule module = createModuleInstance(classesDir, mainClassName);
                            if (module != null) {
                                Logging.info("✅ Successfully created GameModule instance for " + moduleDir.getName());
                                return module;
                            } else {
                                Logging.info("❌ Module " + moduleDir.getName() + " has Main class but doesn't implement GameModule interface");
                                // Clean up compiled files since module is invalid
                                cleanupCompiledFiles(moduleDir);
                                return null;
                            }
                        } else {
                            Logging.info("❌ No Main.class found in " + moduleDir.getName() + " after compilation");
                            return null;
                        }
                    } else {
                        Logging.info("❌ Compilation failed for " + moduleDir.getName());
                        return null;
                    }
                } else {
                    Logging.info("❌ Failed to compile " + moduleDir.getName() + " - continuing without this module");
                    return null;
                }
            }
            
        } catch (Exception e) {
            Logging.error("Error loading module from source in " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Checks if a module has a valid Main.java source file that implements GameModule
     */
    private static boolean hasValidMainSourceFile(File moduleDir) {
        // Check for Main.java directly in src/main/java/ (standardized structure)
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        if (!mainJavaFile.exists()) {
            return false;
        }
        
        // Validate that the source code implements GameModule interface
        return validateMainSourceCode(mainJavaFile);
    }
    
    /**
     * Validates that Main.java source code implements GameModule interface
     */
    private static boolean validateMainSourceCode(File mainJavaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(mainJavaFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            boolean isCommented = false;
            
            while ((line = reader.readLine()) != null) {
                // Skip commented lines
                if (line.trim().startsWith("//")) {
                    continue;
                }
                
                // Handle block comments
                if (line.contains("/*")) {
                    isCommented = true;
                }
                if (line.contains("*/")) {
                    isCommented = false;
                    continue;
                }
                if (isCommented) {
                    continue;
                }
                
                content.append(line).append("\n");
            }
            
            String sourceCode = content.toString();
            
            // Check for required GameModule implementation
            boolean implementsGameModule = sourceCode.contains("implements GameModule") || 
                                         sourceCode.contains("implements gdk.GameModule");
            
            // Check for required methods
            boolean hasLaunchGame = sourceCode.contains("launchGame") && 
                                  sourceCode.contains("Stage primaryStage");
            boolean hasStopGame = sourceCode.contains("stopGame");
            boolean hasHandleMessage = sourceCode.contains("handleMessage") && 
                                     sourceCode.contains("Map<String, Object>");
            boolean hasGetMetadata = sourceCode.contains("getMetadata");
            
            // Check for class declaration
            boolean hasClassDeclaration = sourceCode.contains("class Main") || 
                                        sourceCode.contains("public class Main");
            
            // All conditions must be met for a valid game module
            boolean isValid = implementsGameModule && hasLaunchGame && hasStopGame && 
                            hasHandleMessage && hasGetMetadata && hasClassDeclaration;
            
            if (!isValid) {
                Logging.info("❌ Main.java validation failed for " + mainJavaFile.getParentFile().getParentFile().getParentFile().getName());
                Logging.info("   - Implements GameModule: " + implementsGameModule);
                Logging.info("   - Has launchGame: " + hasLaunchGame);
                Logging.info("   - Has stopGame: " + hasStopGame);
                Logging.info("   - Has handleMessage: " + hasHandleMessage);
                Logging.info("   - Has getMetadata: " + hasGetMetadata);
                Logging.info("   - Has class declaration: " + hasClassDeclaration);
            }
            
            return isValid;
            
        } catch (IOException e) {
            Logging.error("Error reading Main.java file: " + e.getMessage(), e);
            return false;
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
     * Cleans up compiled files for a module that is no longer valid
     * @param moduleDir The module directory to clean up
     */
    private static void cleanupCompiledFiles(File moduleDir) {
        try {
            File targetDir = new File(moduleDir, "target");
            if (targetDir.exists() && targetDir.isDirectory()) {
                // Remove the entire target directory
                deleteDirectory(targetDir);
                Logging.info("🧹 Cleaned up compiled files for " + moduleDir.getName());
            }
        } catch (Exception e) {
            Logging.error("Error cleaning up compiled files for " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Recursively deletes a directory and all its contents
     * @param dir The directory to delete
     */
    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }
    
    /**
     * Compiles a module using Maven
     * @param moduleDir The module directory to compile
     * @return true if compilation was successful, false otherwise
     */
    private static boolean compileModule(File moduleDir) {
        try {
            Logging.info("🔨 Compiling module: " + moduleDir.getName());
            
            // Use incremental compilation - only compile changed files
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile");
            pb.directory(moduleDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read output for logging
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Wait for compilation to complete
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("✅ Compilation successful for " + moduleDir.getName());
                // Add a small delay to ensure files are written
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            } else {
                Logging.warning("⚠️ Compilation failed for " + moduleDir.getName() + " (exit code: " + exitCode + ")");
                if (output.length() > 0) {
                    // Only log the last few lines to avoid spam
                    String[] lines = output.toString().split("\n");
                    int start = Math.max(0, lines.length - 10);
                    StringBuilder shortOutput = new StringBuilder();
                    for (int i = start; i < lines.length; i++) {
                        shortOutput.append(lines[i]).append("\n");
                    }
                    Logging.warning("Compilation output (last 10 lines): " + shortOutput.toString());
                }
                return false;
            }
            
        } catch (Exception e) {
            Logging.warning("⚠️ Exception during compilation of " + moduleDir.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if source code files are newer than compiled files
     */
    private static boolean isSourceCodeNewerThanCompiled(File moduleDir) {
        try {
            File sourceDir = new File(moduleDir, "src/main/java");
            File classesDir = new File(moduleDir, "target/classes");
            
            if (!sourceDir.exists() || !classesDir.exists()) {
                return false;
            }
            
            // Get the most recent modification time of source files
            long latestSourceTime = getLatestModificationTime(sourceDir);
            
            // Get the most recent modification time of compiled files
            long latestCompiledTime = getLatestModificationTime(classesDir);
            
            // If source is newer than compiled, recompilation is needed
            return latestSourceTime > latestCompiledTime;
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error checking source code timestamps for " + moduleDir.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if all required classes are present in the compiled classes directory
     */
    private static boolean areAllRequiredClassesPresent(File classesDir) {
        try {
            // Check for Main.class
            File mainClass = new File(classesDir, "Main.class");
            if (!mainClass.exists()) {
                Logging.info("❌ Main.class missing in " + classesDir.getParentFile().getName());
                return false;
            }
            
            // Check for Metadata.class
            File metadataClass = new File(classesDir, "Metadata.class");
            if (!metadataClass.exists()) {
                Logging.info("❌ Metadata.class missing in " + classesDir.getParentFile().getName());
                return false;
            }
            
            Logging.info("✅ All required classes present in " + classesDir.getParentFile().getName());
            return true;
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error checking required classes in " + classesDir.getParentFile().getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the latest modification time of files in a directory (recursive)
     */
    private static long getLatestModificationTime(File dir) {
        long latestTime = 0;
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        long dirTime = getLatestModificationTime(file);
                        if (dirTime > latestTime) {
                            latestTime = dirTime;
                        }
                    } else {
                        long fileTime = file.lastModified();
                        if (fileTime > latestTime) {
                            latestTime = fileTime;
                        }
                    }
                }
            }
        }
        
        return latestTime;
    }
    
    /**
     * Creates a module instance from a classes directory
     */
    private static GameModule createModuleInstance(File classesDir, String mainClassName) throws Exception {
        // Create classpath URLs including GDK dependencies
        List<URL> classpathUrls = new ArrayList<>();
        classpathUrls.add(classesDir.toURI().toURL());
        
        // Add GDK dependencies to classpath
        try {
            // Get the project root directory (parent of modules directory)
            File modulesDir = classesDir.getParentFile().getParentFile().getParentFile();
            File projectRoot = modulesDir.getParentFile();
            
            // Add gdk module classes
            File gdkClassesDir = new File(projectRoot, "gdk/target/classes");
            if (gdkClassesDir.exists()) {
                classpathUrls.add(gdkClassesDir.toURI().toURL());
                Logging.info("✅ Added GDK classes to classpath: " + gdkClassesDir.getAbsolutePath());
            } else {
                Logging.warning("⚠️ GDK classes directory not found: " + gdkClassesDir.getAbsolutePath());
            }
            
            // Add launcher module classes
            File launcherClassesDir = new File(projectRoot, "launcher/target/classes");
            if (launcherClassesDir.exists()) {
                classpathUrls.add(launcherClassesDir.toURI().toURL());
                Logging.info("✅ Added launcher classes to classpath: " + launcherClassesDir.getAbsolutePath());
            } else {
                Logging.warning("⚠️ Launcher classes directory not found: " + launcherClassesDir.getAbsolutePath());
            }
        } catch (Exception e) {
            Logging.warning("Could not add GDK dependencies to classpath: " + e.getMessage());
        }
        
        URLClassLoader classLoader = new URLClassLoader(
            classpathUrls.toArray(new URL[0]),
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
        Logging.info("🔍 Looking for Main.class in: " + classesDir.getAbsolutePath());
        
        // List all files in the classes directory for debugging
        File[] files = classesDir.listFiles();
        if (files != null) {
            Logging.info("📁 Files in classes directory:");
            for (File file : files) {
                Logging.info("   - " + file.getName() + (file.isDirectory() ? " (dir)" : " (file)"));
            }
        }
        
        File mainClassFile = new File(classesDir, "Main.class");
        boolean exists = mainClassFile.exists();
        Logging.info("🔍 Main.class exists: " + exists + " at " + mainClassFile.getAbsolutePath());
        
        return exists ? "Main" : null;
    }
} 