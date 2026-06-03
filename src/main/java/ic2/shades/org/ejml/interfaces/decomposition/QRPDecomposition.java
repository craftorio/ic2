package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public interface QRPDecomposition<T extends ic2.shades.org.ejml.data.Matrix64F> extends QRDecomposition<T> {
  void setSingularThreshold(double paramDouble);
  
  int getRank();
  
  int[] getPivots();
  
  DenseMatrix64F getPivotMatrix(DenseMatrix64F paramDenseMatrix64F);
}
