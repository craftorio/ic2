// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockInnerRankUpdate
{
    public static void rankNUpdate(final int blockLength, final double alpha, final D1Submatrix64F A, final D1Submatrix64F B) {
        final int heightB = B.row1 - B.row0;
        if (heightB > blockLength) {
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");
        }
        final int N = B.col1 - B.col0;
        if (A.col1 - A.col0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        }
        if (A.row1 - A.row0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");
        }
        for (int i = B.col0; i < B.col1; i += blockLength) {
            final int indexB_i = B.row0 * B.original.numCols + i * heightB;
            final int widthB_i = Math.min(blockLength, B.col1 - i);
            final int rowA = i - B.col0 + A.row0;
            final int heightA = Math.min(blockLength, A.row1 - rowA);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB_j = Math.min(blockLength, B.col1 - j);
                final int indexA = rowA * A.original.numCols + (j - B.col0 + A.col0) * heightA;
                final int indexB_j = B.row0 * B.original.numCols + j * heightB;
                BlockInnerMultiplication.blockMultPlusTransA(alpha, B.original.data, B.original.data, A.original.data, indexB_i, indexB_j, indexA, heightB, widthB_i, widthB_j);
            }
        }
    }
    
    public static void symmRankNMinus_U(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B) {
        final int heightB = B.row1 - B.row0;
        if (heightB > blockLength) {
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");
        }
        final int N = B.col1 - B.col0;
        if (A.col1 - A.col0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        }
        if (A.row1 - A.row0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");
        }
        for (int i = B.col0; i < B.col1; i += blockLength) {
            final int indexB_i = B.row0 * B.original.numCols + i * heightB;
            final int widthB_i = Math.min(blockLength, B.col1 - i);
            final int rowA = i - B.col0 + A.row0;
            final int heightA = Math.min(blockLength, A.row1 - rowA);
            for (int j = i; j < B.col1; j += blockLength) {
                final int widthB_j = Math.min(blockLength, B.col1 - j);
                final int indexA = rowA * A.original.numCols + (j - B.col0 + A.col0) * heightA;
                final int indexB_j = B.row0 * B.original.numCols + j * heightB;
                if (i == j) {
                    multTransABlockMinus_U(B.original.data, A.original.data, indexB_i, indexB_j, indexA, heightB, widthB_i, widthB_j);
                }
                else {
                    multTransABlockMinus(B.original.data, A.original.data, indexB_i, indexB_j, indexA, heightB, widthB_i, widthB_j);
                }
            }
        }
    }
    
    public static void symmRankNMinus_L(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B) {
        final int widthB = B.col1 - B.col0;
        if (widthB > blockLength) {
            throw new IllegalArgumentException("Width of B cannot be greater than the block length");
        }
        final int N = B.row1 - B.row0;
        if (A.col1 - A.col0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's height");
        }
        if (A.row1 - A.row0 != N) {
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's height");
        }
        for (int i = B.row0; i < B.row1; i += blockLength) {
            final int heightB_i = Math.min(blockLength, B.row1 - i);
            final int indexB_i = i * B.original.numCols + heightB_i * B.col0;
            final int rowA = i - B.row0 + A.row0;
            final int heightA = Math.min(blockLength, A.row1 - rowA);
            for (int j = B.row0; j <= i; j += blockLength) {
                final int widthB_j = Math.min(blockLength, B.row1 - j);
                final int indexA = rowA * A.original.numCols + (j - B.row0 + A.col0) * heightA;
                final int indexB_j = j * B.original.numCols + widthB_j * B.col0;
                if (i == j) {
                    multTransBBlockMinus_L(B.original.data, A.original.data, indexB_i, indexB_j, indexA, widthB, heightB_i, widthB_j);
                }
                else {
                    multTransBBlockMinus(B.original.data, A.original.data, indexB_i, indexB_j, indexA, widthB, heightB_i, widthB_j);
                }
            }
        }
    }
    
    protected static void multTransABlockMinus(final double[] dataA, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowB = indexB, endLoopK = rowB + heightA * widthC, startA = indexA; rowB != endLoopK; rowB += widthC, startA += widthA) {
            int a = startA;
            int c = indexC;
            final int endA = a + widthA;
            final int endB = rowB + widthC;
            while (a != endA) {
                final double valA = dataA[a++];
                int n;
                for (int b = rowB; b != endB; dataC[n] -= valA * dataA[b++]) {
                    n = c++;
                }
            }
        }
    }
    
    protected static void multTransABlockMinus_U(final double[] dataA, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < widthA; ++i) {
            for (int k = 0; k < heightA; ++k) {
                final double valA = dataA[k * widthA + i + indexA];
                int n;
                for (int b = k * widthC + indexB + i, c = i * widthC + indexC + i, endC = c - i + widthC; c != endC; n = c++, dataC[n] -= valA * dataA[b++]) {}
            }
        }
    }
    
    protected static void multTransBBlockMinus(final double[] dataA, final double[] dataC, final int indexA, final int indexB, final int indexC, final int widthA, final int heightA, final int widthC) {
        int rowA = indexA;
        int c = indexC;
        for (int i = 0; i < heightA; ++i, rowA += widthA) {
            final int endA = rowA + widthA;
            double sum;
            int n;
            for (int rowB = indexB, endLoopJ = c + widthC; c != endLoopJ; n = c++, dataC[n] -= sum, rowB += widthA) {
                int a;
                int b;
                for (a = rowA, b = rowB, sum = 0.0; a != endA; sum += dataA[a++] * dataA[b++]) {}
            }
        }
    }
    
    protected static void multTransBBlockMinus_L(final double[] dataA, final double[] dataC, final int indexA, final int indexB, final int indexC, final int widthA, final int heightA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            final int rowA = i * widthA + indexA;
            final int endA = rowA + widthA;
            int rowB = indexB;
            final int rowC = i * widthC + indexC;
            for (int j = 0; j <= i; ++j, rowB += widthA) {
                double sum = 0.0;
                for (int a = rowA, b = rowB; a != endA; sum += dataA[a++] * dataA[b++]) {}
                final int n = rowC + j;
                dataC[n] -= sum;
            }
        }
    }
}
