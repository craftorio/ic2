// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockTriangularSolver;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class BlockQrHouseHolderSolver implements LinearSolver<BlockMatrix64F>
{
    protected QRDecompositionHouseholder_B64 decomp;
    protected BlockMatrix64F QR;
    
    public BlockQrHouseHolderSolver() {
        (this.decomp = new QRDecompositionHouseholder_B64()).setSaveW(false);
    }
    
    @Override
    public boolean setA(final BlockMatrix64F A) {
        if (A.numRows < A.numCols) {
            throw new IllegalArgumentException("Number of rows must be more than or equal to the number of columns.  Can't solve an underdetermined system.");
        }
        if (!this.decomp.decompose(A)) {
            return false;
        }
        this.QR = this.decomp.getQR();
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.decomp.getQR());
    }
    
    @Override
    public void solve(final BlockMatrix64F B, final BlockMatrix64F X) {
        if (B.numCols != X.numCols) {
            throw new IllegalArgumentException("Columns of B and X do not match");
        }
        if (this.QR.numCols != X.numRows) {
            throw new IllegalArgumentException("Rows in X do not match the columns in A");
        }
        if (this.QR.numRows != B.numRows) {
            throw new IllegalArgumentException("Rows in B do not match the rows in A.");
        }
        if (B.blockLength != this.QR.blockLength || X.blockLength != this.QR.blockLength) {
            throw new IllegalArgumentException("All matrices must have the same block length.");
        }
        this.decomp.applyQTran(B);
        BlockMatrixOps.extractAligned(B, X);
        final int M = Math.min(this.QR.numRows, this.QR.numCols);
        BlockTriangularSolver.solve(this.QR.blockLength, true, new D1Submatrix64F(this.QR, 0, M, 0, M), new D1Submatrix64F(X), false);
    }
    
    @Override
    public void invert(final BlockMatrix64F A_inv) {
        final int M = Math.min(this.QR.numRows, this.QR.numCols);
        if (A_inv.numRows != M || A_inv.numCols != M) {
            throw new IllegalArgumentException("A_inv must be square an have dimension " + M);
        }
        BlockMatrixOps.setIdentity(A_inv);
        this.decomp.applyQTran(A_inv);
        BlockTriangularSolver.solve(this.QR.blockLength, true, new D1Submatrix64F(this.QR, 0, M, 0, M), new D1Submatrix64F(A_inv), false);
    }
    
    @Override
    public boolean modifiesA() {
        return this.decomp.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return true;
    }
}
