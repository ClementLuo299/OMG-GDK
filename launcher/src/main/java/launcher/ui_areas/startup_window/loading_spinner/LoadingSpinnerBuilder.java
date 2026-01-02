package launcher.ui_areas.startup_window.build.components.loading_spinner;

import launcher.ui_areas.startup_window.styling.theme.Colors;
import launcher.ui_areas.startup_window.styling.theme.SpinnerConstants;

/**
 * Builds and configures a LoadingSpinner component.
 * 
 * Handles the construction and initialization of the spinner component,
 * including size configuration, background setup, and double buffering.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public final class LoadingSpinnerBuilder {
    
    private LoadingSpinnerBuilder() {
        throw new AssertionError("LoadingSpinnerBuilder should not be instantiated");
    }
    
    /**
     * Creates and configures a new LoadingSpinner component.
     * 
     * Sets up the component with:
     * - Fixed size matching SPINNER_SIZE constant
     * - Opaque background matching parent panel (to prevent flickering)
     * - Double buffering enabled for smooth animation
     * 
     * @return A new, configured LoadingSpinner instance
     */
    public static LoadingSpinner build() {
        LoadingSpinner spinner = new LoadingSpinner();
        
        // Set fixed size for the spinner (square component)
        spinner.setPreferredSize(new java.awt.Dimension(
            SpinnerConstants.SPINNER_SIZE,
            SpinnerConstants.SPINNER_SIZE
        ));
        spinner.setMinimumSize(new java.awt.Dimension(
            SpinnerConstants.SPINNER_SIZE,
            SpinnerConstants.SPINNER_SIZE
        ));
        spinner.setMaximumSize(new java.awt.Dimension(
            SpinnerConstants.SPINNER_SIZE,
            SpinnerConstants.SPINNER_SIZE
        ));
        
        // Make opaque with background matching parent panel to prevent flickering
        // This avoids transparency issues that can cause visual artifacts during animation
        spinner.setOpaque(true);
        spinner.setBackground(Colors.BACKGROUND);
        
        // Enable double buffering for smooth animation
        spinner.setDoubleBuffered(true);
        
        return spinner;
    }
}

