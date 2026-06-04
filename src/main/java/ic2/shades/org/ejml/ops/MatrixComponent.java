// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import java.awt.image.ImageObserver;
import java.awt.Image;
import java.awt.Graphics;
import ic2.shades.org.ejml.data.D1Matrix64F;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class MatrixComponent extends JPanel
{
    BufferedImage image;
    
    public MatrixComponent(final int width, final int height) {
        this.image = new BufferedImage(width, height, 1);
        this.setPreferredSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(width, height));
    }
    
    public synchronized void setMatrix(final D1Matrix64F A) {
        final double maxValue = CommonOps.elementMaxAbs(A);
        renderMatrix(A, this.image, maxValue);
        this.repaint();
    }
    
    public static void renderMatrix(final D1Matrix64F M, final BufferedImage image, final double maxValue) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        final double widthStep = M.numCols / (double)image.getWidth();
        final double heightStep = M.numRows / (double)image.getHeight();
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                final double value = M.get((int)(i * heightStep), (int)(j * widthStep));
                if (value == 0.0) {
                    image.setRGB(j, i, -16777216);
                }
                else if (value > 0.0) {
                    final int p = 255 - (int)(255.0 * (value / maxValue));
                    final int rgb = 0xFFFF0000 | p << 8 | p;
                    image.setRGB(j, i, rgb);
                }
                else {
                    final int p = 255 + (int)(255.0 * (value / maxValue));
                    final int rgb = 0xFF000000 | p << 16 | p << 8 | 0xFF;
                    image.setRGB(j, i, rgb);
                }
            }
        }
    }
    
    @Override
    public synchronized void paint(final Graphics g) {
        g.drawImage(this.image, 0, 0, this);
    }
}
