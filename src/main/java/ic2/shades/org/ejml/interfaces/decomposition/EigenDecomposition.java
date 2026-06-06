package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.Matrix64F;

public interface EigenDecomposition<MatrixType extends Matrix64F> extends DecompositionInterface<MatrixType>
{
	int getNumberOfEigenvalues();

	Complex64F getEigenvalue(int var1);

	MatrixType getEigenVector(int var1);
}
