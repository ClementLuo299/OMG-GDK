package launcher.features.persistence.helpers.load.helpers;

import gdk.internal.Logging;
import launcher.features.file_handling.file_paths.GetOtherPaths;
import launcher.ui_areas.lobby.json_editor.JsonEditor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles loading JSON content from file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class LoadJsonFile {
    
    private LoadJsonFile() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads saved JSON content from file.
     * 
     * <p>If the file doesn't exist, this method does nothing.
     * Errors are logged but do not throw exceptions.
     * 
     * @param jsonInputEditor The JSON input editor to load content into
     */
    public static void load(JsonEditor jsonInputEditor) {
        try {
            Path jsonFile = Paths.get(GetOtherPaths.JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                String savedJson = Files.readString(jsonFile);
                jsonInputEditor.setText(savedJson);
            }
        } catch (Exception e) {
            Logging.error("❌ Error loading saved JSON content: " + e.getMessage(), e);
        }
    }
}

