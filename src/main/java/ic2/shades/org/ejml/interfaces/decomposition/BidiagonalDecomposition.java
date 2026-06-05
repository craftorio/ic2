package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public interface BidiagonalDecomposition<T extends ReshapeMatrix64F> extends DecompositionInterface<T> {
   T getB(T var1, boolean var2);

   T getU(T var1, boolean var2, boolean var3);

   T getV(T var1, boolean var2, boolean var3);

   void getDiagonal(double[] var1, double[] var2);
}
