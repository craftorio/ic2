// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.hessenberg;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.alg.block.BlockInnerMultiplication;
import ic2.shades.org.ejml.alg.block.BlockMultiplication;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;

public class TridiagonalDecompositionHouseholder_B64 implements TridiagonalSimilarDecomposition<BlockMatrix64F>
{
    protected BlockMatrix64F A;
    protected BlockMatrix64F V;
    protected BlockMatrix64F tmp;
    protected double[] gammas;
    protected DenseMatrix64F zerosM;
    
    public TridiagonalDecompositionHouseholder_B64() {
        this.V = new BlockMatrix64F(1, 1);
        this.tmp = new BlockMatrix64F(1, 1);
        this.gammas = new double[1];
        this.zerosM = new DenseMatrix64F(1, 1);
    }
    
    @Override
    public BlockMatrix64F getT(BlockMatrix64F T) {
        if (T == null) {
            T = new BlockMatrix64F(this.A.numRows, this.A.numCols, this.A.blockLength);
        }
        else {
            if (T.numRows != this.A.numRows || T.numCols != this.A.numCols) {
                throw new IllegalArgumentException("T must have the same dimensions as the input matrix");
            }
            CommonOps.fill(T, 0.0);
        }
        T.set(0, 0, this.A.data[0]);
        for (int i = 1; i < this.A.numRows; ++i) {
            final double d = this.A.get(i - 1, i);
            T.set(i, i, this.A.get(i, i));
            T.set(i - 1, i, d);
            T.set(i, i - 1, d);
        }
        return T;
    }
    
    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, final boolean transposed) {
        Q = QRDecompositionHouseholder_B64.initializeQ(Q, this.A.numRows, this.A.numCols, this.A.blockLength, false);
        final int height = Math.min(this.A.blockLength, this.A.numRows);
        this.V.reshape(height, this.A.numCols, false);
        this.tmp.reshape(height, this.A.numCols, false);
        final D1Submatrix64F subQ = new D1Submatrix64F(Q);
        final D1Submatrix64F subU = new D1Submatrix64F(this.A);
        final D1Submatrix64F subW = new D1Submatrix64F(this.V);
        final D1Submatrix64F tmp = new D1Submatrix64F(this.tmp);
        final int N = this.A.numRows;
        int start = N - N % this.A.blockLength;
        if (start == N) {
            start -= this.A.blockLength;
        }
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i >= 0; i -= this.A.blockLength) {
            final int blockSize = Math.min(this.A.blockLength, N - i);
            subW.col0 = i;
            subW.row1 = blockSize;
            subW.original.reshape(subW.row1, subW.col1, false);
            if (transposed) {
                tmp.row0 = i;
                tmp.row1 = this.A.numCols;
                tmp.col0 = 0;
                tmp.col1 = blockSize;
            }
            else {
                tmp.col0 = i;
                tmp.row1 = blockSize;
            }
            tmp.original.reshape(tmp.row1, tmp.col1, false);
            subU.col0 = i;
            subU.row0 = i;
            subU.row1 = subU.row0 + blockSize;
            this.copyZeros(subU);
            TridiagonalHelper_B64.computeW_row(this.A.blockLength, subU, subW, this.gammas, i);
            subQ.col0 = i;
            subQ.row0 = i;
            if (transposed) {
                BlockMultiplication.multTransB(this.A.blockLength, subQ, subU, tmp);
            }
            else {
                BlockMultiplication.mult(this.A.blockLength, subU, subQ, tmp);
            }
            if (transposed) {
                BlockMultiplication.multPlus(this.A.blockLength, tmp, subW, subQ);
            }
            else {
                BlockMultiplication.multPlusTransA(this.A.blockLength, subW, tmp, subQ);
            }
            this.replaceZeros(subU);
        }
        return Q;
    }
    
    private void copyZeros(final D1Submatrix64F subU) {
        for (int N = Math.min(this.A.blockLength, subU.col1 - subU.col0), i = 0; i < N; ++i) {
            for (int j = 0; j <= i; ++j) {
                this.zerosM.unsafe_set(i, j, subU.get(i, j));
                subU.set(i, j, 0.0);
            }
            if (subU.col0 + i + 1 < subU.original.numCols) {
                this.zerosM.unsafe_set(i, i + 1, subU.get(i, i + 1));
                subU.set(i, i + 1, 1.0);
            }
        }
    }
    
    private void replaceZeros(final D1Submatrix64F subU) {
        for (int N = Math.min(this.A.blockLength, subU.col1 - subU.col0), i = 0; i < N; ++i) {
            for (int j = 0; j <= i; ++j) {
                subU.set(i, j, this.zerosM.get(i, j));
            }
            if (subU.col0 + i + 1 < subU.original.numCols) {
                subU.set(i, i + 1, this.zerosM.get(i, i + 1));
            }
        }
    }
    
    @Override
    public void getDiagonal(final double[] diag, final double[] off) {
        diag[0] = this.A.data[0];
        for (int i = 1; i < this.A.numRows; ++i) {
            diag[i] = this.A.get(i, i);
            off[i - 1] = this.A.get(i - 1, i);
        }
    }
    
    @Override
    public boolean decompose(final BlockMatrix64F orig) {
        if (orig.numCols != orig.numRows) {
            throw new IllegalArgumentException("Input matrix must be square.");
        }
        this.init(orig);
        final D1Submatrix64F subA = new D1Submatrix64F(this.A);
        final D1Submatrix64F subV = new D1Submatrix64F(this.V);
        final D1Submatrix64F subU = new D1Submatrix64F(this.A);
        for (int N = orig.numCols, i = 0; i < N; i += this.A.blockLength) {
            final int height = Math.min(this.A.blockLength, this.A.numRows - i);
            final D1Submatrix64F d1Submatrix64F = subA;
            final D1Submatrix64F d1Submatrix64F2 = subU;
            final int n = i;
            d1Submatrix64F2.col0 = n;
            d1Submatrix64F.col0 = n;
            final D1Submatrix64F d1Submatrix64F3 = subA;
            final D1Submatrix64F d1Submatrix64F4 = subU;
            final int n2 = i;
            d1Submatrix64F4.row0 = n2;
            d1Submatrix64F3.row0 = n2;
            subU.row1 = subU.row0 + height;
            subV.col0 = i;
            subV.row1 = height;
            subV.original.reshape(subV.row1, subV.col1, false);
            TridiagonalHelper_B64.tridiagUpperRow(this.A.blockLength, subA, this.gammas, subV);
            if (subU.row1 < orig.numCols) {
                final double before = subU.get(this.A.blockLength - 1, this.A.blockLength);
                subU.set(this.A.blockLength - 1, this.A.blockLength, 1.0);
                multPlusTransA(this.A.blockLength, subU, subV, subA);
                multPlusTransA(this.A.blockLength, subV, subU, subA);
                subU.set(this.A.blockLength - 1, this.A.blockLength, before);
            }
        }
        return true;
    }
    
    public static void multPlusTransA(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        final int heightA = Math.min(blockLength, A.row1 - A.row0);
        for (int i = C.row0 + blockLength; i < C.row1; i += blockLength) {
            final int heightC = Math.min(blockLength, C.row1 - i);
            final int indexA = A.row0 * A.original.numCols + (i - C.row0 + A.col0) * heightA;
            for (int j = i; j < C.col1; j += blockLength) {
                final int widthC = Math.min(blockLength, C.col1 - j);
                final int indexC = i * C.original.numCols + j * heightC;
                final int indexB = B.row0 * B.original.numCols + (j - C.col0 + B.col0) * heightA;
                BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, heightC, widthC);
            }
        }
    }
    
    private void init(final BlockMatrix64F orig) {
        this.A = orig;
        final int height = Math.min(this.A.blockLength, this.A.numRows);
        this.V.reshape(height, this.A.numCols, this.A.blockLength, false);
        this.tmp.reshape(height, this.A.numCols, this.A.blockLength, false);
        if (this.gammas.length < this.A.numCols) {
            this.gammas = new double[this.A.numCols];
        }
        this.zerosM.reshape(this.A.blockLength, this.A.blockLength + 1, false);
    }
    
    @Override
    public boolean inputModified() {
        return true;
    }
}
