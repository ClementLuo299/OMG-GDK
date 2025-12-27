package launcher.lifecycle.start.startup_window.component_construction.components;

import javax.swing.JFrame;
import launcher.lifecycle.start.startup_window.styling.theme.StartupWindowTheme;

/**
 * Creates and configures the main JFrame with transparency support.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class MainWindowCreator {
    
    /**
     * Creates and configures the main JFrame with transparency support.
     * 
     * @return A configured JFrame ready for use
     */
    public static JFrame create() {
        // Create the main application window
        JFrame frame = new JFrame(StartupWindowTheme.WINDOW_TITLE);
        
        // Configure window behavior: prevent closing, disable resizing, keep on top
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true); // Remove title bar and borders for custom styling
        
        // Enable window transparency for shadow effects
        try {
            frame.setBackground(StartupWindowTheme.TRANSPARENT);
        } catch (Exception e) {
            // Fallback for systems that don't support transparency
            frame.setBackground(StartupWindowTheme.FALLBACK_BACKGROUND);
        }
        
        return frame;
    }
}

