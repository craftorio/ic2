package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface CholeskyLDLDecomposition<MatrixType extends Matrix64F> extends DecompositionInterface<MatrixType> {
   MatrixType getL(MatrixType var1);

   double[] getDiagonal();

   MatrixType getD(MatrixType var1);
}
