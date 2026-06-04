// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.hessenberg;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;

public class HessenbergSimilarDecomposition_D64 implements DecompositionInterface<DenseMatrix64F>
{
    private DenseMatrix64F QH;
    private int N;
    private double[] gammas;
    private double[] b;
    private double[] u;
    
    public HessenbergSimilarDecomposition_D64(final int initialSize) {
        this.gammas = new double[initialSize];
        this.b = new double[initialSize];
        this.u = new double[initialSize];
    }
    
    public HessenbergSimilarDecomposition_D64() {
        this(5);
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("A must be square.");
        }
        if (A.numRows <= 0) {
            return false;
        }
        this.QH = A;
        this.N = A.numCols;
        if (this.b.length < this.N) {
            this.b = new double[this.N];
            this.gammas = new double[this.N];
            this.u = new double[this.N];
        }
        return this._decompose();
    }
    
    @Override
    public boolean inputModified() {
        return true;
    }
    
    public DenseMatrix64F getQH() {
        return this.QH;
    }
    
    public DenseMatrix64F getH(DenseMatrix64F H) {
        if (H == null) {
            H = new DenseMatrix64F(this.N, this.N);
        }
        else {
            if (this.N != H.numRows || this.N != H.numCols) {
                throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
            }
            H.zero();
        }
        System.arraycopy(this.QH.data, 0, H.data, 0, this.N);
        for (int i = 1; i < this.N; ++i) {
            for (int j = i - 1; j < this.N; ++j) {
                H.set(i, j, this.QH.get(i, j));
            }
        }
        return H;
    }
    
    public DenseMatrix64F getQ(DenseMatrix64F Q) {
        if (Q == null) {
            Q = new DenseMatrix64F(this.N, this.N);
            for (int i = 0; i < this.N; ++i) {
                Q.data[i * this.N + i] = 1.0;
            }
        }
        else {
            if (this.N != Q.numRows || this.N != Q.numCols) {
                throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
            }
            CommonOps.setIdentity(Q);
        }
        for (int j = this.N - 2; j >= 0; --j) {
            this.u[j + 1] = 1.0;
            for (int k = j + 2; k < this.N; ++k) {
                this.u[k] = this.QH.get(k, j);
            }
            QrHelperFunctions.rank1UpdateMultR(Q, this.u, this.gammas[j], j + 1, j + 1, this.N, this.b);
        }
        return Q;
    }
    
    private boolean _decompose() {
        final double[] h = this.QH.data;
        for (int k = 0; k < this.N - 2; ++k) {
            double max = 0.0;
            for (int i = k + 1; i < this.N; ++i) {
                final double[] u = this.u;
                final int n = i;
                final double n2 = h[i * this.N + k];
                u[n] = n2;
                double val = n2;
                val = Math.abs(val);
                if (val > max) {
                    max = val;
                }
            }
            if (max > 0.0) {
                double tau = 0.0;
                for (int j = k + 1; j < this.N; ++j) {
                    final double[] u2 = this.u;
                    final int n3 = j;
                    final double n4 = u2[n3] / max;
                    u2[n3] = n4;
                    final double val2 = n4;
                    tau += val2 * val2;
                }
                tau = Math.sqrt(tau);
                if (this.u[k + 1] < 0.0) {
                    tau = -tau;
                }
                final double nu = this.u[k + 1] + tau;
                this.u[k + 1] = 1.0;
                for (int l = k + 2; l < this.N; ++l) {
                    final double[] array = h;
                    final int n5 = l * this.N + k;
                    final double[] u3 = this.u;
                    final int n6 = l;
                    array[n5] = (u3[n6] /= nu);
                }
                final double gamma = nu / tau;
                this.gammas[k] = gamma;
                QrHelperFunctions.rank1UpdateMultR(this.QH, this.u, gamma, k + 1, k + 1, this.N, this.b);
                QrHelperFunctions.rank1UpdateMultL(this.QH, this.u, gamma, 0, k + 1, this.N);
                h[(k + 1) * this.N + k] = -tau * max;
            }
            else {
                this.gammas[k] = 0.0;
            }
        }
        return true;
    }
    
    public double[] getGammas() {
        return this.gammas;
    }
}
