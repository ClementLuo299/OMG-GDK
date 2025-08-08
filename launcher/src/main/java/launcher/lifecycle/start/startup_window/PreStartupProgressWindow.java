package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Pre-Startup Progress Window
 * 
 * Shows a progress window BEFORE JavaFX starts, using Swing for immediate display.
 * This window appears instantly when the application launches and shows progress
 * during the JavaFX initialization phase.
 * 
 * @author Clement Luo
 * @date August 5, 2025
 * @edited August 8, 2025 
 * @since 1.0
 */
public class PreStartupProgressWindow {
    
    // Swing components for the pre-startup progress window UI
    private JFrame progressFrame;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // Progress tracking for the progress bar
    private int totalSteps = 15; // Default total steps for the progress bar
    
    // Progress bar styling reference
    private ProgressBarStyling progressBarStyling; // Reference to the custom UI for animation updates
    
    /**
     * Initialize the pre-startup progress window
     */
    public PreStartupProgressWindow() {
        System.out.println("🚀 Creating pre-startup progress window...");
        
        // Step 1: Create and configure the main JFrame with transparency
        createAndConfigureFrame();
        
        // Step 2: Create title label with styling
        JLabel titleLabel = createTitleLabel();
        
        // Step 3: Create subtitle label with styling
        JLabel subtitleLabel = createSubtitleLabel();
        
        // Step 4: Create progress bar with basic properties
        createProgressBar();
        
        // Step 5: Apply custom ultra-modern progress bar styling
        applyCustomProgressBarStyling();
        
        // Step 6: Create status label with styling
        createStatusLabel();
        
        // Step 7: Create and configure the main panel layout
        JPanel mainPanel = createMainPanel();
        
        // Step 8: Add all components to the panel with proper spacing
        addComponentsToPanel(mainPanel, titleLabel, subtitleLabel);
        
        // Step 9: Set the main panel as the frame's content pane
        progressFrame.setContentPane(mainPanel);
        
        // Step 10: Pack and center the window on screen
        packAndCenterWindow();
        
        System.out.println("✅ Pre-startup progress window created");
    }
    
    /**
     * Step 1: Create and configure the main JFrame with transparency
     */
    private void createAndConfigureFrame() {
        progressFrame = new JFrame("OMG Game Development Kit");
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        progressFrame.setResizable(false);
        progressFrame.setAlwaysOnTop(true);
        progressFrame.setUndecorated(true);
        
        // Enable window transparency for shadow effects
        try {
            progressFrame.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception e) {
            // Fallback for systems that don't support transparency
            progressFrame.setBackground(new Color(248, 249, 250));
        }
    }
    
    /**
     * Step 2: Create title label with styling
     */
    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("GDK Game Development Kit");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return titleLabel;
    }
    
    /**
     * Step 3: Create subtitle label with styling
     */
    private JLabel createSubtitleLabel() {
        JLabel subtitleLabel = new JLabel("Initializing");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(149, 165, 166));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return subtitleLabel;
    }
    
    /**
     * Step 4: Create progress bar with basic properties
     */
    private void createProgressBar() {
        progressBar = new JProgressBar(0, totalSteps);
        progressBar.setPreferredSize(new Dimension(500, 25));
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(false);
    }
    
    /**
     * Step 5: Apply custom ultra-modern progress bar styling
     */
    private void applyCustomProgressBarStyling() {
        ProgressBarStyling styling = new ProgressBarStyling();
        progressBar.setUI(styling);
        
        // Store reference to the UI for animation updates
        this.progressBarStyling = styling;
    }
    
    /**
     * Step 6: Create status label with styling
     */
    private void createStatusLabel() {
        statusLabel = new JLabel("Starting up...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(52, 73, 94));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /**
     * Step 7: Create and configure the main panel layout
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        mainPanel.setBackground(new Color(255, 255, 255));
        return mainPanel;
    }
    
    /**
     * Step 8: Add all components to the panel with proper spacing
     */
    private void addComponentsToPanel(JPanel mainPanel, JLabel titleLabel, JLabel subtitleLabel) {
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createCenteredComponent(titleLabel));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(createCenteredComponent(subtitleLabel));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createCenteredComponent(progressBar));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createCenteredComponent(statusLabel));
        mainPanel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * Step 10: Pack and center the window on screen
     */
    private void packAndCenterWindow() {
        progressFrame.pack();
        progressFrame.setLocationRelativeTo(null);
    }
    
    /**
     * Create a centered component
     */
    private JPanel createCenteredComponent(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(255, 255, 255));
        panel.add(component);
        return panel;
    }
    
    /**
     * Show the progress window
     */
    /**
     * Show the progress window
     */
    public void show() {
        System.out.println("🎬 Showing pre-startup progress window");
        
        // Show on EDT to ensure thread safety
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(true);
            });
        }
    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        System.out.println("🏁 Hiding pre-startup progress window");
        
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(false);
            progressFrame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(false);
                progressFrame.dispose();
            });
        }
    }
    
    /**
     * Update progress and status
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(step);
            progressBar.setString(step + "/" + totalSteps + " (" + (step * 100 / totalSteps) + "%)");
            statusLabel.setText(status);
            
            System.out.println("📊 Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    
    /**
     * Set the total number of steps
     * @param totalSteps The total number of initialization steps
     */
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(totalSteps);
        });
    }
    
    /**
     * Get the progress frame for potential JavaFX integration
     * @return The progress frame
     */
    public JFrame getProgressFrame() {
        return progressFrame;
    }
    
    /**
     * Update the status text without animation.
     * 
     * @param text The text to display
     */
    public void updateStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }
    
    /**
     * Get the progress bar styling instance.
     * 
     * @return The progress bar styling instance
     */
    public ProgressBarStyling getProgressBarStyling() {
        return progressBarStyling;
    }
    
    /**
     * Repaint the progress bar.
     */
    public void repaintProgressBar() {
        if (progressBar != null) {
            progressBar.repaint();
        }
    }
    
    /**
     * Check if the window is currently visible.
     * 
     * @return true if the window is visible, false otherwise
     */
    public boolean isVisible() {
        return progressFrame != null && progressFrame.isVisible();
    }
} 