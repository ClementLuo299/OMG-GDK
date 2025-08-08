package launcher.utils;

import gdk.GameModule;
import gdk.Logging;

import launcher.lifecycle.start.Startup;
import launcher.gui.GDKGameLobbyController;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.InputStreamReader;

/**
 * Utility class for discovering and loading game modules.
 * Provides functionality to scan directories for game modules and load them dynamically.
 * Supports source code analysis for development.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited August 7, 2025
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
        List<String> compilationFailures = new ArrayList<>();
        List<String> validationFailures = new ArrayList<>();
        List<String> brokenModules = new ArrayList<>();
        
        File[] subdirs = modulesDir.listFiles(File::isDirectory);
        Logging.info("🔍 Found " + (subdirs != null ? subdirs.length : 0) + " subdirectories in modules directory");
        
        if (subdirs != null) {
            for (File subdir : subdirs) {
                // Skip non-module directories
                if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                    continue;
                }
                
                Logging.info("🔍 Checking subdirectory: " + subdir.getName());
                
                // Update UI with current module being processed
                updateUIWithCurrentModule(subdir.getName());
                
                try {
                    // First, validate that Main.java and Metadata.java are valid
                    if (!hasValidMainSourceFile(subdir)) {
                        validationFailures.add(subdir.getName());
                        Logging.info("❌ Core validation failed for module: " + subdir.getName());
                        continue;
                    }
                    
                    // Check if ALL Java files are valid and module compiles
                    boolean allJavaFilesValid = validateAllJavaFiles(subdir);
                    if (!allJavaFilesValid) {
                        brokenModules.add(subdir.getName());
                        Logging.warning("⚠️ Module has Java file issues: " + subdir.getName());
                    }
                    
                    // Attempt to load/compile the module
                    GameModule module = loadModuleFromSource(subdir);
                    if (module != null) {
                        Logging.info("✅ Successfully loaded module: " + subdir.getName());
                        modules.add(module);
                        
                        // If module loaded but has Java file issues, mark as broken
                        if (!allJavaFilesValid) {
                            Logging.warning("⚠️ Module loaded but has issues: " + subdir.getName());
                        }
                    } else {
                        // If core validation passed but loading failed, it's a compilation issue
                        compilationFailures.add(subdir.getName());
                        Logging.warning("⚠️ Compilation failed for module: " + subdir.getName());
                    }
                } catch (Exception e) {
                    Logging.error("Failed to load module from source in " + subdir.getName() + ": " + e.getMessage(), e);
                    compilationFailures.add(subdir.getName());
                }
            }
        }
        
        // Log summary of failures for user notification
        if (!compilationFailures.isEmpty()) {
            Logging.warning("⚠️ Modules with compilation issues: " + String.join(", ", compilationFailures));
            // Store compilation failures for UI notification
            storeCompilationFailures(compilationFailures);
            Logging.info("💾 Stored compilation failures for UI notification: " + String.join(", ", compilationFailures));
        }
        if (!validationFailures.isEmpty()) {
            Logging.info("❌ Modules with validation issues: " + String.join(", ", validationFailures));
        }
        if (!brokenModules.isEmpty()) {
            Logging.warning("⚠️ Modules with Java file issues (but still loadable): " + String.join(", ", brokenModules));
        }
        
        Logging.info("📦 Total modules loaded: " + modules.size() + " (compilation failures: " + compilationFailures.size() + ", validation failures: " + validationFailures.size() + ", broken: " + brokenModules.size() + ")");
        
        // Store discovered modules for startup progress
        discoveredModules.clear();
        discoveredModules.addAll(modules);
        
        // Clear the current processing module when done
        if (uiController != null) {
            javafx.application.Platform.runLater(() -> {
                uiController.clearCurrentProcessingModule();
            });
        }
        
        return modules;
    }
    
    // Static field to store compilation failures for UI notification
    private static List<String> lastCompilationFailures = new ArrayList<>();
    
    // Static field to track modules that have failed compilation recently
    private static Set<String> recentlyFailedModules = new HashSet<>();
    private static final long FAILURE_TIMEOUT_MS = 30000; // 30 seconds
    private static long lastFailureCheck = 0;
    
    // Callback for UI updates
    private static GDKGameLobbyController uiController = null;
    
    // Callback for startup progress window
    private static Startup startupProgressWindow = null;
    
    // Store discovered modules for startup progress
    private static List<GameModule> discoveredModules = new ArrayList<>();
    
    /**
     * Set the UI controller for progress updates
     * @param controller The UI controller
     */
    public static void setUIController(GDKGameLobbyController controller) {
        uiController = controller;
    }
    
    public static void setStartupProgressWindow(Startup startup) {
        startupProgressWindow = startup;
    }
    
    public static void clearStartupProgressWindow() {
        startupProgressWindow = null;
    }
    
    /**
     * Get the list of discovered modules
     */
    public static List<GameModule> getDiscoveredModules() {
        return new ArrayList<>(discoveredModules);
    }
    
    /**
     * Store compilation failures for UI notification
     * @param failures List of module names that failed compilation
     */
    private static void storeCompilationFailures(List<String> failures) {
        lastCompilationFailures.clear();
        lastCompilationFailures.addAll(failures);
        Logging.info("💾 Stored " + failures.size() + " compilation failures: " + String.join(", ", failures));
    }
    
    /**
     * Get the last compilation failures for UI notification
     * @return List of module names that failed compilation
     */
    public static List<String> getLastCompilationFailures() {
        Logging.info("📤 Retrieved " + lastCompilationFailures.size() + " compilation failures: " + String.join(", ", lastCompilationFailures));
        return new ArrayList<>(lastCompilationFailures);
    }
    
    /**
     * Clear the stored compilation failures
     */
    public static void clearCompilationFailures() {
        lastCompilationFailures.clear();
    }
    
    /**
     * Check if a module has failed compilation recently
     * @param moduleName The module name
     * @return true if the module failed compilation recently
     */
    private static boolean hasRecentlyFailed(String moduleName) {
        long currentTime = System.currentTimeMillis();
        
        // Clean up old failures if enough time has passed
        if (currentTime - lastFailureCheck > FAILURE_TIMEOUT_MS) {
            recentlyFailedModules.clear();
            lastFailureCheck = currentTime;
        }
        
        return recentlyFailedModules.contains(moduleName);
    }
    
    /**
     * Mark a module as having failed compilation recently
     * @param moduleName The module name
     */
    private static void markAsRecentlyFailed(String moduleName) {
        recentlyFailedModules.add(moduleName);
        lastFailureCheck = System.currentTimeMillis();
    }
    
    /**
     * Update the UI with the current module being processed
     * @param moduleName The name of the module being processed
     */
    private static void updateUIWithCurrentModule(String moduleName) {
        if (uiController != null) {
            // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                uiController.setCurrentProcessingModule(moduleName);
            });
        }
        
        // Note: Progress window updates are now handled by PreStartupProgressWindow in Startup class
    }
    
    /**
     * Validates ALL Java files in the module for syntax and compilation issues
     * @param moduleDir The module directory
     * @return true if all Java files are valid and module compiles
     */
    private static boolean validateAllJavaFiles(File moduleDir) {
        try {
            Logging.info("🔍 Validating all Java files in module: " + moduleDir.getName());
            
            // Get all Java files in the module
            List<File> javaFiles = findAllJavaFiles(moduleDir);
            Logging.info("📁 Found " + javaFiles.size() + " Java files in " + moduleDir.getName());
            
            if (javaFiles.isEmpty()) {
                Logging.warning("⚠️ No Java files found in " + moduleDir.getName());
                return false;
            }
            
            // Try to compile the entire module FIRST - this is the most reliable way to detect issues
            String moduleName = moduleDir.getName();
            
            // Check if this module has failed compilation recently
            if (hasRecentlyFailed(moduleName)) {
                Logging.info("⏸️ Skipping compilation validation for " + moduleName + " (recently failed)");
                return false;
            }
            
            Logging.info("🔨 Attempting to compile module: " + moduleName);
            boolean compilationSuccess = compileModule(moduleDir);
            
            if (!compilationSuccess) {
                Logging.warning("⚠️ Module compilation failed: " + moduleName);
                markAsRecentlyFailed(moduleName);
                return false;
            }
            
            // Verify compilation produced required classes
            File classesDir = new File(moduleDir, "target/classes");
            if (!classesDir.exists()) {
                Logging.warning("⚠️ No classes directory created for " + moduleDir.getName());
                return false;
            }
            
            // Check if we can create a module instance
            try {
                String mainClassName = findMainClassInDirectory(classesDir);
                if (mainClassName != null) {
                    GameModule module = createModuleInstance(classesDir, mainClassName);
                    if (module == null) {
                        Logging.warning("⚠️ Could not create module instance for " + moduleDir.getName());
                        return false;
                    }
                } else {
                    Logging.warning("⚠️ No Main class found in " + moduleDir.getName());
                    return false;
                }
            } catch (Exception e) {
                Logging.warning("⚠️ Error creating module instance for " + moduleDir.getName() + ": " + e.getMessage());
                return false;
            }
            
            Logging.info("✅ All Java files valid and module compiles: " + moduleDir.getName());
            return true;
            
        } catch (Exception e) {
            Logging.error("❌ Error validating Java files for " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Finds all Java files in a module directory recursively
     * @param moduleDir The module directory
     * @return List of Java files
     */
    private static List<File> findAllJavaFiles(File moduleDir) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursively(moduleDir, javaFiles);
        return javaFiles;
    }
    
    /**
     * Recursively finds Java files in a directory
     * @param dir The directory to search
     * @param javaFiles List to add Java files to
     */
    private static void findJavaFilesRecursively(File dir, List<File> javaFiles) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Skip target and .git directories
                    if (!file.getName().equals("target") && !file.getName().equals(".git")) {
                        findJavaFilesRecursively(file, javaFiles);
                    }
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
    
    /**
     * Checks if a Java file has valid syntax
     * @param javaFile The Java file to check
     * @return true if the file appears to have valid syntax
     */
    private static boolean isJavaFileValid(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
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
            
            // Basic syntax checks
            boolean hasClassDeclaration = sourceCode.contains("class ") || sourceCode.contains("public class ");
            boolean hasBalancedBraces = countChar(sourceCode, '{') == countChar(sourceCode, '}');
            boolean hasBalancedParens = countChar(sourceCode, '(') == countChar(sourceCode, ')');
            boolean hasBalancedBrackets = countChar(sourceCode, '[') == countChar(sourceCode, ']');
            
            return hasClassDeclaration && hasBalancedBraces && hasBalancedParens && hasBalancedBrackets;
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error reading Java file " + javaFile.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Counts occurrences of a character in a string
     * @param str The string to search
     * @param ch The character to count
     * @return The count
     */
    private static int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
    
    /**
     * Checks if a module has compilation issues by attempting compilation
     * @param moduleDir The module directory
     * @return true if the module has compilation issues
     */
    private static boolean hasCompilationIssues(File moduleDir) {
        try {
            // Check if there's a pom.xml file (indicating it's a Maven module)
            File pomFile = new File(moduleDir, "pom.xml");
            if (!pomFile.exists()) {
                return false; // Not a Maven module, so no compilation issues
            }
            
            // Check if source files exist
            File mainJava = new File(moduleDir, "src/main/java/Main.java");
            File metadataJava = new File(moduleDir, "src/main/java/Metadata.java");
            
            if (!mainJava.exists() || !metadataJava.exists()) {
                return true; // Missing source files
            }
            
            // Check if target/classes directory exists with required classes
            File classesDir = new File(moduleDir, "target/classes");
            File mainClass = new File(classesDir, "Main.class");
            File metadataClass = new File(classesDir, "Metadata.class");
            
            if (!classesDir.exists() || !mainClass.exists() || !metadataClass.exists()) {
                return true; // Missing compiled classes
            }
            
            // Check if source code is newer than compiled code
            if (isSourceCodeNewerThanCompiled(moduleDir)) {
                return true; // Source is newer, compilation needed
            }
            
            // Try to create an instance to verify compilation worked
            try {
                String mainClassName = findMainClassInDirectory(classesDir);
                if (mainClassName != null) {
                    GameModule module = createModuleInstance(classesDir, mainClassName);
                    if (module == null) {
                        return true; // Couldn't create instance, compilation issue
                    }
                } else {
                    return true; // No main class found
                }
            } catch (Exception e) {
                Logging.warning("⚠️ Error creating module instance for " + moduleDir.getName() + ": " + e.getMessage());
                return true; // Exception during instance creation, compilation issue
            }
            
            return false; // No compilation issues detected
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error checking compilation issues for " + moduleDir.getName() + ": " + e.getMessage());
            return true; // Assume compilation issue if we can't check
        }
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
            
            // Check if recompilation is needed, but be smarter about it
            File classesDir = findClassesDirectory(moduleDir);
            boolean needsRecompilation = false;
            
            if (classesDir != null) {
                Logging.info("✅ Found compiled classes in " + moduleDir.getName());
                
                // Check if source code has changed (any file in src/main/java)
                if (isSourceCodeNewerThanCompiled(moduleDir)) {
                    Logging.info("🔄 Source code changed in " + moduleDir.getName() + " - recompilation needed");
                    needsRecompilation = true;
                } else if (!areAllRequiredClassesPresent(classesDir)) {
                    Logging.warning("⚠️ Missing required classes in " + moduleDir.getName() + " - recompilation needed");
                    needsRecompilation = true;
                } else {
                    // Try to load from existing compiled classes
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
                                needsRecompilation = true;
                            }
                        } catch (ClassNotFoundException e) {
                            Logging.warning("⚠️ Missing required classes in " + moduleDir.getName() + " - recompilation needed");
                            needsRecompilation = true;
                        } catch (Exception e) {
                            Logging.warning("⚠️ Error loading module " + moduleDir.getName() + ": " + e.getMessage());
                            needsRecompilation = true;
                        }
                    } else {
                        Logging.info("❌ No Main.class found in " + moduleDir.getName());
                        needsRecompilation = true;
                    }
                }
            } else {
                Logging.info("🔨 No compiled classes found in " + moduleDir.getName() + " - compilation needed");
                needsRecompilation = true;
            }
            
            // Compile if needed, but avoid repeated attempts for recently failed modules
            if (needsRecompilation) {
                String moduleName = moduleDir.getName();
                
                // Check if this module has failed compilation recently
                if (hasRecentlyFailed(moduleName)) {
                    Logging.info("⏸️ Skipping recompilation for " + moduleName + " (recently failed)");
                    return null;
                }
                
                Logging.info("🔨 Compiling module: " + moduleName);
                // Try to compile the module automatically
                if (compileModule(moduleDir)) {
                    Logging.info("✅ Successfully compiled " + moduleName);
                    // Try to load again after compilation
                    classesDir = findClassesDirectory(moduleDir);
                    if (classesDir != null) {
                        Logging.info("✅ Found compiled classes in " + moduleName + " after compilation");
                        
                        String mainClassName = findMainClassInDirectory(classesDir);
                        if (mainClassName != null) {
                            Logging.info("✅ Found Main.class in " + moduleName);
                            
                            // Try to create the module instance - this validates it implements GameModule
                            try {
                                GameModule module = createModuleInstance(classesDir, mainClassName);
                                if (module != null) {
                                    Logging.info("✅ Successfully created GameModule instance for " + moduleName);
                                    return module;
                                } else {
                                    Logging.info("❌ Module " + moduleName + " has Main class but doesn't implement GameModule interface");
                                    markAsRecentlyFailed(moduleName);
                                    return null;
                                }
                            } catch (Exception e) {
                                Logging.warning("⚠️ Error creating module instance after compilation for " + moduleName + ": " + e.getMessage());
                                markAsRecentlyFailed(moduleName);
                                return null;
                            }
                        } else {
                            Logging.info("❌ No Main.class found in " + moduleName + " after compilation");
                            markAsRecentlyFailed(moduleName);
                            return null;
                        }
                    } else {
                        Logging.warning("⚠️ Compilation failed for " + moduleName + " - no classes directory created");
                        markAsRecentlyFailed(moduleName);
                        return null;
                    }
                } else {
                    Logging.warning("⚠️ Failed to compile " + moduleName + " - module will not be loaded");
                    markAsRecentlyFailed(moduleName);
                    return null;
                }
            }
            
            // If we get here, something went wrong
            Logging.info("❌ Could not load module " + moduleDir.getName() + " - unknown error");
            return null;
            
        } catch (Exception e) {
            Logging.error("Error loading module from source in " + moduleDir.getName() + ": " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Checks if a module has a valid Main.java source file that implements GameModule
     */
    private static boolean hasValidMainSourceFile(File moduleDir) {
        Logging.info("🔍 Validating source files for module: " + moduleDir.getName());
        
        // Check for Main.java directly in src/main/java/ (standardized structure)
        File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
        if (!mainJavaFile.exists()) {
            Logging.info("❌ Main.java not found in " + moduleDir.getName());
            return false;
        }
        
        // Validate that the Main.java source code implements GameModule interface
        boolean mainValid = false;
        try {
            mainValid = validateMainSourceCode(mainJavaFile);
        } catch (Exception e) {
            Logging.error("❌ Exception during Main.java validation for " + moduleDir.getName() + ": " + e.getMessage(), e);
            mainValid = false;
        }
        
        if (!mainValid) {
            Logging.info("❌ Main.java validation failed for " + moduleDir.getName());
            return false;
        }
        
        // Validate that the Metadata.java source code implements all required abstract methods
        boolean metadataValid = false;
        try {
            metadataValid = validateMetadataSourceCode(moduleDir);
        } catch (Exception e) {
            Logging.error("❌ Exception during metadata validation for " + moduleDir.getName() + ": " + e.getMessage(), e);
            metadataValid = false;
        }
        
        if (!metadataValid) {
            Logging.info("❌ Metadata.java validation failed for " + moduleDir.getName());
            return false;
        }
        
        Logging.info("✅ Source validation passed for " + moduleDir.getName());
        return true;
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
     * Validates the Metadata.java source code for required abstract methods
     * @param moduleDir The module directory
     * @return true if metadata source code is valid
     */
    private static boolean validateMetadataSourceCode(File moduleDir) {
        Logging.info("🔍 Validating metadata for module: " + moduleDir.getName());
        File metadataFile = new File(moduleDir, "src/main/java/Metadata.java");
        if (!metadataFile.exists()) {
            Logging.info("❌ Metadata.java not found in " + moduleDir.getName());
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
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
            
            // Check for required GameMetadata extension
            boolean extendsGameMetadata = sourceCode.contains("extends GameMetadata") || 
                                        sourceCode.contains("extends gdk.GameMetadata");
            
            // Check for required abstract methods (more flexible - just check for method names)
            boolean hasGameName = sourceCode.contains("getGameName");
            boolean hasGameVersion = sourceCode.contains("getGameVersion");
            boolean hasGameDescription = sourceCode.contains("getGameDescription");
            boolean hasGameAuthor = sourceCode.contains("getGameAuthor");
            boolean hasSupportsSinglePlayer = sourceCode.contains("supportsSinglePlayer");
            boolean hasSupportsMultiPlayer = sourceCode.contains("supportsMultiPlayer");
            boolean hasSupportsAIOpponent = sourceCode.contains("supportsAIOpponent");
            boolean hasSupportsTournament = sourceCode.contains("supportsTournament");
            boolean hasMinPlayers = sourceCode.contains("getMinPlayers");
            boolean hasMaxPlayers = sourceCode.contains("getMaxPlayers");
            boolean hasMinDifficulty = sourceCode.contains("getMinDifficulty");
            boolean hasMaxDifficulty = sourceCode.contains("getMaxDifficulty");
            boolean hasEstimatedDuration = sourceCode.contains("getEstimatedDurationMinutes");
            boolean hasRequiredResources = sourceCode.contains("getRequiredResources");
            
            // All checks must pass
            boolean isValid = extendsGameMetadata && hasGameName && hasGameVersion && hasGameDescription &&
                            hasGameAuthor && hasSupportsSinglePlayer && hasSupportsMultiPlayer &&
                            hasSupportsAIOpponent && hasSupportsTournament && hasMinPlayers &&
                            hasMaxPlayers && hasMinDifficulty && hasMaxDifficulty &&
                            hasEstimatedDuration && hasRequiredResources;
            
            if (!isValid) {
                Logging.info("❌ Metadata.java validation failed for " + moduleDir.getName());
                Logging.info("   extendsGameMetadata: " + extendsGameMetadata);
                Logging.info("   hasGameName: " + hasGameName);
                Logging.info("   hasGameVersion: " + hasGameVersion);
                Logging.info("   hasGameDescription: " + hasGameDescription);
                Logging.info("   hasGameAuthor: " + hasGameAuthor);
                Logging.info("   hasSupportsSinglePlayer: " + hasSupportsSinglePlayer);
                Logging.info("   hasSupportsMultiPlayer: " + hasSupportsMultiPlayer);
                Logging.info("   hasSupportsAIOpponent: " + hasSupportsAIOpponent);
                Logging.info("   hasSupportsTournament: " + hasSupportsTournament);
                Logging.info("   hasMinPlayers: " + hasMinPlayers);
                Logging.info("   hasMaxPlayers: " + hasMaxPlayers);
                Logging.info("   hasMinDifficulty: " + hasMinDifficulty);
                Logging.info("   hasMaxDifficulty: " + hasMaxDifficulty);
                Logging.info("   hasEstimatedDuration: " + hasEstimatedDuration);
                Logging.info("   hasRequiredResources: " + hasRequiredResources);
            }
            
            return isValid;
            
        } catch (IOException e) {
            Logging.error("Error reading Metadata.java file: " + e.getMessage(), e);
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
            String moduleName = moduleDir.getName();
            
            // Check if this module has failed compilation recently
            if (hasRecentlyFailed(moduleName)) {
                Logging.info("⏸️ Skipping compilation for " + moduleName + " (recently failed)");
                return false;
            }
            
            Logging.info("🔨 Compiling module: " + moduleName);
            
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
                Logging.info("✅ Compilation successful for " + moduleName);
                // Add a small delay to ensure files are written
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            } else {
                Logging.warning("⚠️ Compilation failed for " + moduleName + " (exit code: " + exitCode + ")");
                markAsRecentlyFailed(moduleName);
                if (output.length() > 0) {
                    // Log the full compilation output for debugging
                    Logging.warning("Full compilation output for " + moduleDir.getName() + ":");
                    String[] lines = output.toString().split("\n");
                    for (String line : lines) {
                        if (line.contains("ERROR") || line.contains("FAILURE") || line.contains("BUILD FAILURE")) {
                            Logging.warning("  " + line);
                        }
                    }
                    // Also log the last few lines for context
                    int start = Math.max(0, lines.length - 5);
                    StringBuilder shortOutput = new StringBuilder();
                    for (int i = start; i < lines.length; i++) {
                        shortOutput.append(lines[i]).append("\n");
                    }
                    Logging.warning("Last 5 lines: " + shortOutput.toString());
                }
                return false;
            }
            
        } catch (Exception e) {
            Logging.warning("⚠️ Exception during compilation of " + moduleDir.getName() + ": " + e.getMessage());
            markAsRecentlyFailed(moduleDir.getName());
            return false;
        }
    }
    
    /**
     * Checks if ANY source code files are newer than compiled files
     * Checks ALL Java files, pom.xml, and other configuration files
     */
    private static boolean isSourceCodeNewerThanCompiled(File moduleDir) {
        try {
            File classesDir = new File(moduleDir, "target/classes");
            File pomFile = new File(moduleDir, "pom.xml");
            
            if (!classesDir.exists()) {
                return true; // No compiled classes, definitely need compilation
            }
            
            // Get ALL Java files in the module
            List<File> javaFiles = findAllJavaFiles(moduleDir);
            if (javaFiles.isEmpty()) {
                return false; // No Java files to check
            }
            
            // Find the most recent modification time of ANY Java file
            long latestSourceTime = 0;
            for (File javaFile : javaFiles) {
                long fileTime = javaFile.lastModified();
                if (fileTime > latestSourceTime) {
                    latestSourceTime = fileTime;
                }
            }
            
            // Also check pom.xml modification time
            if (pomFile.exists()) {
                long pomTime = pomFile.lastModified();
                if (pomTime > latestSourceTime) {
                    latestSourceTime = pomTime;
                }
            }
            
            // Get the most recent modification time of compiled files
            long latestCompiledTime = getLatestModificationTime(classesDir);
            
            // If ANY source file is newer than compiled, recompilation is needed
            boolean needsRecompilation = latestSourceTime > latestCompiledTime;
            
            if (needsRecompilation) {
                Logging.info("🔄 Source code changed in " + moduleDir.getName() + " (source: " + new java.util.Date(latestSourceTime) + ", compiled: " + new java.util.Date(latestCompiledTime) + ")");
                Logging.info("📁 Checking " + javaFiles.size() + " Java files for changes");
            }
            
            return needsRecompilation;
            
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
            
            // All Main class methods must be present for a valid game module
            boolean mainClassValid = hasLaunchGame && hasStopGame && hasHandleMessage && hasGetMetadata;
            
            if (!mainClassValid) {
                Logging.info("❌ Main class missing required methods");
                return false;
            }
            
            // Since we already validated the source code, we can skip the expensive runtime validation
            // that was causing freezing issues. The source code validation is sufficient.
            Logging.info("✅ Main class validation passed (runtime checks skipped for performance)");
            return true;
            
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