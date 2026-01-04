package launcher.features.module_handling.compilation.helpers;

import launcher.ui_areas.lobby.GDKGameLobbyController;

/**
 * Helper class for managing UI controller references.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class UIControllerManager {
    
    /** UI controller for progress updates. */
    private static GDKGameLobbyController uiController = null;
    
    private UIControllerManager() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Sets the UI controller for progress updates.
     * 
     * <p>This method allows the UI controller to be set for displaying
     * compilation and loading progress to the user.
     * 
     * @param controller The UI controller
     */
    public static void set(GDKGameLobbyController controller) {
        uiController = controller;
    }
    
    /**
     * Gets the current UI controller.
     * 
     * @return The current UI controller, or null if not set
     */
    public static GDKGameLobbyController get() {
        return uiController;
    }
}

