package launcher.ui_areas.startup_window.build.builders;

import javax.swing.JFrame;
import launcher.ui_areas.startup_window.styling.theme.Colors;
import launcher.ui_areas.startup_window.styling.theme.TextContent;

/**
 * Creates and configures the main JFrame with transparency support.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class CreateMainFrame {
    
    /**
     * Creates and configures the main JFrame with transparency support.
     * 
     * @return A configured JFrame ready for use
     */
    public static JFrame create() {
        // Create the main application window
        JFrame frame = new JFrame(TextContent.WINDOW_TITLE);
        
        // Configure window behavior: prevent closing, disable resizing, keep on top
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true); // Remove title bar and borders for custom styling
        
        // Enable window transparency for shadow effects
        try {
            frame.setBackground(Colors.TRANSPARENT);
            // Ensure root pane is also transparent
            frame.getRootPane().setOpaque(false);
            frame.getContentPane().setBackground(Colors.TRANSPARENT);
            ((javax.swing.JComponent) frame.getContentPane()).setOpaque(false);
        } catch (Exception e) {
            // Fallback for systems that don't support transparency
            frame.setBackground(Colors.FALLBACK_BACKGROUND);
        }
        
        return frame;
    }
}

