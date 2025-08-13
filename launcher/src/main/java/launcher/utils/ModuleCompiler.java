package launcher.utils;

import gdk.Logging;
import gdk.GameModule;
import launcher.gui.GDKGameLobbyController;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles module compilation and loading.
 * This class is responsible for compiling modules and creating GameModule instances
 * from the compiled classes. Also tracks compilation failures and UI controller.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public class ModuleCompiler {
    
    // ==================== STATIC STATE ====================
    
    /** UI controller for progress updates */
    private static GDKGameLobbyController uiController = null;
    
    /** Track compilation failures for UI notification */
    private static List<String> lastCompilationFailures = new ArrayList<>();
    
    /**
     * Compile a specific module.
     * 
     * @param modulePath The path to the module to compile
     * @return true if compilation was successful, false otherwise
     */
    public static boolean compileModule(String modulePath) {
        Logging.info("Building module: " + modulePath);
        
        try {
            // Find Maven command
            String mavenCommand = findMavenCommand();
            
            // Build the module
            ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand, "clean", "compile");
            processBuilder.directory(new File(modulePath));
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("Module built successfully: " + modulePath);
                return true;
            } else {
                Logging.info("Module build completed with warnings: " + modulePath);
                return false;
            }
        } catch (Exception buildError) {
            Logging.error("Failed to build module " + modulePath + ": " + buildError.getMessage(), buildError);
            return false;
        }
    }
    
    /**
     * Load a module from its compiled classes.
     * 
     * @param moduleDir The module directory
     * @return The loaded GameModule instance, or null if loading failed
     */
    public static GameModule loadModule(File moduleDir) {
        String moduleName = moduleDir.getName();
        Logging.info("Loading module from: " + moduleName);
        
        // Add timeout protection for individual module loading
        long startTime = System.currentTimeMillis();
        long timeout = 10000; // 10 second timeout for individual module loading
        
        try {
            // Validate source files first
            if (!ModuleDiscovery.isValidModuleStructure(moduleDir)) {
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
            URLClassLoader classLoader = createModuleClassLoader(moduleDir);
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Load and validate the Main class
            Class<?> mainClass = classLoader.loadClass("Main");
            if (!validateMainClass(mainClass)) {
                Logging.info("Main class validation failed for " + moduleName);
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module loading timeout for " + moduleName);
                return null;
            }
            
            // Create GameModule instance by instantiating the Main class
            if (GameModule.class.isAssignableFrom(mainClass)) {
                GameModule module = (GameModule) mainClass.getDeclaredConstructor().newInstance();
                Logging.info("Successfully created GameModule instance for " + moduleName);
                return module;
            } else {
                Logging.info("Main class does not implement GameModule interface");
                return null;
            }
            
        } catch (Exception e) {
            Logging.error("Error loading module " + moduleName + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Load multiple modules from their compiled classes.
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
                    Logging.info("Successfully loaded module: " + moduleDir.getName());
                } else {
                    Logging.info("Failed to load module: " + moduleDir.getName());
                }
            } catch (Exception e) {
                Logging.error("Error loading module " + moduleDir.getName() + ": " + e.getMessage(), e);
                // Continue with other modules instead of failing completely
            }
        }
        
        Logging.info("Module loading completed. Successfully loaded " + loadedModules.size() + " modules");
        return loadedModules;
    }
    
    /**
     * Create a class loader for a module with necessary dependencies.
     * 
     * @param moduleDir The module directory
     * @return URLClassLoader configured for the module
     * @throws Exception if class loader creation fails
     */
    private static URLClassLoader createModuleClassLoader(File moduleDir) throws Exception {
        List<URL> classpathUrls = new ArrayList<>();
        
        // Add module's target/classes directory
        File targetClassesDir = new File(moduleDir, "target/classes");
        if (targetClassesDir.exists()) {
            classpathUrls.add(targetClassesDir.toURI().toURL());
        }
        
        // Add GDK classes
        File gdkClassesDir = new File("../gdk/target/classes");
        if (gdkClassesDir.exists()) {
            classpathUrls.add(gdkClassesDir.toURI().toURL());
            Logging.info("Added GDK classes to classpath: " + gdkClassesDir.getAbsolutePath());
        }
        
        // Add launcher classes
        File launcherClassesDir = new File("../launcher/target/classes");
        if (launcherClassesDir.exists()) {
            classpathUrls.add(launcherClassesDir.toURI().toURL());
            Logging.info("Added launcher classes to classpath: " + launcherClassesDir.getAbsolutePath());
        }
        
        return new URLClassLoader(classpathUrls.toArray(new URL[0]), ModuleCompiler.class.getClassLoader());
    }
    
    /**
     * Validate that a Main class has the required main method.
     * 
     * @param mainClass The Main class to validate
     * @return true if the class has a valid main method, false otherwise
     */
    private static boolean validateMainClass(Class<?> mainClass) {
        try {
            boolean implementsGameModule = GameModule.class.isAssignableFrom(mainClass);
            if (!implementsGameModule) {
                Logging.info("Main class does not implement GameModule interface");
            }
            return implementsGameModule;
        } catch (Exception e) {
            Logging.error("Error validating main class: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if a module needs to be compiled.
     * 
     * @param moduleDir The module directory to check
     * @return true if the module needs compilation, false otherwise
     */
    public static boolean needsCompilation(File moduleDir) {
        File targetClassesDir = new File(moduleDir, "target/classes");
        File mainClassFile = new File(targetClassesDir, "Main.class");
        
        // Check if compiled classes exist
        if (!targetClassesDir.exists() || !mainClassFile.exists()) {
            return true;
        }
        
        // Check if source files are newer than compiled classes
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
        
        if (mainJavaFile.exists() && mainJavaFile.lastModified() > mainClassFile.lastModified()) {
            return true;
        }
        
        if (metadataJavaFile.exists() && metadataJavaFile.lastModified() > mainClassFile.lastModified()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Compile all modules that need compilation.
     * 
     * @param moduleDirectories List of module directories to check and compile
     * @return List of successfully compiled module names
     */
    public static List<String> compileModulesIfNeeded(List<File> moduleDirectories) {
        List<String> compiledModules = new ArrayList<>();
        
        for (File moduleDir : moduleDirectories) {
            String moduleName = moduleDir.getName();
            
            if (needsCompilation(moduleDir)) {
                Logging.info("Module " + moduleName + " needs compilation");
                if (compileModule(moduleDir.getAbsolutePath())) {
                    compiledModules.add(moduleName);
                }
            } else {
                Logging.info("Module " + moduleName + " is up to date");
            }
        }
        
        return compiledModules;
    }
    
    /**
     * Find the Maven command to use.
     * 
     * @return The Maven command path
     */
    private static String findMavenCommand() {
        // Try to find Maven in the system PATH
        String[] possibleCommands = {"mvn", "mvn.cmd", "mvn.bat"};
        
        for (String command : possibleCommands) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command, "--version");
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    return command;
                }
            } catch (Exception e) {
                // Continue to next command
            }
        }
        
        // Default to mvn if not found
        return "mvn";
    }
    
    /**
     * Check if modules need to be built (legacy method for compatibility).
     * 
     * @return true if modules need to be built, false otherwise
     */
    public static boolean needToBuildModules() {
        // For now, always return false to avoid Maven execution issues
        // This can be enhanced later with proper build detection logic
        return false;
    }
    
    // ==================== UI CONTROLLER MANAGEMENT ====================
    
    /**
     * Set the UI controller for progress updates.
     * 
     * @param controller The UI controller
     */
    public static void setUIController(GDKGameLobbyController controller) {
        uiController = controller;
    }
    
    /**
     * Get the current UI controller.
     * 
     * @return The current UI controller, or null if not set
     */
    public static GDKGameLobbyController getUIController() {
        return uiController;
    }
    
    // ==================== COMPILATION FAILURE TRACKING ====================
    
    /**
     * Store compilation failures for UI notification.
     * 
     * @param failures List of module names that failed compilation
     */
    public static void storeCompilationFailures(List<String> failures) {
        lastCompilationFailures.clear();
        lastCompilationFailures.addAll(failures);
        Logging.info("💾 Stored " + failures.size() + " compilation failures: " + String.join(", ", failures));
    }
    
    /**
     * Get the last compilation failures for UI notification.
     * 
     * @return List of module names that failed compilation
     */
    public static List<String> getLastCompilationFailures() {
        Logging.info("📤 Retrieved " + lastCompilationFailures.size() + " compilation failures: " + String.join(", ", lastCompilationFailures));
        return new ArrayList<>(lastCompilationFailures);
    }
    
    /**
     * Clear the stored compilation failures.
     */
    public static void clearCompilationFailures() {
        lastCompilationFailures.clear();
    }
    
    /**
     * Add a compilation failure for a specific module.
     * 
     * @param moduleName The name of the module that failed compilation
     */
    public static void addCompilationFailure(String moduleName) {
        if (!lastCompilationFailures.contains(moduleName)) {
            lastCompilationFailures.add(moduleName);
            Logging.info("💾 Added compilation failure: " + moduleName);
        }
    }
} 