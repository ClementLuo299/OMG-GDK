package launcher.ui_areas.startup_window.loading_spinner;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import launcher.ui_areas.startup_window.styling_theme.Colors;
import launcher.ui_areas.startup_window.styling_theme.LoadingSpinnerStyle;

/**
 * A circular load_modules spinner component with smooth rotation animation.
 * 
 * This component is an animated circular arc that rotates continuously
 * to indicate load_modules progress. The component handles only the visual rendering;
 * animation control is managed by LoadingSpinnerController.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class LoadingSpinner extends JComponent {
    
    /** Current rotation angle in degrees (0-360) */
    private double rotationAngle = 0.0;
    
    /**
     * Sets the rotation angle for the spinner.
     * 
     * Called by LoadingSpinnerController to update the rotation angle
     * during animation.
     * 
     * @param angle The rotation angle in degrees (0-360)
     */
    void setRotationAngle(double angle) {
        this.rotationAngle = angle;
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
            int radius = Math.min(width, height) / 2 - LoadingSpinnerStyle.STROKE_WIDTH;
            
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
                LoadingSpinnerStyle.STROKE_WIDTH,
                java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND
            ));
            
            // Draw arc using constants for init angle and extent
            Arc2D arc = new Arc2D.Double(
                -radius, -radius,  // Top-left corner of bounding rectangle
                radius * 2, radius * 2,  // Width and height of bounding rectangle
                LoadingSpinnerStyle.ARC_START_ANGLE, LoadingSpinnerStyle.ARC_EXTENT,  // Start angle and extent
                Arc2D.OPEN  // Open arc (not closed)
            );
            g2d.draw(arc);
        } finally {
            // Always dispose of the graphics context copy
            g2d.dispose();
        }
    }
}

