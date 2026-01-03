package launcher.features.module_handling.directory_management;

import gdk.internal.Logging;
import launcher.features.file_paths.PathUtil;

import java.io.File;

/**
 * Validates the modules directory for existence and accessibility.
 * Responsible for checking if the directory exists and can be accessed.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
final class ModuleDirectoryValidator {
    
    private ModuleDirectoryValidator() {}
    
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
        if (!ModuleDirectoryUtil.testModulesDirectoryAccess(modulesDirectoryPath)) {
            // Directory exists but we can't access it (permissions issue)
            Logging.error("Modules directory access test failed - skipping module discovery");
            launcher.features.module_handling.validation.ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            return ValidationResult.invalid("Modules directory access failed");
        }
        
        // Both checks passed - directory is valid and accessible
        Logging.info("Modules directory access test passed");
        return ValidationResult.valid(modulesDirectory);
    }
}

