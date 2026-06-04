// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public class LinearSolverQrHouseTran extends LinearSolverAbstract
{
    private QRDecompositionHouseholderTran_D64 decomposer;
    private double[] a;
    protected int maxRows;
    protected int maxCols;
    private DenseMatrix64F QR;
    private DenseMatrix64F U;
    
    public LinearSolverQrHouseTran() {
        this.maxRows = -1;
        this.maxCols = -1;
        this.decomposer = new QRDecompositionHouseholderTran_D64();
    }
    
    public void setMaxSize(final int maxRows, final int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
        this.a = new double[maxRows];
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        if (A.numRows > this.maxRows || A.numCols > this.maxCols) {
            this.setMaxSize(A.numRows, A.numCols);
        }
        this._setA(A);
        if (!this.decomposer.decompose(A)) {
            return false;
        }
        this.QR = this.decomposer.getQR();
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.QR);
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        if (X.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = " + X.numRows + " expected = " + this.numCols);
        }
        if (B.numRows != this.numRows || B.numCols != X.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for B");
        }
        this.U = this.decomposer.getR(this.U, true);
        final double[] gammas = this.decomposer.getGammas();
        final double[] dataQR = this.QR.data;
        for (int BnumCols = B.numCols, colB = 0; colB < BnumCols; ++colB) {
            for (int i = 0; i < this.numRows; ++i) {
                this.a[i] = B.data[i * BnumCols + colB];
            }
            for (int n = 0; n < this.numCols; ++n) {
                int indexU = n * this.numRows + n + 1;
                double ub = this.a[n];
                for (int j = n + 1; j < this.numRows; ++j, ++indexU) {
                    ub += dataQR[indexU] * this.a[j];
                }
                ub *= gammas[n];
                final double[] a = this.a;
                final int n2 = n;
                a[n2] -= ub;
                indexU = n * this.numRows + n + 1;
                for (int j = n + 1; j < this.numRows; ++j, ++indexU) {
                    final double[] a2 = this.a;
                    final int n3 = j;
                    a2[n3] -= dataQR[indexU] * ub;
                }
            }
            TriangularSolver.solveU(this.U.data, this.a, this.numCols);
            for (int i = 0; i < this.numCols; ++i) {
                X.data[i * X.numCols + colB] = this.a[i];
            }
        }
    }
    
    @Override
    public boolean modifiesA() {
        return this.decomposer.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
}
