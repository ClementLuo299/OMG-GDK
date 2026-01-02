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
 * @author Clement Luo
 * @date January 2025
 * @since Beta 1.0
 */
public class LoadingSpinner extends JComponent {
    
    private static final int SPINNER_SIZE = 48;
    private static final int STROKE_WIDTH = 4;
    private static final int ANIMATION_DELAY_MS = 16; // ~60 FPS
    
    private double rotationAngle = 0.0;
    private Timer animationTimer;
    
    public LoadingSpinner() {
        setPreferredSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        setMinimumSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        setMaximumSize(new java.awt.Dimension(SPINNER_SIZE, SPINNER_SIZE));
        setOpaque(true); // Make opaque to prevent flickering
        setBackground(Colors.BACKGROUND); // Match parent panel background
        setDoubleBuffered(true); // Enable double buffering to prevent flickering
        
        // Create animation timer
        animationTimer = new Timer(ANIMATION_DELAY_MS, e -> {
            rotationAngle += 8.0; // Rotate 8 degrees per frame
            if (rotationAngle >= 360.0) {
                rotationAngle -= 360.0;
            }
            repaint();
        });
    }
    
    /**
     * Starts the spinner animation.
     */
    public void start() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
    
    /**
     * Stops the spinner animation.
     */
    public void stop() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Paint the background first (since we're now opaque)
        super.paintComponent(g);
        
        // Create a copy of the graphics to avoid modifying the original
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Set up rendering hints for smooth rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int width = getWidth();
            int height = getHeight();
            
            if (width <= 0 || height <= 0) {
                return;
            }
            
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 2 - STROKE_WIDTH;
            
            // Translate to center for rotation
            g2d.translate(centerX, centerY);
            g2d.rotate(Math.toRadians(rotationAngle));
            
            // Draw the spinner arc
            // Create a gradient from purple to blue
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                -radius, -radius, Colors.PROGRESS_START,
                radius, radius, Colors.PROGRESS_END
            );
            g2d.setPaint(gradient);
            g2d.setStroke(new java.awt.BasicStroke(STROKE_WIDTH, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            
            // Draw arc (270 degrees, leaving a gap)
            Arc2D arc = new Arc2D.Double(
                -radius, -radius,
                radius * 2, radius * 2,
                0, 270,
                Arc2D.OPEN
            );
            g2d.draw(arc);
        } finally {
            g2d.dispose();
        }
    }
    
    
    @Override
    public void addNotify() {
        super.addNotify();
        // Don't auto-start here - wait for explicit start() call after window is visible
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        stop();
    }
}

