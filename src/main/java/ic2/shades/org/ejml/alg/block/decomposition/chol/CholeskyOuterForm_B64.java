// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.BlockInnerRankUpdate;
import ic2.shades.org.ejml.alg.block.BlockTriangularSolver;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyDecomposition;

public class CholeskyOuterForm_B64 implements CholeskyDecomposition<BlockMatrix64F>
{
    private boolean lower;
    private BlockMatrix64F T;
    private D1Submatrix64F subA;
    private D1Submatrix64F subB;
    private D1Submatrix64F subC;
    
    public CholeskyOuterForm_B64(final boolean lower) {
        this.lower = false;
        this.subA = new D1Submatrix64F();
        this.subB = new D1Submatrix64F();
        this.subC = new D1Submatrix64F();
        this.lower = lower;
    }
    
    @Override
    public boolean decompose(final BlockMatrix64F A) {
        if (A.numCols != A.numRows) {
            throw new IllegalArgumentException("A must be square");
        }
        this.T = A;
        if (this.lower) {
            return this.decomposeLower();
        }
        return this.decomposeUpper();
    }
    
    private boolean decomposeLower() {
        final int blockLength = this.T.blockLength;
        this.subA.set(this.T);
        this.subB.set(this.T);
        this.subC.set(this.T);
        for (int i = 0; i < this.T.numCols; i += blockLength) {
            final int widthA = Math.min(blockLength, this.T.numCols - i);
            this.subA.col0 = i;
            this.subA.col1 = i + widthA;
            this.subA.row0 = this.subA.col0;
            this.subA.row1 = this.subA.col1;
            this.subB.col0 = i;
            this.subB.col1 = i + widthA;
            this.subB.row0 = i + widthA;
            this.subB.row1 = this.T.numRows;
            this.subC.col0 = i + widthA;
            this.subC.col1 = this.T.numRows;
            this.subC.row0 = i + widthA;
            this.subC.row1 = this.T.numRows;
            if (!InnerCholesky_B64.lower(this.subA)) {
                return false;
            }
            if (widthA == blockLength) {
                BlockTriangularSolver.solveBlock(blockLength, false, this.subA, this.subB, false, true);
                BlockInnerRankUpdate.symmRankNMinus_L(blockLength, this.subC, this.subB);
            }
        }
        BlockMatrixOps.zeroTriangle(true, this.T);
        return true;
    }
    
    private boolean decomposeUpper() {
        final int blockLength = this.T.blockLength;
        this.subA.set(this.T);
        this.subB.set(this.T);
        this.subC.set(this.T);
        for (int i = 0; i < this.T.numCols; i += blockLength) {
            final int widthA = Math.min(blockLength, this.T.numCols - i);
            this.subA.col0 = i;
            this.subA.col1 = i + widthA;
            this.subA.row0 = this.subA.col0;
            this.subA.row1 = this.subA.col1;
            this.subB.col0 = i + widthA;
            this.subB.col1 = this.T.numCols;
            this.subB.row0 = i;
            this.subB.row1 = i + widthA;
            this.subC.col0 = i + widthA;
            this.subC.col1 = this.T.numCols;
            this.subC.row0 = i + widthA;
            this.subC.row1 = this.T.numCols;
            if (!InnerCholesky_B64.upper(this.subA)) {
                return false;
            }
            if (widthA == blockLength) {
                BlockTriangularSolver.solveBlock(blockLength, true, this.subA, this.subB, true, false);
                BlockInnerRankUpdate.symmRankNMinus_U(blockLength, this.subC, this.subB);
            }
        }
        BlockMatrixOps.zeroTriangle(false, this.T);
        return true;
    }
    
    @Override
    public boolean isLower() {
        return this.lower;
    }
    
    @Override
    public BlockMatrix64F getT(final BlockMatrix64F T) {
        if (T == null) {
            return this.T;
        }
        T.set(this.T);
        return T;
    }
    
    @Override
    public boolean inputModified() {
        return true;
    }
}
