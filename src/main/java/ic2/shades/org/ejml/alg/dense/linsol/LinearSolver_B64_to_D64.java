// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolver_B64_to_D64 implements LinearSolver<DenseMatrix64F>
{
    protected LinearSolver<BlockMatrix64F> alg;
    protected BlockMatrix64F blockA;
    protected BlockMatrix64F blockB;
    protected BlockMatrix64F blockX;
    
    public LinearSolver_B64_to_D64(final LinearSolver<BlockMatrix64F> alg) {
        this.alg = new BlockCholeskyOuterSolver();
        this.blockA = new BlockMatrix64F(1, 1);
        this.blockB = new BlockMatrix64F(1, 1);
        this.blockX = new BlockMatrix64F(1, 1);
        this.alg = alg;
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        this.blockA.reshape(A.numRows, A.numCols, false);
        BlockMatrixOps.convert(A, this.blockA);
        return this.alg.setA(this.blockA);
    }
    
    @Override
    public double quality() {
        return this.alg.quality();
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        this.blockB.reshape(B.numRows, B.numCols, false);
        this.blockX.reshape(X.numRows, X.numCols, false);
        BlockMatrixOps.convert(B, this.blockB);
        this.alg.solve(this.blockB, this.blockX);
        BlockMatrixOps.convert(this.blockX, X);
    }
    
    @Override
    public void invert(final DenseMatrix64F A_inv) {
        this.blockB.reshape(A_inv.numRows, A_inv.numCols, false);
        this.alg.invert(this.blockB);
        BlockMatrixOps.convert(this.blockB, A_inv);
    }
    
    @Override
    public boolean modifiesA() {
        return false;
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
}
