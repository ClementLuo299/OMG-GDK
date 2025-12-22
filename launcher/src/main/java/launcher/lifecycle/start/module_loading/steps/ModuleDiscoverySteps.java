package launcher.lifecycle.start.module_loading.steps;

import gdk.internal.Logging;
import launcher.utils.path.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes the module discovery steps.
 * Handles directory validation, access testing, and module discovery.
 * 
 * @author Clement Luo
 * @date December 21, 2025
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
     * Validates the modules directory and discovers valid modules.
     * 
     * @return Discovery result containing valid module directories or error information
     */
    public static DiscoveryResult discover() {
        String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
        Logging.info("Modules directory path: " + modulesDirectoryPath);
        File modulesDirectory = new File(modulesDirectoryPath);
        
        // Check if directory exists
        if (!modulesDirectory.exists()) {
            Logging.warning("Modules directory does not exist: " + modulesDirectoryPath);
            launcher.utils.module.ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            return DiscoveryResult.failure("Modules directory not found: " + modulesDirectoryPath);
        }
        
        Logging.info("Modules directory exists: " + modulesDirectory.getAbsolutePath());
        
        // Test directory access
        Logging.info("Testing modules directory access...");
        if (!launcher.utils.module.ModuleDiscovery.testModulesDirectoryAccess(modulesDirectoryPath)) {
            Logging.error("Modules directory access test failed - skipping module discovery");
            launcher.utils.module.ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            return DiscoveryResult.failure("Modules directory access failed");
        }
        
        // Discover modules
        Logging.info("Modules directory access test passed - proceeding with discovery");
        List<File> validModuleDirectories = performDiscovery(modulesDirectory, modulesDirectoryPath);
        
        if (validModuleDirectories.isEmpty()) {
            Logging.warning("No valid modules found - running diagnostics...");
            launcher.utils.module.ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
            Logging.info("Checking module compilation status...");
            launcher.utils.module.ModuleDiscovery.reportModuleCompilationStatus(modulesDirectory);
        }
        
        return DiscoveryResult.success(validModuleDirectories);
    }
    
    /**
     * Performs the actual module discovery.
     * 
     * @param modulesDirectory The modules directory
     * @param modulesDirectoryPath The path to the modules directory
     * @return List of valid module directories
     */
    private static List<File> performDiscovery(File modulesDirectory, String modulesDirectoryPath) {
        List<File> validModuleDirectories = new ArrayList<>();
        
        try {
            Logging.info("Starting module discovery...");
            validModuleDirectories = launcher.utils.module.ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
            Logging.info("Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
            
        } catch (Exception e) {
            Logging.error("Module discovery failed: " + e.getMessage(), e);
            e.printStackTrace();
            launcher.utils.module.ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        }
        
        return validModuleDirectories;
    }
}

