package launcher.features.module_handling.discovery.diagnostics.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for checking and resolving the modules directory path during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CheckModulesDirectoryPath {
    
    private CheckModulesDirectoryPath() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks and logs the modules directory path, and resolves it to an absolute path.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return The resolved File object for the modules directory
     */
    public static File check(String modulesDirectoryPath) {
        Logging.info("Modules directory path: " + modulesDirectoryPath);
        
        File modulesDirectory = new File(modulesDirectoryPath);
        Logging.info("Resolved modules directory: " + modulesDirectory.getAbsolutePath());
        
        return modulesDirectory;
    }
}

