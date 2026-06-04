// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public class LinearSolverQrHouseCol extends LinearSolverAbstract
{
    private QRDecompositionHouseholderColumn_D64 decomposer;
    private DenseMatrix64F a;
    private DenseMatrix64F temp;
    protected int maxRows;
    protected int maxCols;
    private double[][] QR;
    private DenseMatrix64F R;
    private double[] gammas;
    
    public LinearSolverQrHouseCol() {
        this.a = new DenseMatrix64F(1, 1);
        this.temp = new DenseMatrix64F(1, 1);
        this.maxRows = -1;
        this.maxCols = -1;
        this.R = new DenseMatrix64F(1, 1);
        this.decomposer = new QRDecompositionHouseholderColumn_D64();
    }
    
    public void setMaxSize(final int maxRows, final int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        if (A.numRows > this.maxRows || A.numCols > this.maxCols) {
            this.setMaxSize(A.numRows, A.numCols);
        }
        this.R.reshape(A.numCols, A.numCols);
        this.a.reshape(A.numRows, 1);
        this.temp.reshape(A.numRows, 1);
        this._setA(A);
        if (!this.decomposer.decompose(A)) {
            return false;
        }
        this.gammas = this.decomposer.getGammas();
        this.QR = this.decomposer.getQR();
        this.decomposer.getR(this.R, true);
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.R);
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        if (X.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = " + X.numRows + " expected = " + this.numCols);
        }
        if (B.numRows != this.numRows || B.numCols != X.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for B");
        }
        for (int BnumCols = B.numCols, colB = 0; colB < BnumCols; ++colB) {
            for (int i = 0; i < this.numRows; ++i) {
                this.a.data[i] = B.data[i * BnumCols + colB];
            }
            for (int n = 0; n < this.numCols; ++n) {
                final double[] u = this.QR[n];
                final double vv = u[n];
                u[n] = 1.0;
                QrHelperFunctions.rank1UpdateMultR(this.a, u, this.gammas[n], 0, n, this.numRows, this.temp.data);
                u[n] = vv;
            }
            TriangularSolver.solveU(this.R.data, this.a.data, this.numCols);
            for (int i = 0; i < this.numCols; ++i) {
                X.data[i * X.numCols + colB] = this.a.data[i];
            }
        }
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
