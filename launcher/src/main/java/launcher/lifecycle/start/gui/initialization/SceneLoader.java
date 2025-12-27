package launcher.lifecycle.start.gui.initialization;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;

import java.net.URL;

/**
 * Loads the main user interface scene from FXML resources.
 * Handles FXML loading, scene creation, CSS styling, and controller extraction.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class SceneLoader {
    
    private SceneLoader() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Result class containing the loaded scene and controller.
     */
    public static class SceneLoadResult {
        public final Scene scene;
        public final GDKGameLobbyController controller;
        
        public SceneLoadResult(Scene scene, GDKGameLobbyController controller) {
            this.scene = scene;
            this.controller = controller;
        }
    }
    
    /**
     * Loads the main user interface from FXML and creates the scene.
     * Loads the GDKGameLobby.fxml file, extracts the controller, and applies CSS styling.
     * 
     * @return A SceneLoadResult containing the scene and controller
     * @throws RuntimeException if FXML resource is not found or loading fails
     */
    public static SceneLoadResult loadMainScene() {
        try {
            // Load the main user interface from FXML
            URL fxmlResourceUrl = GDKApplication.class.getResource("/gdk-lobby/GDKGameLobby.fxml");
            if (fxmlResourceUrl == null) {
                throw new RuntimeException("FXML resource not found: /gdk-lobby/GDKGameLobby.fxml");
            }
            
            // Create the FXMLLoader and load the scene
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
            fxmlLoader.setClassLoader(GDKApplication.class.getClassLoader());
            Scene mainLobbyScene = new Scene(fxmlLoader.load());
            
            // Get the controller from the FXML loader
            GDKGameLobbyController lobbyController = fxmlLoader.getController();
            if (lobbyController == null) {
                throw new RuntimeException("Lobby controller is null - FXML loading may have failed");
            }
            
            // Apply CSS styling if available
            URL cssResourceUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
            if (cssResourceUrl != null) {
                mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
            }
            
            // Return the SceneLoadResult containing the scene and controller
            return new SceneLoadResult(mainLobbyScene, lobbyController);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load main user interface scene", e);
        }
    }
}

