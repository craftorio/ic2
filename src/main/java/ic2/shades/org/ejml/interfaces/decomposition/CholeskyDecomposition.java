package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface CholeskyDecomposition<MatrixType extends Matrix64F> extends DecompositionInterface<MatrixType>
{
	boolean isLower();

	MatrixType getT(MatrixType var1);
}
