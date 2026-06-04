// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public class LinearSolverQrHouse extends LinearSolverAbstract
{
    private QRDecompositionHouseholder_D64 decomposer;
    private double[] a;
    private double[] u;
    private int maxRows;
    private DenseMatrix64F QR;
    private double[] gammas;
    
    public LinearSolverQrHouse() {
        this.maxRows = -1;
        this.decomposer = new QRDecompositionHouseholder_D64();
    }
    
    public void setMaxSize(final int maxRows) {
        this.maxRows = maxRows;
        this.a = new double[maxRows];
        this.u = new double[maxRows];
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        if (A.numRows > this.maxRows) {
            this.setMaxSize(A.numRows);
        }
        this._setA(A);
        if (!this.decomposer.decompose(A)) {
            return false;
        }
        this.gammas = this.decomposer.getGammas();
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
            throw new IllegalArgumentException("Unexpected dimensions for X");
        }
        if (B.numRows != this.numRows || B.numCols != X.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for B");
        }
        for (int BnumCols = B.numCols, colB = 0; colB < BnumCols; ++colB) {
            for (int i = 0; i < this.numRows; ++i) {
                this.a[i] = B.data[i * BnumCols + colB];
            }
            for (int n = 0; n < this.numCols; ++n) {
                this.u[n] = 1.0;
                double ub = this.a[n];
                for (int j = n + 1; j < this.numRows; ++j) {
                    final double n2 = ub;
                    final double[] u = this.u;
                    final int n3 = j;
                    final double unsafe_get = this.QR.unsafe_get(j, n);
                    u[n3] = unsafe_get;
                    ub = n2 + unsafe_get * this.a[j];
                }
                ub *= this.gammas[n];
                for (int j = n; j < this.numRows; ++j) {
                    final double[] a = this.a;
                    final int n4 = j;
                    a[n4] -= this.u[j] * ub;
                }
            }
            TriangularSolver.solveU(this.QR.data, this.a, this.numCols);
            for (int i = 0; i < this.numCols; ++i) {
                X.data[i * X.numCols + colB] = this.a[i];
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
