package launcher.core.lifecycle.start;

/**
 * Initializes application fonts before the UI is created.
 * 
 * This class is a thin wrapper that delegates to launcher.core.FontLoader
 * to load and register the Inter font for both Swing (AWT) and JavaFX components.
 * Font initialization must be completed before any UI components are created.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public final class FontLoader {
    
    private FontLoader() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Initializes all application-wide resources.
     * This should be called early in the application lifecycle, before any UI is created.
     */
    public static void initialize() {
        launcher.core.FontLoader.loadFonts();
    }
}

