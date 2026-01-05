package launcher.features.file_handling.file_paths;

import gdk.internal.Logging;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for resolving application paths.
 * 
 * @author Clement Luo
 * @date December 19, 2025
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class GetModulesDirectoryPath {
    
    private GetModulesDirectoryPath() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Returns the resolved absolute path to the modules directory.
     * Tries: "../modules", "modules", "../../modules" (in order).
     * 
     * @return The absolute path to the modules directory
     */
    public static String getModulesDirectoryPath() {
        String[] candidates = {"../modules", "modules", "../../modules"};
        
        for (String candidate : candidates) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (path.toFile().isDirectory()) {
                return path.toString();
            }
        }
        
        Path fallback = Paths.get("../modules").toAbsolutePath().normalize();
        Logging.warning("Unable to locate modules directory; defaulting to: " + fallback);
        return fallback.toString();
    }
}

