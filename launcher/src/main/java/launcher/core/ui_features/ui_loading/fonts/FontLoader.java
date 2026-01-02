package launcher.core.ui_features.ui_loading.fonts;

import gdk.internal.Logging;
import javafx.scene.text.Font;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Global font loader for the application.
 * 
 * <p>This class has a single responsibility: ui_loading and managing the Inter font
 * for both Swing (AWT) and JavaFX components throughout the application.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loading Inter font files from resources</li>
 *   <li>Registering fonts with both AWT and JavaFX</li>
 *   <li>Providing font family names for use in CSS and code</li>
 *   <li>Fallback to system fonts if Inter cannot be loaded</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @since Beta 1.0
 */
public class FontLoader {
    
    // ==================== CONSTANTS ====================
    
    /** Path to Inter Regular font in resources. */
    private static final String INTER_REGULAR_PATH = "/fonts/Inter_18pt-Regular.ttf";
    
    /** Path to Inter Bold font in resources. */
    private static final String INTER_BOLD_PATH = "/fonts/Inter_18pt-Bold.ttf";
    
    /** Font family name for Inter. */
    public static final String INTER_FONT_FAMILY = "Inter";
    
    // ==================== STATE ====================
    
    /** Actual font family name after ui_loading (may include size suffix like "Inter 18pt"). */
    private static String actualInterFontFamily = null;
    
