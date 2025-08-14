package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;
import launcher.utils.ModuleDiscovery;
import launcher.lifecycle.stop.Shutdown;
import gdk.Logging;

/**
 * Manages the running, displaying, and lifecycle of the startup progress window.
 * This class handles showing, hiding, updating progress, and managing animations
 * for the startup window after it has been created.
 * 
 * @authors Clement Luo
 * @date August 12, 2025
 * @edited August 13, 2025
 * @since 1.0
 */
public class StartupWindowManager {
    
    private final PreStartupProgressWindow progressWindow;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private int totalSteps = 15;
    
    // Animation support
    private Timer animationTimer;
    private String fullMessage = "";
    private int currentCharIndex = 0;
    private boolean isAnimating = false;
    
    // Progress bar animation
    private Timer progressAnimationTimer;
    private float shimmerOffset = 0.0f;
    
    public StartupWindowManager(PreStartupProgressWindow progressWindow) {
        this.progressWindow = progressWindow;
    }

    public static StartupWindowManager initializeWithCalculatedSteps() {
        PreStartupProgressWindow window = new PreStartupProgressWindow();
        StartupWindowManager manager = new StartupWindowManager(window);
        int steps = ModuleDiscovery.calculateTotalSteps();
        manager.setTotalSteps(steps);
        manager.show();
        manager.updateProgress(0, "Starting GDK application...");
        return manager;
    }
    
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        progressWindow.setTotalSteps(totalSteps);
    }
    
    public void show() {
        progressWindow.show();
        startAnimations();
    }
    
    public void hide() {
        stopAnimations();
        progressWindow.hide();
        
        // Register cleanup task with shutdown system
        Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up StartupWindowManager resources...");
            try {
                // Ensure animations are stopped
                stopAnimations();
                
                // Dispose of the progress window
                if (progressWindow != null) {
                    progressWindow.hide();
                }
                
                Logging.info("✅ StartupWindowManager cleanup completed");
            } catch (Exception e) {
                Logging.error("❌ Error during StartupWindowManager cleanup: " + e.getMessage(), e);
            }
        });
    }
    
    public void updateProgress(int step, String message) {
        // Ensure UI updates happen on the EDT for Swing components
        if (SwingUtilities.isEventDispatchThread()) {
            updateProgressInternal(step, message);
        } else {
            SwingUtilities.invokeLater(() -> updateProgressInternal(step, message));
        }
    }
    
    /**
     * Update progress with a delay to slow down the progress bar animation.
     * This method schedules the progress update after a delay without blocking the UI.
     */
    public void updateProgressWithDelay(int step, String message, int delayMs) {
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProgress(step, message);
                delayTimer.cancel(); // Clean up the timer
            }
        }, delayMs);
    }
    
    private void updateProgressInternal(int step, String message) {
        currentStep.set(step);
        progressWindow.updateProgress(step, message);
        startTextAnimation(message);
    }
    
    public void updateProgress(int step, String message, int totalSteps) {
        this.totalSteps = totalSteps;
        progressWindow.setTotalSteps(totalSteps);
        updateProgress(step, message);
    }
    
    private void startAnimations() {
        startTextAnimation("");
        startProgressBarAnimation();
    }
    
    private void stopAnimations() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
            progressAnimationTimer = null;
        }
        isAnimating = false;
    }
    
    private void startTextAnimation(String baseMessage) {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        fullMessage = baseMessage;
        currentCharIndex = 0;
        isAnimating = true;
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isAnimating) {
                    return;
                }
                currentCharIndex++;
                if (currentCharIndex > 3) {
                    currentCharIndex = 0;
                }
                SwingUtilities.invokeLater(() -> {
                    String animatedMessage = fullMessage + ".".repeat(currentCharIndex);
                    progressWindow.updateStatusText(animatedMessage);
                });
            }
        }, 0, 500);
    }
    
    private void startProgressBarAnimation() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
        }
        progressAnimationTimer = new Timer();
        progressAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shimmerOffset += 0.02f; // Much slower shimmer movement for development
                if (shimmerOffset > 1.0f) {
                    shimmerOffset = 0.0f;
                }
                SwingUtilities.invokeLater(() -> {
                    if (progressWindow.getProgressBarStyling() != null) {
                        progressWindow.getProgressBarStyling().setShimmerOffset(shimmerOffset);
                    }
                    progressWindow.repaintProgressBar();
                });
            }
        }, 0, 500); // Much slower animation (500ms instead of 200ms) for development
    }
    
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    public int getTotalSteps() {
        return totalSteps;
    }
    
    public boolean isVisible() {
        return progressWindow.isVisible();
    }
} 