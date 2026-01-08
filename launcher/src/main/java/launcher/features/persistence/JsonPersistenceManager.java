package launcher.features.persistence;

import launcher.features.persistence.clear.ClearJsonFile;
import launcher.features.persistence.load.LoadPreviousJsonInput;
import launcher.features.persistence.save.SaveJsonContent;
import launcher.features.persistence.save.SavePersistenceToggleState;
import launcher.features.persistence.save.SavePreviouslySelectedGame;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Coordinates persistence operations.
 * 
 * <p>This class provides static methods to coordinate persistence operations
 * across multiple persistence config.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 5, 2026
 * @since 1.0
 */
public final class JsonPersistenceManager {
    
    private JsonPersistenceManager() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads saved JSON content and persistence toggle state on startup.
     * 
     * @param jsonInputEditor The JSON input editor to load content into
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public static void load(JsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        LoadPreviousJsonInput.load(jsonInputEditor, jsonPersistenceToggle);
    }
    
    /**
     * Saves all persistence settings: JSON content (if enabled) and toggle state.
     * 
     * @param jsonInputEditor The JSON input editor to save content from
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public static void save(JsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        SaveJsonContent.save(jsonInputEditor, jsonPersistenceToggle);
        SavePersistenceToggleState.save(jsonPersistenceToggle);
    }
    
    /**
     * Saves the selected game name to file.
     * 
     * @param gameName The name of the selected game module
     */
    public static void saveSelectedGame(String gameName) {
        SavePreviouslySelectedGame.save(gameName);
    }
    
    /**
     * Clears the JSON persistence file.
     */
    public static void clear() {
        ClearJsonFile.clear();
    }
}
