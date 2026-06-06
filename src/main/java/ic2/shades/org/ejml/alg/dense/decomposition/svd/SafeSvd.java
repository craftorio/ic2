package ic2.shades.org.ejml.alg.dense.decomposition.svd;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class SafeSvd implements SingularValueDecomposition<DenseMatrix64F>
{
	final SingularValueDecomposition<DenseMatrix64F> alg;
	final DenseMatrix64F work = new DenseMatrix64F(1, 1);

	public SafeSvd(SingularValueDecomposition<DenseMatrix64F> alg)
	{
		this.alg = alg;
	}

	@Override
	public double[] getSingularValues()
	{
		return this.alg.getSingularValues();
	}

	@Override
	public int numberOfSingularValues()
	{
		return this.alg.numberOfSingularValues();
	}

	@Override
	public boolean isCompact()
	{
		return this.alg.isCompact();
	}

	public DenseMatrix64F getU(DenseMatrix64F U, boolean transposed)
	{
		return this.alg.getU(U, transposed);
	}

	public DenseMatrix64F getV(DenseMatrix64F V, boolean transposed)
	{
		return this.alg.getV(V, transposed);
	}

	public DenseMatrix64F getW(DenseMatrix64F W)
	{
		return this.alg.getW(W);
	}

	@Override
	public int numRows()
	{
		return this.alg.numRows();
	}

	@Override
	public int numCols()
	{
		return this.alg.numCols();
	}

	public boolean decompose(DenseMatrix64F orig)
	{
		if (this.alg.inputModified())
		{
			this.work.reshape(orig.numRows, orig.numCols);
			this.work.set(orig);
			return this.alg.decompose(this.work);
		} else
		{
			return this.alg.decompose(orig);
		}
	}

	@Override
	public boolean inputModified()
	{
		return false;
	}
}
