package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controls the text animation for status messages in the startup window.
 * Animates dots appearing after status messages (cycles through 0-3 dots).
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 22, 2025
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
    
    /** The progress window that displays the animated status text. */
    private final PreStartupProgressWindow progressWindow;
    
    /**
     * Constructs a new TextAnimationController.
     * 
     * @param progressWindow The progress window to update with animated text
     */
    public TextAnimationController(PreStartupProgressWindow progressWindow) {
        this.progressWindow = progressWindow;
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
                    String animatedMessage = fullMessage + ".".repeat(currentCharIndex);
                    progressWindow.updateStatusText(animatedMessage);
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

