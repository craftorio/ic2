// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class EigenPowerMethod
{
    private double tol;
    private DenseMatrix64F q0;
    private DenseMatrix64F q1;
    private DenseMatrix64F q2;
    private int maxIterations;
    private DenseMatrix64F B;
    private DenseMatrix64F seed;
    
    public EigenPowerMethod(final int size) {
        this.tol = 1.0E-10;
        this.maxIterations = 20;
        this.q0 = new DenseMatrix64F(size, 1);
        this.q1 = new DenseMatrix64F(size, 1);
        this.q2 = new DenseMatrix64F(size, 1);
        this.B = new DenseMatrix64F(size, size);
    }
    
    public void setSeed(final DenseMatrix64F seed) {
        this.seed = seed;
    }
    
    public void setOptions(final int maxIterations, final double tolerance) {
        this.maxIterations = maxIterations;
        this.tol = tolerance;
    }
    
    public boolean computeDirect(final DenseMatrix64F A) {
        this.initPower(A);
        boolean converged = false;
        for (int i = 0; i < this.maxIterations && !converged; converged = this.checkConverged(A), ++i) {
            CommonOps.mult(A, this.q0, this.q1);
            final double s = NormOps.normPInf(this.q1);
            CommonOps.divide(this.q1, s, this.q2);
        }
        return converged;
    }
    
    private void initPower(final DenseMatrix64F A) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("A must be a square matrix.");
        }
        if (this.seed != null) {
            this.q0.set(this.seed);
        }
        else {
            for (int i = 0; i < A.numRows; ++i) {
                this.q0.data[i] = 1.0;
            }
        }
    }
    
    private boolean checkConverged(final DenseMatrix64F A) {
        double worst = 0.0;
        double worst2 = 0.0;
        for (int j = 0; j < A.numRows; ++j) {
            double val = Math.abs(this.q2.data[j] - this.q0.data[j]);
            if (val > worst) {
                worst = val;
            }
            val = Math.abs(this.q2.data[j] + this.q0.data[j]);
            if (val > worst2) {
                worst2 = val;
            }
        }
        final DenseMatrix64F temp = this.q0;
        this.q0 = this.q2;
        this.q2 = temp;
        return worst < this.tol || worst2 < this.tol;
    }
    
    public boolean computeShiftDirect(final DenseMatrix64F A, final double alpha) {
        SpecializedOps.addIdentity(A, this.B, -alpha);
        return this.computeDirect(this.B);
    }
    
    public boolean computeShiftInvert(final DenseMatrix64F A, final double alpha) {
        this.initPower(A);
        final LinearSolver solver = LinearSolverFactory.linear(A.numCols);
        SpecializedOps.addIdentity(A, this.B, -alpha);
        solver.setA(this.B);
        boolean converged = false;
        for (int i = 0; i < this.maxIterations && !converged; converged = this.checkConverged(A), ++i) {
            solver.solve(this.q0, this.q1);
            final double s = NormOps.normPInf(this.q1);
            CommonOps.divide(this.q1, s, this.q2);
        }
        return converged;
    }
    
    public DenseMatrix64F getEigenVector() {
        return this.q0;
    }
}
