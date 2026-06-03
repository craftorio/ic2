package ic2.shades.org.ejml.interfaces.decomposition;

public interface TridiagonalSimilarDecomposition<MatrixType extends ic2.shades.org.ejml.data.ReshapeMatrix64F> extends DecompositionInterface<MatrixType> {
  MatrixType getT(MatrixType paramMatrixType);
  
  MatrixType getQ(MatrixType paramMatrixType, boolean paramBoolean);
  
  void getDiagonal(double[] paramArrayOfdouble1, double[] paramArrayOfdouble2);
}
