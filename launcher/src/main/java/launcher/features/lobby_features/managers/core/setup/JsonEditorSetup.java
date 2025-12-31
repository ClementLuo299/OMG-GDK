package launcher.features.lobby_features.managers.core.setup;

import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import launcher.ui.lobby.json_editor.JsonEditor;
import launcher.features.lobby_features.business.JsonPersistenceManager;

/**
 * Handles setup of JSON editor containers and persistence listeners.
 * Encapsulates JSON editor UI setup logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class JsonEditorSetup {
    
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
        
        // Add editors to containers
        jsonInputEditorContainer.getChildren().add(jsonInputEditor);
        jsonOutputEditorContainer.getChildren().add(jsonOutputEditor);
        
        // Configure layout constraints
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
     * @param jsonPersistenceManager The JSON persistence manager
     */
    public static void setupPersistenceListener(
            JsonEditor jsonInputEditor,
            JFXToggleButton jsonPersistenceToggle,
            JsonPersistenceManager jsonPersistenceManager) {
        
        Platform.runLater(() -> {
            jsonInputEditor.textProperty().addListener((observable, oldValue, newValue) -> {
                if (jsonPersistenceToggle.isSelected()) {
                    jsonPersistenceManager.saveJsonContent();
                }
            });
        });
    }
}

