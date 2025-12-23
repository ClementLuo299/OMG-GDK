package launcher.lifecycle.start.startup_window.create;

import javax.swing.JFrame;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates and configures the main JFrame with transparency support.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class FrameCreator {
    
    /**
     * Creates and configures the main JFrame with transparency support.
     * 
     * @return A configured JFrame ready for use
     */
    public static JFrame create() {
        JFrame frame = new JFrame(StartupWindowTheme.WINDOW_TITLE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        
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

