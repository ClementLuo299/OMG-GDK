package launcher.ui_areas.startup_window.build;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.ui_areas.startup_window.build.arrangement.ComponentAssembler;
import launcher.ui_areas.startup_window.build.arrangement.WindowPositioner;
import launcher.ui_areas.startup_window.build.builders.CreateMainFrame;
import launcher.ui_areas.startup_window.build.builders.LoadingLabelCreator;
import launcher.ui_areas.startup_window.build.builders.MainPanelCreator;
import launcher.ui_areas.startup_window.loading_spinner.LoadingSpinner;
import launcher.ui_areas.startup_window.loading_spinner.LoadingSpinnerBuilder;
import launcher.ui_areas.startup_window.loading_spinner.LoadingSpinnerAnimator;
import launcher.ui_areas.startup_window.styling_theme.Font;

/**
 * Builds and assembles all components for the startup window.
 * Handles the complete setup process from component creation to final assembly.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class StartupWindowBuilder {
    
    /**
     * Builds the complete startup window with all components.
     * 
     * @return A new StartupWindow instance
     */
    public static StartupWindow build() {
        System.out.println("Creating startup loading window");
        System.out.println("Using font: " + Font.getCurrentFontFamily());
        
        // Create and configure the main JFrame with transparency
        JFrame frame = CreateMainFrame.create();
        
        // Create animated spinner using builder
        LoadingSpinner spinner = LoadingSpinnerBuilder.build();
        
        // Create controller for the spinner
        LoadingSpinnerAnimator spinnerController = new LoadingSpinnerAnimator(spinner);

        // Create "Loading" label
        JLabel loadingLabel = LoadingLabelCreator.create();
        
        // Create and configure the main panel layout
        JPanel mainPanel = MainPanelCreator.create();
        
        // Add components to the panel with proper spacing
        ComponentAssembler.assemble(mainPanel, spinner, loadingLabel);
        
        // Set the main panel as the frame's content pane
        frame.setContentPane(mainPanel);
        
        // Pack and center the window on screen
        WindowPositioner.position(frame);
        
        System.out.println("Startup loading window created");
        
        // Create and return the StartupWindow
        return new StartupWindow(frame, spinner, spinnerController);
    }
}

