package launcher.features.lobby_features.managers.core.factories;

import gdk.api.GameModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import com.jfoenix.controls.JFXToggleButton;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.lobby.managers.json.JsonEditorOperations;
import launcher.ui_areas.lobby.managers.messaging.MessageManager;
import launcher.ui_areas.lobby.managers.ui.LaunchButtonManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;

/**
 * Factory for creating all subcontroller instances used in the lobby.
 * Encapsulates subcontroller creation logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class SubcontrollerFactory {
    
    /**
     * Result containing all created subcontrollers and the game modules list.
     */
    public record SubcontrollerCreationResult(
        ObservableList<GameModule> availableGameModules,
        GameSelectionController gameSelectionController,
        JsonActionButtonsController jsonActionButtonsController,
        TopBarController topBarController
    ) {}
    
    /**
     * Create all subcontrollers for initial initialization.
     * 
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param jsonInputEditor The JSON input editor
     * @param jsonOutputEditor The JSON output editor
     * @param clearInputButton The clear input button
     * @param clearOutputButton The clear output button
     * @param metadataRequestButton The metadata request button
     * @param sendMessageButton The send message button
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param exitButton The exit button
     * @param refreshButton The refresh button
     * @param settingsButton The settings button
     * @param messageManager The message manager
     * @param launchButtonManager The launch button manager
     * @param jsonPersistenceManager The JSON persistence manager
     * @param jsonEditorOperations The JSON editor operations manager
     * @return Result containing all created subcontrollers
     */
    public static SubcontrollerCreationResult createSubcontrollers(
            ComboBox<GameModule> gameSelector,
            Button launchGameButton,
            JsonEditor jsonInputEditor,
            JsonEditor jsonOutputEditor,
            Button clearInputButton,
            Button clearOutputButton,
            Button metadataRequestButton,
            Button sendMessageButton,
            JFXToggleButton jsonPersistenceToggle,
            Button exitButton,
            Button refreshButton,
            Button settingsButton,
            MessageManager messageManager,
            LaunchButtonManager launchButtonManager,
            JsonPersistenceManager jsonPersistenceManager,
            JsonEditorOperations jsonEditorOperations) {
        
        // Create game modules list
        ObservableList<GameModule> availableGameModules = FXCollections.observableArrayList();
        
        // Create game selection controller
        GameSelectionController gameSelectionController = new GameSelectionController(
            gameSelector,
            launchGameButton,
            availableGameModules,
            messageManager,
            launchButtonManager,
            jsonPersistenceManager
        );
        
        // Create JSON action buttons controller
        JsonActionButtonsController jsonActionButtonsController = new JsonActionButtonsController(
            jsonInputEditor,
            jsonOutputEditor,
            clearInputButton,
            clearOutputButton,
            metadataRequestButton,
            sendMessageButton,
            jsonPersistenceToggle,
            jsonEditorOperations,
            jsonPersistenceManager,
            messageManager
        );
        
        // Create top bar controller
        TopBarController topBarController = new TopBarController(
            exitButton,
            refreshButton,
            settingsButton
        );
        
        return new SubcontrollerCreationResult(
            availableGameModules,
            gameSelectionController,
            jsonActionButtonsController,
            topBarController
        );
    }
}

