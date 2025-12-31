package launcher.ui.startup_window.component_construction.components;

import java.awt.Dimension;
import javax.swing.JProgressBar;
import launcher.ui.startup_window.styling.theme.Colors;
import launcher.ui.startup_window.styling.theme.Dimensions;

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
        // Create progress bar with range from 0 to totalSteps
        JProgressBar progressBar = new JProgressBar(0, totalSteps);
        
        // Set fixed size for consistent appearance
        progressBar.setPreferredSize(new Dimension(
            Dimensions.PROGRESS_BAR_WIDTH,
            Dimensions.PROGRESS_BAR_HEIGHT
        ));
        
        // Disable default border for custom styling
        // Keep opaque=true to ensure proper clipping of painting area
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(true);
        // Set background to white to match the panel background
        progressBar.setBackground(Colors.BACKGROUND);
        return progressBar;
    }
}

