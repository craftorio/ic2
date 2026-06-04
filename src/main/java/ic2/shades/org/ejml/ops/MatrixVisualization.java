// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import java.awt.Component;
import javax.swing.JFrame;
import ic2.shades.org.ejml.data.D1Matrix64F;

public class MatrixVisualization
{
    public static void show(final D1Matrix64F A, final String title) {
        final JFrame frame = new JFrame(title);
        int width = 300;
        int height = 300;
        if (A.numRows > A.numCols) {
            width = width * A.numCols / A.numRows;
        }
        else {
            height = height * A.numRows / A.numCols;
        }
        final MatrixComponent panel = new MatrixComponent(width, height);
        panel.setMatrix(A);
        frame.add(panel, "Center");
        frame.pack();
        frame.setVisible(true);
    }
}
