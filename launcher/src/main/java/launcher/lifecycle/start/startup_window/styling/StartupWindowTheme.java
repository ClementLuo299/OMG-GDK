package launcher.lifecycle.start.startup_window.styling;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    public static final Color PRIMARY_TEXT = Color.rgb(52, 73, 94);
    
    /** Secondary text color (light gray) */
    public static final Color SECONDARY_TEXT = Color.rgb(149, 165, 166);
    
    /** Background color (white) */
    public static final Color BACKGROUND = Color.rgb(255, 255, 255);
    
    /** Border color (light gray) */
    public static final Color BORDER = Color.rgb(230, 230, 230);
    
    /** Fallback background color for systems without transparency support */
    public static final Color FALLBACK_BACKGROUND = Color.rgb(248, 249, 250);
    
    /** Progress bar fill color start (indigo/violet) */
    public static final Color PROGRESS_FILL_START = Color.rgb(99, 102, 241);
    
    /** Progress bar fill color end (deeper indigo) */
    public static final Color PROGRESS_FILL_END = Color.rgb(67, 56, 202);
    
    // ============================================================================
    // Fonts
    // ============================================================================
    
    /** Font family used throughout the window */
    public static final String FONT_FAMILY = "Segoe UI";
    
    /** Title font (bold, 28pt) */
    public static Font getTitleFont() {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, 28);
    }
    
    /** Subtitle font (plain, 16pt) */
    public static Font getSubtitleFont() {
        return Font.font(FONT_FAMILY, FontWeight.NORMAL, 16);
    }
    
    /** Status font (bold, 16pt) */
    public static Font getStatusFont() {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, 16);
    }
    
    /** Percentage font (plain, 14pt) */
    public static Font getPercentageFont() {
        return Font.font(FONT_FAMILY, FontWeight.NORMAL, 14);
    }
    
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

