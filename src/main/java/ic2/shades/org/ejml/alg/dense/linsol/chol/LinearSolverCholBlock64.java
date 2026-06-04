// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64;

public class LinearSolverCholBlock64 extends LinearSolver_B64_to_D64
{
    public LinearSolverCholBlock64() {
        super(new BlockCholeskyOuterSolver());
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        this.blockB.reshape(B.numRows, B.numCols, false);
        BlockMatrixOps.convert(B, this.blockB);
        this.alg.solve(this.blockB, null);
        BlockMatrixOps.convert(this.blockB, X);
    }
}
