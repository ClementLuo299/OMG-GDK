package launcher.lifecycle.start.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;
import launcher.gui.GDKViewModel;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import gdk.Logging;

import java.net.URL;
import javafx.application.Platform;

/**
 * Initializes the main user interface components.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 18, 2025
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
            
            // Add close handler for proper cleanup
            primaryApplicationStage.setOnCloseRequest(event -> {
                Logging.info("🚪 Main GDK window closing - initiating shutdown");
                try {
                    // Trigger the shutdown process
                    launcher.lifecycle.stop.Shutdown.shutdown();
                } catch (Exception e) {
                    Logging.error("❌ Error during shutdown: " + e.getMessage(), e);
                    // Force exit if shutdown fails
                    System.exit(1);
                }
            });
            
            // Lightweight resize performance optimization - doesn't change layout
            primaryApplicationStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                // Add performance class during resize
                mainLobbyScene.getRoot().getStyleClass().add("resize-active");
                
                // Use JavaFX's built-in resize optimization
                Platform.runLater(() -> {
                    // Reduce layout complexity during resize
                    mainLobbyScene.getRoot().setCache(true);
                    mainLobbyScene.getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
                });
                
                // Remove after a short delay to re-enable effects
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
                pause.setOnFinished(event -> {
                    mainLobbyScene.getRoot().getStyleClass().remove("resize-active");
                    
                    // Restore normal rendering after resize
                    Platform.runLater(() -> {
                        mainLobbyScene.getRoot().setCache(false);
                    });
                });
                pause.play();
            });
            
            primaryApplicationStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                // Add performance class during resize
                mainLobbyScene.getRoot().getStyleClass().add("resize-active");
                
                // Use JavaFX's built-in resize optimization
                Platform.runLater(() -> {
                    // Reduce layout complexity during resize
                    mainLobbyScene.getRoot().setCache(true);
                    mainLobbyScene.getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
                });
                
                // Remove after a short delay to re-enable effects
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
                pause.setOnFinished(event -> {
                    mainLobbyScene.getRoot().getStyleClass().remove("resize-active");
                    
                    // Restore normal rendering after resize
                    Platform.runLater(() -> {
                        mainLobbyScene.getRoot().setCache(false);
                    });
                });
                pause.play();
            });
            
            // Enable hardware acceleration for better performance
            try {
                // Force hardware acceleration
                System.setProperty("prism.order", "d3d,opengl,sw");
                System.setProperty("prism.vsync", "false");
                System.setProperty("prism.forceGPU", "true");
                System.setProperty("prism.text", "native");
                
                // Optimize JavaFX rendering
                System.setProperty("javafx.animation.fullspeed", "true");
                System.setProperty("javafx.animation.pulse", "60");
                System.setProperty("javafx.animation.force", "false");
                
                // Disable expensive features during resize
                System.setProperty("prism.disableRegionCaching", "true");
                System.setProperty("prism.disableBlending", "false");
                
                // Additional optimizations for better resize performance
                System.setProperty("prism.verbose", "false");
                System.setProperty("prism.debug", "false");
                System.setProperty("prism.trace", "false");
                
                Logging.info("🚀 Hardware acceleration and performance optimizations enabled");
            } catch (Exception e) {
                Logging.warning("⚠️ Could not enable hardware acceleration: " + e.getMessage());
            }
            
            // Optimize the main layout container for better resize performance
            Platform.runLater(() -> {
                // Enable caching for the main container
                mainLobbyScene.getRoot().setCache(true);
                mainLobbyScene.getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
                
                // Optimize specific containers that might cause slowdown
                if (mainLobbyScene.getRoot() instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.VBox rootVBox = (javafx.scene.layout.VBox) mainLobbyScene.getRoot();
                    rootVBox.setCache(true);
                    rootVBox.setCacheHint(javafx.scene.CacheHint.SPEED);
                }
            });
            
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