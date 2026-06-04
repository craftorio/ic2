// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.linsol.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.BlockTriangularSolver;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.alg.block.decomposition.chol.CholeskyOuterForm_B64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class BlockCholeskyOuterSolver implements LinearSolver<BlockMatrix64F>
{
    private CholeskyOuterForm_B64 chol;
    private int blockLength;
    private double[] temp;
    
    public BlockCholeskyOuterSolver() {
        this.chol = new CholeskyOuterForm_B64(true);
    }
    
    @Override
    public boolean setA(final BlockMatrix64F A) {
        if (!this.chol.decompose(A)) {
            return false;
        }
        this.blockLength = A.blockLength;
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(false, this.chol.getT((BlockMatrix64F)null));
    }
    
    @Override
    public void solve(final BlockMatrix64F B, final BlockMatrix64F X) {
        if (B.blockLength != this.blockLength) {
            throw new IllegalArgumentException("Unexpected blocklength in B.");
        }
        final D1Submatrix64F L = new D1Submatrix64F(this.chol.getT((BlockMatrix64F)null));
        if (X != null) {
            if (X.blockLength != this.blockLength) {
                throw new IllegalArgumentException("Unexpected blocklength in X.");
            }
            if (X.numRows != L.col1) {
                throw new IllegalArgumentException("Not enough rows in X");
            }
        }
        if (B.numRows != L.col1) {
            throw new IllegalArgumentException("Not enough rows in B");
        }
        BlockTriangularSolver.solve(this.blockLength, false, L, new D1Submatrix64F(B), false);
        BlockTriangularSolver.solve(this.blockLength, false, L, new D1Submatrix64F(B), true);
        if (X != null) {
            BlockMatrixOps.extractAligned(B, X);
        }
    }
    
    @Override
    public void invert(final BlockMatrix64F A_inv) {
        final BlockMatrix64F T = this.chol.getT((BlockMatrix64F)null);
        if (A_inv.numRows != T.numRows || A_inv.numCols != T.numCols) {
            throw new IllegalArgumentException("Unexpected number or rows and/or columns");
        }
        if (this.temp == null || this.temp.length < this.blockLength * this.blockLength) {
            this.temp = new double[this.blockLength * this.blockLength];
        }
        BlockMatrixOps.zeroTriangle(true, A_inv);
        final D1Submatrix64F L = new D1Submatrix64F(T);
        final D1Submatrix64F B = new D1Submatrix64F(A_inv);
        BlockTriangularSolver.invert(this.blockLength, false, L, B, this.temp);
        BlockTriangularSolver.solveL(this.blockLength, L, B, true);
    }
    
    @Override
    public boolean modifiesA() {
        return this.chol.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return true;
    }
}
