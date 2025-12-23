package launcher.lifecycle.start.startup_window.initialization.components;

import java.awt.Dimension;
import javax.swing.JProgressBar;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates a progress bar with basic properties.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class ProgressBarCreator {
    
    /**
     * Creates a progress bar with basic properties.
     * 
     * @param totalSteps The total number of steps for the progress bar
     * @return A configured progress bar
     */
    public static JProgressBar create(int totalSteps) {
        JProgressBar progressBar = new JProgressBar(0, totalSteps);
        progressBar.setPreferredSize(new Dimension(
            StartupWindowTheme.PROGRESS_BAR_WIDTH,
            StartupWindowTheme.PROGRESS_BAR_HEIGHT
        ));
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(false);
        return progressBar;
    }
}

