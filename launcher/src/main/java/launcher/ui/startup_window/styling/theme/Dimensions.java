package launcher.ui.startup_window.styling.theme;

/**
 * Dimension constants for the startup window theme.
 * Defines sizes, widths, heights, and radii used throughout the UI.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class Dimensions {
    
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
    
    /** Private constructor to prevent instantiation */
    private Dimensions() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

