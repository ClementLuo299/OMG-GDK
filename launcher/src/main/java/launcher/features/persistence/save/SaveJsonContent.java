package launcher.features.persistence.save;

import gdk.internal.Logging;
import launcher.features.file_handling.directory_existence.ParentDirectoryExistenceCheck;
import launcher.features.file_handling.file_paths.GetOtherPaths;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import com.jfoenix.controls.JFXToggleButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages saving JSON content to file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class SaveJsonContent {
    
    private SaveJsonContent() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Saves JSON content to file if persistence is enabled.
     * 
     * @param jsonInputEditor The JSON input editor to save content from
     * @param jsonPersistenceToggle The toggle button to check if persistence is enabled
     */
    public static void save(JsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        if (!jsonPersistenceToggle.isSelected()) {
            return;
        }
        
        try {
            Path jsonFile = Paths.get(GetOtherPaths.JSON_PERSISTENCE_FILE);
            ParentDirectoryExistenceCheck.exists(jsonFile);
            String jsonContent = jsonInputEditor.getText();
            Files.writeString(jsonFile, jsonContent);
            Logging.info("📋 Saved JSON input content to file");
        } catch (Exception e) {
            Logging.error("❌ Error saving JSON content: " + e.getMessage(), e);
        }
    }
}

