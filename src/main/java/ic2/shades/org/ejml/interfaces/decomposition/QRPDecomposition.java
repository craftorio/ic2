package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;

public interface QRPDecomposition<T extends Matrix64F> extends QRDecomposition<T> {
   void setSingularThreshold(double var1);

   int getRank();

   int[] getPivots();

   DenseMatrix64F getPivotMatrix(DenseMatrix64F var1);
}
