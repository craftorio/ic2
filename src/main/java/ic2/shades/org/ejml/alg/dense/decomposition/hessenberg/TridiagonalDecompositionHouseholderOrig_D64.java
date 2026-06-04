// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.hessenberg;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class TridiagonalDecompositionHouseholderOrig_D64
{
    DenseMatrix64F QT;
    int N;
    double[] w;
    double[] gammas;
    double[] b;
    
    public TridiagonalDecompositionHouseholderOrig_D64() {
        this.N = 1;
        this.QT = new DenseMatrix64F(this.N, this.N);
        this.w = new double[this.N];
        this.b = new double[this.N];
        this.gammas = new double[this.N];
    }
    
    public DenseMatrix64F getQT() {
        return this.QT;
    }
    
    public DenseMatrix64F getT(DenseMatrix64F T) {
        if (T == null) {
            T = new DenseMatrix64F(this.N, this.N);
        }
        else {
            if (this.N != T.numRows || this.N != T.numCols) {
                throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
            }
            T.zero();
        }
        T.data[0] = this.QT.data[0];
        T.data[1] = this.QT.data[1];
        for (int i = 1; i < this.N - 1; ++i) {
            T.set(i, i, this.QT.get(i, i));
            T.set(i, i + 1, this.QT.get(i, i + 1));
            T.set(i, i - 1, this.QT.get(i - 1, i));
        }
        T.data[(this.N - 1) * this.N + this.N - 1] = this.QT.data[(this.N - 1) * this.N + this.N - 1];
        T.data[(this.N - 1) * this.N + this.N - 2] = this.QT.data[(this.N - 2) * this.N + this.N - 1];
        return T;
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
        for (int i = 0; i < this.N; ++i) {
            this.w[i] = 0.0;
        }
        for (int j = this.N - 2; j >= 0; --j) {
            this.w[j + 1] = 1.0;
            for (int k = j + 2; k < this.N; ++k) {
                this.w[k] = this.QT.get(j, k);
            }
            QrHelperFunctions.rank1UpdateMultR(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N, this.b);
        }
        return Q;
    }
    
    public void decompose(final DenseMatrix64F A) {
        this.init(A);
        for (int k = 1; k < this.N; ++k) {
            this.similarTransform(k);
        }
    }
    
    private void similarTransform(final int k) {
        final double[] t = this.QT.data;
        double max = 0.0;
        final int rowU = (k - 1) * this.N;
        for (int i = k; i < this.N; ++i) {
            final double val = Math.abs(t[rowU + i]);
            if (val > max) {
                max = val;
            }
        }
        if (max > 0.0) {
            double tau = 0.0;
            for (int j = k; j < this.N; ++j) {
                final double[] array = t;
                final int n = rowU + j;
                final double n2 = array[n] / max;
                array[n] = n2;
                final double val2 = n2;
                tau += val2 * val2;
            }
            tau = Math.sqrt(tau);
            if (t[rowU + k] < 0.0) {
                tau = -tau;
            }
            final double nu = t[rowU + k] + tau;
            t[rowU + k] = 1.0;
            for (int l = k + 1; l < this.N; ++l) {
                final double[] array2 = t;
                final int n3 = rowU + l;
                array2[n3] /= nu;
            }
            final double gamma = nu / tau;
            this.householderSymmetric(k, this.gammas[k] = gamma);
            t[rowU + k] = -tau * max;
        }
        else {
            this.gammas[k] = 0.0;
        }
    }
    
    public void householderSymmetric(final int row, final double gamma) {
        final int startU = (row - 1) * this.N;
        for (int i = row; i < this.N; ++i) {
            double total = 0.0;
            for (int j = row; j < this.N; ++j) {
                total += this.QT.data[i * this.N + j] * this.QT.data[startU + j];
            }
            this.w[i] = -gamma * total;
        }
        double alpha = 0.0;
        for (int k = row; k < this.N; ++k) {
            alpha += this.QT.data[startU + k] * this.w[k];
        }
        alpha *= -0.5 * gamma;
        for (int k = row; k < this.N; ++k) {
            final double[] w = this.w;
            final int n = k;
            w[n] += alpha * this.QT.data[startU + k];
        }
        for (int k = row; k < this.N; ++k) {
            final double ww = this.w[k];
            final double uu = this.QT.data[startU + k];
            for (int l = k; l < this.N; ++l) {
                final double[] data = this.QT.data;
                final int n2 = l * this.N + k;
                final double[] data2 = this.QT.data;
                final int n3 = k * this.N + l;
                data[n2] = (data2[n3] += ww * this.QT.data[startU + l] + this.w[l] * uu);
            }
        }
    }
    
    public void init(final DenseMatrix64F A) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("Must be square");
        }
        if (A.numCols != this.N) {
            this.N = A.numCols;
            this.QT.reshape(this.N, this.N, false);
            if (this.w.length < this.N) {
                this.w = new double[this.N];
                this.gammas = new double[this.N];
                this.b = new double[this.N];
            }
        }
        this.QT.set(A);
    }
    
    public double getGamma(final int index) {
        return this.gammas[index];
    }
}
