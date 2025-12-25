package launcher.lifecycle.start.startup_window.styling;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * Centralized theme and styling constants for the startup window.
 * All colors, fonts, sizes, and spacing values are defined here.
 * Modern design with improved colors, typography, and visual effects.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class StartupWindowTheme {
    
    // ============================================================================
    // Colors - Modern Palette
    // ============================================================================
    
    /** Primary text color (softer dark gray) */
    public static final Color PRIMARY_TEXT = new Color(30, 30, 35);
    
    /** Secondary text color (softer light gray) */
    public static final Color SECONDARY_TEXT = new Color(120, 120, 130);
    
    /** Background color (soft white with slight warmth) */
    public static final Color BACKGROUND = new Color(250, 250, 252);
    
    /** Border color (very subtle gray) */
    public static final Color BORDER = new Color(232, 232, 237);
    
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
    
    // ============================================================================
    // Fonts - System Font Stack with Better Typography
    // ============================================================================
    
    /** Font family stack - tries system fonts first, falls back to generic */
    private static String getSystemFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        
        // Try modern system fonts in order of preference
        // Expanded list with more fallbacks for better cross-platform support
        String[] preferredFonts = {
            // macOS - modern system fonts
            "SF Pro Display", "SF Pro Text", "SF Mono", "Helvetica Neue",
            // Windows - modern system fonts
            "Segoe UI", "Segoe UI Variable", "Segoe UI Semibold",
            // Linux/Android - modern fonts
            "Roboto", "Roboto Medium", "Noto Sans", "Ubuntu", "Cantarell",
            // Cross-platform alternatives
            "Inter", "Source Sans Pro", "Open Sans", "Lato",
            // Classic fallbacks
            "Arial", "Helvetica", "Verdana",
            // Logical font names (Java fallback)
            "SansSerif", "Dialog", "DialogInput"
        };
        
        for (String preferred : preferredFonts) {
            for (String available : fonts) {
                if (available.equalsIgnoreCase(preferred)) {
                    return preferred;
                }
            }
        }
        
        // Final fallback to logical font name
        return "SansSerif";
    }
    
    public static final String FONT_FAMILY = getSystemFontFamily();
    
    /**
     * Creates a font with high-quality rendering hints applied.
     * Uses derived fonts for better control over font attributes.
     */
    private static Font createFont(String family, int style, int size) {
        Font baseFont = new Font(family, style, size);
        // Use derived font for better rendering
        return baseFont.deriveFont((float) size);
    }
    
    /** Title font (bold, larger, 32pt) - for main heading */
    public static final Font TITLE_FONT = createFont(FONT_FAMILY, Font.BOLD, 32);
    
    /** Subtitle font (regular weight, 17pt) - for secondary heading */
    public static final Font SUBTITLE_FONT = createFont(FONT_FAMILY, Font.PLAIN, 17);
    
    /** Status font (bold, 16pt) - for status messages */
    public static final Font STATUS_FONT = createFont(FONT_FAMILY, Font.BOLD, 16);
    
    /** Percentage font (regular weight, 15pt) - for percentage display */
    public static final Font PERCENTAGE_FONT = createFont(FONT_FAMILY, Font.PLAIN, 15);
    
    /**
     * Gets the currently selected font family name.
     * Useful for debugging or logging.
     */
    public static String getCurrentFontFamily() {
        return FONT_FAMILY;
    }
    
    // ============================================================================
    // Sizes and Dimensions
    // ============================================================================
    
    /** Progress bar width */
    public static final int PROGRESS_BAR_WIDTH = 520;
    
    /** Progress bar height (taller for better visibility) */
    public static final int PROGRESS_BAR_HEIGHT = 32;
    
    /** Panel padding (more generous, 55px) */
    public static final int PANEL_PADDING = 55;
    
    /** Border width (subtle) */
    public static final int BORDER_WIDTH = 1;
    
    /** Corner radius for rounded rectangles */
    public static final int CORNER_RADIUS = 12;
    
    /** Progress bar corner radius */
    public static final int PROGRESS_CORNER_RADIUS = 10;
    
    /** Shadow blur radius */
    public static final int SHADOW_BLUR = 20;
    
    /** Shadow offset */
    public static final int SHADOW_OFFSET = 4;
    
    /** Progress bar glow radius */
    public static final int PROGRESS_GLOW_RADIUS = 8;
    
    // ============================================================================
    // Spacing - 8px Grid System
    // ============================================================================
    
    /** Extra small vertical spacing (8px) */
    public static final int SPACING_XS = 8;
    
    /** Small vertical spacing (12px) */
    public static final int SPACING_SMALL = 12;
    
    /** Medium vertical spacing (20px) */
    public static final int SPACING_MEDIUM = 20;
    
    /** Large vertical spacing (28px) */
    public static final int SPACING_LARGE = 28;
    
    /** Extra large vertical spacing (36px) */
    public static final int SPACING_XL = 36;
    
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
    
    // ============================================================================
    // Typography Helpers
    // ============================================================================
    
    /**
     * Creates a font with letter spacing adjustment.
     * Note: Java's Font doesn't directly support letter-spacing, but we can use
     * AttributedString for rendering with tracking.
     */
    public static AttributedString createTextWithSpacing(String text, Font font, float tracking) {
        AttributedString attributed = new AttributedString(text);
        attributed.addAttribute(TextAttribute.FONT, font);
        if (tracking != 0) {
            attributed.addAttribute(TextAttribute.TRACKING, tracking);
        }
        return attributed;
    }
    
    // Private constructor to prevent instantiation
    private StartupWindowTheme() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

