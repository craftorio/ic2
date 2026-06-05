package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface SingularValueDecomposition<T extends Matrix64F> extends DecompositionInterface<T> {
   double[] getSingularValues();

   int numberOfSingularValues();

   boolean isCompact();

   T getU(T var1, boolean var2);

   T getV(T var1, boolean var2);

   T getW(T var1);

   int numRows();

   int numCols();
}
