package launcher.lifecycle.start.resources;

import launcher.utils.FontLoader;

/**
 * Initializes application-wide resources before the UI is created.
 * 
 * This class handles initialization tasks that must be completed before
 * any UI components (Swing or JavaFX) are created.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @since Beta 1.0
 */
public final class ResourceInitializer {
    
    private ResourceInitializer() {}
    
    /**
     * Initializes all application-wide resources.
     * This should be called early in the application lifecycle, before any UI is created.
     */
    public static void initialize() {
        FontLoader.loadFonts();
    }
}

