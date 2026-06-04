// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;

public class QRDecompositionHouseholder_D64 implements QRDecomposition<DenseMatrix64F>
{
    protected DenseMatrix64F QR;
    protected double[] u;
    protected double[] v;
    protected int numCols;
    protected int numRows;
    protected int minLength;
    protected double[] dataQR;
    protected double[] gammas;
    protected double gamma;
    protected double tau;
    protected boolean error;
    
    public void setExpectedMaxSize(final int numRows, final int numCols) {
        this.error = false;
        this.numCols = numCols;
        this.numRows = numRows;
        this.minLength = Math.min(numRows, numCols);
        final int maxLength = Math.max(numRows, numCols);
        if (this.QR == null) {
            this.QR = new DenseMatrix64F(numRows, numCols);
            this.u = new double[maxLength];
            this.v = new double[maxLength];
            this.gammas = new double[this.minLength];
        }
        else {
            this.QR.reshape(numRows, numCols, false);
        }
        this.dataQR = this.QR.data;
        if (this.u.length < maxLength) {
            this.u = new double[maxLength];
            this.v = new double[maxLength];
        }
        if (this.gammas.length < this.minLength) {
            this.gammas = new double[this.minLength];
        }
    }
    
    public DenseMatrix64F getQR() {
        return this.QR;
    }
    
    @Override
    public DenseMatrix64F getQ(DenseMatrix64F Q, final boolean compact) {
        if (compact) {
            if (Q == null) {
                Q = CommonOps.identity(this.numRows, this.minLength);
            }
            else {
                if (Q.numRows != this.numRows || Q.numCols != this.minLength) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                }
                CommonOps.setIdentity(Q);
            }
        }
        else if (Q == null) {
            Q = CommonOps.identity(this.numRows);
        }
        else {
            if (Q.numRows != this.numRows || Q.numCols != this.numRows) {
                throw new IllegalArgumentException("Unexpected matrix dimension.");
            }
            CommonOps.setIdentity(Q);
        }
        for (int j = this.minLength - 1; j >= 0; --j) {
            this.u[j] = 1.0;
            for (int i = j + 1; i < this.numRows; ++i) {
                this.u[i] = this.QR.get(i, j);
            }
            QrHelperFunctions.rank1UpdateMultR(Q, this.u, this.gammas[j], j, j, this.numRows, this.v);
        }
        return Q;
    }
    
    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, final boolean compact) {
        if (R == null) {
            if (compact) {
                R = new DenseMatrix64F(this.minLength, this.numCols);
            }
            else {
                R = new DenseMatrix64F(this.numRows, this.numCols);
            }
        }
        else {
            if (compact) {
                if (R.numCols != this.numCols || R.numRows != this.minLength) {
                    throw new IllegalArgumentException("Unexpected dimensions");
                }
            }
            else if (R.numCols != this.numCols || R.numRows != this.numRows) {
                throw new IllegalArgumentException("Unexpected dimensions");
            }
            for (int i = 0; i < R.numRows; ++i) {
                for (int min = Math.min(i, R.numCols), j = 0; j < min; ++j) {
                    R.set(i, j, 0.0);
                }
            }
        }
        for (int i = 0; i < this.minLength; ++i) {
            for (int k = i; k < this.numCols; ++k) {
                final double val = this.QR.get(i, k);
                R.set(i, k, val);
            }
        }
        return R;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        this.commonSetup(A);
        for (int j = 0; j < this.minLength; ++j) {
            this.householder(j);
            this.updateA(j);
        }
        return !this.error;
    }
    
    @Override
    public boolean inputModified() {
        return false;
    }
    
    protected void householder(final int j) {
        int index = j + j * this.numCols;
        double max = 0.0;
        for (int i = j; i < this.numRows; ++i) {
            final double[] u = this.u;
            final int n = i;
            final double n2 = this.dataQR[index];
            u[n] = n2;
            double d = n2;
            if (d < 0.0) {
                d = -d;
            }
            if (max < d) {
                max = d;
            }
            index += this.numCols;
        }
        if (max == 0.0) {
            this.gamma = 0.0;
            this.error = true;
        }
        else {
            this.tau = 0.0;
            for (int i = j; i < this.numRows; ++i) {
                final double[] u2 = this.u;
                final int n3 = i;
                u2[n3] /= max;
                final double d = this.u[i];
                this.tau += d * d;
            }
            this.tau = Math.sqrt(this.tau);
            if (this.u[j] < 0.0) {
                this.tau = -this.tau;
            }
            final double u_0 = this.u[j] + this.tau;
            this.gamma = u_0 / this.tau;
            for (int k = j + 1; k < this.numRows; ++k) {
                final double[] u3 = this.u;
                final int n4 = k;
                u3[n4] /= u_0;
            }
            this.u[j] = 1.0;
            this.tau *= max;
        }
        this.gammas[j] = this.gamma;
    }
    
    protected void updateA(final int w) {
        for (int i = w + 1; i < this.numCols; ++i) {
            this.v[i] = this.u[w] * this.dataQR[w * this.numCols + i];
        }
        for (int k = w + 1; k < this.numRows; ++k) {
            int indexQR = k * this.numCols + w + 1;
            for (int j = w + 1; j < this.numCols; ++j) {
                final double[] v = this.v;
                final int n = j;
                v[n] += this.u[k] * this.dataQR[indexQR++];
            }
        }
        for (int i = w + 1; i < this.numCols; ++i) {
            final double[] v2 = this.v;
            final int n2 = i;
            v2[n2] *= this.gamma;
        }
        for (int i = w; i < this.numRows; ++i) {
            final double valU = this.u[i];
            int indexQR2 = i * this.numCols + w + 1;
            for (int l = w + 1; l < this.numCols; ++l) {
                final double[] dataQR = this.dataQR;
                final int n3 = indexQR2++;
                dataQR[n3] -= valU * this.v[l];
            }
        }
        if (w < this.numCols) {
            this.dataQR[w + w * this.numCols] = -this.tau;
        }
        for (int i = w + 1; i < this.numRows; ++i) {
            this.dataQR[w + i * this.numCols] = this.u[i];
        }
    }
    
    protected void commonSetup(final DenseMatrix64F A) {
        this.setExpectedMaxSize(A.numRows, A.numCols);
        this.QR.set(A);
    }
    
    public double[] getGammas() {
        return this.gammas;
    }
}
