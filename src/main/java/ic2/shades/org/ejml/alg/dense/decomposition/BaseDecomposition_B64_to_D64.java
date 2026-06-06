package ic2.shades.org.ejml.alg.dense.decomposition;

import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;

public class BaseDecomposition_B64_to_D64 implements DecompositionInterface<DenseMatrix64F>
{
	protected final DecompositionInterface<BlockMatrix64F> alg;
	protected double[] tmp;
	protected final BlockMatrix64F Ablock = new BlockMatrix64F();
	protected final int blockLength;

	public BaseDecomposition_B64_to_D64(DecompositionInterface<BlockMatrix64F> alg, int blockLength)
	{
		this.alg = alg;
		this.blockLength = blockLength;
	}

	public boolean decompose(DenseMatrix64F A)
	{
		this.Ablock.numRows = A.numRows;
		this.Ablock.numCols = A.numCols;
		this.Ablock.blockLength = this.blockLength;
		this.Ablock.data = A.data;
		int tmpLength = Math.min(this.Ablock.blockLength, A.numRows) * A.numCols;
		if (this.tmp == null || this.tmp.length < tmpLength)
		{
			this.tmp = new double[tmpLength];
		}

		BlockMatrixOps.convertRowToBlock(A.numRows, A.numCols, this.Ablock.blockLength, A.data, this.tmp);
		boolean ret = this.alg.decompose(this.Ablock);
		if (!this.alg.inputModified())
		{
			BlockMatrixOps.convertBlockToRow(A.numRows, A.numCols, this.Ablock.blockLength, A.data, this.tmp);
		}

		return ret;
	}

	public void convertBlockToRow(int numRows, int numCols, int blockLength, double[] data)
	{
		int tmpLength = Math.min(blockLength, numRows) * numCols;
		if (this.tmp == null || this.tmp.length < tmpLength)
		{
			this.tmp = new double[tmpLength];
		}

		BlockMatrixOps.convertBlockToRow(numRows, numCols, this.Ablock.blockLength, data, this.tmp);
	}

	@Override
	public boolean inputModified()
	{
		return this.alg.inputModified();
	}
}
