package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

public abstract class BaseLinearSolverQrp extends LinearSolverAbstract
{
	final QRPDecomposition<DenseMatrix64F> decomposition;
	protected final boolean norm2Solution;
	protected final DenseMatrix64F Y = new DenseMatrix64F(1, 1);
	protected final DenseMatrix64F R = new DenseMatrix64F(1, 1);
	protected final DenseMatrix64F R11 = new DenseMatrix64F(1, 1);
	protected final DenseMatrix64F I = new DenseMatrix64F(1, 1);
	protected int rank;
	protected LinearSolver<DenseMatrix64F> internalSolver = LinearSolverFactory.leastSquares(1, 1);
	private final DenseMatrix64F W = new DenseMatrix64F(1, 1);

	protected BaseLinearSolverQrp(QRPDecomposition<DenseMatrix64F> decomposition, boolean norm2Solution)
	{
		this.decomposition = decomposition;
		this.norm2Solution = norm2Solution;
		if (this.internalSolver.modifiesA())
		{
			this.internalSolver = new LinearSolverSafe<>(this.internalSolver);
		}
	}

	public boolean setA(DenseMatrix64F A)
	{
		this._setA(A);
		if (!this.decomposition.decompose(A))
		{
			return false;
		}

		this.rank = this.decomposition.getRank();
		this.R.reshape(this.numRows, this.numCols);
		this.decomposition.getR(this.R, false);
		this.R11.reshape(this.rank, this.rank);
		CommonOps.extract(this.R, 0, this.rank, 0, this.rank, this.R11, 0, 0);
		if (this.norm2Solution && this.rank < this.numCols)
		{
			this.W.reshape(this.rank, this.numCols - this.rank);
			CommonOps.extract(this.R, 0, this.rank, this.rank, this.numCols, this.W, 0, 0);
			TriangularSolver.solveU(this.R11.data, 0, this.R11.numCols, this.R11.numCols, this.W.data, 0, this.W.numCols, this.W.numCols);
			this.W.reshape(this.numCols, this.W.numCols, true);

			for (int i = 0; i < this.numCols - this.rank; i++)
			{
				for (int j = 0; j < this.numCols - this.rank; j++)
				{
					if (i == j)
					{
						this.W.set(i + this.rank, j, -1.0);
					} else
					{
						this.W.set(i + this.rank, j, 0.0);
					}
				}
			}
		}

		return true;
	}

	@Override
	public double quality()
	{
		return SpecializedOps.qualityTriangular(true, this.R);
	}

	protected void upgradeSolution(DenseMatrix64F X)
	{
		DenseMatrix64F z = this.Y;
		if (!this.internalSolver.setA(this.W))
		{
			throw new RuntimeException("This should never happen.  Is input NaN?");
		}

		z.reshape(this.numCols - this.rank, 1);
		this.internalSolver.solve(X, z);
		CommonOps.multAdd(-1.0, this.W, z, X);
	}

	@Override
	public void invert(DenseMatrix64F A_inv)
	{
		if (A_inv.numCols == this.numRows && A_inv.numRows == this.numCols)
		{
			this.I.reshape(this.numRows, this.numRows);
			CommonOps.setIdentity(this.I);
			this.solve(this.I, A_inv);
		} else
		{
			throw new IllegalArgumentException("Unexpected dimensions for A_inv");
		}
	}

	public QRPDecomposition<DenseMatrix64F> getDecomposition()
	{
		return this.decomposition;
	}
}
