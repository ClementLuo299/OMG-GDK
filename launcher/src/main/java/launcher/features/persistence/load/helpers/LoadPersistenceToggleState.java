package launcher.features.persistence.load.helpers;

import com.jfoenix.controls.JFXToggleButton;
import gdk.internal.Logging;
import launcher.features.file_handling.file_paths.GetOtherPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles loading persistence toggle state from file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class LoadPersistenceToggleState {
    
    private LoadPersistenceToggleState() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads the persistence toggle state from file.
     * 
     * <p>If the file doesn't exist or an error occurs, defaults to enabled (true).
     * 
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public static void load(JFXToggleButton jsonPersistenceToggle) {
        try {
            Path toggleFile = Paths.get(GetOtherPaths.PERSISTENCE_TOGGLE_FILE);
            boolean isEnabled = true; // Default to enabled
            if (Files.exists(toggleFile)) {
                String toggleState = Files.readString(toggleFile).trim();
                isEnabled = Boolean.parseBoolean(toggleState);
            }
            jsonPersistenceToggle.setSelected(isEnabled);
        } catch (Exception e) {
            Logging.error("❌ Error loading persistence toggle state: " + e.getMessage(), e);
            jsonPersistenceToggle.setSelected(true); // Default to enabled on error
        }
    }
}

