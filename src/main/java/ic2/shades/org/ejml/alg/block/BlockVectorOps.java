// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockVectorOps
{
    public static void scale_row(final int blockLength, final D1Submatrix64F A, int rowA, final double alpha, final D1Submatrix64F B, int rowB, int offset, final int end) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        final int startI = offset - offset % blockLength;
        offset %= blockLength;
        final int rowBlockA = A.row0 + rowA - rowA % blockLength;
        rowA %= blockLength;
        final int rowBlockB = B.row0 + rowB - rowB % blockLength;
        rowB %= blockLength;
        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);
        for (int i = startI; i < end; i += blockLength) {
            final int segment = Math.min(blockLength, end - i);
            final int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            final int widthB = Math.min(blockLength, B.col1 - B.col0 - i);
            int indexA = rowBlockA * A.original.numCols + (A.col0 + i) * heightA + rowA * widthA;
            int indexB = rowBlockB * B.original.numCols + (B.col0 + i) * heightB + rowB * widthB;
            if (i == startI) {
                indexA += offset;
                indexB += offset;
                for (int j = offset; j < segment; ++j) {
                    dataB[indexB++] = alpha * dataA[indexA++];
                }
            }
            else {
                for (int j = 0; j < segment; ++j) {
                    dataB[indexB++] = alpha * dataA[indexA++];
                }
            }
        }
    }
    
    public static void div_row(final int blockLength, final D1Submatrix64F A, int rowA, final double alpha, final D1Submatrix64F B, int rowB, int offset, final int end) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        final int startI = offset - offset % blockLength;
        offset %= blockLength;
        final int rowBlockA = A.row0 + rowA - rowA % blockLength;
        rowA %= blockLength;
        final int rowBlockB = B.row0 + rowB - rowB % blockLength;
        rowB %= blockLength;
        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);
        for (int i = startI; i < end; i += blockLength) {
            final int segment = Math.min(blockLength, end - i);
            final int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            final int widthB = Math.min(blockLength, B.col1 - B.col0 - i);
            int indexA = rowBlockA * A.original.numCols + (A.col0 + i) * heightA + rowA * widthA;
            int indexB = rowBlockB * B.original.numCols + (B.col0 + i) * heightB + rowB * widthB;
            if (i == startI) {
                indexA += offset;
                indexB += offset;
                for (int j = offset; j < segment; ++j) {
                    dataB[indexB++] = dataA[indexA++] / alpha;
                }
            }
            else {
                for (int j = 0; j < segment; ++j) {
                    dataB[indexB++] = dataA[indexA++] / alpha;
                }
            }
        }
    }
    
    public static void add_row(final int blockLength, final D1Submatrix64F A, final int rowA, final double alpha, final D1Submatrix64F B, final int rowB, final double beta, final D1Submatrix64F C, final int rowC, int offset, final int end) {
        final int heightA = Math.min(blockLength, A.row1 - A.row0);
        final int heightB = Math.min(blockLength, B.row1 - B.row0);
        final int heightC = Math.min(blockLength, C.row1 - C.row0);
        final int startI = offset - offset % blockLength;
        offset %= blockLength;
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        final double[] dataC = C.original.data;
        for (int i = startI; i < end; i += blockLength) {
            final int segment = Math.min(blockLength, end - i);
            final int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            final int widthB = Math.min(blockLength, B.col1 - B.col0 - i);
            final int widthC = Math.min(blockLength, C.col1 - C.col0 - i);
            int indexA = A.row0 * A.original.numCols + (A.col0 + i) * heightA + rowA * widthA;
            int indexB = B.row0 * B.original.numCols + (B.col0 + i) * heightB + rowB * widthB;
            int indexC = C.row0 * C.original.numCols + (C.col0 + i) * heightC + rowC * widthC;
            if (i == startI) {
                indexA += offset;
                indexB += offset;
                indexC += offset;
                for (int j = offset; j < segment; ++j) {
                    dataC[indexC++] = alpha * dataA[indexA++] + beta * dataB[indexB++];
                }
            }
            else {
                for (int j = 0; j < segment; ++j) {
                    dataC[indexC++] = alpha * dataA[indexA++] + beta * dataB[indexB++];
                }
            }
        }
    }
    
    public static double dot_row(final int blockLength, final D1Submatrix64F A, int rowA, final D1Submatrix64F B, int rowB, int offset, final int end) {
        final int startI = offset - offset % blockLength;
        offset %= blockLength;
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        double total = 0.0;
        final int rowBlockA = A.row0 + rowA - rowA % blockLength;
        rowA %= blockLength;
        final int rowBlockB = B.row0 + rowB - rowB % blockLength;
        rowB %= blockLength;
        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);
        if (A.col1 - A.col0 != B.col1 - B.col0) {
            throw new RuntimeException();
        }
        for (int i = startI; i < end; i += blockLength) {
            final int segment = Math.min(blockLength, end - i);
            final int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            final int widthB = Math.min(blockLength, B.col1 - B.col0 - i);
            int indexA = rowBlockA * A.original.numCols + (A.col0 + i) * heightA + rowA * widthA;
            int indexB = rowBlockB * B.original.numCols + (B.col0 + i) * heightB + rowB * widthB;
            if (i == startI) {
                indexA += offset;
                indexB += offset;
                for (int j = offset; j < segment; ++j) {
                    total += dataB[indexB++] * dataA[indexA++];
                }
            }
            else {
                for (int j = 0; j < segment; ++j) {
                    total += dataB[indexB++] * dataA[indexA++];
                }
            }
        }
        return total;
    }
    
    public static double dot_row_col(final int blockLength, final D1Submatrix64F A, int rowA, final D1Submatrix64F B, int colB, int offset, final int end) {
        final int startI = offset - offset % blockLength;
        offset %= blockLength;
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        double total = 0.0;
        final int rowBlockA = A.row0 + rowA - rowA % blockLength;
        rowA %= blockLength;
        final int colBlockB = B.col0 + colB - colB % blockLength;
        colB %= blockLength;
        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int widthB = Math.min(blockLength, B.col1 - colBlockB);
        if (A.col1 - A.col0 != B.col1 - B.col0) {
            throw new RuntimeException();
        }
        for (int i = startI; i < end; i += blockLength) {
            final int segment = Math.min(blockLength, end - i);
            final int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            final int heightB = Math.min(blockLength, B.row1 - B.row0 - i);
            int indexA = rowBlockA * A.original.numCols + (A.col0 + i) * heightA + rowA * widthA;
            int indexB = (B.row0 + i) * B.original.numCols + colBlockB * heightB + colB;
            if (i == startI) {
                indexA += offset;
                indexB += offset * widthB;
                for (int j = offset; j < segment; ++j, indexB += widthB) {
                    total += dataB[indexB] * dataA[indexA++];
                }
            }
            else {
                for (int j = 0; j < segment; ++j, indexB += widthB) {
                    total += dataB[indexB] * dataA[indexA++];
                }
            }
        }
        return total;
    }
}
