package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public interface TridiagonalSimilarDecomposition<MatrixType extends ReshapeMatrix64F> extends DecompositionInterface<MatrixType>
{
	MatrixType getT(MatrixType var1);

	MatrixType getQ(MatrixType var1, boolean var2);

	void getDiagonal(double[] var1, double[] var2);
}
