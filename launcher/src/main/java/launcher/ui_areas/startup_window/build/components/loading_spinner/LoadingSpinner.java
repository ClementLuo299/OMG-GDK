package launcher.ui_areas.startup_window.build.components;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import launcher.ui_areas.startup_window.styling.theme.Colors;

/**
 * A circular loading spinner component with smooth rotation animation.
 * 
 * This component displays an animated circular arc that rotates continuously
 * to indicate loading progress.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class LoadingSpinner extends JComponent {
    
    /** The size of the spinner in pixels (width and height) */
    private static final int SPINNER_SIZE = 48;
    
    /** The width of the arc stroke in pixels */
    private static final int STROKE_WIDTH = 4;
    
    /** Animation delay in milliseconds (~60 FPS) */
    private static final int ANIMATION_DELAY_MS = 16;
    
    /** Current rotation angle in degrees (0-360) */
    private double rotationAngle = 0.0;
    
    /** Timer that drives the animation */
    private Timer animationTimer;
    
    /**
     * Constructs a new LoadingSpinner component.
     * 
     * Initializes the component with fixed size, opaque background (to prevent flickering),
     * and sets up the animation timer that rotates the spinner continuously.
     */
    public LoadingSpinner() {
        // Set fixed size for the spinner (square component)
        setPreferredSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        setMinimumSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        setMaximumSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        
        // Make opaque with background matching parent panel to prevent flickering
        // This avoids transparency issues that can cause visual artifacts during animation
        setOpaque(true);
        setBackground(Colors.BACKGROUND);
        
        // Enable double buffering for smooth animation
        setDoubleBuffered(true);
        
        // Create animation timer that updates rotation angle and triggers repaint
        animationTimer = new Timer(ANIMATION_DELAY_MS, e -> {
            // Rotate 8 degrees per frame (360 / 45 = 8, so 45 frames per full rotation)
            rotationAngle += 8.0;
            if (rotationAngle >= 360.0) {
                rotationAngle -= 360.0; // Keep angle in 0-360 range
            }
            repaint(); // Trigger repaint to show new rotation
        });
    }
    
    /**
     * Starts the spinner animation.
     * 
     * Begins the continuous rotation animation. Safe to call multiple times;
     * will only start if not already running.
     */
    public void start() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
    
    /**
     * Stops the spinner animation.
     * 
     * Halts the rotation animation. Safe to call multiple times;
     * will only stop if currently running.
     */
    public void stop() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    /**
     * Paints the spinner component.
     * 
     * First paints the background, then draws
     * the rotating arc with a gradient from purple to blue. The arc is drawn
     * at the current rotation angle and covers 270 degrees, leaving a 90-degree
     * gap for visual clarity.
     * 
     * @param g The graphics context to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Paint the background first (since we're now opaque)
        super.paintComponent(g);
        
        // Create a copy of the graphics context to avoid modifying the original
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Set up rendering hints for smooth, high-quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int width = getWidth();
            int height = getHeight();
            
            // Early return if component has no size
            if (width <= 0 || height <= 0) {
                return;
            }
            
            // Calculate center point and radius
            int centerX = width / 2;
            int centerY = height / 2;
            // Radius accounts for stroke width to keep arc within bounds
            int radius = Math.min(width, height) / 2 - STROKE_WIDTH;
            
            // Transform graphics context: translate to center, then rotate
            // This allows us to draw the arc at a fixed position and rotate it
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(rotationAngle));
            
            // Create gradient paint from purple to blue
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                -radius, -radius, Colors.PROGRESS_START,  // Start color (purple) at top-left
                radius, radius, Colors.PROGRESS_END        // End color (blue) at bottom-right
            );
            g2d.setPaint(gradient);
            
            // Set stroke with rounded caps and joins for smooth appearance
            g2d.setStroke(new java.awt.BasicStroke(
                STROKE_WIDTH,
                java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND
            ));
            
            // Draw arc: 270 degrees starting at 0 degrees (top), leaving 90-degree gap
            Arc2D arc = new Arc2D.Double(
                -radius, -radius,  // Top-left corner of bounding rectangle
                radius * 2, radius * 2,  // Width and height of bounding rectangle
                0, 270,  // Start angle (0 = top) and extent (270 degrees)
                Arc2D.OPEN  // Open arc (not closed)
            );
            g2d.draw(arc);
        } finally {
            // Always dispose of the graphics context copy
            g2d.dispose();
        }
    }
    
    /**
     * Called when the component is added to a container.
     * 
     * Note: We intentionally do NOT auto-start the spinner here. The spinner
     * should only start after the window is fully visible to prevent it from
     * appearing before the startup window background is painted.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // Don't auto-start here - wait for explicit start() call after window is visible
    }
    
    /**
     * Called when the component is removed from a container.
     * 
     * Automatically stops the animation when the component is removed to
     * prevent resource leaks and unnecessary processing.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        stop();
    }
}

