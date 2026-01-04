package launcher.features.module_handling.discovery.diagnostics.steps;

import gdk.internal.Logging;

/**
 * Helper class for checking the current working directory during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CheckWorkingDirectory {
    
    private CheckWorkingDirectory() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks and logs the current working directory.
     */
    public static void check() {
        String currentWorkingDir = System.getProperty("user.dir");
        Logging.info("Current working directory: " + currentWorkingDir);
    }
}

