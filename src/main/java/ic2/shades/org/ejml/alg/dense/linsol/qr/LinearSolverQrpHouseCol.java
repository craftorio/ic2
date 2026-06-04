// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;

public class LinearSolverQrpHouseCol extends BaseLinearSolverQrp
{
    private QRColPivDecompositionHouseholderColumn_D64 decomposition;
    private DenseMatrix64F x_basic;
    
    public LinearSolverQrpHouseCol(final QRColPivDecompositionHouseholderColumn_D64 decomposition, final boolean norm2Solution) {
        super(decomposition, norm2Solution);
        this.x_basic = new DenseMatrix64F(1, 1);
        this.decomposition = decomposition;
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
        final double[][] qr = this.decomposition.getQR();
        final double[] gammas = this.decomposition.getGammas();
        for (int colB = 0; colB < BnumCols; ++colB) {
            this.x_basic.reshape(this.numRows, 1);
            this.Y.reshape(this.numRows, 1);
            for (int i = 0; i < this.numRows; ++i) {
                this.x_basic.data[i] = B.get(i, colB);
            }
            for (int i = 0; i < this.rank; ++i) {
                final double[] u = qr[i];
                final double vv = u[i];
                u[i] = 1.0;
                QrHelperFunctions.rank1UpdateMultR(this.x_basic, u, gammas[i], 0, i, this.numRows, this.Y.data);
                u[i] = vv;
            }
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
