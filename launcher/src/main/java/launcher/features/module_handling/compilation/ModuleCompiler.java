package launcher.features.module_handling.compilation;

import gdk.internal.Logging;
import gdk.api.GameModule;
import launcher.features.module_handling.discovery.ModuleFolderFilter;
import launcher.ui_areas.lobby.GDKGameLobbyController;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

/**
 * Handles module compilation and ui_loading.
 * 
 * <p>This class has a single responsibility: compiling modules and creating
 * GameModule instances from compiled classes. It also tracks compilation failures
 * and manages UI controller references for progress updates.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Compiling modules using Maven</li>
 *   <li>Loading modules from compiled classes</li>
 *   <li>Creating class loaders with proper dependencies</li>
 *   <li>Validating loaded classes</li>
 *   <li>Tracking compilation failures</li>
 *   <li>Managing UI controller for progress updates</li>
 * </ul>
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public class ModuleCompiler {
    
    // ==================== STATIC STATE ====================
    
    /** UI controller for progress updates. */
    private static GDKGameLobbyController uiController = null;
    
    /** Tracks compilation failures for UI notification. */
    private static List<String> lastCompilationFailures = new ArrayList<>();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ModuleCompiler() {
        throw new AssertionError("ModuleCompiler should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - COMPILATION ====================
    
    /**
     * Compiles a specific module.
     * 
     * <p>This method uses Maven to compile a module by running "mvn clean compile".
     * It finds the Maven command automatically and executes it in the module directory.
     * 
     * @param modulePath The file_paths to the module to compile
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
    
    // ==================== PUBLIC METHODS - MODULE LOADING ====================
    
    /**
     * Loads a module from its compiled classes.
     * 
     * <p>This method performs the complete module ui_loading process:
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
     * @return The loaded GameModule instance, or null if ui_loading failed
     */
    public static GameModule loadModule(File moduleDir) {
        String moduleName = moduleDir.getName();
        Logging.info("Loading module from: " + moduleName);
        Logging.info("   Current helpers: " + Thread.currentThread().getName());
        Logging.info("   Is JavaFX helpers: " + javafx.application.Platform.isFxApplicationThread());
        
        // Add timeout protection for individual module ui_loading
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 second timeout for individual module ui_loading (increased for JavaFX initialization)
        
        try {
            // Validate source files first
            if (!launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(moduleDir)) {
                Logging.info("Module " + moduleName + " has invalid structure");
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module ui_loading timeout for " + moduleName);
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
                Logging.warning("Module ui_loading timeout for " + moduleName);
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
                Logging.warning("Module ui_loading timeout for " + moduleName);
                return null;
            }
            
            // Create class loader with necessary dependencies
            Logging.info("🔧 Creating classloader for module: " + moduleName);
            URLClassLoader classLoader = createModuleClassLoader(moduleDir);
            Logging.info("✅ Classloader created successfully for module: " + moduleName);
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module ui_loading timeout for " + moduleName);
                return null;
            }
            
            // Load and validate the Main class
            Logging.info("📥 Loading Main class for module: " + moduleName);
            Logging.info("   Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
            
            // Ensure JavaFX Platform is initialized before ui_loading JavaFX-dependent classes
            Logging.info("   JavaFX Platform check - on FX helpers: " + Platform.isFxApplicationThread());
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
                Logging.error("❌ Error ui_loading Main class for module " + moduleName + ": " + e.getMessage(), e);
                Logging.error("   Exception type: " + e.getClass().getName());
                e.printStackTrace();
                return null;
            }
            
            if (!validateMainClass(mainClass)) {
                Logging.info("Main class validation failed for " + moduleName);
                return null;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module ui_loading timeout for " + moduleName);
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
            Logging.error("❌ Error ui_loading module " + moduleName + ": " + e.getMessage(), e);
            Logging.error("   Module directory: " + moduleDir.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * Loads multiple modules from their compiled classes.
     * 
     * <p>This method loads multiple modules sequentially with timeout protection.
     * It continues ui_loading other modules even if one fails, ensuring maximum
     * module availability.
     * 
     * @param moduleDirectories List of module directories to load
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> loadModules(List<File> moduleDirectories) {
        List<GameModule> loadedModules = new ArrayList<>();
        
        // Add timeout protection for module ui_loading
        long startTime = System.currentTimeMillis();
        long timeout = 15000; // 15 second timeout for all module ui_loading
        
        for (File moduleDir : moduleDirectories) {
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("Module ui_loading timeout reached, stopping module ui_loading");
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
                Logging.error("❌ Exception while ui_loading module " + moduleDir.getName() + ": " + e.getMessage(), e);
                // Continue with other modules instead of failing completely
            }
        }
        
        Logging.info("Module ui_loading completed. Successfully loaded " + loadedModules.size() + " modules");
        return loadedModules;
    }
    
    // ==================== PRIVATE METHODS - CLASS LOADER ====================
    
    /**
     * Creates a class loader for a module with necessary dependencies.
     * 
     * <p>This method creates a URLClassLoader that includes:
     * <ul>
     *   <li>The module's compiled classes (target/classes)</li>
     *   <li>GDK classes (gdk/target/classes)</li>
     *   <li>Launcher classes (launcher/target/classes)</li>
     * </ul>
     * 
     * <p>It tries multiple possible locations for dependencies to handle
     * different project structures and launch scenarios.
     * 
     * @param moduleDir The module directory
     * @return URLClassLoader configured for the module
     * @throws Exception if class loader creation fails (e.g., no valid classpath URLs found)
     */
    private static URLClassLoader createModuleClassLoader(File moduleDir) throws Exception {
        List<URL> classpathUrls = new ArrayList<>();
        
        // Add module's target/classes directory
        File targetClassesDir = new File(moduleDir, "target/classes");
        if (targetClassesDir.exists()) {
            classpathUrls.add(targetClassesDir.toURI().toURL());
            Logging.info("Added module classes to classpath: " + targetClassesDir.getAbsolutePath());
        } else {
            Logging.warning("Module target/classes directory does not exist: " + targetClassesDir.getAbsolutePath());
        }
        
        // Resolve GDK and launcher paths relative to the module directory
        // Modules are in modules/ subdirectory, so we need to go up to project root
        File moduleParent = moduleDir.getParentFile(); // modules/
        File projectRoot = moduleParent != null ? moduleParent.getParentFile() : null; // project root
        
        if (projectRoot != null) {
            // Add GDK classes - try multiple possible locations
            File[] gdkCandidates = {
                new File(projectRoot, "gdk/target/classes"),
                new File(moduleDir, "../../gdk/target/classes"),
                new File("../gdk/target/classes"),
                new File("gdk/target/classes")
            };
            
            boolean gdkAdded = false;
            for (File gdkCandidate : gdkCandidates) {
                File gdkClassesDir = gdkCandidate.getAbsoluteFile();
                if (gdkClassesDir.exists() && gdkClassesDir.isDirectory()) {
                    classpathUrls.add(gdkClassesDir.toURI().toURL());
                    Logging.info("Added GDK classes to classpath: " + gdkClassesDir.getAbsolutePath());
                    gdkAdded = true;
                    break;
                }
            }
            
            if (!gdkAdded) {
                StringBuilder triedPaths = new StringBuilder();
                for (File candidate : gdkCandidates) {
                    if (triedPaths.length() > 0) triedPaths.append(", ");
                    triedPaths.append(candidate.getAbsolutePath());
                }
                Logging.warning("GDK classes directory not found. Tried: " + triedPaths.toString());
            }
            
            // Add launcher classes - try multiple possible locations
            File[] launcherCandidates = {
                new File(projectRoot, "launcher/target/classes"),
                new File(moduleDir, "../../launcher/target/classes"),
                new File("../launcher/target/classes"),
                new File("launcher/target/classes")
            };
            
            boolean launcherAdded = false;
            for (File launcherCandidate : launcherCandidates) {
                File launcherClassesDir = launcherCandidate.getAbsoluteFile();
                if (launcherClassesDir.exists() && launcherClassesDir.isDirectory()) {
                    classpathUrls.add(launcherClassesDir.toURI().toURL());
                    Logging.info("Added launcher classes to classpath: " + launcherClassesDir.getAbsolutePath());
                    launcherAdded = true;
                    break;
                }
            }
            
            if (!launcherAdded) {
                StringBuilder triedPaths = new StringBuilder();
                for (File candidate : launcherCandidates) {
                    if (triedPaths.length() > 0) triedPaths.append(", ");
                    triedPaths.append(candidate.getAbsolutePath());
                }
                Logging.warning("Launcher classes directory not found. Tried: " + triedPaths.toString());
            }
        } else {
            Logging.warning("Could not determine project root from module directory: " + moduleDir.getAbsolutePath());
            // Fallback to relative paths
            File gdkClassesDir = new File("../gdk/target/classes").getAbsoluteFile();
            if (gdkClassesDir.exists()) {
                classpathUrls.add(gdkClassesDir.toURI().toURL());
                Logging.info("Added GDK classes to classpath (fallback): " + gdkClassesDir.getAbsolutePath());
            }
            
            File launcherClassesDir = new File("../launcher/target/classes").getAbsoluteFile();
            if (launcherClassesDir.exists()) {
                classpathUrls.add(launcherClassesDir.toURI().toURL());
                Logging.info("Added launcher classes to classpath (fallback): " + launcherClassesDir.getAbsolutePath());
            }
        }
        
        if (classpathUrls.isEmpty()) {
            throw new Exception("No valid classpath URLs found for module: " + moduleDir.getAbsolutePath());
        }
        
        Logging.info("Created classloader with " + classpathUrls.size() + " classpath entries for module: " + moduleDir.getName());
        return new URLClassLoader(classpathUrls.toArray(new URL[0]), ModuleCompiler.class.getClassLoader());
    }
    
    // ==================== PRIVATE METHODS - VALIDATION ====================
    
    /**
     * Validates that a Main class implements the GameModule interface.
     * 
     * @param mainClass The Main class to validate
     * @return true if the class implements GameModule, false otherwise
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
    
    // ==================== PUBLIC METHODS - COMPILATION STATUS ====================
    
    /**
     * Reports compilation status for all modules in the directory.
     * 
     * <p>This method scans all modules and reports which ones need compilation.
     * Useful for debugging and user feedback.
     * 
     * @param modulesDirectory The modules directory to check
     */
    public static void reportModuleCompilationStatus(File modulesDirectory) {
        try {
            Logging.info("=== MODULE COMPILATION STATUS ===");
            
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.info("No subdirectories found");
                return;
            }
            
            for (File subdir : subdirs) {
                // Skip infrastructure and hidden directories
                if (ModuleFolderFilter.shouldSkip(subdir)) {
                    continue;
                }
                
                String moduleName = subdir.getName();
                
                Logging.info("Module: " + moduleName);
                boolean needsCompilation = launcher.features.module_handling.validation.ModuleValidator.moduleNeedsCompilation(subdir);
                Logging.info("Compilation needed: " + needsCompilation);
                
                if (needsCompilation) {
                    Logging.info("Run 'mvn compile' in modules/" + moduleName + " to compile");
                }
            }
            
            Logging.info("=== END COMPILATION STATUS ===");
            
        } catch (Exception e) {
            Logging.error("Error reporting compilation status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a module needs to be compiled.
     * 
     * <p>This method checks if:
     * <ul>
     *   <li>Compiled classes directory doesn't exist</li>
     *   <li>Main.class file doesn't exist</li>
     *   <li>Source files are newer than compiled classes</li>
     * </ul>
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
     * Compiles all modules that need compilation.
     * 
     * <p>This method checks each module and compiles only those that need it.
     * It returns a list of successfully compiled module names.
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
    
    // ==================== PRIVATE METHODS - MAVEN ====================
    
    /**
     * Finds the Maven command to use.
     * 
     * <p>This method tries common Maven command names (mvn, mvn.cmd, mvn.bat)
     * and tests if they work by running "mvn --version". Returns the first
     * working command, or "mvn" as a default.
     * 
     * @return The Maven command file_paths
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
     * Checks if modules need to be built (legacy method for compatibility).
     * 
     * <p>This method currently always returns false to avoid Maven execution issues.
     * It can be enhanced later with proper build detection logic.
     * 
     * @return true if modules need to be built, false otherwise (currently always false)
     */
    public static boolean needToBuildModules() {
        // For now, always return false to avoid Maven execution issues
        // This can be enhanced later with proper build detection logic
        return false;
    }
    
    // ==================== UI CONTROLLER MANAGEMENT ====================
    
    // ==================== PUBLIC METHODS - UI CONTROLLER MANAGEMENT ====================
    
    /**
     * Sets the UI controller for progress updates.
     * 
     * <p>This method allows the UI controller to be set for displaying
     * compilation and ui_loading progress to the user.
     * 
     * @param controller The UI controller
     */
    public static void setUIController(GDKGameLobbyController controller) {
        uiController = controller;
    }
    
    /**
     * Gets the current UI controller.
     * 
     * @return The current UI controller, or null if not set
     */
    public static GDKGameLobbyController getUIController() {
        return uiController;
    }
    
    // ==================== COMPILATION FAILURE TRACKING ====================
    
    /**
     * Stores compilation failures for UI notification.
     * 
     * <p>This method replaces the current list of compilation failures with
     * the provided list. Used to track which modules failed to compile.
     * 
     * @param failures List of module names that failed compilation
     */
    public static void storeCompilationFailures(List<String> failures) {
        lastCompilationFailures.clear();
        lastCompilationFailures.addAll(failures);
        Logging.info("💾 Stored " + failures.size() + " compilation failures: " + String.join(", ", failures));
    }
    
    /**
     * Gets the last compilation failures for UI notification.
     * 
     * <p>This method returns a copy of the stored compilation failures list.
     * 
     * @return List of module names that failed compilation
     */
    public static List<String> getLastCompilationFailures() {
        Logging.info("📤 Retrieved " + lastCompilationFailures.size() + " compilation failures: " + String.join(", ", lastCompilationFailures));
        return new ArrayList<>(lastCompilationFailures);
    }
    
    /**
     * Clears the stored compilation failures.
     * 
     * <p>This method clears all stored compilation failure information.
     */
    public static void clearCompilationFailures() {
        lastCompilationFailures.clear();
    }
    
    /**
     * Adds a compilation failure for a specific module.
     * 
     * <p>This method adds a module name to the compilation failures list
     * if it's not already present.
     * 
     * @param moduleName The name of the module that failed compilation
     */
    public static void addCompilationFailure(String moduleName) {
        if (!lastCompilationFailures.contains(moduleName)) {
            lastCompilationFailures.add(moduleName);
            Logging.info("💾 Added compilation failure: " + moduleName);
        }
    }
    
    /**
     * Checks for compilation failures in modules.
     * 
     * <p>This method checks both the ModuleCompiler's failure list and
     * scans the modules directory for modules that have source files but
     * no compiled classes.
     * 
     * @return List of module names that failed to compile
     */
    public static List<String> checkForCompilationFailures() {
        List<String> failures = new ArrayList<>();
        try {
            // Get compilation failures from ModuleCompiler
            List<String> compilerFailures = getLastCompilationFailures();
            failures.addAll(compilerFailures);
            
            // Check for additional compilation issues
            String modulesDirectoryPath = launcher.features.file_paths.PathUtil.getModulesDirectoryPath();
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue;
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Check if compiled classes exist
                            File targetClassesDir = new File(subdir, "target/classes");
                            if (!targetClassesDir.exists() || targetClassesDir.listFiles() == null || targetClassesDir.listFiles().length == 0) {
                                if (!failures.contains(subdir.getName())) {
                                    failures.add(subdir.getName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking compilation failures: " + e.getMessage(), e);
        }
        return failures;
    }
}

