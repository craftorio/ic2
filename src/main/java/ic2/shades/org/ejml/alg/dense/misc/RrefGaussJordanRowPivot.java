package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.ReducedRowEchelonForm;

public class RrefGaussJordanRowPivot implements ReducedRowEchelonForm<DenseMatrix64F> {
   double tol;

   @Override
   public void setTolerance(double tol) {
      this.tol = tol;
   }

   public void reduce(DenseMatrix64F A, int coefficientColumns) {
      if (A.numCols < coefficientColumns) {
         throw new IllegalArgumentException("The system must be at least as wide as A");
      }

      int leadIndex = 0;

      for (int i = 0; i < coefficientColumns; i++) {
         int pivotRow = -1;
         double maxValue = this.tol;

         for (int row = leadIndex; row < A.numRows; row++) {
            double v = Math.abs(A.data[row * A.numCols + i]);
            if (v > maxValue) {
               maxValue = v;
               pivotRow = row;
            }
         }

         if (pivotRow != -1) {
            if (leadIndex != pivotRow) {
               swapRows(A, leadIndex, pivotRow);
            }

            for (int row = 0; row < A.numRows; row++) {
               if (row != leadIndex) {
                  int indexPivot = leadIndex * A.numCols + i;
                  int indexTarget = row * A.numCols + i;
                  double alpha = A.data[indexTarget] / A.data[indexPivot++];
                  A.data[indexTarget++] = 0.0;

                  for (int col = i + 1; col < A.numCols; col++) {
                     int var22 = indexTarget++;
                     A.data[var22] = A.data[var22] - A.data[indexPivot++] * alpha;
                  }
               }
            }

            int indexPivot = leadIndex * A.numCols + i;
            double alpha = 1.0 / A.data[indexPivot];
            A.data[indexPivot++] = 1.0;

            for (int col = i + 1; col < A.numCols; col++) {
               int var24 = indexPivot++;
               A.data[var24] = A.data[var24] * alpha;
            }

            leadIndex++;
         }
      }
   }

   protected static void swapRows(DenseMatrix64F A, int rowA, int rowB) {
      int indexA = rowA * A.numCols;
      int indexB = rowB * A.numCols;

      for (int i = 0; i < A.numCols; indexB++) {
         double temp = A.data[indexA];
         A.data[indexA] = A.data[indexB];
         A.data[indexB] = temp;
         i++;
         indexA++;
      }
   }
}
