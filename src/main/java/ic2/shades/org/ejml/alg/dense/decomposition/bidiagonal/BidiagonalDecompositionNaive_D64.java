// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal;

import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.simple.SimpleMatrix;

public class BidiagonalDecompositionNaive_D64
{
    private SimpleMatrix U;
    private SimpleMatrix B;
    private SimpleMatrix V;
    private int m;
    private int n;
    private int min;
    DenseMatrix64F u;
    
    public SimpleMatrix getU() {
        return this.U;
    }
    
    public SimpleMatrix getB() {
        return this.B;
    }
    
    public SimpleMatrix getV() {
        return this.V;
    }
    
    public boolean decompose(final DenseMatrix64F A) {
        this.init(A);
        return this._decompose();
    }
    
    protected void init(final DenseMatrix64F A) {
        this.m = A.numRows;
        this.n = A.numCols;
        this.min = Math.min(this.m, this.n);
        this.U = SimpleMatrix.identity(this.m);
        this.B = new SimpleMatrix(A);
        this.V = SimpleMatrix.identity(this.n);
        final int max = Math.max(this.m, this.n);
        this.u = new DenseMatrix64F(max, 1);
    }
    
    private boolean _decompose() {
        for (int k = 0; k < this.min; ++k) {
            this.computeU(k);
            this.computeV(k);
        }
        return true;
    }
    
    protected void computeU(final int k) {
        this.u.reshape(this.m, 1, false);
        final double[] u = this.u.data;
        double max = 0.0;
        for (int i = k; i < this.m; ++i) {
            final double[] array = u;
            final int n = i;
            final double value = this.B.get(i, k);
            array[n] = value;
            double val = value;
            val = Math.abs(val);
            if (val > max) {
                max = val;
            }
        }
        if (max > 0.0) {
            double tau = 0.0;
            for (int j = k; j < this.m; ++j) {
                final double[] array2 = u;
                final int n2 = j;
                final double n3 = array2[n2] / max;
                array2[n2] = n3;
                final double val2 = n3;
                tau += val2 * val2;
            }
            tau = Math.sqrt(tau);
            if (u[k] < 0.0) {
                tau = -tau;
            }
            final double nu = u[k] + tau;
            u[k] = 1.0;
            for (int l = k + 1; l < this.m; ++l) {
                final double[] array3 = u;
                final int n4 = l;
                array3[n4] /= nu;
            }
            final SimpleMatrix Q_k = SimpleMatrix.wrap(SpecializedOps.createReflector(this.u, nu / tau));
            this.U = this.U.mult(Q_k);
            this.B = Q_k.mult(this.B);
        }
    }
    
    protected void computeV(final int k) {
        this.u.reshape(this.n, 1, false);
        this.u.zero();
        final double[] u = this.u.data;
        double max = 0.0;
        for (int i = k + 1; i < this.n; ++i) {
            final double[] array = u;
            final int n = i;
            final double value = this.B.get(k, i);
            array[n] = value;
            double val = value;
            val = Math.abs(val);
            if (val > max) {
                max = val;
            }
        }
        if (max > 0.0) {
            double tau = 0.0;
            for (int j = k + 1; j < this.n; ++j) {
                final double[] array2 = u;
                final int n2 = j;
                final double n3 = array2[n2] / max;
                array2[n2] = n3;
                final double val2 = n3;
                tau += val2 * val2;
            }
            tau = Math.sqrt(tau);
            if (u[k + 1] < 0.0) {
                tau = -tau;
            }
            final double nu = u[k + 1] + tau;
            u[k + 1] = 1.0;
            for (int l = k + 2; l < this.n; ++l) {
                final double[] array3 = u;
                final int n4 = l;
                array3[n4] /= nu;
            }
            final SimpleMatrix Q_k = SimpleMatrix.wrap(SpecializedOps.createReflector(this.u, nu / tau));
            this.V = this.V.mult(Q_k);
            this.B = this.B.mult(Q_k);
        }
    }
}
