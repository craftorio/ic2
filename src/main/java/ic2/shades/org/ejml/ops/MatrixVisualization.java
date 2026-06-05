package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.D1Matrix64F;
import javax.swing.JFrame;

public class MatrixVisualization {
   public static void show(D1Matrix64F A, String title) {
      JFrame frame = new JFrame(title);
      int width = 300;
      int height = 300;
      if (A.numRows > A.numCols) {
         width = width * A.numCols / A.numRows;
      } else {
         height = height * A.numRows / A.numCols;
      }

      MatrixComponent panel = new MatrixComponent(width, height);
      panel.setMatrix(A);
      frame.add(panel, "Center");
      frame.pack();
      frame.setVisible(true);
   }
}
