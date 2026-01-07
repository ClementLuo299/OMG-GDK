package launcher.features.persistence.helpers.clear;

import gdk.internal.Logging;
import launcher.features.file_handling.file_paths.GetOtherPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles clearing JSON content from file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class ClearJsonFile {
    
    private ClearJsonFile() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Clears the JSON persistence file.
     */
    public static void clear() {
        try {
            Path jsonFile = Paths.get(GetOtherPaths.JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                Files.delete(jsonFile);
                Logging.info("🗑️ Cleared JSON persistence file");
            }
        } catch (Exception e) {
            Logging.error("❌ Error clearing JSON persistence file: " + e.getMessage(), e);
        }
    }
}

