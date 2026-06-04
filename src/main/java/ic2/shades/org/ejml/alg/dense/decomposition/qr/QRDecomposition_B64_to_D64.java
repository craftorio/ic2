// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;
import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;

public class QRDecomposition_B64_to_D64 extends BaseDecomposition_B64_to_D64 implements QRDecomposition<DenseMatrix64F>
{
    public QRDecomposition_B64_to_D64() {
        super(new QRDecompositionHouseholder_B64(), EjmlParameters.BLOCK_WIDTH);
    }
    
    @Override
    public DenseMatrix64F getQ(DenseMatrix64F Q, final boolean compact) {
        final int minLength = Math.min(this.Ablock.numRows, this.Ablock.numCols);
        if (Q == null) {
            if (compact) {
                Q = new DenseMatrix64F(this.Ablock.numRows, minLength);
                CommonOps.setIdentity(Q);
            }
            else {
                Q = new DenseMatrix64F(this.Ablock.numRows, this.Ablock.numRows);
                CommonOps.setIdentity(Q);
            }
        }
        final BlockMatrix64F Qblock = new BlockMatrix64F();
        Qblock.numRows = Q.numRows;
        Qblock.numCols = Q.numCols;
        Qblock.blockLength = this.blockLength;
        Qblock.data = Q.data;
        ((QRDecompositionHouseholder_B64)this.alg).getQ(Qblock, compact);
        this.convertBlockToRow(Q.numRows, Q.numCols, this.Ablock.blockLength, Q.data);
        return Q;
    }
    
    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, final boolean compact) {
        final BlockMatrix64F Rblock = ((QRDecompositionHouseholder_B64)this.alg).getR((BlockMatrix64F)null, compact);
        if (R == null) {
            R = new DenseMatrix64F(Rblock.numRows, Rblock.numCols);
        }
        BlockMatrixOps.convert(Rblock, R);
        return R;
    }
}
