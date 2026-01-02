package launcher.ui_areas.startup_window.component_construction;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import launcher.ui_areas.startup_window.component_construction.arrangement.ComponentAssembler;
import launcher.ui_areas.startup_window.component_construction.arrangement.WindowPositioner;
import launcher.ui_areas.startup_window.component_construction.components.MainWindowCreator;
import launcher.ui_areas.startup_window.component_construction.components.LabelCreator;
import launcher.ui_areas.startup_window.component_construction.components.MainPanelCreator;
import launcher.ui_areas.startup_window.component_construction.components.LoadingSpinner;
import launcher.ui_areas.startup_window.styling.theme.Typography;

/**
 * Builds and assembles all components for the startup window.
 * Handles the complete setup process from component creation to final assembly.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class StartupWindowBuilder {
    
    /**
     * Result class containing all initialized components.
     */
    public static class InitializationResult {
        public final JFrame frame;
        public final LoadingSpinner spinner;
        public final JLabel loadingLabel;
        
        public InitializationResult(
                JFrame frame,
                LoadingSpinner spinner,
                JLabel loadingLabel) {
            this.frame = frame;
            this.spinner = spinner;
            this.loadingLabel = loadingLabel;
        }
    }
    
    /**
     * Builds the complete startup window with all components.
     * 
     * @return An InitializationResult containing all initialized components
     */
    public static InitializationResult build() {
        System.out.println("Creating startup loading window");
        System.out.println("Using font: " + Typography.getCurrentFontFamily());
        
        // Create and configure the main JFrame with transparency
        JFrame frame = MainWindowCreator.create();
        
        // Create UI components
        LoadingSpinner spinner = new LoadingSpinner();
        JLabel loadingLabel = LabelCreator.create(LabelCreator.LabelType.LOADING);
        
        // Create and configure the main panel layout
        JPanel mainPanel = MainPanelCreator.create();
        
        // Add components to the panel with proper spacing
        ComponentAssembler.assemble(mainPanel, spinner, loadingLabel);
        
        // Set the main panel as the frame's content pane
        frame.setContentPane(mainPanel);
        
        // Pack and center the window on screen
        WindowPositioner.position(frame);
        
        System.out.println("Startup loading window created");
        
        return new InitializationResult(frame, spinner, loadingLabel);
    }
}

