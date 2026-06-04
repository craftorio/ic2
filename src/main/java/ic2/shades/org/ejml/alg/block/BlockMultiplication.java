// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockMultiplication
{
    public static void mult(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.row0; i < A.row1; i += blockLength) {
            final int heightA = Math.min(blockLength, A.row1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
                for (int k = A.col0; k < A.col1; k += blockLength) {
                    final int widthA = Math.min(blockLength, A.col1 - k);
                    final int indexA = i * A.original.numCols + k * heightA;
                    final int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
                    if (k == A.col0) {
                        BlockInnerMultiplication.blockMultSet(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                    else {
                        BlockInnerMultiplication.blockMultPlus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                }
            }
        }
    }
    
    public static void multPlus(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.row0; i < A.row1; i += blockLength) {
            final int heightA = Math.min(blockLength, A.row1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
                for (int k = A.col0; k < A.col1; k += blockLength) {
                    final int widthA = Math.min(blockLength, A.col1 - k);
                    final int indexA = i * A.original.numCols + k * heightA;
                    final int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
                    BlockInnerMultiplication.blockMultPlus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
            }
        }
    }
    
    public static void multMinus(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        checkInput(blockLength, A, B, C);
        for (int i = A.row0; i < A.row1; i += blockLength) {
            final int heightA = Math.min(blockLength, A.row1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
                for (int k = A.col0; k < A.col1; k += blockLength) {
                    final int widthA = Math.min(blockLength, A.col1 - k);
                    final int indexA = i * A.original.numCols + k * heightA;
                    final int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
                    BlockInnerMultiplication.blockMultMinus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
            }
        }
    }
    
    private static void checkInput(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        final int Arow = A.getRows();
        final int Acol = A.getCols();
        final int Brow = B.getRows();
        final int Bcol = B.getCols();
        final int Crow = C.getRows();
        final int Ccol = C.getCols();
        if (Arow != Crow) {
            throw new RuntimeException("Mismatch A and C rows");
        }
        if (Bcol != Ccol) {
            throw new RuntimeException("Mismatch B and C columns");
        }
        if (Acol != Brow) {
            throw new RuntimeException("Mismatch A columns and B rows");
        }
        if (!BlockMatrixOps.blockAligned(blockLength, A)) {
            throw new RuntimeException("Sub-Matrix A is not block aligned");
        }
        if (!BlockMatrixOps.blockAligned(blockLength, B)) {
            throw new RuntimeException("Sub-Matrix B is not block aligned");
        }
        if (!BlockMatrixOps.blockAligned(blockLength, C)) {
            throw new RuntimeException("Sub-Matrix C is not block aligned");
        }
    }
    
    public static void multTransA(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.col0; i < A.col1; i += blockLength) {
            final int widthA = Math.min(blockLength, A.col1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
                for (int k = A.row0; k < A.row1; k += blockLength) {
                    final int heightA = Math.min(blockLength, A.row1 - k);
                    final int indexA = k * A.original.numCols + i * heightA;
                    final int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
                    if (k == A.row0) {
                        BlockInnerMultiplication.blockMultSetTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                    else {
                        BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                }
            }
        }
    }
    
    public static void multPlusTransA(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.col0; i < A.col1; i += blockLength) {
            final int widthA = Math.min(blockLength, A.col1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
                for (int k = A.row0; k < A.row1; k += blockLength) {
                    final int heightA = Math.min(blockLength, A.row1 - k);
                    final int indexA = k * A.original.numCols + i * heightA;
                    final int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
                    BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
            }
        }
    }
    
    public static void multMinusTransA(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.col0; i < A.col1; i += blockLength) {
            final int widthA = Math.min(blockLength, A.col1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
                for (int k = A.row0; k < A.row1; k += blockLength) {
                    final int heightA = Math.min(blockLength, A.row1 - k);
                    final int indexA = k * A.original.numCols + i * heightA;
                    final int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
                    BlockInnerMultiplication.blockMultMinusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
            }
        }
    }
    
    public static void multTransB(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        for (int i = A.row0; i < A.row1; i += blockLength) {
            final int heightA = Math.min(blockLength, A.row1 - i);
            for (int j = B.row0; j < B.row1; j += blockLength) {
                final int widthC = Math.min(blockLength, B.row1 - j);
                final int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.row0 + C.col0) * heightA;
                for (int k = A.col0; k < A.col1; k += blockLength) {
                    final int widthA = Math.min(blockLength, A.col1 - k);
                    final int indexA = i * A.original.numCols + k * heightA;
                    final int indexB = j * B.original.numCols + (k - A.col0 + B.col0) * widthC;
                    if (k == A.col0) {
                        BlockInnerMultiplication.blockMultSetTransB(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthC);
                    }
                    else {
                        BlockInnerMultiplication.blockMultPlusTransB(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthC);
                    }
                }
            }
        }
    }
}
