package launcher.startup_window.styling.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * Typography constants and utilities for the startup window theme.
 * Handles font loading, font creation, and typography-related functionality.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class Typography {
    
    /** Path to custom font file in resources */
    private static final String CUSTOM_FONT_PATH = "/fonts/Inter_18pt-Regular.ttf";
    
    /** Custom font family name (will be set if custom font loads successfully) */
    private static String customFontFamily = null;
    
    // ============================================================================
    // Public Constants
    // ============================================================================
    
    public static final String FONT_FAMILY = getSystemFontFamily();
    
    /** Title font (bold, larger, 32pt) - for main heading */
    public static final Font TITLE_FONT = createFont(FONT_FAMILY, Font.BOLD, 32);
    
    /** Subtitle font (regular weight, 17pt) - for secondary heading */
    public static final Font SUBTITLE_FONT = createFont(FONT_FAMILY, Font.PLAIN, 17);
    
    /** Status font (bold, 16pt) - for status messages */
    public static final Font STATUS_FONT = createFont(FONT_FAMILY, Font.BOLD, 16);
    
    /** Percentage font (regular weight, 15pt) - for percentage display */
    public static final Font PERCENTAGE_FONT = createFont(FONT_FAMILY, Font.PLAIN, 15);
    
    // ============================================================================
    // Public Methods
    // ============================================================================
    
    /**
     * Gets the currently selected font family name.
     * Useful for debugging or logging.
     * 
     * @return The current font family name
     */
    public static String getCurrentFontFamily() {
        return FONT_FAMILY;
    }
    
    /**
     * Creates a font with letter spacing adjustment.
     * Note: Java's Font doesn't directly support letter-spacing, but we can use
     * AttributedString for rendering with tracking.
     * 
     * @param text The text to create an attributed string for
     * @param font The font to apply
     * @param tracking The letter spacing tracking value
     * @return An AttributedString with the font and tracking applied
     */
    public static AttributedString createTextWithSpacing(String text, Font font, float tracking) {
        AttributedString attributed = new AttributedString(text);
        attributed.addAttribute(TextAttribute.FONT, font);
        if (tracking != 0) {
            attributed.addAttribute(TextAttribute.TRACKING, tracking);
        }
        return attributed;
    }
    
    // ============================================================================
    // Private Font Loading Methods
    // ============================================================================
    
    /**
     * Attempts to load a custom font from resources.
     * Place your font file in: src/main/resources/startup-window/
     * 
     * @return The font family name if loaded successfully, null otherwise
     */
    private static String loadCustomFont() {
        try {
            java.io.InputStream fontStream = Typography.class.getResourceAsStream(CUSTOM_FONT_PATH);
            if (fontStream == null) {
                // Font file not found - this is okay, we'll use system fonts
                return null;
            }
            
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            
            String fontFamily = customFont.getFamily();
            fontStream.close();
            
            System.out.println("Custom font loaded: " + fontFamily);
            return fontFamily;
        } catch (Exception e) {
            System.out.println("Could not load custom font from " + CUSTOM_FONT_PATH + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads a specific weight of the custom font (Regular or Bold).
     * Handles both naming conventions:
     * - Inter_18pt-Regular.ttf / Inter_18pt-Bold.ttf (Inter font package)
     * - Inter-Regular.ttf / Inter-Bold.ttf (standard naming)
     * 
     * @param bold Whether to load the bold weight
     * @return The loaded font, or null if not found
     */
    private static Font loadCustomFontWeight(boolean bold) {
        try {
            // Handle Inter font package naming (Inter_18pt-Regular.ttf -> Inter_18pt-Bold.ttf)
            // or standard naming (Inter-Regular.ttf -> Inter-Bold.ttf)
            String fontPath;
            if (CUSTOM_FONT_PATH.contains("_18pt-")) {
                fontPath = bold 
                    ? CUSTOM_FONT_PATH.replace("_18pt-Regular.ttf", "_18pt-Bold.ttf")
                    : CUSTOM_FONT_PATH;
            } else {
                fontPath = bold 
                    ? CUSTOM_FONT_PATH.replace("-Regular.ttf", "-Bold.ttf")
                    : CUSTOM_FONT_PATH;
            }
            // Ensure path uses /fonts/ directory
            fontPath = fontPath.replace("/startup-window/", "/fonts/");
            
            java.io.InputStream fontStream = Typography.class.getResourceAsStream(fontPath);
            if (fontStream == null) {
                // Bold weight not found, fall back to deriving bold from regular
                if (bold) {
                    fontStream = Typography.class.getResourceAsStream(CUSTOM_FONT_PATH);
                    if (fontStream != null) {
                        Font regularFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                        fontStream.close();
                        return regularFont.deriveFont(Font.BOLD);
                    }
                }
                return null;
            }
            
            Font weightFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            fontStream.close();
            return weightFont;
        } catch (Exception e) {
            // If specific weight fails, try to derive it from regular
            if (bold) {
                try {
                    java.io.InputStream fontStream = Typography.class.getResourceAsStream(CUSTOM_FONT_PATH);
                    if (fontStream != null) {
                        Font regularFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                        fontStream.close();
                        return regularFont.deriveFont(Font.BOLD);
                    }
                } catch (Exception e2) {
                    // Ignore
                }
            }
            return null;
        }
    }
    
    /**
     * Determines the best available font family for the system.
     * Tries to load a custom font first, then falls back to modern system fonts
     * in order of preference (macOS, Windows, Linux), and finally uses a generic
     * logical font name if nothing else is available.
     * 
     * @return The font family name to use for the application
     */
    private static String getSystemFontFamily() {
        // Try to load custom font first
        customFontFamily = loadCustomFont();
        if (customFontFamily != null) {
            return customFontFamily;
        }
        
        // Fall back to system fonts
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
    
    /**
     * Creates a font with high-quality rendering hints applied.
     * Uses derived fonts for better control over font attributes.
     * If using a custom font, attempts to load the appropriate weight.
     * 
     * @param family The font family name
     * @param style The font style (Font.PLAIN, Font.BOLD, etc.)
     * @param size The font size in points
     * @return The created font
     */
    private static Font createFont(String family, int style, int size) {
        // If using custom font, try to load the specific weight file
        if (customFontFamily != null && customFontFamily.equals(family)) {
            Font customWeightFont = loadCustomFontWeight(style == Font.BOLD);
            if (customWeightFont != null) {
                return customWeightFont.deriveFont((float) size);
            }
        }
        
        // Fall back to standard font creation
        Font baseFont = new Font(family, style, size);
        // Use derived font for better rendering
        return baseFont.deriveFont((float) size);
    }
    
    /** Private constructor to prevent instantiation */
    private Typography() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

