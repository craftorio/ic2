// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;
import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.block.decomposition.chol.CholeskyOuterForm_B64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;

public class CholeskyDecomposition_B64_to_D64 extends BaseDecomposition_B64_to_D64 implements CholeskyDecomposition<DenseMatrix64F>
{
    public CholeskyDecomposition_B64_to_D64(final boolean lower) {
        super(new CholeskyOuterForm_B64(lower), EjmlParameters.BLOCK_WIDTH);
    }
    
    @Override
    public boolean isLower() {
        return ((CholeskyOuterForm_B64)this.alg).isLower();
    }
    
    @Override
    public DenseMatrix64F getT(DenseMatrix64F T) {
        final BlockMatrix64F T_block = ((CholeskyOuterForm_B64)this.alg).getT((BlockMatrix64F)null);
        if (T == null) {
            T = new DenseMatrix64F(T_block.numRows, T_block.numCols);
        }
        BlockMatrixOps.convert(T_block, T);
        return T;
    }
}
