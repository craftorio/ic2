// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.simple.SimpleMatrix;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.MatrixFeatures;
import ic2.shades.org.ejml.ops.RandomMatrices;
import java.util.Random;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class BlockMatrixOps
{
    public static void convert(final DenseMatrix64F src, final BlockMatrix64F dst) {
        if (src.numRows != dst.numRows || src.numCols != dst.numCols) {
            throw new IllegalArgumentException("Must be the same size.");
        }
        for (int i = 0; i < dst.numRows; i += dst.blockLength) {
            final int blockHeight = Math.min(dst.blockLength, dst.numRows - i);
            for (int j = 0; j < dst.numCols; j += dst.blockLength) {
                final int blockWidth = Math.min(dst.blockLength, dst.numCols - j);
                int indexDst = i * dst.numCols + blockHeight * j;
                int indexSrcRow = i * dst.numCols + j;
                for (int k = 0; k < blockHeight; ++k) {
                    System.arraycopy(src.data, indexSrcRow, dst.data, indexDst, blockWidth);
                    indexDst += blockWidth;
                    indexSrcRow += dst.numCols;
                }
            }
        }
    }
    
    public static void convertRowToBlock(final int numRows, final int numCols, final int blockLength, final double[] data, final double[] tmp) {
        final int minLength = Math.min(blockLength, numRows) * numCols;
        if (tmp.length < minLength) {
            throw new IllegalArgumentException("tmp must be at least " + minLength + " long ");
        }
        for (int i = 0; i < numRows; i += blockLength) {
            final int blockHeight = Math.min(blockLength, numRows - i);
            System.arraycopy(data, i * numCols, tmp, 0, blockHeight * numCols);
            for (int j = 0; j < numCols; j += blockLength) {
                final int blockWidth = Math.min(blockLength, numCols - j);
                int indexDst = i * numCols + blockHeight * j;
                int indexSrcRow = j;
                for (int k = 0; k < blockHeight; ++k) {
                    System.arraycopy(tmp, indexSrcRow, data, indexDst, blockWidth);
                    indexDst += blockWidth;
                    indexSrcRow += numCols;
                }
            }
        }
    }
    
    public static DenseMatrix64F convert(final BlockMatrix64F src, DenseMatrix64F dst) {
        if (dst != null) {
            if (dst.numRows != src.numRows || dst.numCols != src.numCols) {
                throw new IllegalArgumentException("Must be the same size.");
            }
        }
        else {
            dst = new DenseMatrix64F(src.numRows, src.numCols);
        }
        for (int i = 0; i < src.numRows; i += src.blockLength) {
            final int blockHeight = Math.min(src.blockLength, src.numRows - i);
            for (int j = 0; j < src.numCols; j += src.blockLength) {
                final int blockWidth = Math.min(src.blockLength, src.numCols - j);
                int indexSrc = i * src.numCols + blockHeight * j;
                int indexDstRow = i * dst.numCols + j;
                for (int k = 0; k < blockHeight; ++k) {
                    System.arraycopy(src.data, indexSrc, dst.data, indexDstRow, blockWidth);
                    indexSrc += blockWidth;
                    indexDstRow += dst.numCols;
                }
            }
        }
        return dst;
    }
    
    public static void convertBlockToRow(final int numRows, final int numCols, final int blockLength, final double[] data, final double[] tmp) {
        final int minLength = Math.min(blockLength, numRows) * numCols;
        if (tmp.length < minLength) {
            throw new IllegalArgumentException("tmp must be at least " + minLength + " long and not " + tmp.length);
        }
        for (int i = 0; i < numRows; i += blockLength) {
            final int blockHeight = Math.min(blockLength, numRows - i);
            System.arraycopy(data, i * numCols, tmp, 0, blockHeight * numCols);
            for (int j = 0; j < numCols; j += blockLength) {
                final int blockWidth = Math.min(blockLength, numCols - j);
                int indexSrc = blockHeight * j;
                int indexDstRow = i * numCols + j;
                for (int k = 0; k < blockHeight; ++k) {
                    System.arraycopy(tmp, indexSrc, data, indexDstRow, blockWidth);
                    indexSrc += blockWidth;
                    indexDstRow += numCols;
                }
            }
        }
    }
    
    public static void convertTranSrc(final DenseMatrix64F src, final BlockMatrix64F dst) {
        if (src.numRows != dst.numCols || src.numCols != dst.numRows) {
            throw new IllegalArgumentException("Incompatible matrix shapes.");
        }
        for (int i = 0; i < dst.numRows; i += dst.blockLength) {
            final int blockHeight = Math.min(dst.blockLength, dst.numRows - i);
            for (int j = 0; j < dst.numCols; j += dst.blockLength) {
                final int blockWidth = Math.min(dst.blockLength, dst.numCols - j);
                final int indexDst = i * dst.numCols + blockHeight * j;
                final int indexSrc = j * src.numCols + i;
                for (int l = 0; l < blockWidth; ++l) {
                    int rowSrc = indexSrc + l * src.numCols;
                    for (int rowDst = indexDst + l, k = 0; k < blockHeight; ++k, rowDst += blockWidth) {
                        dst.data[rowDst] = src.data[rowSrc++];
                    }
                }
            }
        }
    }
    
    public static void mult(final BlockMatrix64F A, final BlockMatrix64F B, final BlockMatrix64F C) {
        if (A.numCols != B.numRows) {
            throw new IllegalArgumentException("Columns in A are incompatible with rows in B");
        }
        if (A.numRows != C.numRows) {
            throw new IllegalArgumentException("Rows in A are incompatible with rows in C");
        }
        if (B.numCols != C.numCols) {
            throw new IllegalArgumentException("Columns in B are incompatible with columns in C");
        }
        if (A.blockLength != B.blockLength || A.blockLength != C.blockLength) {
            throw new IllegalArgumentException("Block lengths are not all the same.");
        }
        final int blockLength = A.blockLength;
        final D1Submatrix64F Asub = new D1Submatrix64F(A, 0, A.numRows, 0, A.numCols);
        final D1Submatrix64F Bsub = new D1Submatrix64F(B, 0, B.numRows, 0, B.numCols);
        final D1Submatrix64F Csub = new D1Submatrix64F(C, 0, C.numRows, 0, C.numCols);
        BlockMultiplication.mult(blockLength, Asub, Bsub, Csub);
    }
    
    public static void multTransA(final BlockMatrix64F A, final BlockMatrix64F B, final BlockMatrix64F C) {
        if (A.numRows != B.numRows) {
            throw new IllegalArgumentException("Rows in A are incompatible with rows in B");
        }
        if (A.numCols != C.numRows) {
            throw new IllegalArgumentException("Columns in A are incompatible with rows in C");
        }
        if (B.numCols != C.numCols) {
            throw new IllegalArgumentException("Columns in B are incompatible with columns in C");
        }
        if (A.blockLength != B.blockLength || A.blockLength != C.blockLength) {
            throw new IllegalArgumentException("Block lengths are not all the same.");
        }
        final int blockLength = A.blockLength;
        final D1Submatrix64F Asub = new D1Submatrix64F(A, 0, A.numRows, 0, A.numCols);
        final D1Submatrix64F Bsub = new D1Submatrix64F(B, 0, B.numRows, 0, B.numCols);
        final D1Submatrix64F Csub = new D1Submatrix64F(C, 0, C.numRows, 0, C.numCols);
        BlockMultiplication.multTransA(blockLength, Asub, Bsub, Csub);
    }
    
    public static void multTransB(final BlockMatrix64F A, final BlockMatrix64F B, final BlockMatrix64F C) {
        if (A.numCols != B.numCols) {
            throw new IllegalArgumentException("Columns in A are incompatible with columns in B");
        }
        if (A.numRows != C.numRows) {
            throw new IllegalArgumentException("Rows in A are incompatible with rows in C");
        }
        if (B.numRows != C.numCols) {
            throw new IllegalArgumentException("Rows in B are incompatible with columns in C");
        }
        if (A.blockLength != B.blockLength || A.blockLength != C.blockLength) {
            throw new IllegalArgumentException("Block lengths are not all the same.");
        }
        final int blockLength = A.blockLength;
        final D1Submatrix64F Asub = new D1Submatrix64F(A, 0, A.numRows, 0, A.numCols);
        final D1Submatrix64F Bsub = new D1Submatrix64F(B, 0, B.numRows, 0, B.numCols);
        final D1Submatrix64F Csub = new D1Submatrix64F(C, 0, C.numRows, 0, C.numCols);
        BlockMultiplication.multTransB(blockLength, Asub, Bsub, Csub);
    }
    
    public static BlockMatrix64F transpose(final BlockMatrix64F A, BlockMatrix64F A_tran) {
        if (A_tran != null) {
            if (A.numRows != A_tran.numCols || A.numCols != A_tran.numRows) {
                throw new IllegalArgumentException("Incompatible dimensions.");
            }
            if (A.blockLength != A_tran.blockLength) {
                throw new IllegalArgumentException("Incompatible block size.");
            }
        }
        else {
            A_tran = new BlockMatrix64F(A.numCols, A.numRows, A.blockLength);
        }
        for (int i = 0; i < A.numRows; i += A.blockLength) {
            final int blockHeight = Math.min(A.blockLength, A.numRows - i);
            for (int j = 0; j < A.numCols; j += A.blockLength) {
                final int blockWidth = Math.min(A.blockLength, A.numCols - j);
                final int indexA = i * A.numCols + blockHeight * j;
                final int indexC = j * A_tran.numCols + blockWidth * i;
                transposeBlock(A, A_tran, indexA, indexC, blockWidth, blockHeight);
            }
        }
        return A_tran;
    }
    
    private static void transposeBlock(final BlockMatrix64F A, final BlockMatrix64F A_tran, final int indexA, final int indexC, final int width, final int height) {
        for (int i = 0; i < height; ++i) {
            int rowIndexC = indexC + i;
            for (int rowIndexA = indexA + width * i, end = rowIndexA + width; rowIndexA < end; ++rowIndexA) {
                A_tran.data[rowIndexC] = A.data[rowIndexA];
                rowIndexC += height;
            }
        }
    }
    
    public static BlockMatrix64F createRandom(final int numRows, final int numCols, final double min, final double max, final Random rand) {
        final BlockMatrix64F ret = new BlockMatrix64F(numRows, numCols);
        RandomMatrices.setRandom(ret, min, max, rand);
        return ret;
    }
    
    public static BlockMatrix64F createRandom(final int numRows, final int numCols, final double min, final double max, final Random rand, final int blockLength) {
        final BlockMatrix64F ret = new BlockMatrix64F(numRows, numCols, blockLength);
        RandomMatrices.setRandom(ret, min, max, rand);
        return ret;
    }
    
    public static BlockMatrix64F convert(final DenseMatrix64F A, final int blockLength) {
        final BlockMatrix64F ret = new BlockMatrix64F(A.numRows, A.numCols, blockLength);
        convert(A, ret);
        return ret;
    }
    
    public static BlockMatrix64F convert(final DenseMatrix64F A) {
        final BlockMatrix64F ret = new BlockMatrix64F(A.numRows, A.numCols);
        convert(A, ret);
        return ret;
    }
    
    public static boolean isEquals(final BlockMatrix64F A, final BlockMatrix64F B) {
        return A.blockLength == B.blockLength && MatrixFeatures.isEquals(A, B);
    }
    
    public static boolean isEquals(final BlockMatrix64F A, final BlockMatrix64F B, final double tol) {
        return A.blockLength == B.blockLength && MatrixFeatures.isEquals(A, B, tol);
    }
    
    public static void zeroTriangle(final boolean upper, final BlockMatrix64F A) {
        final int blockLength = A.blockLength;
        if (upper) {
            for (int i = 0; i < A.numRows; i += blockLength) {
                final int h = Math.min(blockLength, A.numRows - i);
                for (int j = i; j < A.numCols; j += blockLength) {
                    final int w = Math.min(blockLength, A.numCols - j);
                    final int index = i * A.numCols + h * j;
                    if (j == i) {
                        for (int k = 0; k < h; ++k) {
                            for (int l = k + 1; l < w; ++l) {
                                A.data[index + w * k + l] = 0.0;
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < h; ++k) {
                            for (int l = 0; l < w; ++l) {
                                A.data[index + w * k + l] = 0.0;
                            }
                        }
                    }
                }
            }
        }
        else {
            for (int i = 0; i < A.numRows; i += blockLength) {
                final int h = Math.min(blockLength, A.numRows - i);
                for (int j = 0; j <= i; j += blockLength) {
                    final int w = Math.min(blockLength, A.numCols - j);
                    final int index = i * A.numCols + h * j;
                    if (j == i) {
                        for (int k = 0; k < h; ++k) {
                            for (int z = Math.min(k, w), m = 0; m < z; ++m) {
                                A.data[index + w * k + m] = 0.0;
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < h; ++k) {
                            for (int l = 0; l < w; ++l) {
                                A.data[index + w * k + l] = 0.0;
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void copyTriangle(final boolean upper, final BlockMatrix64F src, final BlockMatrix64F dst) {
        if (src.blockLength != dst.blockLength) {
            throw new IllegalArgumentException("Block size is different");
        }
        if (src.numRows < dst.numRows) {
            throw new IllegalArgumentException("The src has fewer rows than dst");
        }
        if (src.numCols < dst.numCols) {
            throw new IllegalArgumentException("The src has fewer columns than dst");
        }
        final int blockLength = src.blockLength;
        final int numRows = Math.min(src.numRows, dst.numRows);
        final int numCols = Math.min(src.numCols, dst.numCols);
        if (upper) {
            for (int i = 0; i < numRows; i += blockLength) {
                final int heightSrc = Math.min(blockLength, src.numRows - i);
                final int heightDst = Math.min(blockLength, dst.numRows - i);
                for (int j = i; j < numCols; j += blockLength) {
                    final int widthSrc = Math.min(blockLength, src.numCols - j);
                    final int widthDst = Math.min(blockLength, dst.numCols - j);
                    final int indexSrc = i * src.numCols + heightSrc * j;
                    final int indexDst = i * dst.numCols + heightDst * j;
                    if (j == i) {
                        for (int k = 0; k < heightDst; ++k) {
                            for (int l = k; l < widthDst; ++l) {
                                dst.data[indexDst + widthDst * k + l] = src.data[indexSrc + widthSrc * k + l];
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < heightDst; ++k) {
                            System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                        }
                    }
                }
            }
        }
        else {
            for (int i = 0; i < numRows; i += blockLength) {
                final int heightSrc = Math.min(blockLength, src.numRows - i);
                final int heightDst = Math.min(blockLength, dst.numRows - i);
                for (int j = 0; j <= i; j += blockLength) {
                    final int widthSrc = Math.min(blockLength, src.numCols - j);
                    final int widthDst = Math.min(blockLength, dst.numCols - j);
                    final int indexSrc = i * src.numCols + heightSrc * j;
                    final int indexDst = i * dst.numCols + heightDst * j;
                    if (j == i) {
                        for (int k = 0; k < heightDst; ++k) {
                            for (int z = Math.min(k + 1, widthDst), m = 0; m < z; ++m) {
                                dst.data[indexDst + widthDst * k + m] = src.data[indexSrc + widthSrc * k + m];
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < heightDst; ++k) {
                            System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                        }
                    }
                }
            }
        }
    }
    
    public static void set(final BlockMatrix64F A, final double value) {
        CommonOps.fill(A, value);
    }
    
    public static void setIdentity(final BlockMatrix64F A) {
        final int minLength = Math.min(A.numRows, A.numCols);
        CommonOps.fill(A, 0.0);
        for (int blockLength = A.blockLength, i = 0; i < minLength; i += blockLength) {
            final int h = Math.min(blockLength, A.numRows - i);
            final int w = Math.min(blockLength, A.numCols - i);
            final int index = i * A.numCols + h * i;
            for (int m = Math.min(h, w), k = 0; k < m; ++k) {
                A.data[index + k * w + k] = 1.0;
            }
        }
    }
    
    public static SimpleMatrix convertSimple(final BlockMatrix64F A) {
        final DenseMatrix64F B = convert(A, null);
        return SimpleMatrix.wrap(B);
    }
    
    public static BlockMatrix64F identity(final int numRows, final int numCols, final int blockLength) {
        final BlockMatrix64F A = new BlockMatrix64F(numRows, numCols, blockLength);
        for (int minLength = Math.min(numRows, numCols), i = 0; i < minLength; i += blockLength) {
            final int h = Math.min(blockLength, A.numRows - i);
            final int w = Math.min(blockLength, A.numCols - i);
            final int index = i * A.numCols + h * i;
            for (int m = Math.min(h, w), k = 0; k < m; ++k) {
                A.data[index + k * w + k] = 1.0;
            }
        }
        return A;
    }
    
    public static void checkIdenticalShape(final BlockMatrix64F A, final BlockMatrix64F B) {
        if (A.blockLength != B.blockLength) {
            throw new IllegalArgumentException("Block size is different");
        }
        if (A.numRows != B.numRows) {
            throw new IllegalArgumentException("Number of rows is different");
        }
        if (A.numCols != B.numCols) {
            throw new IllegalArgumentException("NUmber of columns is different");
        }
    }
    
    public static void extractAligned(final BlockMatrix64F src, final BlockMatrix64F dst) {
        if (src.blockLength != dst.blockLength) {
            throw new IllegalArgumentException("Block size is different");
        }
        if (src.numRows < dst.numRows) {
            throw new IllegalArgumentException("The src has fewer rows than dst");
        }
        if (src.numCols < dst.numCols) {
            throw new IllegalArgumentException("The src has fewer columns than dst");
        }
        final int blockLength = src.blockLength;
        final int numRows = Math.min(src.numRows, dst.numRows);
        final int numCols = Math.min(src.numCols, dst.numCols);
        for (int i = 0; i < numRows; i += blockLength) {
            final int heightSrc = Math.min(blockLength, src.numRows - i);
            final int heightDst = Math.min(blockLength, dst.numRows - i);
            for (int j = 0; j < numCols; j += blockLength) {
                final int widthSrc = Math.min(blockLength, src.numCols - j);
                final int widthDst = Math.min(blockLength, dst.numCols - j);
                final int indexSrc = i * src.numCols + heightSrc * j;
                final int indexDst = i * dst.numCols + heightDst * j;
                for (int k = 0; k < heightDst; ++k) {
                    System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                }
            }
        }
    }
    
    public static boolean blockAligned(final int blockLength, final D1Submatrix64F A) {
        return A.col0 % blockLength == 0 && A.row0 % blockLength == 0 && (A.col1 % blockLength == 0 || A.col1 == A.original.numCols) && (A.row1 % blockLength == 0 || A.row1 == A.original.numRows);
    }
}
