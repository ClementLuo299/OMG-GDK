package launcher.features.persistence.load;

import launcher.features.persistence.load.helpers.LoadJsonFile;
import launcher.features.persistence.load.helpers.LoadPersistenceToggleState;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Handles loading persistence settings on startup.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class LoadPreviousJsonInput {
    
    private LoadPreviousJsonInput() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Loads saved JSON content and persistence toggle state on startup.
     * 
     * <p>This method:
     * <ul>
     *   <li>Loads the persistence toggle state from file</li>
     *   <li>If persistence is enabled, loads the saved JSON content</li>
     * </ul>
     * 
     * @param jsonInputEditor The JSON input editor to load content into
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public static void load(JsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        LoadPersistenceToggleState.load(jsonPersistenceToggle);
        
        if (jsonPersistenceToggle.isSelected()) {
            LoadJsonFile.load(jsonInputEditor);
        }
    }
}

