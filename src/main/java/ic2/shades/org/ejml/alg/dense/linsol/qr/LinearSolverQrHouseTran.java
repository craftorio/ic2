package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverQrHouseTran extends LinearSolverAbstract
{
	private final QRDecompositionHouseholderTran_D64 decomposer;
	private double[] a;
	protected int maxRows = -1;
	protected int maxCols = -1;
	private DenseMatrix64F QR;
	private DenseMatrix64F U;

	public LinearSolverQrHouseTran()
	{
		this.decomposer = new QRDecompositionHouseholderTran_D64();
	}

	public void setMaxSize(int maxRows, int maxCols)
	{
		this.maxRows = maxRows;
		this.maxCols = maxCols;
		this.a = new double[maxRows];
	}

	public boolean setA(DenseMatrix64F A)
	{
		if (A.numRows > this.maxRows || A.numCols > this.maxCols)
		{
			this.setMaxSize(A.numRows, A.numCols);
		}

		this._setA(A);
		if (!this.decomposer.decompose(A))
		{
			return false;
		}

		this.QR = this.decomposer.getQR();
		return true;
	}

	@Override
	public double quality()
	{
		return SpecializedOps.qualityTriangular(true, this.QR);
	}

	public void solve(DenseMatrix64F B, DenseMatrix64F X)
	{
		if (X.numRows != this.numCols)
		{
			throw new IllegalArgumentException("Unexpected dimensions for X: X rows = " + X.numRows + " expected = " + this.numCols);
		}

		if (B.numRows == this.numRows && B.numCols == X.numCols)
		{
			this.U = this.decomposer.getR(this.U, true);
			double[] gammas = this.decomposer.getGammas();
			double[] dataQR = this.QR.data;
			int BnumCols = B.numCols;

			for (int colB = 0; colB < BnumCols; colB++)
			{
				for (int i = 0; i < this.numRows; i++)
				{
					this.a[i] = B.data[i * BnumCols + colB];
				}

				for (int n = 0; n < this.numCols; n++)
				{
					int indexU = n * this.numRows + n + 1;
					double ub = this.a[n];

					for (int i = n + 1; i < this.numRows; indexU++)
					{
						ub += dataQR[indexU] * this.a[i];
						i++;
					}

					ub *= gammas[n];
					this.a[n] = this.a[n] - ub;
					indexU = n * this.numRows + n + 1;

					for (int i = n + 1; i < this.numRows; indexU++)
					{
						this.a[i] = this.a[i] - dataQR[indexU] * ub;
						i++;
					}
				}

				TriangularSolver.solveU(this.U.data, this.a, this.numCols);

				for (int i = 0; i < this.numCols; i++)
				{
					X.data[i * X.numCols + colB] = this.a[i];
				}
			}
		} else
		{
			throw new IllegalArgumentException("Unexpected dimensions for B");
		}
	}

	@Override
	public boolean modifiesA()
	{
		return this.decomposer.inputModified();
	}

	@Override
	public boolean modifiesB()
	{
		return false;
	}
}
