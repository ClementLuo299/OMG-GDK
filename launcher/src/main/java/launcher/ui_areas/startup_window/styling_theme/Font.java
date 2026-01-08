package launcher.ui_areas.startup_window.styling_theme;

import launcher.ui_areas.shared.fonts.FontLoader;

/**
 * Typography constants for the startup window theme.
 * Uses centralized font load_modules from FontLoader.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class Font {
    
    /** Font family name (from centralized font loader) */
    public static final String FONT_FAMILY = FontLoader.getSwingFontFamily();
    
    /** Status font (bold, 16pt) - for load_modules label */
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

