// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;

public class QRDecompositionHouseholderColumn_D64 implements QRDecomposition<DenseMatrix64F>
{
    protected double[][] dataQR;
    protected double[] v;
    protected int numCols;
    protected int numRows;
    protected int minLength;
    protected double[] gammas;
    protected double gamma;
    protected double tau;
    protected boolean error;
    
    public void setExpectedMaxSize(final int numRows, final int numCols) {
        this.numCols = numCols;
        this.numRows = numRows;
        this.minLength = Math.min(numCols, numRows);
        final int maxLength = Math.max(numCols, numRows);
        if (this.dataQR == null || this.dataQR.length < numCols || this.dataQR[0].length < numRows) {
            this.dataQR = new double[numCols][numRows];
            this.v = new double[maxLength];
            this.gammas = new double[this.minLength];
        }
        if (this.v.length < maxLength) {
            this.v = new double[maxLength];
        }
        if (this.gammas.length < this.minLength) {
            this.gammas = new double[this.minLength];
        }
    }
    
    public double[][] getQR() {
        return this.dataQR;
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
            final double[] u = this.dataQR[j];
            final double vv = u[j];
            u[j] = 1.0;
            QrHelperFunctions.rank1UpdateMultR(Q, u, this.gammas[j], j, j, this.numRows, this.v);
            u[j] = vv;
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
                    throw new IllegalArgumentException("Unexpected dimensions: found( " + R.numRows + " " + R.numCols + " ) expected( " + this.minLength + " " + this.numCols + " )");
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
        for (int k = 0; k < this.numCols; ++k) {
            final double[] colR = this.dataQR[k];
            for (int l = Math.min(k, this.numRows - 1), m = 0; m <= l; ++m) {
                final double val = colR[m];
                R.set(m, k, val);
            }
        }
        return R;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        this.setExpectedMaxSize(A.numRows, A.numCols);
        this.convertToColumnMajor(A);
        this.error = false;
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
    
    protected void convertToColumnMajor(final DenseMatrix64F A) {
        for (int x = 0; x < this.numCols; ++x) {
            final double[] colQ = this.dataQR[x];
            for (int y = 0; y < this.numRows; ++y) {
                colQ[y] = A.data[y * this.numCols + x];
            }
        }
    }
    
    protected void householder(final int j) {
        final double[] u = this.dataQR[j];
        final double max = QrHelperFunctions.findMax(u, j, this.numRows - j);
        if (max == 0.0) {
            this.gamma = 0.0;
            this.error = true;
        }
        else {
            this.tau = QrHelperFunctions.computeTauAndDivide(j, this.numRows, u, max);
            final double u_0 = u[j] + this.tau;
            QrHelperFunctions.divideElements(j + 1, this.numRows, u, u_0);
            this.gamma = u_0 / this.tau;
            this.tau *= max;
            u[j] = -this.tau;
        }
        this.gammas[j] = this.gamma;
    }
    
    protected void updateA(final int w) {
        final double[] u = this.dataQR[w];
        for (int j = w + 1; j < this.numCols; ++j) {
            final double[] colQ = this.dataQR[j];
            double val = colQ[w];
            for (int k = w + 1; k < this.numRows; ++k) {
                val += u[k] * colQ[k];
            }
            val *= this.gamma;
            final double[] array = colQ;
            array[w] -= val;
            for (int i = w + 1; i < this.numRows; ++i) {
                final double[] array2 = colQ;
                final int n = i;
                array2[n] -= u[i] * val;
            }
        }
    }
    
    public double[] getGammas() {
        return this.gammas;
    }
}
