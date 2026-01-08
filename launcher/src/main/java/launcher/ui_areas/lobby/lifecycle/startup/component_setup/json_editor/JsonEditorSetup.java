package launcher.ui_areas.lobby.lifecycle.startup.component_setup.json_editor;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.features.persistence.JsonPersistenceManager;

/**
 * Handles setup of JSON editor containers and persistence listeners.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public final class JsonEditorSetup {
    
    private JsonEditorSetup() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Set up JSON editor containers in the UI.
     * Adds editors to containers and configures layout constraints.
     * 
     * @param jsonInputEditor The JSON input editor
     * @param jsonOutputEditor The JSON output editor
     * @param jsonInputEditorContainer The JSON input editor container
     * @param jsonOutputEditorContainer The JSON output editor container
     */
    public static void setupEditorContainers(
            JsonEditor jsonInputEditor,
            JsonEditor jsonOutputEditor,
            VBox jsonInputEditorContainer,
            VBox jsonOutputEditorContainer) {
        
        jsonInputEditorContainer.getChildren().add(jsonInputEditor);
        jsonOutputEditorContainer.getChildren().add(jsonOutputEditor);
        
        VBox.setVgrow(jsonInputEditorContainer, Priority.ALWAYS);
        VBox.setVgrow(jsonOutputEditorContainer, Priority.ALWAYS);
        VBox.setVgrow(jsonInputEditor, Priority.ALWAYS);
        VBox.setVgrow(jsonOutputEditor, Priority.ALWAYS);
    }
    
    /**
     * Set up JSON persistence auto-save listener.
     * Listens for text changes and saves when persistence is enabled.
     * 
     * @param jsonInputEditor The JSON input editor
     * @param jsonPersistenceToggle The JSON persistence toggle
     */
    public static void setupPersistenceListener(
            JsonEditor jsonInputEditor,
            JFXToggleButton jsonPersistenceToggle) {
        jsonInputEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (jsonPersistenceToggle.isSelected()) {
                JsonPersistenceManager.save(jsonInputEditor, jsonPersistenceToggle);
            }
        });
    }
}

