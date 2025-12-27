package launcher.lifecycle.start.startup_window.styling.components;

import javax.swing.JLabel;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * A modern JLabel with support for letter spacing (tracking).
 * Uses AttributedString to apply typography enhancements.
 * 
 * @author Clement Luo
 * @date December 24, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class StyledLabel extends JLabel {
    
    private float letterSpacing = 0.0f;
    private String originalText = "";
    
    public StyledLabel(String text) {
        super(text);
        this.originalText = text;
    }
    
    public void setLetterSpacing(float spacing) {
        this.letterSpacing = spacing;
        repaint();
    }
    
    public float getLetterSpacing() {
        return letterSpacing;
    }
    
    @Override
    public void setText(String text) {
        this.originalText = text;
        super.setText(text);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Always use high-quality rendering for modern look
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Apply high-quality rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            if (originalText != null && !originalText.isEmpty()) {
                // Create attributed string with font and color
                AttributedString attributed = new AttributedString(originalText);
                attributed.addAttribute(TextAttribute.FONT, getFont());
                attributed.addAttribute(TextAttribute.FOREGROUND, getForeground());
                
                // Add letter spacing if specified
                if (letterSpacing != 0.0f) {
                    attributed.addAttribute(TextAttribute.TRACKING, letterSpacing);
                }
                
                // Calculate text position for centering
                FontMetrics fm = g2d.getFontMetrics(getFont());
                
                // Calculate text width - letter spacing affects width, so we need to measure the attributed string
                int textWidth;
                if (letterSpacing != 0.0f) {
                    // Measure the attributed string directly
                    java.awt.font.TextLayout layout = new java.awt.font.TextLayout(
                        attributed.getIterator(), g2d.getFontRenderContext()
                    );
                    textWidth = (int) layout.getAdvance();
                } else {
                    textWidth = fm.stringWidth(originalText);
                }
                
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Draw the attributed string
                g2d.drawString(attributed.getIterator(), x, y);
            } else {
                // Fallback to default rendering if no text
                super.paintComponent(g);
            }
        } finally {
            g2d.dispose();
        }
    }
}

