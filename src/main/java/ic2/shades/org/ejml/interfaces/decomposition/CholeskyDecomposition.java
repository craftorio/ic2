package ic2.shades.org.ejml.interfaces.decomposition;

public interface CholeskyDecomposition<MatrixType extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<MatrixType> {
  boolean isLower();
  
  MatrixType getT(MatrixType paramMatrixType);
}
