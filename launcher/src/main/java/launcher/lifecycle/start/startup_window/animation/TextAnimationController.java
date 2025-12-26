package launcher.lifecycle.start.startup_window.animation;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import launcher.lifecycle.start.startup_window.ui.UIUpdateHandler;

/**
 * Controls the text animation for status messages in the startup window.
 * Animates dots appearing after status messages (cycles through 0-3 dots).
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class TextAnimationController {
    
    /** Timer that controls the text animation (dots appearing after messages). */
    private Timer animationTimer;
    
    /** The complete message text to display (before animation dots are added). */
    private String fullMessage = "";
    
    /** Current index for the animated dots (0-3 dots cycle). */
    private int currentCharIndex = 0;
    
    /** Flag indicating whether text animation is currently active. */
    private boolean isAnimating = false;
    
    /** The UI update handler for updating UI components. */
    private final UIUpdateHandler uiUpdateHandler;
    
    /**
     * Constructs a new TextAnimationController.
     * 
     * @param uiUpdateHandler The UI update handler for updating UI components
     */
    public TextAnimationController(UIUpdateHandler uiUpdateHandler) {
        this.uiUpdateHandler = uiUpdateHandler;
    }
    
    /**
     * Starts the text animation that cycles dots after the status message.
     * The animation cycles through 0-3 dots (., .., ..., then repeats).
     * 
     * @param baseMessage The base message text to animate (dots will be appended)
     */
    public void start(String baseMessage) {
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
                    // Always reserve 3 characters for dots to prevent text shifting
                    // Use spaces for dots that aren't visible yet to maintain constant width
                    String dots = ".".repeat(currentCharIndex);
                    String spaces = " ".repeat(3 - currentCharIndex);
                    String animatedMessage = fullMessage + dots + spaces;
                    uiUpdateHandler.updateStatusText(animatedMessage);
                });
            }
        }, 0, 500);
    }
    
    /**
     * Stops the text animation and cleans up the timer.
     */
    public void stop() {
        isAnimating = false;
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
    }
}

