package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.ops.SingularOps;

public class SimpleSVD<T extends SimpleMatrix>
{
	private SingularValueDecomposition<DenseMatrix64F> svd;
	private T U;
	private T W;
	private T V;
	private final DenseMatrix64F mat;

	public SimpleSVD(DenseMatrix64F mat, boolean compact)
	{
		this.mat = mat;
		this.svd = DecompositionFactory.svd(mat.numRows, mat.numCols, true, true, compact);
		if (!this.svd.decompose(mat))
		{
			throw new RuntimeException("Decomposition failed");
		}

		this.U = (T) SimpleMatrix.wrap(this.svd.getU(null, false));
		this.W = (T) SimpleMatrix.wrap(this.svd.getW(null));
		this.V = (T) SimpleMatrix.wrap(this.svd.getV(null, false));
		SingularOps.descendingOrder(this.U.getMatrix(), false, this.W.getMatrix(), this.V.getMatrix(), false);
	}

	public T getU()
	{
		return this.U;
	}

	public T getW()
	{
		return this.W;
	}

	public T getV()
	{
		return this.V;
	}

	public double quality()
	{
		return DecompositionFactory.quality(this.mat, this.U.getMatrix(), this.W.getMatrix(), this.V.transpose().getMatrix());
	}

	public SimpleMatrix nullSpace()
	{
		return SimpleMatrix.wrap(SingularOps.nullSpace(this.svd, null, UtilEjml.EPS));
	}

	public double getSingleValue(int index)
	{
		return this.W.get(index, index);
	}

	public int rank()
	{
		return SingularOps.rank(this.svd, 10.0 * UtilEjml.EPS);
	}

	public int nullity()
	{
		return SingularOps.nullity(this.svd, 10.0 * UtilEjml.EPS);
	}

	public SingularValueDecomposition getSVD()
	{
		return this.svd;
	}
}
