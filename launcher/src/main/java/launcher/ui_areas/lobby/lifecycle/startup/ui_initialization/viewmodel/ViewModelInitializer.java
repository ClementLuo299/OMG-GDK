package launcher.ui_areas.lobby.lifecycle.startup.ui_initialization.viewmodel;

import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.GDKViewModel;

/**
 * Handles the creation and configuration of the GDKViewModel.
 * 
 * <p>This class is responsible for creating ViewModel instances and configuring
 * them with the necessary dependencies (primary stage, main lobby scene).
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 2, 2026
 * @since Beta 1.0
 */
public final class ViewModelInitializer {
    
    private ViewModelInitializer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Creates and configures a GDKViewModel for the lobby.
     * 
     * <p>This method creates a new ViewModel instance configured with:
     * <ul>
     *   <li>The primary application stage</li>
     *   <li>The main lobby scene</li>
     * </ul>
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @param mainLobbyScene The main lobby scene
     * @return A fully configured GDKViewModel instance
     */
    public static GDKViewModel createForStandardLaunch(Stage primaryApplicationStage, Scene mainLobbyScene) {
        return new GDKViewModel(primaryApplicationStage, mainLobbyScene);
    }
    
    /**
     * Creates and configures a GDKViewModel for auto-launch mode.
     * 
     * <p>This method creates a ViewModel for auto-launch scenarios where
     * the main lobby scene is not needed.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @return A configured GDKViewModel instance (without lobby scene)
     */
    public static GDKViewModel createForAutoLaunch(Stage primaryApplicationStage) {
        return new GDKViewModel(primaryApplicationStage);
    }
}

