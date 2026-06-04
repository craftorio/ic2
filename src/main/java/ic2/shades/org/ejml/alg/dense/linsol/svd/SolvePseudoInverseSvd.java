// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.svd;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class SolvePseudoInverseSvd implements LinearSolver<DenseMatrix64F>
{
    private SingularValueDecomposition<DenseMatrix64F> svd;
    private DenseMatrix64F pinv;
    private double threshold;
    
    public SolvePseudoInverseSvd(final int maxRows, final int maxCols) {
        this.pinv = new DenseMatrix64F(1, 1);
        this.threshold = UtilEjml.EPS;
        this.svd = DecompositionFactory.svd(maxRows, maxCols, true, true, true);
    }
    
    public SolvePseudoInverseSvd() {
        this(100, 100);
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        this.pinv.reshape(A.numCols, A.numRows, false);
        if (!this.svd.decompose(A)) {
            return false;
        }
        final DenseMatrix64F U_t = this.svd.getU(null, true);
        final DenseMatrix64F V = this.svd.getV(null, false);
        final double[] S = this.svd.getSingularValues();
        final int N = Math.min(A.numRows, A.numCols);
        double maxSingular = 0.0;
        for (int i = 0; i < N; ++i) {
            if (S[i] > maxSingular) {
                maxSingular = S[i];
            }
        }
        final double tau = this.threshold * Math.max(A.numCols, A.numRows) * maxSingular;
        if (maxSingular != 0.0) {
            for (int j = 0; j < N; ++j) {
                final double s = S[j];
                if (s < tau) {
                    S[j] = 0.0;
                }
                else {
                    S[j] = 1.0 / S[j];
                }
            }
        }
        for (int j = 0; j < V.numRows; ++j) {
            int index = j * V.numCols;
            for (int k = 0; k < V.numCols; ++k) {
                final double[] data = V.data;
                final int n = index++;
                data[n] *= S[k];
            }
        }
        CommonOps.mult(V, U_t, this.pinv);
        return true;
    }
    
    @Override
    public double quality() {
        throw new IllegalArgumentException("Not supported by this solver.");
    }
    
    @Override
    public void solve(final DenseMatrix64F b, final DenseMatrix64F x) {
        CommonOps.mult(this.pinv, b, x);
    }
    
    @Override
    public void invert(final DenseMatrix64F A_inv) {
        A_inv.set(this.pinv);
    }
    
    @Override
    public boolean modifiesA() {
        return this.svd.inputModified();
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
    
    public void setThreshold(final double threshold) {
        this.threshold = threshold;
    }
    
    public SingularValueDecomposition<DenseMatrix64F> getDecomposer() {
        return this.svd;
    }
}
