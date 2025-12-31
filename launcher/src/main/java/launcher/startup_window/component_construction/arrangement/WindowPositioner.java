package launcher.startup_window.component_construction.arrangement;

import javax.swing.JFrame;

/**
 * Packs and centers a frame on the screen.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class WindowPositioner {
    
    /**
     * Packs and centers a frame on the screen.
     * 
     * @param frame The frame to pack and center
     */
    public static void position(JFrame frame) {
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
}

