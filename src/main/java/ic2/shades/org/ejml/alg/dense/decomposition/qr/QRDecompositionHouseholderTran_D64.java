// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;

public class QRDecompositionHouseholderTran_D64 implements QRDecomposition<DenseMatrix64F>
{
    protected DenseMatrix64F QR;
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
        if (this.QR == null) {
            this.QR = new DenseMatrix64F(numCols, numRows);
            this.v = new double[maxLength];
            this.gammas = new double[this.minLength];
        }
        else {
            this.QR.reshape(numCols, numRows, false);
        }
        if (this.v.length < maxLength) {
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
            final int diagIndex = j * this.numRows + j;
            final double before = this.QR.data[diagIndex];
            this.QR.data[diagIndex] = 1.0;
            QrHelperFunctions.rank1UpdateMultR(Q, this.QR.data, j * this.numRows, this.gammas[j], j, j, this.numRows, this.v);
            this.QR.data[diagIndex] = before;
        }
        return Q;
    }
    
    public void applyQ(final DenseMatrix64F A) {
        if (A.numRows != this.numRows) {
            throw new IllegalArgumentException("A must have at least " + this.numRows + " rows.");
        }
        for (int j = this.minLength - 1; j >= 0; --j) {
            final int diagIndex = j * this.numRows + j;
            final double before = this.QR.data[diagIndex];
            this.QR.data[diagIndex] = 1.0;
            QrHelperFunctions.rank1UpdateMultR(A, this.QR.data, j * this.numRows, this.gammas[j], 0, j, this.numRows, this.v);
            this.QR.data[diagIndex] = before;
        }
    }
    
    public void applyTranQ(final DenseMatrix64F A) {
        for (int j = 0; j < this.minLength; ++j) {
            final int diagIndex = j * this.numRows + j;
            final double before = this.QR.data[diagIndex];
            this.QR.data[diagIndex] = 1.0;
            QrHelperFunctions.rank1UpdateMultR(A, this.QR.data, j * this.numRows, this.gammas[j], 0, j, this.numRows, this.v);
            this.QR.data[diagIndex] = before;
        }
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
                    R.unsafe_set(i, j, 0.0);
                }
            }
        }
        for (int i = 0; i < R.numRows; ++i) {
            for (int k = i; k < R.numCols; ++k) {
                R.unsafe_set(i, k, this.QR.unsafe_get(k, i));
            }
        }
        return R;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        this.setExpectedMaxSize(A.numRows, A.numCols);
        CommonOps.transpose(A, this.QR);
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
    
    protected void householder(final int j) {
        int startQR = j * this.numRows;
        final int endQR = startQR + this.numRows;
        startQR += j;
        final double max = QrHelperFunctions.findMax(this.QR.data, startQR, this.numRows - j);
        if (max == 0.0) {
            this.gamma = 0.0;
            this.error = true;
        }
        else {
            this.tau = QrHelperFunctions.computeTauAndDivide(startQR, endQR, this.QR.data, max);
            final double u_0 = this.QR.data[startQR] + this.tau;
            QrHelperFunctions.divideElements(startQR + 1, endQR, this.QR.data, u_0);
            this.gamma = u_0 / this.tau;
            this.tau *= max;
            this.QR.data[startQR] = -this.tau;
        }
        this.gammas[j] = this.gamma;
    }
    
    protected void updateA(final int w) {
        final double[] data = this.QR.data;
        final int rowW = w * this.numRows + w + 1;
        int rowJ = rowW + this.numRows;
        final int rowJEnd = rowJ + (this.numCols - w - 1) * this.numRows;
        final int indexWEnd = rowW + this.numRows - w - 1;
        while (rowJEnd != rowJ) {
            double val = data[rowJ - 1];
            for (int indexW = rowW, indexJ = rowJ; indexW != indexWEnd; val += data[indexW++] * data[indexJ++]) {}
            val *= this.gamma;
            final double[] array = data;
            final int n = rowJ - 1;
            array[n] -= val;
            int indexW = rowW;
            int indexJ = rowJ;
            while (indexW != indexWEnd) {
                final double[] array2 = data;
                final int n2 = indexJ++;
                array2[n2] -= data[indexW++] * val;
            }
            rowJ += this.numRows;
        }
    }
    
    public double[] getGammas() {
        return this.gammas;
    }
}
