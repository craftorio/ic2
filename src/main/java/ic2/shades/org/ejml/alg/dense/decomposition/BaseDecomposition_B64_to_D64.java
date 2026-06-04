// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;

public class BaseDecomposition_B64_to_D64 implements DecompositionInterface<DenseMatrix64F>
{
    protected DecompositionInterface<BlockMatrix64F> alg;
    protected double[] tmp;
    protected BlockMatrix64F Ablock;
    protected int blockLength;
    
    public BaseDecomposition_B64_to_D64(final DecompositionInterface<BlockMatrix64F> alg, final int blockLength) {
        this.Ablock = new BlockMatrix64F();
        this.alg = alg;
        this.blockLength = blockLength;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        this.Ablock.numRows = A.numRows;
        this.Ablock.numCols = A.numCols;
        this.Ablock.blockLength = this.blockLength;
        this.Ablock.data = A.data;
        final int tmpLength = Math.min(this.Ablock.blockLength, A.numRows) * A.numCols;
        if (this.tmp == null || this.tmp.length < tmpLength) {
            this.tmp = new double[tmpLength];
        }
        BlockMatrixOps.convertRowToBlock(A.numRows, A.numCols, this.Ablock.blockLength, A.data, this.tmp);
        final boolean ret = this.alg.decompose(this.Ablock);
        if (!this.alg.inputModified()) {
            BlockMatrixOps.convertBlockToRow(A.numRows, A.numCols, this.Ablock.blockLength, A.data, this.tmp);
        }
        return ret;
    }
    
    public void convertBlockToRow(final int numRows, final int numCols, final int blockLength, final double[] data) {
        final int tmpLength = Math.min(blockLength, numRows) * numCols;
        if (this.tmp == null || this.tmp.length < tmpLength) {
            this.tmp = new double[tmpLength];
        }
        BlockMatrixOps.convertBlockToRow(numRows, numCols, this.Ablock.blockLength, data, this.tmp);
    }
    
    @Override
    public boolean inputModified() {
        return this.alg.inputModified();
    }
}
