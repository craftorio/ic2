package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.ReducedRowEchelonForm;

public class RrefGaussJordanRowPivot implements ReducedRowEchelonForm<DenseMatrix64F> {
  double tol;
  
  public void setTolerance(double tol) {
    this.tol = tol;
  }
  
  public void reduce(DenseMatrix64F A, int coefficientColumns) {
    if (A.numCols < coefficientColumns)
      throw new IllegalArgumentException("The system must be at least as wide as A"); 
    int leadIndex = 0;
    for (int i = 0; i < coefficientColumns; i++) {
      int pivotRow = -1;
      double maxValue = this.tol;
      int row;
      for (row = leadIndex; row < A.numRows; row++) {
        double v = Math.abs(A.data[row * A.numCols + i]);
        if (v > maxValue) {
          maxValue = v;
          pivotRow = row;
        } 
      } 
      if (pivotRow != -1) {
        if (leadIndex != pivotRow)
          swapRows(A, leadIndex, pivotRow); 
        for (row = 0; row < A.numRows; row++) {
          if (row != leadIndex) {
            int j = leadIndex * A.numCols + i;
            int indexTarget = row * A.numCols + i;
            double d = A.data[indexTarget] / A.data[j++];
            A.data[indexTarget++] = 0.0D;
            for (int k = i + 1; k < A.numCols; k++)
              A.data[indexTarget++] = A.data[indexTarget++] - A.data[j++] * d; 
          } 
        } 
        int indexPivot = leadIndex * A.numCols + i;
        double alpha = 1.0D / A.data[indexPivot];
        A.data[indexPivot++] = 1.0D;
        for (int col = i + 1; col < A.numCols; col++)
          A.data[indexPivot++] = A.data[indexPivot++] * alpha; 
        leadIndex++;
      } 
    } 
  }
  
  protected static void swapRows(DenseMatrix64F A, int rowA, int rowB) {
    int indexA = rowA * A.numCols;
    int indexB = rowB * A.numCols;
    for (int i = 0; i < A.numCols; i++, indexA++, indexB++) {
      double temp = A.data[indexA];
      A.data[indexA] = A.data[indexB];
      A.data[indexB] = temp;
    } 
  }
}
