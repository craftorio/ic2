// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public abstract class BaseLinearSolverQrp extends LinearSolverAbstract
{
    QRPDecomposition<DenseMatrix64F> decomposition;
    protected boolean norm2Solution;
    protected DenseMatrix64F Y;
    protected DenseMatrix64F R;
    protected DenseMatrix64F R11;
    protected DenseMatrix64F I;
    protected int rank;
    protected LinearSolver<DenseMatrix64F> internalSolver;
    private DenseMatrix64F W;
    
    protected BaseLinearSolverQrp(final QRPDecomposition<DenseMatrix64F> decomposition, final boolean norm2Solution) {
        this.Y = new DenseMatrix64F(1, 1);
        this.R = new DenseMatrix64F(1, 1);
        this.R11 = new DenseMatrix64F(1, 1);
        this.I = new DenseMatrix64F(1, 1);
        this.internalSolver = LinearSolverFactory.leastSquares(1, 1);
        this.W = new DenseMatrix64F(1, 1);
        this.decomposition = decomposition;
        this.norm2Solution = norm2Solution;
        if (this.internalSolver.modifiesA()) {
            this.internalSolver = new LinearSolverSafe<DenseMatrix64F>(this.internalSolver);
        }
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        this._setA(A);
        if (!this.decomposition.decompose(A)) {
            return false;
        }
        this.rank = this.decomposition.getRank();
        this.R.reshape(this.numRows, this.numCols);
        this.decomposition.getR(this.R, false);
        this.R11.reshape(this.rank, this.rank);
        CommonOps.extract(this.R, 0, this.rank, 0, this.rank, this.R11, 0, 0);
        if (this.norm2Solution && this.rank < this.numCols) {
            this.W.reshape(this.rank, this.numCols - this.rank);
            CommonOps.extract(this.R, 0, this.rank, this.rank, this.numCols, this.W, 0, 0);
            TriangularSolver.solveU(this.R11.data, 0, this.R11.numCols, this.R11.numCols, this.W.data, 0, this.W.numCols, this.W.numCols);
            this.W.reshape(this.numCols, this.W.numCols, true);
            for (int i = 0; i < this.numCols - this.rank; ++i) {
                for (int j = 0; j < this.numCols - this.rank; ++j) {
                    if (i == j) {
                        this.W.set(i + this.rank, j, -1.0);
                    }
                    else {
                        this.W.set(i + this.rank, j, 0.0);
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.R);
    }
    
    protected void upgradeSolution(final DenseMatrix64F X) {
        final DenseMatrix64F z = this.Y;
        if (!this.internalSolver.setA(this.W)) {
            throw new RuntimeException("This should never happen.  Is input NaN?");
        }
        z.reshape(this.numCols - this.rank, 1);
        this.internalSolver.solve(X, z);
        CommonOps.multAdd(-1.0, this.W, z, X);
    }
    
    @Override
    public void invert(final DenseMatrix64F A_inv) {
        if (A_inv.numCols != this.numRows || A_inv.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected dimensions for A_inv");
        }
        this.I.reshape(this.numRows, this.numRows);
        CommonOps.setIdentity(this.I);
        this.solve(this.I, A_inv);
    }
    
    public QRPDecomposition<DenseMatrix64F> getDecomposition() {
        return this.decomposition;
    }
}
