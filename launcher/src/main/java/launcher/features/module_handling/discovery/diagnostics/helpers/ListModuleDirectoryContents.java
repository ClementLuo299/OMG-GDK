package launcher.features.module_handling.discovery.diagnostics.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for listing directory contents during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ListModuleDirectoryContents {
    
    private ListModuleDirectoryContents() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Lists all contents of the modules directory.
     * 
     * @param modulesDirectory The modules directory to list
     * @return Array of files in the directory, or null if listing failed
     */
    public static File[] list(File modulesDirectory) {
        File[] allContents = modulesDirectory.listFiles();
        if (allContents == null) {
            Logging.error("Cannot list directory contents (null returned)");
            return null;
        }
        
        Logging.info("Directory contains " + allContents.length + " items:");
        for (File item : allContents) {
            Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ", readable: " + item.canRead() + ")");
        }
        
        return allContents;
    }
}

