package launcher.features.file_handling;

import gdk.internal.Logging;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for resolving application paths.
 * 
 * This class handles path resolution for the GDK application, particularly
 * for finding the modules directory regardless of how the application is launched
 * (IDE vs command line).
 * 
 * @author Clement Luo
 * @date December 19, 2025
 * @since Beta 1.0
 */
public final class PathUtil {
    
    /**
     * Cached resolved path to the modules directory.
     * This is resolved once at class initialization time.
     */
    private static final String MODULES_DIRECTORY_PATH = resolveModulesDirectoryPath();
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private PathUtil() {
        throw new AssertionError("PathUtil should not be instantiated");
    }
    
    /**
     * Get the resolved path to the modules directory.
     * 
     * This method returns the path to the modules directory, which is resolved
     * at application startup. The path is cached for performance.
     * 
     * @return The absolute path to the modules directory
     */
    public static String getModulesDirectoryPath() {
        return MODULES_DIRECTORY_PATH;
    }
    
    /**
     * Resolve the modules directory path for both CLI and IDE runs.
     * 
     * This method tries multiple candidate paths to find the modules directory,
     * ensuring the application works regardless of the working directory when launched.
     * 
     * Candidate paths tried (in order):
     * 1. "../modules" - Works when launcher/ is the working directory
     * 2. "modules" - Works when project root is the working directory
     * 3. "../../modules" - Fallback for IDEs launching from submodules
     * 
     * @return The resolved absolute path to the modules directory
     */
    private static String resolveModulesDirectoryPath() {
        String userDir = System.getProperty("user.dir");
        String[] candidates = {
            "../modules",   // Works when launcher/ is the working directory
            "modules",      // Works when project root is the working directory
            "../../modules" // Fallback for IDEs launching from submodules
        };
        
        for (String candidate : candidates) {
            Path candidatePath = Paths.get(candidate).toAbsolutePath().normalize();
            if (candidatePath.toFile().isDirectory()) {
                Logging.info("Modules directory resolved to " + candidatePath + " (user.dir=" + userDir + ")");
                return candidatePath.toString();
            }
        }
        
        Path fallback = Paths.get("../modules").toAbsolutePath().normalize();
        Logging.warning("Unable to locate modules directory from user.dir=" + userDir + "; defaulting to " + fallback);
        return fallback.toString();
    }
}

