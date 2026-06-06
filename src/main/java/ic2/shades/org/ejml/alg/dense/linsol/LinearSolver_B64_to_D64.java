package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolver_B64_to_D64 implements LinearSolver<DenseMatrix64F>
{
	protected LinearSolver<BlockMatrix64F> alg = new BlockCholeskyOuterSolver();
	protected final BlockMatrix64F blockA = new BlockMatrix64F(1, 1);
	protected final BlockMatrix64F blockB = new BlockMatrix64F(1, 1);
	protected final BlockMatrix64F blockX = new BlockMatrix64F(1, 1);

	public LinearSolver_B64_to_D64(LinearSolver<BlockMatrix64F> alg)
	{
		this.alg = alg;
	}

	public boolean setA(DenseMatrix64F A)
	{
		this.blockA.reshape(A.numRows, A.numCols, false);
		BlockMatrixOps.convert(A, this.blockA);
		return this.alg.setA(this.blockA);
	}

	@Override
	public double quality()
	{
		return this.alg.quality();
	}

	public void solve(DenseMatrix64F B, DenseMatrix64F X)
	{
		this.blockB.reshape(B.numRows, B.numCols, false);
		this.blockX.reshape(X.numRows, X.numCols, false);
		BlockMatrixOps.convert(B, this.blockB);
		this.alg.solve(this.blockB, this.blockX);
		BlockMatrixOps.convert(this.blockX, X);
	}

	public void invert(DenseMatrix64F A_inv)
	{
		this.blockB.reshape(A_inv.numRows, A_inv.numCols, false);
		this.alg.invert(this.blockB);
		BlockMatrixOps.convert(this.blockB, A_inv);
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
