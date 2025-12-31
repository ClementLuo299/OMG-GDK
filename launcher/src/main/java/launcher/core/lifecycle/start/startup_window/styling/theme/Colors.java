package launcher.core.lifecycle.start.startup_window.styling.theme;

import java.awt.Color;

/**
 * Color constants for the startup window theme.
 * Modern color palette with improved contrast and visual appeal.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class Colors {
    
    /** Primary text color (softer dark gray) */
    public static final Color PRIMARY_TEXT = new Color(30, 30, 35);
    
    /** Secondary text color (softer light gray) */
    public static final Color SECONDARY_TEXT = new Color(120, 120, 130);
    
    /** Background color (soft white with slight warmth) */
    public static final Color BACKGROUND = new Color(250, 250, 252);
    
    /** Border color (very subtle gray) */
    public static final Color BORDER = new Color(232, 232, 237);
    
    /** Progress bar border color (more visible) */
    public static final Color PROGRESS_BORDER = new Color(200, 200, 210);
    
    /** Fallback background color for systems without transparency support */
    public static final Color FALLBACK_BACKGROUND = new Color(248, 249, 250);
    
    /** Transparent color for window transparency */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    
    /** Progress bar gradient start (vibrant purple-blue) */
    public static final Color PROGRESS_START = new Color(139, 92, 246); // Purple
    
    /** Progress bar gradient end (vibrant blue) */
    public static final Color PROGRESS_END = new Color(59, 130, 246); // Blue
    
    /** Progress bar glow color (semi-transparent purple) */
    public static final Color PROGRESS_GLOW = new Color(139, 92, 246, 100);
    
    /** Shadow color for window and progress bar */
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 40);
    
    /** Private constructor to prevent instantiation */
    private Colors() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

