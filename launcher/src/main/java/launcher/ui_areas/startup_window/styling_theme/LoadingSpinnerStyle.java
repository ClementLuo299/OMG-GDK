package launcher.ui_areas.startup_window.styling_theme;

/**
 * Constants for the load_modules spinner component.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public final class LoadingSpinnerStyle {
    
    /** The size of the spinner in pixels (width and height) */
    public static final int SPINNER_SIZE = 48;
    
    /** The width of the arc stroke in pixels */
    public static final int STROKE_WIDTH = 4;
    
    /** Animation delay in milliseconds (~60 FPS) */
    public static final int ANIMATION_DELAY_MS = 16;
    
    /** Rotation increment per frame in degrees */
    public static final double ROTATION_INCREMENT = 8.0;
    
    /** Arc extent in degrees (270 degrees leaves a 90-degree gap) */
    public static final double ARC_EXTENT = 270.0;
    
    /** Start angle for the arc in degrees (0 = top) */
    public static final double ARC_START_ANGLE = 0.0;
    
    /** Private constructor to prevent instantiation */
    private LoadingSpinnerStyle() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

