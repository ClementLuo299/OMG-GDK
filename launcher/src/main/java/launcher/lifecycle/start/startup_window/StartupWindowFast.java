package launcher.lifecycle.start.startup_window;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Fast startup window created programmatically (no FXML) for better performance.
 * This version loads much faster than the FXML-based version.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @since Beta 1.0
 */
public class StartupWindowFast implements IStartupWindow {
    
    // JavaFX components
    Stage progressStage;
    ProgressBar progressBar;
    Label percentageLabel;
    Label statusLabel;
    
    private final int totalSteps;
    private double smoothProgress = 0.0;
    
    public StartupWindowFast(int totalSteps) {
        this.totalSteps = totalSteps;
        createWindow();
    }
    
    private void createWindow() {
        // Create components programmatically (much faster than FXML)
        Label titleLabel = new Label("GDK Game Development Kit");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.rgb(52, 73, 94));
        
        Label subtitleLabel = new Label("Initializing");
        subtitleLabel.setFont(Font.font("Inter", 16));
        subtitleLabel.setTextFill(Color.rgb(149, 165, 166));
        
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(500);
        progressBar.setPrefHeight(25);
        progressBar.setId("progressBar");
        
        percentageLabel = new Label("0%");
        percentageLabel.setFont(Font.font("Inter", 14));
        percentageLabel.setTextFill(Color.rgb(149, 165, 166));
        percentageLabel.setId("percentageLabel");
        
        statusLabel = new Label("Starting up...");
        statusLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.rgb(52, 73, 94));
        statusLabel.setId("statusLabel");
        
        // Layout
        VBox progressBox = new VBox(5, progressBar, percentageLabel);
        progressBox.setAlignment(Pos.CENTER);
        
        VBox mainBox = new VBox(20, titleLabel, subtitleLabel, progressBox, statusLabel);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(60, 40, 60, 40));
        mainBox.setStyle("-fx-background-color: white; -fx-border-color: rgb(230, 230, 230); -fx-border-width: 1px;");
        
        // Apply CSS to progress bar for rounded corners
        progressBar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12.5px; " +
            "-fx-border-color: rgb(220, 220, 220); " +
            "-fx-border-radius: 12.5px; " +
            "-fx-border-width: 1px;"
        );
        // Note: Progress bar styling will be applied after scene is shown
        // The .bar style needs to be set after the scene is created
        
        Scene scene = new Scene(mainBox);
        
        // Create stage
        progressStage = new Stage(StageStyle.UNDECORATED);
        progressStage.setTitle("OMG Game Development Kit");
        progressStage.setScene(scene);
        progressStage.setAlwaysOnTop(true);
        progressStage.setResizable(false);
        
        // Center position
        Screen screen = Screen.getPrimary();
        var bounds = screen.getVisualBounds();
        progressStage.setX(bounds.getMinX() + bounds.getWidth() / 2.0);
        progressStage.setY(bounds.getMinY() + bounds.getHeight() / 2.0);
        
        progressStage.sizeToScene();
    }
    
    public void show() {
        if (Platform.isFxApplicationThread()) {
            showWindowSync();
        } else {
            final Object lock = new Object();
            final boolean[] completed = new boolean[1];
            
            Platform.runLater(() -> {
                try {
                    showWindowSync();
                } finally {
                    synchronized (lock) {
                        completed[0] = true;
                        lock.notify();
                    }
                }
            });
            
            synchronized (lock) {
                while (!completed[0]) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    
    private void showWindowSync() {
        progressStage.getScene().getRoot().applyCss();
        progressStage.getScene().getRoot().layout();
        progressStage.sizeToScene();
        
        // Apply progress bar bar styling after CSS is applied
        var bar = progressBar.lookup(".bar");
        if (bar != null) {
            bar.setStyle(
                "-fx-background-color: linear-gradient(to right, rgb(99, 102, 241), rgb(67, 56, 202)); " +
                "-fx-background-radius: 12.5px 0 0 12.5px;"
            );
        }
        
        Screen screen = Screen.getPrimary();
        var bounds = screen.getVisualBounds();
        double w = progressStage.getWidth();
        double h = progressStage.getHeight();
        progressStage.setX(bounds.getMinX() + (bounds.getWidth() - w) / 2.0);
        progressStage.setY(bounds.getMinY() + (bounds.getHeight() - h) / 2.0 + (bounds.getHeight() * 0.05));
        
        progressStage.show();
        progressStage.requestFocus();
    }
    
    public void hide() {
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
    
    public void updateProgress(int step, String status) {
        Platform.runLater(() -> {
            double progress = totalSteps > 0 ? (double) step / totalSteps : 0.0;
            progressBar.setProgress(progress);
            int percentage = totalSteps > 0 ? (step * 100 / totalSteps) : 0;
            percentageLabel.setText(percentage + "%");
            statusLabel.setText(status);
        });
    }
    
    public void setSmoothProgress(double progress) {
        smoothProgress = Math.max(0.0, Math.min(1.0, progress));
        Platform.runLater(() -> {
            progressBar.setProgress(smoothProgress);
            if (percentageLabel != null) {
                int percentage = (int) Math.round(smoothProgress * 100);
                percentageLabel.setText(percentage + "%");
            }
        });
    }
    
    public void updateStatusText(String text) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(text));
        }
    }
    
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}

