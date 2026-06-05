package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface QRDecomposition<T extends Matrix64F> extends DecompositionInterface<T> {
   T getQ(T var1, boolean var2);

   T getR(T var1, boolean var2);
}
