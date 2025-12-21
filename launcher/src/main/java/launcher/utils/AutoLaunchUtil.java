package launcher.utils;

import gdk.internal.Logging;
import launcher.utils.path.FilePaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for auto-launch functionality.
 * Handles reading and writing auto-launch configuration settings.
 * 
 * @authors Clement Luo
 * @date December 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public final class AutoLaunchUtil {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private AutoLaunchUtil() {
        throw new AssertionError("AutoLaunchUtil should not be instantiated");
    }
    
    /**
     * Check if auto-launch functionality is enabled.
     * 
     * @return true if auto-launch is enabled, false otherwise
     */
    public static boolean isAutoLaunchEnabled() {
        try {
            // Get the auto-launch enabled file path
            Path autoLaunchFile = Paths.get(FilePaths.AUTO_LAUNCH_ENABLED_FILE);

            // Check if the auto-launch enabled file exists
            if (!Files.exists(autoLaunchFile)) {
                return false; // Default to disabled
            }

            // Read the content of the auto-launch enabled file
            String content = Files.readString(autoLaunchFile).trim();

            // Parse the content as a boolean
            return Boolean.parseBoolean(content);
        } catch (Exception e) {
            Logging.error("Error checking auto-launch status: " + e.getMessage());
            return false;
        }
    }
}

