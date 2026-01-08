package launcher.ui_areas.lobby.lifecycle.ui_initialization.main_stage.config;

/**
 * Constants for main stage configuration.
 * 
 * <p>This class contains all constants used for configuring the primary
 * application stage, including window dimensions, title, and initial properties.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 2, 2026
 * @since Beta 1.0
 */
public final class MainStageConfiguration {
    
    private MainStageConfiguration() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    // ==================== WINDOW PROPERTIES ====================
    
    /**
     * The title displayed in the main application window.
     */
    public static final String WINDOW_TITLE = "OMG Game Development Kit (GDK)";
    
    // ==================== WINDOW DIMENSIONS ====================
    
    /**
     * Minimum width of the main application window in pixels.
     */
    public static final double MIN_WIDTH = 800.0;
    
    /**
     * Minimum height of the main application window in pixels.
     */
    public static final double MIN_HEIGHT = 600.0;
    
    /**
     * Default width of the main application window in pixels.
     */
    public static final double DEFAULT_WIDTH = 1200.0;
    
    /**
     * Default height of the main application window in pixels.
     */
    public static final double DEFAULT_HEIGHT = 900.0;
    
    // ==================== INITIAL STATE ====================
    
    /**
     * Initial opacity of the main application window.
     * Set to 0.0 to allow smooth transition from startup window.
     */
    public static final double INITIAL_OPACITY = 0.0;
}

