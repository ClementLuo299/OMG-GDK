package launcher.lifecycle.start.startup_window.styling;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme and styling constants for the startup window.
 * All colors, fonts, sizes, and spacing values are defined here.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class StartupWindowTheme {
    
    // ============================================================================
    // Colors
    // ============================================================================
    
    /** Primary text color (dark blue-gray) */
    public static final Color PRIMARY_TEXT = new Color(52, 73, 94);
    
    /** Secondary text color (light gray) */
    public static final Color SECONDARY_TEXT = new Color(149, 165, 166);
    
    /** Background color (white) */
    public static final Color BACKGROUND = new Color(255, 255, 255);
    
    /** Border color (light gray) */
    public static final Color BORDER = new Color(230, 230, 230);
    
    /** Fallback background color for systems without transparency support */
    public static final Color FALLBACK_BACKGROUND = new Color(248, 249, 250);
    
    /** Transparent color for window transparency */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    
    // ============================================================================
    // Fonts
    // ============================================================================
    
    /** Font family used throughout the window */
    public static final String FONT_FAMILY = "Segoe UI";
    
    /** Title font (bold, 28pt) */
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 28);
    
    /** Subtitle font (plain, 16pt) */
    public static final Font SUBTITLE_FONT = new Font(FONT_FAMILY, Font.PLAIN, 16);
    
    /** Status font (bold, 16pt) */
    public static final Font STATUS_FONT = new Font(FONT_FAMILY, Font.BOLD, 16);
    
    /** Percentage font (plain, 14pt) */
    public static final Font PERCENTAGE_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    
    // ============================================================================
    // Sizes and Dimensions
    // ============================================================================
    
    /** Progress bar width */
    public static final int PROGRESS_BAR_WIDTH = 500;
    
    /** Progress bar height */
    public static final int PROGRESS_BAR_HEIGHT = 25;
    
    /** Panel padding (all sides) */
    public static final int PANEL_PADDING = 40;
    
    /** Border width */
    public static final int BORDER_WIDTH = 1;
    
    // ============================================================================
    // Spacing
    // ============================================================================
    
    /** Small vertical spacing */
    public static final int SPACING_SMALL = 5;
    
    /** Medium vertical spacing */
    public static final int SPACING_MEDIUM = 10;
    
    /** Large vertical spacing */
    public static final int SPACING_LARGE = 20;
    
    // ============================================================================
    // Text Content
    // ============================================================================
    
    /** Window title */
    public static final String WINDOW_TITLE = "OMG Game Development Kit";
    
    /** Title label text */
    public static final String TITLE_TEXT = "GDK Game Development Kit";
    
    /** Initial subtitle text */
    public static final String INITIAL_SUBTITLE = "Initializing";
    
    /** Initial status text */
    public static final String INITIAL_STATUS = "Starting up...";
    
    /** Initial percentage text */
    public static final String INITIAL_PERCENTAGE = "0%";
    
    // Private constructor to prevent instantiation
    private StartupWindowTheme() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

