package launcher.ui_areas.startup_window.styling_theme;

import java.awt.Color;

/**
 * Color constants for the startup window theme.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class Colors {
    
    /** Primary text color */
    public static final Color PRIMARY_TEXT = new Color(30, 30, 35);
    
    /** Background color */
    public static final Color BACKGROUND = new Color(250, 250, 252);
    
    /** Fallback background color for systems without transparency support */
    public static final Color FALLBACK_BACKGROUND = new Color(248, 249, 250);
    
    /** Transparent color for window transparency */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    
    /** Spinner gradient start color (purple) */
    public static final Color PROGRESS_START = new Color(139, 92, 246);
    
    /** Spinner gradient end color (blue) */
    public static final Color PROGRESS_END = new Color(59, 130, 246);
    
    /** Private constructor to prevent instantiation */
    private Colors() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

