package launcher.utils;

import gdk.internal.Logging;
import javafx.scene.text.Font;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Global font loader for the application.
 * Loads the Inter font for both Swing (AWT) and JavaFX.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @since Beta 1.0
 */
public class FontLoader {
    
    /** Path to Inter Regular font in resources */
    private static final String INTER_REGULAR_PATH = "/fonts/Inter_18pt-Regular.ttf";
    
    /** Path to Inter Bold font in resources */
    private static final String INTER_BOLD_PATH = "/fonts/Inter_18pt-Bold.ttf";
    
    /** Font family name for Inter */
    public static final String INTER_FONT_FAMILY = "Inter";
    
    /** Actual font family name after loading (may include size suffix like "Inter 18pt") */
    private static String actualInterFontFamily = null;
    
    private static boolean fontsLoaded = false;
    
    /**
     * Loads the Inter font for both Swing (AWT) and JavaFX.
     * This should be called early in the application lifecycle.
     * 
     * @return true if fonts were loaded successfully, false otherwise
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
    
    /**
     * Loads the Inter font for Swing (AWT) components.
     * 
     * @return true if loaded successfully
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
     * @return true if loaded successfully
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
    
    /**
     * Gets the Inter font family name for use in CSS and code.
     * 
     * @return "Inter" if fonts are loaded, null otherwise
     */
    public static String getInterFontFamily() {
        return fontsLoaded ? INTER_FONT_FAMILY : null;
    }
    
    /**
     * Checks if fonts have been loaded.
     * 
     * @return true if fonts are loaded
     */
    public static boolean areFontsLoaded() {
        return fontsLoaded;
    }
    
    /**
     * Gets the font family to use for the application.
     * Uses the same logic as the startup window - tries Inter first, then falls back to system fonts.
     * 
     * @return The font family name to use in CSS
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
}

