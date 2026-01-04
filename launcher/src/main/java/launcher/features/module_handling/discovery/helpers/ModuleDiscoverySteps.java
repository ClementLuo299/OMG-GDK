package launcher.features.module_handling.discovery.helpers;

import gdk.internal.Logging;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;

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
     * Coordinates directory access check, module discovery, and result assembly.
     * 
     * @return Discovery result containing valid module directories or error information
     */
    public static DiscoveryResult discover() {
        // Step 1: Get all module directories (checks access internally)
        String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
        List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
        
        File modulesDirectory = new File(modulesDirectoryPath);
        
        if (moduleDirectories.isEmpty()) {
            // No modules found or directory not accessible - run diagnostics
            if (!modulesDirectory.exists()) {
                return DiscoveryResult.failure("Modules directory does not exist: " + modulesDirectoryPath);
            }
            // Directory exists but no modules found - run diagnostics
            ModuleDiscoveryProcess.runEmptyDiscoveryDiagnostics(modulesDirectory, modulesDirectoryPath);
            return DiscoveryResult.failure("No module directories found in: " + modulesDirectoryPath);
        }
        
        // Step 2: Validate the found module folders
        Logging.info("Found " + moduleDirectories.size() + " possible module folders - validating...");
        List<File> validModuleDirectories = ModuleDiscoveryProcess.findModules(modulesDirectory, modulesDirectoryPath, moduleDirectories);
        
        // Step 3: If no modules found, run diagnostics to help identify the issue
        if (validModuleDirectories.isEmpty()) {
            ModuleDiscoveryProcess.runEmptyDiscoveryDiagnostics(modulesDirectory, modulesDirectoryPath);
        }
        
        // Step 4: Return success result with discovered modules (empty list is still success)
        return DiscoveryResult.success(validModuleDirectories);
    }
}

