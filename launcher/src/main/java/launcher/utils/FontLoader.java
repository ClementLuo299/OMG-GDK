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
    private static final String INTER_REGULAR_PATH = "/startup-window/fonts/static/Inter_18pt-Regular.ttf";
    
    /** Path to Inter Bold font in resources */
    private static final String INTER_BOLD_PATH = "/startup-window/fonts/static/Inter_18pt-Bold.ttf";
    
    /** Font family name for Inter */
    public static final String INTER_FONT_FAMILY = "Inter";
    
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
            
            Font.loadFont(regularStream, 12); // Size doesn't matter, just needs to be > 0
            regularStream.close();
            
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
}

