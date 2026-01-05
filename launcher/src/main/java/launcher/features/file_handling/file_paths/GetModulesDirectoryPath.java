package launcher.features.file_handling;

import gdk.internal.Logging;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for resolving application paths.
 * 
 * @author Clement Luo
 * @date December 19, 2025
 * @since Beta 1.0
 */
public final class PathUtil {
    
    private static final String MODULES_DIRECTORY_PATH = resolveModulesDirectoryPath();
    
    private PathUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Returns the resolved absolute path to the modules directory.
     * 
     * @return The absolute path to the modules directory
     */
    public static String getModulesDirectoryPath() {
        return MODULES_DIRECTORY_PATH;
    }
    
    /**
     * Resolves the modules directory path by trying multiple candidate paths.
     * Tries: "../modules", "modules", "../../modules" (in order).
     * 
     * @return The resolved absolute path to the modules directory
     */
    private static String resolveModulesDirectoryPath() {
        String[] candidates = {"../modules", "modules", "../../modules"};
        
        for (String candidate : candidates) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (path.toFile().isDirectory()) {
                Logging.info("Modules directory resolved to: " + path);
                return path.toString();
            }
        }
        
        Path fallback = Paths.get("../modules").toAbsolutePath().normalize();
        Logging.warning("Unable to locate modules directory; defaulting to: " + fallback);
        return fallback.toString();
    }
}

