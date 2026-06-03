package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Complex64F;

public interface EigenDecomposition<MatrixType extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<MatrixType> {
  int getNumberOfEigenvalues();
  
  Complex64F getEigenvalue(int paramInt);
  
  MatrixType getEigenVector(int paramInt);
}
