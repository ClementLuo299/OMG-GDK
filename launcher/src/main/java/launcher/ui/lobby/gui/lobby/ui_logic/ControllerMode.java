package launcher.ui.lobby.gui.lobby.ui_logic;

/**
 * Represents the operational mode of the GDK Game Lobby Controller.
 * 
 * The controller can operate in different modes depending on the application's
 * launch configuration and requirements.
 * 
 * @author Clement Luo
 * @date December 28, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public enum ControllerMode {
    /**
     * Auto-launch mode: No GUI is loaded, controller is used for ViewModel
     * management only. This mode is used when the application needs to launch
     * a game automatically without user interaction.
     */
    AUTO_LAUNCH,
    
    /**
     * Normal mode: Full GUI is loaded and displayed. This is the standard
     * operational mode where users can interact with the game selection
     * interface, configure JSON settings, and launch games manually.
     */
    NORMAL
}

