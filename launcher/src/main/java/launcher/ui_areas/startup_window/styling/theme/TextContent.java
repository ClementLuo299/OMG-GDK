package launcher.ui_areas.startup_window.styling.theme;

/**
 * Text content constants for the startup window theme.
 * Defines all user-facing text strings displayed in the window.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class TextContent {
    
    /** Window title */
    public static final String WINDOW_TITLE = "OMG Game Development Kit";
    
    /** Title label text */
    public static final String TITLE_TEXT = "GDK Game Development Kit";
    
    /** Initial subtitle text */
    public static final String INITIAL_SUBTITLE = "Initializing";
    
    /** Initial status text */
    public static final String INITIAL_STATUS = "Starting up";
    
    /** Initial percentage text */
    public static final String INITIAL_PERCENTAGE = "0%";
    
    /** Private constructor to prevent instantiation */
    private TextContent() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

