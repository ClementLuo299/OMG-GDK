package launcher.features.module_handling.discovery;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;
import launcher.features.file_paths.PathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles module discovery and validation.
 * 
 * <p>This class has a single responsibility: finding and validating game modules
 * in the modules directory. It checks module structure and required components
 * but does not handle compilation or ui_loading (delegated to ModuleCompiler).
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Discovering modules in the modules directory</li>
 *   <li>Validating module structure and required files</li>
 *   <li>Counting valid modules</li>
 *   <li>Getting lists of valid module directories</li>
 *   <li>Checking compilation status</li>
 *   <li>Diagnosing module detection issues</li>
 *   <li>Finding modules by name</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public class ModuleDiscovery {
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ModuleDiscovery() {
        throw new AssertionError("ModuleDiscovery should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - MODULE DISCOVERY ====================
    
    /**
     * Discovers all valid modules in the modules directory.
     * 
     * <p>This method identifies module candidates and logs validation results.
     * Actual ui_loading is delegated to ModuleCompiler (kept intentionally
     * side-effect free here).
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return List of discovered game modules (currently empty, ui_loading is done elsewhere)
     */
    public static List<GameModule> discoverModules(String modulesDirectoryPath) {
        Logging.info("Starting module discovery in: " + modulesDirectoryPath);
        
        List<GameModule> discoveredModules = new ArrayList<>();
        File modulesDirectory = new File(modulesDirectoryPath);
        
        if (!modulesDirectory.exists() || !modulesDirectory.isDirectory()) {
            Logging.info("Modules directory does not exist: " + modulesDirectoryPath);
            return discoveredModules; // return empty by design
        }
        
        File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
        if (subdirs == null) {
            Logging.info("No subdirectories found in modules directory");
            return discoveredModules;
        }
        
        Logging.info("Found " + subdirs.length + " subdirectories in modules directory");
        
        for (File subdir : subdirs) {
            String moduleName = subdir.getName();
            Logging.info("Checking subdirectory: " + moduleName);
            
            // Only log validity here; ui_loading is performed by ModuleCompiler
            if (isValidModuleStructure(subdir)) {
                Logging.info("Valid module structure found: " + moduleName);
            } else {
                Logging.info("Invalid module structure: " + moduleName);
            }
        }
        
        Logging.info("Total modules discovered: " + discoveredModules.size());
        return discoveredModules;
    }
    
    /**
     * Counts the number of valid modules in the modules directory.
     * 
     * <p>This method scans the modules directory and counts only those directories
     * that pass structural validation checks. It skips infrastructure directories
     * like "target" and ".git".
     * 
     * @param modulesDirectory The modules directory to scan
     * @return The number of valid modules found
     */
    public static int countValidModules(File modulesDirectory) {
        int validCount = 0;
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0; // nothing to scan
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip infra/hidden directories that are not game modules
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Only structural validity is checked here (not compilation)
                if (isValidModuleStructure(subdir)) {
                    validCount++;
                    Logging.info("Valid module found: " + moduleName);
                } else {
                    Logging.info("Invalid module found: " + moduleName);
                }
            }
        } catch (Exception e) {
            Logging.error("Error counting valid modules: " + e.getMessage(), e);
        }
        
        return validCount;
    }
    
    /**
     * Gets a list of valid module directories for processing.
     * 
     * <p>This method scans the modules directory and returns a list of directories
     * that pass structural validation. It includes timeout protection for file
     * operations and filters out infrastructure directories.
     * 
     * @param modulesDirectory The modules directory to scan
     * @return List of valid module directories
     */
    public static List<File> getValidModuleDirectories(File modulesDirectory) {
        List<File> validModules = new ArrayList<>();
        
        try {
            Logging.info("🔍 Starting module discovery in: " + modulesDirectory.getAbsolutePath());
            
            // Add timeout protection for file operations
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // Reduced to 5 second timeout for faster failure detection
            
            // First, check if we can even list the directory
            Logging.info("🔍 Attempting to list directory contents...");
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("⚠️ Directory listing timeout reached");
                return validModules;
            }
            
            if (subdirs == null) {
                Logging.info("📁 No subdirectories found in modules directory");
                return validModules; // empty if nothing to scan
            }
            
            Logging.info("📁 Found " + subdirs.length + " subdirectories to check");
            
            for (File subdir : subdirs) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > timeout) {
                    Logging.warning("⚠️ Module discovery timeout reached, stopping discovery");
                    break;
                }
                
                String moduleName = subdir.getName();
                Logging.info("🔍 Checking module: " + moduleName);
                
                // Filter out infra/hidden directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    Logging.info("⏭️ Skipping internal directory: " + moduleName);
                    continue;
                }
                
                try {
                    Logging.info("✅ Validating module structure for: " + moduleName);
                    // Collect only those passing structural checks
                    if (isValidModuleStructure(subdir)) {
                        validModules.add(subdir);
                        Logging.info("✅ Valid module found: " + moduleName);
                    } else {
                        Logging.info("❌ Invalid module structure: " + moduleName);
                    }
                } catch (Exception e) {
                    Logging.error("💥 Error validating module " + moduleName + ": " + e.getMessage());
                    // Continue with other modules instead of failing completely
                }
            }
            
            Logging.info("🏁 Module discovery completed. Found " + validModules.size() + " valid modules");
            
        } catch (Exception e) {
            Logging.error("💥 Error getting valid module directories: " + e.getMessage(), e);
        }
        
        return validModules;
    }
    
    // ==================== PUBLIC METHODS - VALIDATION ====================
    
    /**
     * Checks if a directory has a valid module structure.
     * 
     * <p>Validity is determined by:
     * <ul>
     *   <li>Presence of required source files (Main.java, Metadata.java)</li>
     *   <li>Minimal API signatures in those files</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the directory has a valid module structure, false otherwise
     */
    public static boolean isValidModuleStructure(File moduleDir) {
        try {
            // Require top-level entry points (minimal contract for modules)
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            
            if (!mainJavaFile.exists() || !metadataJavaFile.exists()) {
                Logging.info("Module " + moduleDir.getName() + " missing required source files");
                return false;
            }
            
            // Minimal content checks for Main and Metadata
            if (!validateMainJavaFile(mainJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Main.java");
                return false;
            }
            
            if (!validateMetadataJavaFile(metadataJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Metadata.java");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Logging.error("Error validating module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PRIVATE METHODS - VALIDATION ====================
    
    /**
     * Validates that Main.java contains required methods.
     * 
     * <p>This method checks that Main.java:
     * <ul>
     *   <li>Implements the GameModule interface</li>
     *   <li>Contains a class named "Main"</li>
     * </ul>
     * 
     * <p>Includes timeout protection for file reading operations.
     * 
     * @param mainJavaFile The Main.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    private static boolean validateMainJavaFile(File mainJavaFile) {
        try {
            // Add timeout protection for file reading
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 second timeout for file operations
            
            String content = Files.readString(mainJavaFile.toPath());
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("File reading timeout for Main.java");
                return false;
            }
            
            boolean implementsGameModule = content.contains("implements GameModule");
            boolean hasClassMain = content.contains("class Main");
            return implementsGameModule && hasClassMain;
        } catch (IOException e) {
            Logging.error("Error reading Main.java file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validates that Metadata.java exposes minimal game metadata contract.
     * 
     * <p>This method checks that Metadata.java:
     * <ul>
     *   <li>Extends GameMetadata</li>
     *   <li>Contains getGameName() method</li>
     *   <li>Contains getGameVersion() method</li>
     *   <li>Contains getGameDescription() method</li>
     * </ul>
     * 
     * <p>Includes timeout protection for file reading operations.
     * 
     * @param metadataJavaFile The Metadata.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    private static boolean validateMetadataJavaFile(File metadataJavaFile) {
        try {
            // Add timeout protection for file reading
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 second timeout for file operations
            
            String content = Files.readString(metadataJavaFile.toPath());
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("File reading timeout for Metadata.java");
                return false;
            }
            
            boolean extendsGameMetadata = content.contains("extends GameMetadata");
            boolean hasGetGameName = content.contains("getGameName()");
            boolean hasGetGameVersion = content.contains("getGameVersion()");
            boolean hasGetGameDescription = content.contains("getGameDescription()");
            return extendsGameMetadata && hasGetGameName && hasGetGameVersion && hasGetGameDescription;
        } catch (IOException e) {
            Logging.error("Error reading Metadata.java file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if a module directory exists and is accessible.
     * 
     * @param modulePath The path to the module directory
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean moduleDirectoryExists(String modulePath) {
        File moduleDir = new File(modulePath);
        return moduleDir.exists() && moduleDir.isDirectory() && moduleDir.canRead();
    }
    
    /**
     * Calculates the total number of steps needed for startup progress tracking.
     * 
     * <p>This method calculates progress steps based on fixed startup steps plus
     * one step per valid module. The total is clamped to a reasonable range for UX.
     * 
     * @return The total number of progress steps
     */
    public static int calculateTotalSteps() {
        Logging.info("Calculating total steps based on actual message count...");
        
        try {
            // Count all messages that will be displayed:
            // Initial: 2 messages (steps 0-1)
            // Module ui_loading init: 4 messages (steps 2-5)
            // Module processing: moduleCount messages (steps 6 to 5+moduleCount)
            // Module ui_loading final: 3 messages (ui_loading, found, finalizing)
            // Final: 3 messages (compilation check, complete, ready)
            // Total: 2 + 4 + moduleCount + 3 + 3 = 12 + moduleCount
            
            int initialSteps = 2; // "Starting GDK application...", "Starting module ui_loading..."
            int moduleInitSteps = 4; // "Initializing...", "Building...", "Preparing...", "Discovering..."
            int moduleFinalSteps = 3; // "Loading compiled...", "Found X modules", "Finalizing..."
            int finalSteps = 3; // "Checking compilation...", "Startup complete", "Ready!"
            int fixedSteps = initialSteps + moduleInitSteps + moduleFinalSteps + finalSteps; // 12 fixed steps
            
            // Relative to launcher module root
            String modulesDirectoryPath = "../modules";
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.info("Modules directory not found, using fixed steps only");
                return fixedSteps;
            }
            
            int validModuleCount = countValidModules(modulesDirectory);
            Logging.info("Found " + validModuleCount + " valid modules");
            
            // Total = fixed steps + one per module for processing
            int totalSteps = fixedSteps + validModuleCount;
            // Maintain a reasonable range for the progress bar
            totalSteps = Math.max(fixedSteps, Math.min(100, totalSteps));
            Logging.info("Calculated total steps: " + totalSteps + " (fixed: " + fixedSteps + ", modules: " + validModuleCount + ")");
            return totalSteps;
        } catch (Exception e) {
            Logging.error("Error calculating total steps: " + e.getMessage(), e);
            return 12; // safe fallback (fixed steps only)
        }
    }
    
    /**
     * Gets the list of all module directories (valid or invalid).
     * 
     * <p>This method returns all directories in the modules directory, regardless
     * of validation status. Useful for diagnostics or bulk operations before validation.
     * 
     * @param modulesDirectory The modules directory to scan
     * @return List of all module directories (excluding infrastructure directories)
     */
    public static List<File> getAllModuleDirectories(File modulesDirectory) {
        List<File> allModules = new ArrayList<>();
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return allModules;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip infra/hidden directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                allModules.add(subdir);
            }
        } catch (Exception e) {
            Logging.error("Error getting all module directories: " + e.getMessage(), e);
        }
        
        return allModules;
    }

    // ==================== PUBLIC METHODS - DIAGNOSTICS ====================
    
    /**
     * Quick test to check if modules directory is accessible.
     * 
     * <p>This method performs basic accessibility checks including existence,
     * directory type, readability, and listing capability. This can help identify
     * if the issue is with file system access.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return true if the directory is accessible, false otherwise
     */
    public static boolean testModulesDirectoryAccess(String modulesDirectoryPath) {
        try {
            Logging.info("🧪 Testing modules directory access: " + modulesDirectoryPath);
            
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.warning("❌ Modules directory does not exist: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.isDirectory()) {
                Logging.warning("❌ Path exists but is not a directory: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.canRead()) {
                Logging.warning("❌ Modules directory is not readable: " + modulesDirectoryPath);
                return false;
            }
            
            Logging.info("✅ Modules directory is accessible and readable");
            
            // Try to list contents
            long startTime = System.currentTimeMillis();
            File[] contents = modulesDirectory.listFiles();
            long listTime = System.currentTimeMillis() - startTime;
            
            if (contents == null) {
                Logging.warning("❌ Cannot list directory contents (null returned)");
                return false;
            }
            
            Logging.info("✅ Directory listing successful in " + listTime + "ms. Found " + contents.length + " items");
            
            // List first few items for debugging
            for (int i = 0; i < Math.min(5, contents.length); i++) {
                File item = contents[i];
                Logging.info("📁 Item " + i + ": " + item.getName() + " (dir: " + item.isDirectory() + ")");
            }
            
            return true;
            
        } catch (Exception e) {
            Logging.error("💥 Error testing modules directory access: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Comprehensive diagnostic method to check why no modules are being detected.
     * 
     * <p>This method performs extensive diagnostics including:
     * <ul>
     *   <li>Checking current working directory</li>
     *   <li>Verifying modules directory existence and accessibility</li>
     *   <li>Listing all contents</li>
     *   <li>Validating each potential module's structure</li>
     *   <li>Checking compilation status</li>
     * </ul>
     * 
     * <p>This will help identify file system, path, or validation issues.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     */
    public static void diagnoseModuleDetectionIssues(String modulesDirectoryPath) {
        Logging.info("🔍 === MODULE DETECTION DIAGNOSTICS ===");
        
        try {
            // Check current working directory
            String currentWorkingDir = System.getProperty("user.dir");
            Logging.info("📁 Current working directory: " + currentWorkingDir);
            
            // Check the modules directory path
            Logging.info("🔍 Modules directory path: " + modulesDirectoryPath);
            
            // Try to resolve the path
            File modulesDirectory = new File(modulesDirectoryPath);
            Logging.info("📁 Resolved modules directory: " + modulesDirectory.getAbsolutePath());
            
            // Check if it exists
            if (!modulesDirectory.exists()) {
                Logging.error("❌ Modules directory does not exist!");
                
                // Try to find modules directory in common locations
                String[] commonPaths = {
                    "modules",
                    "../modules", 
                    "./modules",
                    "../../modules",
                    "target/modules"
                };
                
                Logging.info("🔍 Checking common module directory locations...");
                for (String path : commonPaths) {
                    File testPath = new File(path);
                    if (testPath.exists()) {
                        Logging.info("✅ Found potential modules directory: " + testPath.getAbsolutePath());
                        if (testPath.isDirectory()) {
                            Logging.info("✅ It's a directory");
                            File[] contents = testPath.listFiles();
                            if (contents != null) {
                                Logging.info("📁 Contains " + contents.length + " items");
                                for (File item : contents) {
                                    Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ")");
                                }
                            }
                        }
                    }
                }
                
                return;
            }
            
            // Check if it's a directory
            if (!modulesDirectory.isDirectory()) {
                Logging.error("❌ Path exists but is not a directory!");
                return;
            }
            
            // Check if it's readable
            if (!modulesDirectory.canRead()) {
                Logging.error("❌ Directory exists but is not readable!");
                return;
            }
            
            Logging.info("✅ Modules directory exists, is a directory, and is readable");
            
            // List all contents
            File[] allContents = modulesDirectory.listFiles();
            if (allContents == null) {
                Logging.error("❌ Cannot list directory contents (null returned)");
                return;
            }
            
            Logging.info("📁 Directory contains " + allContents.length + " items:");
            for (File item : allContents) {
                Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ", readable: " + item.canRead() + ")");
            }
            
            // Check for subdirectories that might be modules
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.error("❌ Cannot list subdirectories (null returned)");
                return;
            }
            
            Logging.info("📁 Found " + subdirs.length + " subdirectories:");
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                Logging.info("🔍 Checking subdirectory: " + moduleName);
                
                // Skip internal directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    Logging.info("⏭️ Skipping internal directory: " + moduleName);
                    continue;
                }
                
                // Check module structure
                Logging.info("✅ Validating module structure for: " + moduleName);
                
                // Check for required files
                File mainJavaFile = new File(subdir, "src/main/java/Main.java");
                File metadataJavaFile = new File(subdir, "src/main/java/Metadata.java");
                File targetClassesDir = new File(subdir, "target/classes");
                File mainClassFile = new File(targetClassesDir, "Main.class");
                
                Logging.info("   📄 Main.java exists: " + mainJavaFile.exists());
                Logging.info("   📄 Metadata.java exists: " + metadataJavaFile.exists());
                Logging.info("   📁 target/classes exists: " + targetClassesDir.exists());
                Logging.info("   📄 Main.class exists: " + mainClassFile.exists());
                
                // Try to validate the structure
                try {
                    boolean isValid = isValidModuleStructure(subdir);
                    Logging.info("   ✅ Module structure validation result: " + isValid);
                    
                    if (isValid) {
                        Logging.info("   🎉 Module " + moduleName + " is VALID!");
                        
                        // Check compilation status
                        boolean needsCompilation = moduleNeedsCompilation(subdir);
                        Logging.info("   📦 Compilation needed: " + needsCompilation);
                        
                        if (needsCompilation) {
                            Logging.info("   💡 Run 'mvn compile' in modules/" + moduleName + " to compile");
                        }
                    } else {
                        Logging.info("   ❌ Module " + moduleName + " is INVALID");
                    }
                } catch (Exception e) {
                    Logging.error("   💥 Error validating module " + moduleName + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Logging.error("💥 Error during diagnostics: " + e.getMessage(), e);
        }
        
        Logging.info("🔍 === END DIAGNOSTICS ===");
    }

    // ==================== PUBLIC METHODS - COMPILATION STATUS ====================
    
    /**
     * Checks if a module needs to be compiled by checking for compiled classes.
     * 
     * <p>This method checks if:
     * <ul>
     *   <li>Compiled classes directory exists</li>
     *   <li>Main.class file exists</li>
     *   <li>Source files are newer than compiled classes</li>
     * </ul>
     * 
     * @param moduleDir The module directory to check
     * @return true if the module needs compilation, false otherwise
     */
    public static boolean moduleNeedsCompilation(File moduleDir) {
        try {
            File targetClassesDir = new File(moduleDir, "target/classes");
            File mainClassFile = new File(targetClassesDir, "Main.class");
            
            if (!targetClassesDir.exists()) {
                Logging.info("📁 Module " + moduleDir.getName() + " missing target/classes directory");
                return true;
            }
            
            if (!mainClassFile.exists()) {
                Logging.info("📄 Module " + moduleDir.getName() + " missing Main.class");
                return true;
            }
            
            // Check if source files are newer than compiled classes
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            
            if (mainJavaFile.exists() && mainClassFile.exists()) {
                if (mainJavaFile.lastModified() > mainClassFile.lastModified()) {
                    Logging.info("📝 Module " + moduleDir.getName() + " source files are newer than compiled classes");
                    return true;
                }
            }
            
            // Note: metadataJavaFile is checked but not used in the comparison above
            // This is intentional - we only need to check if Main.java is newer
            
            Logging.info("✅ Module " + moduleDir.getName() + " is compiled and up to date");
            return false;
            
        } catch (Exception e) {
            Logging.error("💥 Error checking compilation status for " + moduleDir.getName() + ": " + e.getMessage());
            return true; // Assume compilation is needed if we can't check
        }
    }
    
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
            Logging.info("🔍 === MODULE COMPILATION STATUS ===");
            
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.info("📁 No subdirectories found");
                return;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip internal directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                Logging.info("🔍 Module: " + moduleName);
                boolean needsCompilation = moduleNeedsCompilation(subdir);
                Logging.info("   📦 Compilation needed: " + needsCompilation);
                
                if (needsCompilation) {
                    Logging.info("   💡 Run 'mvn compile' in modules/" + moduleName + " to compile");
                }
            }
            
            Logging.info("🔍 === END COMPILATION STATUS ===");
            
        } catch (Exception e) {
            Logging.error("💥 Error reporting compilation status: " + e.getMessage(), e);
        }
    }
    
    // ==================== PUBLIC METHODS - MODULE LOOKUP ====================
    
    /**
     * Finds and loads a game module by its game name.
     * 
     * <p>This method loads modules one by one until finding a match, stopping
     * early for efficiency. It uses ModuleCompiler to load each module.
     * 
     * @param gameName The name of the game to find (as returned by getGameName())
     * @return The loaded GameModule instance, or null if not found
     */
    public static GameModule findModuleByName(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            Logging.info("Module lookup: Game name is null or empty");
            return null;
        }
        
        try {
            // Discover valid module directories
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            File modulesDirectory = new File(modulesDirectoryPath);
            List<File> validModuleDirectories = getValidModuleDirectories(modulesDirectory);
            
            if (validModuleDirectories.isEmpty()) {
                Logging.info("Module lookup: No valid modules found");
                return null;
            }
            
            // Load modules one by one until we find the selected game
            for (File moduleDir : validModuleDirectories) {
                GameModule module = ModuleCompiler.loadModule(moduleDir);
                if (module != null && gameName.equals(module.getMetadata().getGameName())) {
                    Logging.info("Module lookup: Found game module: " + gameName);
                    return module;
                }
            }
            
            Logging.info("Module lookup: Game module not found: " + gameName);
            return null;
            
        } catch (Exception e) {
            Logging.error("Module lookup failed: " + e.getMessage(), e);
            return null;
        }
    }
}

