package launcher.ui_areas.lobby.lifecycle.init.post_init;

import gdk.internal.Logging;
import javafx.scene.layout.VBox;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.ui_areas.lobby.messaging.MessageBridgeManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;
import launcher.ui_areas.shared.fonts.FontLoader;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Handles post-initialization setup tasks including UI styling, persistence loading,
 * message bridge setup, and subcontroller initialization.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 6, 2026
 * @since Beta 1.0
 */
public final class PostInitializationSetup {
    
    private PostInitializationSetup() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Perform all post-initialization setup tasks.
     * 
     * @param controller The main lobby controller
     * @param messageBridgeManager The message bridge manager
     * @param jsonInputEditor The JSON input editor
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param statusLabelManager The status label manager
     * @param gameSelectionController The game selection controller
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param topBarController The top bar controller
     * @param messageContainer The message container VBox
     */
    public static void performSetup(
            GDKGameLobbyController controller,
            MessageBridgeManager messageBridgeManager,
            JsonEditor jsonInputEditor,
            JFXToggleButton jsonPersistenceToggle,
            StatusLabelManager statusLabelManager,
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            TopBarController topBarController,
            VBox messageContainer) {
        
        // UI styling
        setupMessageContainerStyling(messageContainer);
        
        // Message bridge setup
        messageBridgeManager.subscribeToEndMessageMirror();
        
        // Persistence setup
        JsonPersistenceManager.load(jsonInputEditor, jsonPersistenceToggle);
        
        // UI state initialization
        if (statusLabelManager != null && gameSelectionController != null) {
            statusLabelManager.updateGameCountStatus(gameSelectionController.getAvailableGameModules().size());
        }
        
        // Subcontroller initialization
        gameSelectionController.initialize();
        jsonActionButtonsController.initialize();
        topBarController.initialize();
        
        Logging.info("GDK Game Picker Controller initialized successfully");
    }
    
    /**
     * Set up message container styling with consistent font.
     * 
     * @param messageContainer The message container VBox
     */
    private static void setupMessageContainerStyling(VBox messageContainer) {
        String fontFamily = FontLoader.getApplicationFontFamily();
        String fontStyle = String.format("-fx-font-family: '%s', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;", fontFamily);
        messageContainer.setStyle(fontStyle);
    }
}

