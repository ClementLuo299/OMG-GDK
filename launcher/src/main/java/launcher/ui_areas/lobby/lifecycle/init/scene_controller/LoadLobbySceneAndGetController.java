package launcher.ui_areas.lobby.lifecycle.start.scene_controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import launcher.core.ui_features.ui_loading.fonts.FontLoader;
import launcher.core.GDKApplication;
import launcher.ui_areas.lobby.GDKGameLobbyController;

import java.net.URL;

/**
 * Loads the main user interface scene from FXML resources.
 * Handles FXML ui_loading, scene creation, CSS styling_theme, and controller extraction.
 *
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class LoadLobbySceneAndGetController {

    private LoadLobbySceneAndGetController() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Result record containing the loaded scene and controller.
     */
    public record SceneLoadResult(Scene scene, GDKGameLobbyController controller) {}

    /**
     * Loads the main user interface from FXML and creates the scene.
     * Loads the GDKGameLobby.fxml file, extracts the controller, and applies CSS styling_theme and fonts.
     *
     * @return A SceneLoadResult containing the scene and controller
     * @throws RuntimeException if FXML resource is not found or ui_loading fails
     */
    public static SceneLoadResult loadMainScene() {
        try {
            // Load the main user interface from FXML
            URL fxmlResourceUrl = GDKApplication.class.getResource("/lobby/GDKGameLobby.fxml");
            if (fxmlResourceUrl == null) {
                throw new RuntimeException("FXML resource not found: /lobby/GDKGameLobby.fxml");
            }

            // Create the FXMLLoader and load the scene
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
            fxmlLoader.setClassLoader(GDKApplication.class.getClassLoader());
            Scene mainLobbyScene = new Scene(fxmlLoader.load());

            // Get the controller from the FXML loader
            GDKGameLobbyController lobbyController = fxmlLoader.getController();
            if (lobbyController == null) {
                throw new RuntimeException("Lobby controller is null - FXML ui_loading may have failed");
            }

            // Apply CSS styling_theme first (CSS has global font rules)
            URL cssResourceUrl = GDKApplication.class.getResource("/lobby/gdk-lobby.css");
            if (cssResourceUrl != null) {
                mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
                gdk.internal.Logging.info("✅ CSS stylesheet loaded");
            } else {
                gdk.internal.Logging.warning("⚠️ CSS stylesheet not found");
            }

            // Get the font family to use
            String fontFamily = FontLoader.getApplicationFontFamily();
            gdk.internal.Logging.info("🎨 Font family to apply: " + fontFamily);
            gdk.internal.Logging.info("🎨 Fonts loaded: " + FontLoader.areFontsLoaded());

            // Build the font style string with fallbacks and !important
            String fontStyle = String.format("-fx-font-family: '%s', 'Segoe UI', 'SF Pro Display', 'SF Pro Text', 'Roboto', 'Noto Sans', Arial, sans-serif !important;", fontFamily);

            // Apply font to root node AFTER CSS (so inline style overrides CSS)
            javafx.scene.Parent root = mainLobbyScene.getRoot();
            String existingStyle = root.getStyle();
            if (existingStyle != null && !existingStyle.isEmpty()) {
                root.setStyle(existingStyle + " " + fontStyle);
            } else {
                root.setStyle(fontStyle);
            }
            gdk.internal.Logging.info("🎨 Applied font style to root: " + root.getStyle());

            // Log available JavaFX fonts for debugging
            java.util.List<String> availableFonts = javafx.scene.text.Font.getFamilies();
            boolean interFound = availableFonts.stream().anyMatch(f -> f.equalsIgnoreCase("Inter"));
            gdk.internal.Logging.info("🎨 Inter font available in JavaFX: " + interFound);
            gdk.internal.Logging.info("🎨 Available JavaFX fonts (first 15): " + availableFonts.subList(0, Math.min(15, availableFonts.size())));

            // Return the SceneLoadResult containing the scene and controller
            return new SceneLoadResult(mainLobbyScene, lobbyController);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load main user interface scene", e);
        }
    }
}

