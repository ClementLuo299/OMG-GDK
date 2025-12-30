package launcher.gui.lobby.ui_logic.managers.core.setup;

import gdk.internal.Logging;
import javafx.scene.layout.VBox;

/**
 * Handles UI component setup and styling.
 * Encapsulates UI setup logic to reduce complexity in LobbyInitializationManager.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class UiSetup {
    
    /**
     * Set up all user interface components.
     * Applies styling and configuration to UI elements.
     * 
     * @param messageContainer The message container VBox
     */
    public static void setupUserInterface(VBox messageContainer) {
        // Apply consistent styling to the message container for better readability
        // Use the same font as the startup window
        String fontFamily = launcher.utils.FontLoader.getApplicationFontFamily();
        Logging.info("Message container font: " + fontFamily);
        String fontStyle = String.format("-fx-font-family: '%s', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;", fontFamily);
        messageContainer.setStyle(fontStyle);
        Logging.info("UI components initialized");
    }
}

