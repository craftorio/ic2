package ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.BidiagonalDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class BidiagonalDecompositionTall_D64 implements BidiagonalDecomposition<DenseMatrix64F>
{
	final QRPDecomposition<DenseMatrix64F> decompQRP = DecompositionFactory.qrp(500, 100);
	final BidiagonalDecomposition<DenseMatrix64F> decompBi = new BidiagonalDecompositionRow_D64();
	final DenseMatrix64F B = new DenseMatrix64F(1, 1);
	int m;
	int n;
	int min;

	@Override
	public void getDiagonal(double[] diag, double[] off)
	{
		diag[0] = this.B.get(0);

		for (int i = 1; i < this.n; i++)
		{
			diag[i] = this.B.unsafe_get(i, i);
			off[i - 1] = this.B.unsafe_get(i - 1, i);
		}
	}

	public DenseMatrix64F getB(DenseMatrix64F B, boolean compact)
	{
		B = BidiagonalDecompositionRow_D64.handleB(B, compact, this.m, this.n, this.min);
		B.set(0, 0, this.B.get(0, 0));

		for (int i = 1; i < this.min; i++)
		{
			B.set(i, i, this.B.get(i, i));
			B.set(i - 1, i, this.B.get(i - 1, i));
		}

		if (this.n > this.m)
		{
			B.set(this.min - 1, this.min, this.B.get(this.min - 1, this.min));
		}

		return B;
	}

	public DenseMatrix64F getU(DenseMatrix64F U, boolean transpose, boolean compact)
	{
		U = BidiagonalDecompositionRow_D64.handleU(U, false, compact, this.m, this.n, this.min);
		if (compact)
		{
			DenseMatrix64F Q1 = this.decompQRP.getQ(null, true);
			DenseMatrix64F U1 = this.decompBi.getU(null, false, true);
			CommonOps.mult(Q1, U1, U);
		} else
		{
			DenseMatrix64F Q = this.decompQRP.getQ(U, false);
			DenseMatrix64F U1 = this.decompBi.getU(null, false, true);
			DenseMatrix64F Q1 = CommonOps.extract(Q, 0, Q.numRows, 0, this.min);
			DenseMatrix64F tmp = new DenseMatrix64F(Q1.numRows, U1.numCols);
			CommonOps.mult(Q1, U1, tmp);
			CommonOps.insert(tmp, Q, 0, 0);
		}

		if (transpose)
		{
			CommonOps.transpose(U);
		}

		return U;
	}

	public DenseMatrix64F getV(DenseMatrix64F V, boolean transpose, boolean compact)
	{
		return this.decompBi.getV(V, transpose, compact);
	}

	public boolean decompose(DenseMatrix64F orig)
	{
		if (!this.decompQRP.decompose(orig))
		{
			return false;
		}

		this.m = orig.numRows;
		this.n = orig.numCols;
		this.min = Math.min(this.m, this.n);
		this.B.reshape(this.min, this.n, false);
		this.decompQRP.getR(this.B, true);
		DenseMatrix64F result = new DenseMatrix64F(this.min, this.n);
		DenseMatrix64F P = this.decompQRP.getPivotMatrix(null);
		CommonOps.multTransB(this.B, P, result);
		this.B.set(result);
		return this.decompBi.decompose(this.B);
	}

	@Override
	public boolean inputModified()
	{
		return this.decompQRP.inputModified();
	}
}
