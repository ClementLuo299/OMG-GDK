package launcher.features.persistence.save;

import com.jfoenix.controls.JFXToggleButton;
import gdk.internal.Logging;
import launcher.features.file_handling.directory_existence.ParentDirectoryExistenceCheck;
import launcher.features.file_handling.file_paths.GetOtherPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages saving persistence toggle state to file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class SavePersistenceToggleState {
    
    private SavePersistenceToggleState() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Saves persistence toggle state to file.
     * 
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public static void save(JFXToggleButton jsonPersistenceToggle) {
        try {
            Path toggleFile = Paths.get(GetOtherPaths.PERSISTENCE_TOGGLE_FILE);
            ParentDirectoryExistenceCheck.exists(toggleFile);
            boolean isEnabled = jsonPersistenceToggle.isSelected();
            Files.writeString(toggleFile, String.valueOf(isEnabled));
            Logging.info("📋 Saved persistence toggle state: " + (isEnabled ? "enabled" : "disabled"));
        } catch (Exception e) {
            Logging.error("❌ Error saving persistence toggle state: " + e.getMessage(), e);
        }
    }
}

