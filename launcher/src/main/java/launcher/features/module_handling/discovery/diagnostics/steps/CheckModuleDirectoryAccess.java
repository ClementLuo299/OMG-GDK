package launcher.features.module_handling.discovery.diagnostics.steps;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for checking directory access during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CheckModuleDirectoryAccess {
    
    private CheckModuleDirectoryAccess() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if the modules directory exists, is a directory, and is readable.
     * 
     * @param modulesDirectory The modules directory to check
     * @return true if the directory is accessible, false otherwise
     */
    public static boolean check(File modulesDirectory) {
        if (!modulesDirectory.exists()) {
            Logging.error("Modules directory does not exist!");
            return false;
        }
        
        if (!modulesDirectory.isDirectory()) {
            Logging.error("Path exists but is not a directory!");
            return false;
        }
        
        if (!modulesDirectory.canRead()) {
            Logging.error("Directory exists but is not readable!");
            return false;
        }
        
        Logging.info("Modules directory exists, is a directory, and is readable");
        return true;
    }
}

