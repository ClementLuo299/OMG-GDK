package launcher.ui_areas.startup_window.build.arrangement;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * Packs and centers a frame on the screen.
 * Makes the window square by setting both dimensions to the larger value.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class WindowPositioner {
    
    /**
     * Packs and centers a frame on the screen.
     * After packing, makes the window square by setting both dimensions to the larger value.
     * 
     * @param frame The frame to pack and center
     */
    public static void position(JFrame frame) {
        frame.pack();
        
        // Get the current size after packing
        Dimension size = frame.getSize();
        int width = size.width;
        int height = size.height;
        
        // Make it square by using the larger dimension for both width and height
        int squareSize = Math.max(width, height);
        frame.setSize(squareSize, squareSize);
        
        // Center the window on the screen
        frame.setLocationRelativeTo(null);
    }
}

