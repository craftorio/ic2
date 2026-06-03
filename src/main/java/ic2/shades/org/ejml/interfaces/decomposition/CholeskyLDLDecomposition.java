package ic2.shades.org.ejml.interfaces.decomposition;

public interface CholeskyLDLDecomposition<MatrixType extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<MatrixType> {
  MatrixType getL(MatrixType paramMatrixType);
  
  double[] getDiagonal();
  
  MatrixType getD(MatrixType paramMatrixType);
}
