package ic2.shades.org.ejml.interfaces.linsol;

import ic2.shades.org.ejml.data.Matrix64F;

public interface ReducedRowEchelonForm<T extends Matrix64F>
{
	void reduce(T var1, int var2);

	void setTolerance(double var1);
}
