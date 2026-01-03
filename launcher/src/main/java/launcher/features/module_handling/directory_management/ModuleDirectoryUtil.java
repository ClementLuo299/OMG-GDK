package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for operations on the modules directory.
 * 
 * <p>This class handles directory-level operations such as:
 * <ul>
 *   <li>Scanning the modules directory for module subdirectories</li>
 *   <li>Testing directory accessibility</li>
 *   <li>Diagnosing directory access issues</li>
 *   <li>Counting modules in the directory</li>
 *   <li>Reporting compilation status for all modules in the directory</li>
 * </ul>
 * 
 * <p>This class does NOT handle:
 * <ul>
 *   <li>Validating individual module structures (see ModuleDiscovery)</li>
 *   <li>Loading or processing GameModule instances (see ModuleDiscovery)</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @since Beta 1.0
 */
public final class ModuleDirectoryUtil {
    
    private ModuleDirectoryUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - DIRECTORY OPERATIONS ====================
    
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
                if (ModuleDiscovery.isValidModuleStructure(subdir)) {
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
                    if (ModuleDiscovery.isValidModuleStructure(subdir)) {
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
                    boolean isValid = ModuleDiscovery.isValidModuleStructure(subdir);
                    Logging.info("   ✅ Module structure validation result: " + isValid);
                    
                    if (isValid) {
                        Logging.info("   🎉 Module " + moduleName + " is VALID!");
                        
                        // Check compilation status
                        boolean needsCompilation = ModuleDiscovery.moduleNeedsCompilation(subdir);
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
                boolean needsCompilation = ModuleDiscovery.moduleNeedsCompilation(subdir);
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
}

