package launcher.features.module_handling.discovery;

import gdk.internal.Logging;
import launcher.utils.path.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the module discovery workflow.
 * Coordinates directory validation, module discovery, and result assembly.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleDiscoverySteps {
    
    private ModuleDiscoverySteps() {}
    
    /**
     * Result container for module discovery operations.
     */
    public static class DiscoveryResult {
        private final List<File> validModuleDirectories;
        private final boolean success;
        private final String errorMessage;
        
        private DiscoveryResult(List<File> validModuleDirectories, boolean success, String errorMessage) {
            this.validModuleDirectories = validModuleDirectories != null ? validModuleDirectories : new ArrayList<>();
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static DiscoveryResult success(List<File> validModuleDirectories) {
            return new DiscoveryResult(validModuleDirectories, true, null);
        }
        
        public static DiscoveryResult failure(String errorMessage) {
            return new DiscoveryResult(null, false, errorMessage);
        }
        
        public List<File> getValidModuleDirectories() {
            return validModuleDirectories;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * Orchestrates the complete module discovery workflow.
     * Coordinates validation, discovery execution, and result assembly.
     * 
     * @return Discovery result containing valid module directories or error information
     */
    public static DiscoveryResult discover() {
        // Step 1: Validate that the modules directory exists and is accessible
        ModuleDirectoryValidator.ValidationResult validationResult = ModuleDirectoryValidator.validate();
        if (!validationResult.isValid()) {
            // Directory validation failed - return error immediately
            return DiscoveryResult.failure(validationResult.getErrorMessage());
        }
        
        // Extract the validated directory and path for discovery
        File modulesDirectory = validationResult.getModulesDirectory();
        String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
        
        // Step 2: Scan the directory and find all valid module subdirectories
        Logging.info("Modules directory access test passed - proceeding with discovery");
        List<File> validModuleDirectories = ModuleDiscoveryProcess.findModules(modulesDirectory, modulesDirectoryPath);
        
        // Step 3: If no modules found, run diagnostics to help identify the issue
        if (validModuleDirectories.isEmpty()) {
            ModuleDiscoveryProcess.runEmptyDiscoveryDiagnostics(modulesDirectory, modulesDirectoryPath);
        }
        
        // Step 4: Return success result with discovered modules (empty list is still success)
        return DiscoveryResult.success(validModuleDirectories);
    }
}