    /** Flag indicating whether fonts have been loaded. */
    private static boolean fontsLoaded = false;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FontLoader() {
        throw new AssertionError("FontLoader should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - FONT LOADING ====================
    
    /**
     * Loads the Inter font for both Swing (AWT) and JavaFX.
     * 
     * <p>This method should be called early in the application lifecycle,
     * ideally during application initialization. It attempts to load fonts
     * for both AWT and JavaFX, and considers the operation successful if
     * at least one succeeds.
     * 
     * @return true if fonts were loaded successfully (at least one platform), false otherwise
     */
    public static boolean loadFonts() {
        if (fontsLoaded) {
            return true; // Already loaded
        }
        
        boolean swingLoaded = loadSwingFont();
        boolean javafxLoaded = loadJavaFXFont();
        
        fontsLoaded = swingLoaded || javafxLoaded; // At least one should work
        
        if (fontsLoaded) {
            Logging.info("✅ Inter font loaded successfully for application");
        } else {
            Logging.warning("⚠️ Could not load Inter font - using system fonts");
        }
        
        return fontsLoaded;
    }
    
    // ==================== PRIVATE METHODS - FONT LOADING ====================
    
    /**
     * Loads the Inter font for Swing (AWT) components.
     * 
     * <p>This method loads both regular and bold weights of the Inter font
     * and registers them with the AWT GraphicsEnvironment.
     * 
     * @return true if loaded successfully, false otherwise
     */
    private static boolean loadSwingFont() {
        try {
            InputStream regularStream = FontLoader.class.getResourceAsStream(INTER_REGULAR_PATH);
            if (regularStream == null) {
                return false;
            }
            
            java.awt.Font regularFont = java.awt.Font.createFont(
                java.awt.Font.TRUETYPE_FONT, regularStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(regularFont);
            regularStream.close();
            
            // Also load bold if available
            InputStream boldStream = FontLoader.class.getResourceAsStream(INTER_BOLD_PATH);
            if (boldStream != null) {
                java.awt.Font boldFont = java.awt.Font.createFont(
                    java.awt.Font.TRUETYPE_FONT, boldStream);
                ge.registerFont(boldFont);
                boldStream.close();
            }
            
            return true;
        } catch (Exception e) {
            Logging.warning("Could not load Inter font for Swing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads the Inter font for JavaFX components.
     * 
     * <p>This method loads both regular and bold weights of the Inter font
     * and stores the actual font family name for later use.
     * 
     * @return true if loaded successfully, false otherwise
     */
    private static boolean loadJavaFXFont() {
        try {
            // Load regular weight
            InputStream regularStream = FontLoader.class.getResourceAsStream(INTER_REGULAR_PATH);
            if (regularStream == null) {
                return false;
            }
            
            Font loadedFont = Font.loadFont(regularStream, 12); // Size doesn't matter, just needs to be > 0
            regularStream.close();
            
            // Get the actual font family name from the loaded font
            if (loadedFont != null) {
                actualInterFontFamily = loadedFont.getFamily();
                Logging.info("✅ JavaFX Inter font loaded with family name: " + actualInterFontFamily);
            }
            
            // Load bold weight
            InputStream boldStream = FontLoader.class.getResourceAsStream(INTER_BOLD_PATH);
            if (boldStream != null) {
                Font.loadFont(boldStream, 12);
                boldStream.close();
            }
            
            return true;
        } catch (Exception e) {
            Logging.warning("Could not load Inter font for JavaFX: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== PUBLIC METHODS - FONT QUERY ====================
    
    /**
     * Gets the Inter font family name for use in CSS and code.
     * 
     * <p>This method returns the standard Inter font family name if fonts
     * have been successfully loaded.
     * 
     * @return "Inter" if fonts are loaded, null otherwise
     */
    public static String getInterFontFamily() {
        return fontsLoaded ? INTER_FONT_FAMILY : null;
    }
    
    /**
     * Checks if fonts have been loaded.
     * 
     * @return true if fonts are loaded, false otherwise
     */
    public static boolean areFontsLoaded() {
        return fontsLoaded;
    }
    
    /**
     * Gets the font family to use for the application.
     * 
     * <p>This method uses the same logic as the startup window - tries Inter first,
     * then falls back to system fonts. It checks both JavaFX and AWT font lists
     * to find the best available font.
     * 
     * <p>The method prioritizes modern system fonts (SF Pro, Segoe UI, Roboto, etc.)
     * and falls back to classic fonts (Arial, Helvetica) if needed.
     * 
     * @return The font family name to use in CSS, or "sans-serif" as final fallback
     */
    public static String getApplicationFontFamily() {
        // If Inter is loaded, use the actual font family name (may be "Inter 18pt" not "Inter")
        if (fontsLoaded && actualInterFontFamily != null) {
            // Verify the actual font family is available in JavaFX font families
            java.util.List<String> javafxFontFamilies = javafx.scene.text.Font.getFamilies();
            for (String family : javafxFontFamilies) {
                if (family.equalsIgnoreCase(actualInterFontFamily)) {
                    Logging.info("✅ Using Inter font family: " + family);
                    return family;
                }
            }
            // If the actual family name isn't found, try just "Inter"
            for (String family : javafxFontFamilies) {
                if (family.equalsIgnoreCase(INTER_FONT_FAMILY)) {
                    Logging.info("✅ Using Inter font family (without size): " + family);
                    return family;
                }
            }
            // If neither is found, use the actual family name anyway (it should work)
            Logging.info("✅ Using Inter font family (actual name): " + actualInterFontFamily);
            return actualInterFontFamily;
        }
        
        // Otherwise, use the same fallback logic as startup window
        try {
            // Check JavaFX available fonts first (more accurate for JavaFX)
            java.util.List<String> javafxFontFamilies = javafx.scene.text.Font.getFamilies();
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
                "Arial", "Helvetica", "Verdana"
            };
            
            for (String preferred : preferredFonts) {
                for (String available : javafxFontFamilies) {
                    if (available.equalsIgnoreCase(preferred)) {
                        Logging.info("✅ Using system font: " + available);
                        return available;
                    }
                }
            }
            
            // Fallback to AWT fonts if JavaFX doesn't have them
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] awtFonts = ge.getAvailableFontFamilyNames();
            
            for (String preferred : preferredFonts) {
                for (String available : awtFonts) {
                    if (available.equalsIgnoreCase(preferred)) {
                        Logging.info("✅ Using AWT font: " + available);
                        return available;
                    }
                }
            }
        } catch (Exception e) {
            Logging.warning("⚠️ Error getting font family: " + e.getMessage());
        }
        
        // Final fallback
        Logging.info("⚠️ Using final fallback: sans-serif");
        return "sans-serif";
    }
    
    /**
     * Gets the font family to use for Swing (AWT) components.
     * 
     * <p>This method tries Inter first if loaded, then falls back to system fonts.
     * It checks AWT font lists to find the best available font.
     * 
     * <p>The method prioritizes modern system fonts (SF Pro, Segoe UI, Roboto, etc.)
     * and falls back to classic fonts (Arial, Helvetica) if needed.
     * 
     * @return The font family name to use for Swing components, or "SansSerif" as final fallback
     */
    public static String getSwingFontFamily() {
        // If Inter is loaded, try to use it
        if (fontsLoaded) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] awtFonts = ge.getAvailableFontFamilyNames();
            
            // Try to find Inter or Inter 18pt in AWT fonts
            for (String font : awtFonts) {
                if (font.equalsIgnoreCase(INTER_FONT_FAMILY) || 
                    font.equalsIgnoreCase("Inter 18pt") ||
                    font.startsWith("Inter")) {
                    return font;
                }
            }
        }
        
        // Fall back to system fonts
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] awtFonts = ge.getAvailableFontFamilyNames();
            
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
                for (String available : awtFonts) {
                    if (available.equalsIgnoreCase(preferred)) {
                        return available;
                    }
                }
            }
        } catch (Exception e) {
            Logging.warning("⚠️ Error getting Swing font family: " + e.getMessage());
        }
        
        // Final fallback
        return "SansSerif";
    }
    
    /**
     * Creates a Swing (AWT) font with the specified family, style, and size.
     * 
     * <p>This method creates a font using the centralized font family selection.
     * If a custom font is loaded, it will attempt to use the appropriate weight.
     * 
     * @param family The font family name (if null, uses getSwingFontFamily())
     * @param style The font style (Font.PLAIN, Font.BOLD, etc.)
     * @param size The font size in points
     * @return The created font
     */
    public static java.awt.Font createSwingFont(String family, int style, int size) {
        if (family == null) {
            family = getSwingFontFamily();
        }
        
        // Create the font
        java.awt.Font font = new java.awt.Font(family, style, size);
        // Use derived font for better rendering
        return font.deriveFont((float) size);
    }
}

