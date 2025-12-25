package launcher.lifecycle.start.startup_window;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

import java.io.IOException;
import java.net.URL;

/**
 * Startup progress window that displays progress during application initialization.
 * This window uses JavaFX for modern styling and smooth animations.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited December 24, 2025 
 * @since Beta 1.0
 */
public class StartupWindow {
    
    // JavaFX components for the startup progress window UI
    private Stage progressStage;
    private ProgressBar progressBar;
    private Label percentageLabel;
    private Label statusLabel;
    
    // Progress tracking for the progress bar
    private final int totalSteps; // Total steps for the progress bar 
    
    // Smooth progress value for animation (0.0 to 1.0, can be fractional)
    private double smoothProgress = 0.0;
    
    /**
     * Initialize the startup progress window
     * 
     * @param totalSteps The total number of steps for the progress bar
     */
    public StartupWindow(int totalSteps) {
        this.totalSteps = totalSteps;
        
        try {
            // Load FXML
            URL fxmlUrl = getClass().getResource("/startup-window/StartupWindow.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Could not find StartupWindow.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();
            
            // Create scene from root
            Scene scene = new Scene(root);
            
            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/startup-window/startup-window.css").toExternalForm());
            
            // Create and configure stage
            progressStage = new Stage(StageStyle.UNDECORATED);
            progressStage.setTitle(StartupWindowTheme.WINDOW_TITLE);
            progressStage.setScene(scene);
            progressStage.setAlwaysOnTop(true);
            progressStage.setResizable(false);
            
            // Get references to UI components
            progressBar = (ProgressBar) scene.lookup("#progressBar");
            percentageLabel = (Label) scene.lookup("#percentageLabel");
            statusLabel = (Label) scene.lookup("#statusLabel");
            
            // Set initial approximate position to avoid appearing at (0,0)
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D visualBounds = screen.getVisualBounds();
            progressStage.setX(visualBounds.getMinX() + visualBounds.getWidth() / 2.0);
            progressStage.setY(visualBounds.getMinY() + visualBounds.getHeight() / 2.0);
            
            // Do minimal sizing - full layout will happen in show()
            progressStage.sizeToScene();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load startup window FXML", e);
        }
    }
    
    /**
     * Show the progress window
     */
    public void show() {
        System.out.println("Showing startup progress window");
        
        // Show on JavaFX Application Thread
        Runnable showWindow = () -> {
            // Show the window immediately for faster appearance
            progressStage.show();
            
            // Apply CSS and layout after showing (non-blocking)
            Platform.runLater(() -> {
                // Force CSS application and layout for proper sizing
                progressStage.getScene().getRoot().applyCss();
                progressStage.getScene().getRoot().layout();
                
                // Size the window to fit its content
                progressStage.sizeToScene();
                
                // Position the window - slightly lower than center for better visual balance
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D visualBounds = screen.getVisualBounds();
                
                // Get the actual window dimensions
                double windowWidth = progressStage.getWidth();
                double windowHeight = progressStage.getHeight();
                
                // Calculate center position with a slight downward offset (about 5% of screen height)
                double centerX = visualBounds.getMinX() + (visualBounds.getWidth() - windowWidth) / 2.0;
                double centerY = visualBounds.getMinY() + (visualBounds.getHeight() - windowHeight) / 2.0 + (visualBounds.getHeight() * 0.05);
                
                // Set the position
                progressStage.setX(centerX);
                progressStage.setY(centerY);
            });
        };
        
        if (Platform.isFxApplicationThread()) {
            showWindow.run();
        } else {
            Platform.runLater(showWindow);
        }
    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        System.out.println("Hiding startup progress window");
        
        if (Platform.isFxApplicationThread()) {
            progressStage.hide();
            progressStage.close();
        } else {
            Platform.runLater(() -> {
                progressStage.hide();
                progressStage.close();
            });
        }
    }
    
    /**
     * Update progress and status
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        Platform.runLater(() -> {
            // Calculate progress (0.0 to 1.0)
            double progress = totalSteps > 0 ? (double) step / totalSteps : 0.0;
            progressBar.setProgress(progress);
            
            // Update percentage label
            int percentage = totalSteps > 0 ? (step * 100 / totalSteps) : 0;
            percentageLabel.setText(percentage + "%");
            
            statusLabel.setText(status);
            
            System.out.println("Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    
    /**
     * Sets the smooth progress value for animation (0.0 to 1.0).
     * This allows fractional progress values for smooth animation.
     * Also updates the percentage label to reflect the smooth progress.
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setSmoothProgress(double progress) {
        smoothProgress = Math.max(0.0, Math.min(1.0, progress)); // Clamp to 0.0-1.0
        
        Platform.runLater(() -> {
            progressBar.setProgress(smoothProgress);
            
            // Update percentage label with smooth progress
            if (percentageLabel != null) {
                int percentage = (int) Math.round(smoothProgress * 100);
                percentageLabel.setText(percentage + "%");
            }
        });
    }
    
    /**
     * Update the status text without animation.
     * 
     * @param text The text to display
     */
    public void updateStatusText(String text) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(text));
        }
    }
    
    /**
     * Get the progress bar component.
     * 
     * @return The progress bar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
