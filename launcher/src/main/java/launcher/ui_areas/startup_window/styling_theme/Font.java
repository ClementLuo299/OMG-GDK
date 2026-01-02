package launcher.ui_areas.startup_window.styling_theme;

import launcher.core.ui_features.ui_loading.fonts.FontLoader;

/**
 * Typography constants for the startup window theme.
 * Uses centralized font loading from FontLoader.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class Font {
    
    /** Font family name (from centralized font loader) */
    public static final String FONT_FAMILY = FontLoader.getSwingFontFamily();
    
    /** Status font (bold, 16pt) - for loading label */
    public static final java.awt.Font STATUS_FONT = FontLoader.createSwingFont(FONT_FAMILY, java.awt.Font.BOLD, 16);
    
    /**
     * Gets the currently selected font family name.
     * Useful for debugging or logging.
     * 
     * @return The current font family name
     */
    public static String getCurrentFontFamily() {
        return FONT_FAMILY;
    }
    
    /** Private constructor to prevent instantiation */
    private Font() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

