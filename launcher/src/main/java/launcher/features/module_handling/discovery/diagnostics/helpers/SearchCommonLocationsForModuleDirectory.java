package launcher.features.module_handling.discovery.diagnostics.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for searching common locations for the modules directory during diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class SearchCommonLocationsForModuleDirectory {
    
    private SearchCommonLocationsForModuleDirectory() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Searches common locations for the modules directory and logs findings.
     */
    public static void search() {
        String[] commonPaths = {
            "modules",
            "../modules", 
            "./modules",
            "../../modules",
            "target/modules"
        };
        
        Logging.info("Checking common module directory locations...");
        for (String path : commonPaths) {
            File testPath = new File(path);
            if (testPath.exists()) {
                Logging.info("Found potential modules directory: " + testPath.getAbsolutePath());
                if (testPath.isDirectory()) {
                    Logging.info("It's a directory");
                    File[] contents = testPath.listFiles();
                    if (contents != null) {
                        Logging.info("Contains " + contents.length + " items");
                        for (File item : contents) {
                            Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ")");
                        }
                    }
                }
            }
        }
    }
}

