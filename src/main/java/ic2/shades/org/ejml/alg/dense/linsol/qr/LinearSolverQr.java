// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public class LinearSolverQr extends LinearSolverAbstract
{
    private QRDecomposition<DenseMatrix64F> decomposer;
    protected int maxRows;
    protected int maxCols;
    protected DenseMatrix64F Q;
    protected DenseMatrix64F R;
    private DenseMatrix64F Y;
    private DenseMatrix64F Z;
    
    public LinearSolverQr(final QRDecomposition<DenseMatrix64F> decomposer) {
        this.maxRows = -1;
        this.maxCols = -1;
        this.decomposer = decomposer;
    }
    
    public void setMaxSize(final int maxRows, final int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
        this.Q = new DenseMatrix64F(maxRows, maxRows);
        this.R = new DenseMatrix64F(maxRows, maxCols);
        this.Y = new DenseMatrix64F(maxRows, 1);
        this.Z = new DenseMatrix64F(maxRows, 1);
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
        this.Q.reshape(this.numRows, this.numRows, false);
        this.R.reshape(this.numRows, this.numCols, false);
        this.decomposer.getQ(this.Q, false);
        this.decomposer.getR(this.R, false);
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.R);
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
        this.Y.reshape(this.numRows, 1, false);
        this.Z.reshape(this.numRows, 1, false);
        for (int colB = 0; colB < BnumCols; ++colB) {
            for (int i = 0; i < this.numRows; ++i) {
                this.Y.data[i] = B.get(i, colB);
            }
            CommonOps.multTransA(this.Q, this.Y, this.Z);
            TriangularSolver.solveU(this.R.data, this.Z.data, this.numCols);
            for (int i = 0; i < this.numCols; ++i) {
                X.set(i, colB, this.Z.data[i]);
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
    
    public QRDecomposition<DenseMatrix64F> getDecomposer() {
        return this.decomposer;
    }
    
    public DenseMatrix64F getQ() {
        return this.Q;
    }
    
    public DenseMatrix64F getR() {
        return this.R;
    }
}
