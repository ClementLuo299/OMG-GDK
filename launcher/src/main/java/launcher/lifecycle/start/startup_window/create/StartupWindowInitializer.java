package launcher.lifecycle.start.startup_window.create;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import launcher.lifecycle.start.startup_window.styling.ProgressBarStyling;

/**
 * Initializes and assembles all components for the startup window.
 * Handles the complete setup process from component creation to final assembly.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class StartupWindowInitializer {
    
    /**
     * Result class containing all initialized components.
     */
    public static class InitializationResult {
        public final JFrame frame;
        public final JProgressBar progressBar;
        public final JLabel percentageLabel;
        public final JLabel statusLabel;
        public final ProgressBarStyling progressBarStyling;
        
        public InitializationResult(
                JFrame frame,
                JProgressBar progressBar,
                JLabel percentageLabel,
                JLabel statusLabel,
                ProgressBarStyling progressBarStyling) {
            this.frame = frame;
            this.progressBar = progressBar;
            this.percentageLabel = percentageLabel;
            this.statusLabel = statusLabel;
            this.progressBarStyling = progressBarStyling;
        }
    }
    
    /**
     * Initializes the complete startup window with all components.
     * 
     * @param totalSteps The total number of steps for the progress bar
     * @return An InitializationResult containing all initialized components
     */
    public static InitializationResult initialize(int totalSteps) {
        System.out.println("Creating startup progress window");
        
        // Create and configure the main JFrame with transparency
        JFrame frame = FrameCreator.create();
        
        // Create UI components using creators
        JLabel titleLabel = TitleLabelCreator.create();
        JLabel subtitleLabel = SubtitleLabelCreator.create();
        JProgressBar progressBar = ProgressBarCreator.create(totalSteps);
        JLabel percentageLabel = PercentageLabelCreator.create();
        JLabel statusLabel = StatusLabelCreator.create();
        
        // Apply custom ultra-modern progress bar styling
        ProgressBarStyling styling = new ProgressBarStyling();
        progressBar.setUI(styling);
        
        // Create and configure the main panel layout
        JPanel mainPanel = MainPanelCreator.create();
        
        // Add all components to the panel with proper spacing
        ComponentAssembler.assemble(
            mainPanel, titleLabel, subtitleLabel, progressBar, percentageLabel, statusLabel);
        
        // Set the main panel as the frame's content pane
        frame.setContentPane(mainPanel);
        
        // Pack and center the window on screen
        WindowPositioner.position(frame);
        
        System.out.println("Startup progress window created");
        
        return new InitializationResult(frame, progressBar, percentageLabel, statusLabel, styling);
    }
}

