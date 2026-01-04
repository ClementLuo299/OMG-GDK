package launcher.features.module_handling.compilation;

import gdk.api.GameModule;
import launcher.features.module_handling.compilation.helpers.CompilationFailureTracker;
import launcher.features.module_handling.compilation.helpers.CompilationStatusReporter;
import launcher.features.module_handling.compilation.helpers.MavenCompiler;
import launcher.features.module_handling.compilation.helpers.ModuleLoader;
import launcher.features.module_handling.compilation.helpers.UIControllerManager;
import launcher.ui_areas.lobby.GDKGameLobbyController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Public API for module compilation and loading operations.
 * 
 * <p>This class provides methods for:
 * <ul>
 *   <li>Compiling modules using Maven</li>
 *   <li>Loading modules from compiled classes</li>
 *   <li>Reporting compilation status</li>
 *   <li>Tracking compilation failures</li>
 *   <li>Managing UI controller for progress updates</li>
 * </ul>
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for compilation and loading operations.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited January 3, 2026
 * @since 1.0
 */
public final class ModuleCompiler {
    
    private ModuleCompiler() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - COMPILATION ====================
    
    /**
     * Compiles a specific module.
     * 
     * <p>This method uses Maven to compile a module by running "mvn clean compile".
     * It finds the Maven command automatically and executes it in the module directory.
     * 
     * @param modulePath The path to the module to compile
     * @return true if compilation was successful, false otherwise
     */
    public static boolean compileModule(String modulePath) {
        return MavenCompiler.compileModule(modulePath);
    }
    
    // ==================== PUBLIC METHODS - MODULE LOADING ====================
    
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
        return ModuleLoader.loadModule(moduleDir);
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
        return ModuleLoader.loadModules(moduleDirectories);
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
        CompilationStatusReporter.report(modulesDirectory);
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
                gdk.internal.Logging.info("Module " + moduleName + " needs compilation");
                if (compileModule(moduleDir.getAbsolutePath())) {
                    compiledModules.add(moduleName);
                }
            } else {
                gdk.internal.Logging.info("Module " + moduleName + " is up to date");
            }
        }
        
        return compiledModules;
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
    
    // ==================== PUBLIC METHODS - UI CONTROLLER MANAGEMENT ====================
    
    /**
     * Sets the UI controller for progress updates.
     * 
     * <p>This method allows the UI controller to be set for displaying
     * compilation and loading progress to the user.
     * 
     * @param controller The UI controller
     */
    public static void setUIController(GDKGameLobbyController controller) {
        UIControllerManager.set(controller);
    }
    
    /**
     * Gets the current UI controller.
     * 
     * @return The current UI controller, or null if not set
     */
    public static GDKGameLobbyController getUIController() {
        return UIControllerManager.get();
    }
    
    // ==================== PUBLIC METHODS - COMPILATION FAILURE TRACKING ====================
    
    /**
     * Stores compilation failures for UI notification.
     * 
     * <p>This method replaces the current list of compilation failures with
     * the provided list. Used to track which modules failed to compile.
     * 
     * @param failures List of module names that failed compilation
     */
    public static void storeCompilationFailures(List<String> failures) {
        CompilationFailureTracker.store(failures);
    }
    
    /**
     * Gets the last compilation failures for UI notification.
     * 
     * <p>This method returns a copy of the stored compilation failures list.
     * 
     * @return List of module names that failed compilation
     */
    public static List<String> getLastCompilationFailures() {
        return CompilationFailureTracker.getLastFailures();
    }
    
    /**
     * Clears the stored compilation failures.
     * 
     * <p>This method clears all stored compilation failure information.
     */
    public static void clearCompilationFailures() {
        CompilationFailureTracker.clear();
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
        CompilationFailureTracker.addFailure(moduleName);
    }
    
    /**
     * Checks for compilation failures in modules.
     * 
     * <p>This method checks both the stored failure list and
     * scans the modules directory for modules that have source files but
     * no compiled classes.
     * 
     * @return List of module names that failed to compile
     */
    public static List<String> checkForCompilationFailures() {
        return CompilationFailureTracker.checkForFailures();
    }
}

