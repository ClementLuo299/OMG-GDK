package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility class for operations on the modules directory.
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
 * <p>This class is package-private. External code should use {@link ModuleDirectoryValidator}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
final class ModuleDirectoryUtil {
    
    private ModuleDirectoryUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - DIRECTORY OPERATIONS ====================
    
    /**
     * Counts the number of valid modules in the modules directory.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return The number of valid modules found
     */
    public static int countValidModules(String modulesDirectoryPath) {

        // Number of valid modules found
        int validCount = 0;
        
        try {

            // Create File object from path string 
            File modulesDirectory = new File(modulesDirectoryPath);

            // Get the subdirectories of the modules directory
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0; // nothing to scan
            }
            
            for (File subdir : subdirs) {

                // Skip infrastructure and hidden directories that are not game modules
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
                    continue;
                }
                
                // Get the name of the module
                String moduleName = subdir.getName();
                
                // Only structural validity is checked here (not compilation)
                if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(subdir)) {
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
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return List of valid module directories
     */
    public static List<File> getValidModuleDirectories(String modulesDirectoryPath) {
        List<File> validModules = new ArrayList<>();
        
        try {
            // Create File object from path string to enable file system operations
            File modulesDirectory = new File(modulesDirectoryPath);
            Logging.info("🔍 Starting module discovery in: " + modulesDirectory.getAbsolutePath());
            
            // Add timeout protection for file operations
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // Reduced to 5 second timeout for faster failure detection
            
            // Use file operations to discover subdirectories - this is necessary because:
            // 1. We need to read the filesystem to see what module directories actually exist
            // 2. There's no registry or database of modules - discovery happens by scanning
            // 3. listFiles(File::isDirectory) filters to only directories, excluding files
            // 4. Returns File objects needed for validation and further operations
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
                
                // Filter out infrastructure and hidden directories
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
                    String moduleName = subdir.getName();
                    Logging.info("⏭️ Skipping internal directory: " + moduleName);
                    continue;
                }
                
                String moduleName = subdir.getName();
                Logging.info("🔍 Checking module: " + moduleName);
                
                try {
                    Logging.info("✅ Validating module structure for: " + moduleName);
                    // Collect only those passing structural checks
                    if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(subdir)) {
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
            // Use file operations to discover subdirectories - this is necessary because:
            // 1. We need to read the filesystem to see what module directories actually exist
            // 2. There's no registry or database of modules - discovery happens by scanning
            // 3. listFiles(File::isDirectory) filters to only directories, excluding files
            // 4. Returns File objects needed for further operations
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return allModules;
            }
            
            for (File subdir : subdirs) {
                // Skip infrastructure and hidden directories
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
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
            
            int validModuleCount = countValidModules(modulesDirectoryPath);
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
                // Skip infrastructure and hidden directories
                if (ModuleDirectoryFilter.shouldSkip(subdir)) {
                    continue;
                }
                
                String moduleName = subdir.getName();
                
                Logging.info("🔍 Module: " + moduleName);
                boolean needsCompilation = launcher.features.module_handling.validation.ModuleValidator.moduleNeedsCompilation(subdir);
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

