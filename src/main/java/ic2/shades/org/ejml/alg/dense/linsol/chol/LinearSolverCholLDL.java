// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public class LinearSolverCholLDL extends LinearSolverAbstract
{
    private CholeskyDecompositionLDL_D64 decomp;
    private int n;
    private double[] vv;
    private double[] el;
    private double[] d;
    
    public LinearSolverCholLDL(final CholeskyDecompositionLDL_D64 decomp) {
        this.decomp = decomp;
    }
    
    public LinearSolverCholLDL() {
        this.decomp = new CholeskyDecompositionLDL_D64();
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        this._setA(A);
        if (this.decomp.decompose(A)) {
            this.n = A.numCols;
            this.vv = this.decomp._getVV();
            this.el = this.decomp.getL().data;
            this.d = this.decomp.getDiagonal();
            return true;
        }
        return false;
    }
    
    @Override
    public double quality() {
        return Math.abs(SpecializedOps.diagProd(this.decomp.getL()));
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        if (B.numCols != X.numCols && B.numRows != this.n && X.numRows != this.n) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        final int numCols = B.numCols;
        final double[] dataB = B.data;
        final double[] dataX = X.data;
        for (int j = 0; j < numCols; ++j) {
            for (int i = 0; i < this.n; ++i) {
                this.vv[i] = dataB[i * numCols + j];
            }
            this.solveInternal();
            for (int i = 0; i < this.n; ++i) {
                dataX[i * numCols + j] = this.vv[i];
            }
        }
    }
    
    private void solveInternal() {
        TriangularSolver.solveL(this.el, this.vv, this.n);
        for (int i = 0; i < this.n; ++i) {
            final double[] vv = this.vv;
            final int n = i;
            vv[n] /= this.d[i];
        }
        TriangularSolver.solveTranL(this.el, this.vv, this.n);
    }
    
    @Override
    public void invert(final DenseMatrix64F inv) {
        if (inv.numRows != this.n || inv.numCols != this.n) {
            throw new RuntimeException("Unexpected matrix dimension");
        }
        final double[] a = inv.data;
        for (int i = 0; i < this.n; ++i) {
            for (int j = 0; j <= i; ++j) {
                double sum = (i == j) ? 1.0 : 0.0;
                for (int k = i - 1; k >= j; --k) {
                    sum -= this.el[i * this.n + k] * a[j * this.n + k];
                }
                a[j * this.n + i] = sum;
            }
        }
        for (int i = 0; i < this.n; ++i) {
            final double inv_d = 1.0 / this.d[i];
            for (int l = 0; l <= i; ++l) {
                final double[] array = a;
                final int n = l * this.n + i;
                array[n] *= inv_d;
            }
        }
        for (int i = this.n - 1; i >= 0; --i) {
            for (int j = 0; j <= i; ++j) {
                double sum = (i < j) ? 0.0 : a[j * this.n + i];
                for (int k = i + 1; k < this.n; ++k) {
                    sum -= this.el[k * this.n + i] * a[j * this.n + k];
                }
                a[i * this.n + j] = (a[j * this.n + i] = sum);
            }
        }
    }
    
    @Override
    public boolean modifiesA() {
        return this.decomp.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
}
