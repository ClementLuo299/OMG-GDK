package launcher.lifecycle.stop;

import gdk.Logging;

/**
 * Handles the shutdown process of the GDK application.
 * 
 * @author: Clement Luo
 * @date: August 6, 2025
 * @edited: August 7, 2025
 * @since: 1.0
 */
public class Shutdown {
    
    /**
     * Execute the shutdown process.
     */
    public static void shutdown() {
        Logging.info("GDK application shutdown completed");
    }
}
