package launcher.gui.managers;

import gdk.internal.Logging;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

/**
 * Manages loading animation with progress bar and animated text.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class LoadingAnimationManager {
    
    private final Button refreshButton;
    private final ProgressBar loadingProgressBar;
    private final Label loadingStatusLabel;
    
    private Timeline loadingAnimation;
    private int loadingDots = 0;
    private String currentProcessingModule = "";
    private boolean isRefreshing = false;
    
    private static final String[] LOADING_TASKS = {
        "Discovering modules",
        "Validating source code",
        "Loading compiled classes",
        "Initializing game modules",
        "Updating UI components"
    };
    
    /**
     * Create a new LoadingAnimationManager.
     * 
     * @param refreshButton The refresh button to disable during loading
     * @param loadingProgressBar The progress bar to show progress
     * @param loadingStatusLabel The status label to show loading messages
     */
    public LoadingAnimationManager(Button refreshButton, ProgressBar loadingProgressBar, Label loadingStatusLabel) {
        this.refreshButton = refreshButton;
        this.loadingProgressBar = loadingProgressBar;
        this.loadingStatusLabel = loadingStatusLabel;
    }
    
    /**
     * Start the loading animation with animated text and progress bar.
     */
    public void startAnimation() {
        isRefreshing = true;
        refreshButton.setDisable(true);
        loadingProgressBar.setVisible(true);
        loadingProgressBar.setProgress(0.0);
        loadingStatusLabel.setVisible(true);
        loadingStatusLabel.setText("Starting module discovery...");
        
        Logging.info("🎯 Progress bar made visible and set to 0%");
        
        // Create animated text with dots
        loadingDots = 0;
        loadingAnimation = new Timeline(
            new KeyFrame(Duration.millis(300), event -> {
                loadingDots = (loadingDots + 1) % 4;
                String dots = ".".repeat(loadingDots);
                
                // Update status message with current task
                String currentTask = getCurrentLoadingTask();
                loadingStatusLabel.setText(currentTask + dots);
                
                // Update progress bar - increment by larger amounts and faster
                double currentProgress = loadingProgressBar.getProgress();
                double newProgress = currentProgress + 0.25; // Larger increment - 25% per step
                if (newProgress > 0.9) newProgress = 0.9; // Don't complete until actually done
                loadingProgressBar.setProgress(newProgress);
                
                Logging.info("🎯 Progress bar updated: " + (int)(newProgress * 100) + "%");
            })
        );
        loadingAnimation.setCycleCount(Timeline.INDEFINITE);
        loadingAnimation.play();
    }
    
    /**
     * Set the current module being processed.
     * 
     * @param moduleName The name of the module being processed
     */
    public void setCurrentProcessingModule(String moduleName) {
        this.currentProcessingModule = moduleName;
        Logging.info("🎯 Now processing module: " + moduleName);
    }
    
    /**
     * Clear the current processing module.
     */
    public void clearCurrentProcessingModule() {
        this.currentProcessingModule = "";
        Logging.info("✅ Finished processing all modules");
    }
    
    /**
     * Get the current loading task description based on animation state.
     */
    private String getCurrentLoadingTask() {
        // Cycle through different tasks based on animation state
        int taskIndex = (loadingDots / 2) % LOADING_TASKS.length; // Change task every 2 dots
        String baseTask = LOADING_TASKS[taskIndex];
        
        // Add current module name if available
        if (!currentProcessingModule.isEmpty()) {
            return baseTask + " - " + currentProcessingModule;
        }
        
        return baseTask;
    }
    
    /**
     * Stop the loading animation.
     */
    public void stopAnimation() {
        Logging.info("🎯 Stopping loading animation");
        isRefreshing = false;
        refreshButton.setDisable(false);
        loadingProgressBar.setVisible(false);
        loadingStatusLabel.setVisible(false);
        
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        
        // Complete the progress bar
        loadingProgressBar.setProgress(1.0);
        loadingStatusLabel.setText("Reload completed!");
        Logging.info("🎯 Progress bar completed to 100%");
    }
    
    /**
     * Check if currently refreshing.
     * 
     * @return true if refreshing, false otherwise
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }
}

