package launcher.lifecycle.start.startup_window.styling;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom flat and minimal progress bar UI with modern styling.
 */
public class ProgressBarStyling extends BasicProgressBarUI {
    
    /** Smooth progress value (0.0 to 1.0) used for animation instead of progressBar.getPercentComplete(). */
    private Double smoothProgress = null;
    
    /**
     * Sets the smooth progress value for animation.
     * When set, this overrides the progress bar's internal percentage calculation.
     * 
     * @param progress The progress value (0.0 to 1.0), or null to use default behavior
     */
    public void setSmoothProgress(Double progress) {
        this.smoothProgress = progress;
    }
    
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Get component bounds directly - no inset manipulation
            Rectangle bounds = c.getBounds();
            int width = bounds.width;
            int height = bounds.height;
            
            // Skip rendering if component has invalid dimensions
            if (width <= 0 || height <= 0) {
                return;
            }
            
            // Set component-level clip: ALL drawing is constrained to component bounds
            // Use the component's actual bounds (0, 0, width, height)
            Shape originalClip = g2d.getClip();
            Rectangle componentClip = new Rectangle(0, 0, width, height);
            Area componentClipArea = new Area(componentClip);
            if (originalClip != null) {
                componentClipArea.intersect(new Area(originalClip));
            }
            g2d.setClip(componentClipArea);
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Modern flat background
            paintModernBackground(g2d, width, height);
            
            // Use smooth progress if available, otherwise use standard progress bar percentage
            double progressPercent = (smoothProgress != null) ? smoothProgress : progressBar.getPercentComplete();
            
            // Add a small initial prefill (equivalent to ~2% or minimum 4 pixels) to prevent overflow
            // This ensures the progress bar always has a small fill from the start
            double minProgress = Math.max(0.02, 4.0 / width);
            double adjustedProgress = Math.max(progressPercent, minProgress);
            
            // Calculate progress width and ensure it never exceeds width
            int progressWidth = Math.min((int) (width * adjustedProgress), width);
            
            // Always draw progress fill (it will always be at least the minimum)
            paintModernProgressFill(g2d, progressWidth, width, height);
            
            // Clean, minimal border
            paintModernBorder(g2d, width, height);
            
            // Restore original clip
            g2d.setClip(originalClip);
        } finally {
            g2d.dispose();
        }
    }
    
    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        paintDeterminate(g, c);
    }
    
    private int getArcSize(int height) {
        // For perfect pill shape, arc height should equal the full height
        // This creates maximum rounded ends
        return height;
    }
    
    private void paintModernBackground(Graphics2D g2d, int width, int height) {
        // Modern flat background - subtle gray with slight gradient
        int arcSize = getArcSize(height);
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(248, 248, 248),
            0, height, new Color(242, 242, 242)
        );
        g2d.setPaint(bgGradient);
        // Perfect pill shape: arc height equals full height
        g2d.fillRoundRect(0, 0, width, height, arcSize, arcSize);
    }
    
    private void paintModernProgressFill(Graphics2D g2d, int progressWidth, int totalWidth, int height) {
        // Skip if dimensions are invalid
        if (progressWidth <= 0 || totalWidth <= 0 || height <= 0) {
            return;
        }
        
        // Double-check: ensure progress width never exceeds total width
        int actualProgressWidth = Math.min(progressWidth, totalWidth);
        int arcSize = getArcSize(height);
        
        // Save current clip
        Shape currentClip = g2d.getClip();
        
        // Create a STRICT clip that is exactly the progress bounds
        Rectangle progressBounds = new Rectangle(0, 0, actualProgressWidth, height);
        Area progressClipArea = new Area(progressBounds);
        if (currentClip != null) {
            progressClipArea.intersect(new Area(currentClip));
        }
        g2d.setClip(progressClipArea);
        
        // Build a shape with rounded left corners using GeneralPath
        // This gives us precise control to ensure it never exceeds bounds
        GeneralPath fillPath = new GeneralPath();
        
        double radius = height / 2.0;
        
        if (actualProgressWidth >= height) {
            // Progress is wide enough for rounded left corners
            // Build the path point by point to ensure it's always within bounds
            
            // Start at top-left, just below the rounded corner
            fillPath.moveTo(0, radius);
            
            // Draw left semicircle (top half) - using arc
            fillPath.quadTo(0, 0, radius, 0);
            
            // Top edge (straight line to right)
            fillPath.lineTo(actualProgressWidth, 0);
            
            // Right edge (straight line down)
            fillPath.lineTo(actualProgressWidth, height);
            
            // Bottom edge (straight line back to left)
            fillPath.lineTo(radius, height);
            
            // Draw left semicircle (bottom half)
            fillPath.quadTo(0, height, 0, height - radius);
            
            // Close the path
            fillPath.closePath();
        } else {
            // Progress is too narrow - use a simple rectangle
            fillPath.moveTo(0, 0);
            fillPath.lineTo(actualProgressWidth, 0);
            fillPath.lineTo(actualProgressWidth, height);
            fillPath.lineTo(0, height);
            fillPath.closePath();
        }
        
        // Intersect the path with progress bounds to ensure no overflow
        Area fillArea = new Area(fillPath);
        Area boundsArea = new Area(progressBounds);
        fillArea.intersect(boundsArea);
        Shape fillShape = fillArea;
        
        // Modern indigo/violet gradient
        int gradientEnd = Math.max(actualProgressWidth, 1);
        GradientPaint mainGradient = new GradientPaint(
            0, 0, new Color(99, 102, 241), // Modern indigo/violet
            gradientEnd, 0, new Color(67, 56, 202)  // Deeper indigo
        );
        g2d.setPaint(mainGradient);
        
        // Fill the shape - the clip ensures it cannot overflow
        g2d.fill(fillShape);
        
        // Restore clip
        g2d.setClip(currentClip);
    }
    
    private void paintModernBorder(Graphics2D g2d, int width, int height) {
        // Clean, minimal border - single subtle stroke
        int arcSize = getArcSize(height);
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);
    }
}

