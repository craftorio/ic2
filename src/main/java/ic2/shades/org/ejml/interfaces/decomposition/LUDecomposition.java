package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface LUDecomposition<T extends Matrix64F> extends DecompositionInterface<T>
{
	T getLower(T var1);

	T getUpper(T var1);

	T getPivot(T var1);

	boolean isSingular();

	double computeDeterminant();
}
