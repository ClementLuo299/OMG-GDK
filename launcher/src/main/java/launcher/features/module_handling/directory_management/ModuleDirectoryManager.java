package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.discovery.helpers.ModuleCounter;

import java.io.File;
import java.util.List;

/**
 * Public API for module directory management operations.
 * 
 * <p>This class serves as the single entry point for all directory management operations.
 * It provides methods for:
 * <ul>
 *   <li>Validating the modules directory for existence and accessibility</li>
 *   <li>Scanning the modules directory for module subdirectories</li>
 *   <li>Testing directory accessibility</li>
 *   <li>Counting valid modules in the directory</li>
 *   <li>Filtering infrastructure and hidden directories</li>
 * </ul>
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for directory management operations.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDirectoryValidator {
    
    private ModuleDirectoryValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validation result for directory checks.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final File modulesDirectory;
        
        private ValidationResult(boolean isValid, String errorMessage, File modulesDirectory) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.modulesDirectory = modulesDirectory;
        }
        
        public static ValidationResult valid(File modulesDirectory) {
            return new ValidationResult(true, null, modulesDirectory);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage, null);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public File getModulesDirectory() {
            return modulesDirectory;
        }
    }
    
    /**
     * Validates the modules directory exists and is accessible.
     * 
     * @return Validation result containing the directory if valid, or error information
     */
    public static ValidationResult validate() {
        // Get the configured modules directory file_paths
        String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
        Logging.info("Modules directory file_paths: " + modulesDirectoryPath);
        File modulesDirectory = new File(modulesDirectoryPath);
        
        // First check: verify the directory actually exists on the filesystem
        if (!modulesDirectory.exists()) {
            Logging.warning("Modules directory does not exist: " + modulesDirectoryPath);
            // Run diagnostics to help identify where the directory should be
            launcher.features.module_handling.validation.ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            return ValidationResult.invalid("Modules directory not found: " + modulesDirectoryPath);
        }
        
        Logging.info("Modules directory exists: " + modulesDirectory.getAbsolutePath());
        
        // Second check: verify we can actually read and list contents of the directory
        Logging.info("Testing modules directory access...");
        if (!ModuleDirectoryAccessCheck.checkAccess(modulesDirectoryPath)) {
            // Directory exists but we can't access it (permissions issue)
            Logging.error("Modules directory access test failed - skipping module discovery");
            launcher.features.module_handling.validation.ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            return ValidationResult.invalid("Modules directory access failed");
        }
        
        // Both checks passed - directory is valid and accessible
        Logging.info("Modules directory access test passed");
        return ValidationResult.valid(modulesDirectory);
    }
    
    // ==================== PUBLIC METHODS - DIRECTORY OPERATIONS ====================
    
    /**
     * Counts the number of valid modules in the modules directory.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return The number of valid modules found
     */
    public static int countValidModules(String modulesDirectoryPath) {
        return ModuleCounter.countValidModules(modulesDirectoryPath);
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
        return ModuleDirectoryFinder.getAllValidModuleDirectories(modulesDirectoryPath);
    }
    
    /**
     * Gets the list of all module directories (valid or invalid).
     * 
     * <p>This method returns all directories in the modules directory, regardless
     * of validation status. Useful for diagnostics or bulk operations before validation.
     * 
     * @param modulesDirectoryPath The path to the modules directory to scan
     * @return List of all module directories (excluding infrastructure directories)
     */
    public static List<File> getAllModuleDirectories(String modulesDirectoryPath) {
        return ModuleDirectoryFinder.getAllModuleDirectories(modulesDirectoryPath);
    }
    
    /**
     * Checks if a module directory exists and is accessible.
     * 
     * @param modulePath The path to the module directory
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean moduleDirectoryExists(String modulePath) {
        return ModuleDirectoryAccessCheck.moduleDirectoryExists(modulePath);
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
        return ModuleDirectoryAccessCheck.checkAccess(modulesDirectoryPath);
    }
    
    /**
     * Checks if a directory should be skipped during module discovery.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target", ".git"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directory The directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(File directory) {
        return ModuleDirectoryFilter.shouldSkip(directory);
    }
    
    /**
     * Checks if a directory name indicates it should be skipped during module discovery.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target", ".git"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directoryName The name of the directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(String directoryName) {
        return ModuleDirectoryFilter.shouldSkip(directoryName);
    }
}

