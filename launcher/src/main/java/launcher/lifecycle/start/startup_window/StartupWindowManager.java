package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;
import launcher.utils.ModuleDiscovery;

/**
 * Manages the running, displaying, and lifecycle of the startup progress window.
 * This class handles showing, hiding, updating progress, and managing animations
 * for the startup window after it has been created.
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
    }
    
    public void updateProgress(int step, String message) {
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
                shimmerOffset += 0.1f;
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
        }, 0, 50);
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