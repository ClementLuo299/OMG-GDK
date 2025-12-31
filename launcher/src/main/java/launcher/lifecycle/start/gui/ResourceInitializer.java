package launcher.lifecycle.start.gui;

import launcher.utils.FontLoader;

/**
 * Initializes application-wide resources before the UI is created.
 * 
 * This class handles initialization tasks that must be completed before
 * any UI components (Swing or JavaFX) are created.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public final class ResourceInitializer {
    
    private ResourceInitializer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Initializes all application-wide resources.
     * This should be called early in the application lifecycle, before any UI is created.
     */
    static void initialize() {
        FontLoader.loadFonts();
    }
}

