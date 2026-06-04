// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class CholeskyDecompositionBlock_D64 extends CholeskyDecompositionCommon_D64
{
    private int blockWidth;
    private DenseMatrix64F B;
    private CholeskyBlockHelper_D64 chol;
    
    public CholeskyDecompositionBlock_D64(final int blockWidth) {
        super(true);
        this.blockWidth = blockWidth;
    }
    
    @Override
    public void setExpectedMaxSize(final int numRows, final int numCols) {
        super.setExpectedMaxSize(numRows, numCols);
        if (numRows < this.blockWidth) {
            this.B = new DenseMatrix64F(0, 0);
        }
        else {
            this.B = new DenseMatrix64F(this.blockWidth, this.maxWidth);
        }
        this.chol = new CholeskyBlockHelper_D64(this.blockWidth);
    }
    
    @Override
    protected boolean decomposeLower() {
        if (this.n < this.blockWidth) {
            this.B.reshape(0, 0, false);
        }
        else {
            this.B.reshape(this.blockWidth, this.n - this.blockWidth, false);
        }
        int numBlocks = this.n / this.blockWidth;
        final int remainder = this.n % this.blockWidth;
        if (remainder > 0) {
            ++numBlocks;
        }
        this.B.numCols = this.n;
        for (int i = 0; i < numBlocks; ++i) {
            final DenseMatrix64F b = this.B;
            b.numCols -= this.blockWidth;
            if (this.B.numCols > 0) {
                if (!this.chol.decompose(this.T, i * this.blockWidth * this.T.numCols + i * this.blockWidth, this.blockWidth)) {
                    return false;
                }
                final int indexSrc = i * this.blockWidth * this.T.numCols + (i + 1) * this.blockWidth;
                final int indexDst = (i + 1) * this.blockWidth * this.T.numCols + i * this.blockWidth;
                solveL_special(this.chol.getL().data, this.T, indexSrc, indexDst, this.B);
                final int indexL = (i + 1) * this.blockWidth * this.n + (i + 1) * this.blockWidth;
                symmRankTranA_sub(this.B, this.T, indexL);
            }
            else {
                final int width = (remainder > 0) ? remainder : this.blockWidth;
                if (!this.chol.decompose(this.T, i * this.blockWidth * this.T.numCols + i * this.blockWidth, width)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < this.n; ++i) {
            for (int j = i + 1; j < this.n; ++j) {
                this.t[i * this.n + j] = 0.0;
            }
        }
        return true;
    }
    
    @Override
    protected boolean decomposeUpper() {
        throw new RuntimeException("Not implemented.  Do a lower decomposition and transpose it...");
    }
    
    public static void solveL_special(final double[] L, final DenseMatrix64F b_src, final int indexSrc, final int indexDst, final DenseMatrix64F B) {
        final double[] dataSrc = b_src.data;
        final double[] b = B.data;
        final int m = B.numRows;
        final int n = B.numCols;
        final int widthL = m;
        for (int j = 0; j < n; ++j) {
            for (int indexb = j, rowL = 0, i = 0; i < widthL; ++i, indexb += n, rowL += widthL) {
                double sum = dataSrc[indexSrc + i * b_src.numCols + j];
                for (int indexL = rowL, endL = indexL + i, indexB = j; indexL != endL; sum -= L[indexL++] * b[indexB], indexB += n) {}
                final double val = sum / L[i * widthL + i];
                b[indexb] = (dataSrc[indexDst + j * b_src.numCols + i] = val);
            }
        }
    }
    
    public static void symmRankTranA_sub(final DenseMatrix64F a, final DenseMatrix64F c, int startIndexC) {
        final double[] dataA = a.data;
        final double[] dataC = c.data;
        final int strideC = c.numCols + 1;
        for (int i = 0; i < a.numCols; ++i) {
            for (int indexA = i, endR = a.numCols, k = 0; k < a.numRows; ++k, indexA += a.numCols, endR += a.numCols) {
                int indexC = startIndexC;
                final double valA = dataA[indexA];
                double[] array;
                int n;
                for (int indexR = indexA; indexR < endR; array[n] -= valA * dataA[indexR++]) {
                    array = dataC;
                    n = indexC++;
                }
            }
            startIndexC += strideC;
        }
    }
}
