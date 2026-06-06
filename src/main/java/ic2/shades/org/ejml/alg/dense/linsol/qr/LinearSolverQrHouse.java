package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverQrHouse extends LinearSolverAbstract
{
	private final QRDecompositionHouseholder_D64 decomposer;
	private double[] a;
	private double[] u;
	private int maxRows = -1;
	private DenseMatrix64F QR;
	private double[] gammas;

	public LinearSolverQrHouse()
	{
		this.decomposer = new QRDecompositionHouseholder_D64();
	}

	public void setMaxSize(int maxRows)
	{
		this.maxRows = maxRows;
		this.a = new double[maxRows];
		this.u = new double[maxRows];
	}

	public boolean setA(DenseMatrix64F A)
	{
		if (A.numRows > this.maxRows)
		{
			this.setMaxSize(A.numRows);
		}

		this._setA(A);
		if (!this.decomposer.decompose(A))
		{
			return false;
		}

		this.gammas = this.decomposer.getGammas();
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
			throw new IllegalArgumentException("Unexpected dimensions for X");
		}

		if (B.numRows == this.numRows && B.numCols == X.numCols)
		{
			int BnumCols = B.numCols;

			for (int colB = 0; colB < BnumCols; colB++)
			{
				for (int i = 0; i < this.numRows; i++)
				{
					this.a[i] = B.data[i * BnumCols + colB];
				}

				for (int n = 0; n < this.numCols; n++)
				{
					this.u[n] = 1.0;
					double ub = this.a[n];

					for (int i = n + 1; i < this.numRows; i++)
					{
						ub += (this.u[i] = this.QR.unsafe_get(i, n)) * this.a[i];
					}

					ub *= this.gammas[n];

					for (int i = n; i < this.numRows; i++)
					{
						this.a[i] = this.a[i] - this.u[i] * ub;
					}
				}

				TriangularSolver.solveU(this.QR.data, this.a, this.numCols);

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
		return false;
	}

	@Override
	public boolean modifiesB()
	{
		return false;
	}
}
