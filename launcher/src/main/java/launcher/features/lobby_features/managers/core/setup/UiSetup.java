package launcher.features.lobby_features.managers.core.setup;

import gdk.internal.Logging;
import javafx.scene.layout.VBox;
import launcher.core.FontLoader;

/**
 * Handles UI component setup and styling.
 * Encapsulates UI setup logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
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
        String fontFamily = FontLoader.getApplicationFontFamily();
        Logging.info("Message container font: " + fontFamily);
        String fontStyle = String.format("-fx-font-family: '%s', 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;", fontFamily);
        messageContainer.setStyle(fontStyle);
        Logging.info("UI components initialized");
    }
}

