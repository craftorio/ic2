// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class SolvePseudoInverseQrp extends BaseLinearSolverQrp
{
    private DenseMatrix64F Q;
    private DenseMatrix64F x_basic;
    
    public SolvePseudoInverseQrp(final QRPDecomposition<DenseMatrix64F> decomposition, final boolean norm2Solution) {
        super(decomposition, norm2Solution);
        this.Q = new DenseMatrix64F(1, 1);
        this.x_basic = new DenseMatrix64F(1, 1);
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        if (!super.setA(A)) {
            return false;
        }
        this.Q.reshape(A.numRows, A.numRows);
        this.decomposition.getQ(this.Q, false);
        return true;
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        if (X.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for X");
        }
        if (B.numRows != this.numRows || B.numCols != X.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for B");
        }
        final int BnumCols = B.numCols;
        final int[] pivots = this.decomposition.getPivots();
        for (int colB = 0; colB < BnumCols; ++colB) {
            this.x_basic.reshape(this.numRows, 1);
            this.Y.reshape(this.numRows, 1);
            for (int i = 0; i < this.numRows; ++i) {
                this.Y.data[i] = B.get(i, colB);
            }
            CommonOps.multTransA(this.Q, this.Y, this.x_basic);
            TriangularSolver.solveU(this.R11.data, this.x_basic.data, this.rank);
            this.x_basic.reshape(this.numCols, 1, true);
            for (int i = this.rank; i < this.numCols; ++i) {
                this.x_basic.data[i] = 0.0;
            }
            if (this.norm2Solution && this.rank < this.numCols) {
                this.upgradeSolution(this.x_basic);
            }
            for (int i = 0; i < this.numCols; ++i) {
                X.set(pivots[i], colB, this.x_basic.data[i]);
            }
        }
    }
    
    @Override
    public boolean modifiesA() {
        return this.decomposition.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
}
