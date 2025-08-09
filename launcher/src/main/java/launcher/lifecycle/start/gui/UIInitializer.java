package launcher.lifecycle.start.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;
import launcher.gui.GDKViewModel;
import launcher.lifecycle.start.startup_window.StartupWindowManager;

import java.net.URL;

/*
 * Initializes the main user interface components.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public final class UIInitializer {

    private UIInitializer() {}

    public static GDKGameLobbyController initialize(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.updateProgress(1, "Loading user interface...");
        GDKGameLobbyController[] controllerHolder = new GDKGameLobbyController[1];
        Scene mainLobbyScene = initializeMainUserInterface(controllerHolder);
        GDKGameLobbyController lobbyController = controllerHolder[0];
        GDKViewModel applicationViewModel = initializeApplicationViewModel(primaryApplicationStage);
        applicationViewModel.setMainLobbyScene(mainLobbyScene);
        configurePrimaryApplicationStage(primaryApplicationStage, mainLobbyScene);
        wireUpControllerWithViewModel(applicationViewModel, lobbyController);
        return lobbyController;
    }

    private static Scene initializeMainUserInterface(GDKGameLobbyController[] controllerHolder) {
        try {
            URL fxmlResourceUrl = GDKApplication.class.getResource("/gdk-lobby/GDKGameLobby.fxml");
            if (fxmlResourceUrl == null) {
                throw new RuntimeException("FXML resource not found: /gdk-lobby/GDKGameLobby.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
            fxmlLoader.setClassLoader(GDKApplication.class.getClassLoader());
            Scene mainLobbyScene = new Scene(fxmlLoader.load());
            GDKGameLobbyController lobbyController = fxmlLoader.getController();
            controllerHolder[0] = lobbyController;
            URL cssResourceUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
            if (cssResourceUrl != null) {
                mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
            }
            if (lobbyController == null) {
                throw new RuntimeException("Lobby controller is null - FXML loading may have failed");
            }
            return mainLobbyScene;
        } catch (Exception uiInitializationError) {
            throw new RuntimeException("Failed to initialize main user interface", uiInitializationError);
        }
    }

    private static GDKViewModel initializeApplicationViewModel(Stage primaryApplicationStage) {
        try {
            GDKViewModel applicationViewModel = new GDKViewModel();
            applicationViewModel.setPrimaryStage(primaryApplicationStage);
            return applicationViewModel;
        } catch (Exception viewModelInitializationError) {
            throw new RuntimeException("Failed to initialize ViewModel", viewModelInitializationError);
        }
    }

    private static void configurePrimaryApplicationStage(Stage primaryApplicationStage, Scene mainLobbyScene) {
        try {
            primaryApplicationStage.setScene(mainLobbyScene);
            primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
            primaryApplicationStage.setMinWidth(800);
            primaryApplicationStage.setMinHeight(600);
            primaryApplicationStage.setWidth(1200);
            primaryApplicationStage.setHeight(900);
            primaryApplicationStage.setOpacity(0.0);
        } catch (Exception stageConfigurationError) {
            throw new RuntimeException("Failed to configure primary application stage", stageConfigurationError);
        }
    }

    private static void wireUpControllerWithViewModel(GDKViewModel applicationViewModel, GDKGameLobbyController lobbyController) {
        try {
            if (lobbyController != null) {
                lobbyController.setViewModel(applicationViewModel);
            } else {
                throw new RuntimeException("Lobby controller is null - cannot wire up ViewModel");
            }
        } catch (Exception wiringError) {
            throw new RuntimeException("Failed to wire up controller with ViewModel", wiringError);
        }
    }
} 