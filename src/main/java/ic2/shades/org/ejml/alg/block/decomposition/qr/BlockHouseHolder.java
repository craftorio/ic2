// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.qr;

import ic2.shades.org.ejml.alg.block.BlockInnerMultiplication;
import ic2.shades.org.ejml.alg.block.BlockVectorOps;
import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockHouseHolder
{
    public static boolean decomposeQR_block_col(final int blockLength, final D1Submatrix64F Y, final double[] gamma) {
        final int width = Y.col1 - Y.col0;
        final int height = Y.row1 - Y.row0;
        for (int min = Math.min(width, height), i = 0; i < min; ++i) {
            if (!computeHouseHolderCol(blockLength, Y, gamma, i)) {
                return false;
            }
            rank1UpdateMultR_Col(blockLength, Y, i, gamma[Y.col0 + i]);
        }
        return true;
    }
    
    public static boolean computeHouseHolderCol(final int blockLength, final D1Submatrix64F Y, final double[] gamma, final int i) {
        final double max = findMaxCol(blockLength, Y, i);
        if (max == 0.0) {
            return false;
        }
        double tau = computeTauAndDivideCol(blockLength, Y, i, max);
        final double u_0 = Y.get(i, i) + tau;
        divideElementsCol(blockLength, Y, i, u_0);
        gamma[Y.col0 + i] = u_0 / tau;
        tau *= max;
        Y.set(i, i, -tau);
        return true;
    }
    
    public static boolean computeHouseHolderRow(final int blockLength, final D1Submatrix64F Y, final double[] gamma, final int i) {
        final double max = findMaxRow(blockLength, Y, i, i + 1);
        if (max == 0.0) {
            return false;
        }
        final double tau = computeTauAndDivideRow(blockLength, Y, i, i + 1, max);
        final double u_0 = Y.get(i, i + 1) + tau;
        BlockVectorOps.div_row(blockLength, Y, i, u_0, Y, i, i + 1, Y.col1 - Y.col0);
        gamma[Y.row0 + i] = u_0 / tau;
        Y.set(i, i + 1, -tau * max);
        return true;
    }
    
    public static void rank1UpdateMultR_Col(final int blockLength, final D1Submatrix64F A, final int col, final double gamma) {
        final int width = Math.min(blockLength, A.col1 - A.col0);
        final double[] dataA = A.original.data;
        for (int j = col + 1; j < width; ++j) {
            double total = innerProdCol(blockLength, A, col, width, j, width);
            total *= gamma;
            for (int i = A.row0; i < A.row1; i += blockLength) {
                final int height = Math.min(blockLength, A.row1 - i);
                int indexU = i * A.original.numCols + height * A.col0 + col;
                int indexA = i * A.original.numCols + height * A.col0 + j;
                if (i == A.row0) {
                    indexU += width * (col + 1);
                    indexA += width * col;
                    final double[] array = dataA;
                    final int n = indexA;
                    array[n] -= total;
                    indexA += width;
                    for (int k = col + 1; k < height; ++k, indexU += width, indexA += width) {
                        final double[] array2 = dataA;
                        final int n2 = indexA;
                        array2[n2] -= total * dataA[indexU];
                    }
                }
                else {
                    for (int endU = indexU + width * height; indexU != endU; indexU += width, indexA += width) {
                        final double[] array3 = dataA;
                        final int n3 = indexA;
                        array3[n3] -= total * dataA[indexU];
                    }
                }
            }
        }
    }
    
    public static void rank1UpdateMultR_TopRow(final int blockLength, final D1Submatrix64F A, final int col, final double gamma) {
        final double[] dataA = A.original.data;
        final int widthCol = Math.min(blockLength, A.col1 - col);
        for (int colStartJ = A.col0 + blockLength; colStartJ < A.col1; colStartJ += blockLength) {
            for (int widthJ = Math.min(blockLength, A.col1 - colStartJ), j = 0; j < widthJ; ++j) {
                final double total = innerProdCol(blockLength, A, col, widthCol, colStartJ - A.col0 + j, widthJ) * gamma;
                final int i = A.row0;
                final int height = Math.min(blockLength, A.row1 - i);
                int indexU = i * A.original.numCols + height * A.col0 + col;
                int indexA = i * A.original.numCols + height * colStartJ + j;
                indexU += widthCol * (col + 1);
                indexA += widthJ * col;
                final double[] array = dataA;
                final int n = indexA;
                array[n] -= total;
                indexA += widthJ;
                for (int k = col + 1; k < height; ++k, indexU += widthCol, indexA += widthJ) {
                    final double[] array2 = dataA;
                    final int n2 = indexA;
                    array2[n2] -= total * dataA[indexU];
                }
            }
        }
    }
    
    public static void rank1UpdateMultL_Row(final int blockLength, final D1Submatrix64F A, final int row, final int colStart, final double gamma) {
        final int height = Math.min(blockLength, A.row1 - A.row0);
        final double[] dataA = A.original.data;
        final int zeroOffset = colStart - row;
        for (int i = row + 1; i < height; ++i) {
            double total = innerProdRow(blockLength, A, row, A, i, zeroOffset);
            total *= gamma;
            for (int j = A.col0; j < A.col1; j += blockLength) {
                final int width = Math.min(blockLength, A.col1 - j);
                int indexU = A.row0 * A.original.numCols + height * j + row * width;
                int indexA = A.row0 * A.original.numCols + height * j + i * width;
                if (j == A.col0) {
                    indexU += colStart + 1;
                    indexA += colStart;
                    final double[] array = dataA;
                    final int n = indexA++;
                    array[n] -= total;
                    for (int k = colStart + 1; k < width; ++k) {
                        final double[] array2 = dataA;
                        final int n2 = indexA++;
                        array2[n2] -= total * dataA[indexU++];
                    }
                }
                else {
                    for (int k = 0; k < width; ++k) {
                        final double[] array3 = dataA;
                        final int n3 = indexA++;
                        array3[n3] -= total * dataA[indexU++];
                    }
                }
            }
        }
    }
    
    public static void rank1UpdateMultL_LeftCol(final int blockLength, final D1Submatrix64F A, final int row, final double gamma, final int zeroOffset) {
        final int heightU = Math.min(blockLength, A.row1 - A.row0);
        final int width = Math.min(blockLength, A.col1 - A.col0);
        final double[] data = A.original.data;
        for (int blockStart = A.row0 + blockLength; blockStart < A.row1; blockStart += blockLength) {
            for (int heightA = Math.min(blockLength, A.row1 - blockStart), i = 0; i < heightA; ++i) {
                double total = innerProdRow(blockLength, A, row, A, i + (blockStart - A.row0), zeroOffset);
                total *= gamma;
                int indexU = A.row0 * A.original.numCols + heightU * A.col0 + row * width;
                int indexA = blockStart * A.original.numCols + heightA * A.col0 + i * width;
                indexU += zeroOffset + 1;
                indexA += zeroOffset;
                final double[] array = data;
                final int n = indexA++;
                array[n] -= total;
                for (int k = zeroOffset + 1; k < width; ++k) {
                    final double[] array2 = data;
                    final int n2 = indexA++;
                    array2[n2] -= total * data[indexU++];
                }
            }
        }
    }
    
    public static double innerProdCol(final int blockLength, final D1Submatrix64F A, int colA, final int widthA, int colB, final int widthB) {
        double total = 0.0;
        final double[] data = A.original.data;
        final int colBlockA = A.col0 + colA - colA % blockLength;
        final int colBlockB = A.col0 + colB - colB % blockLength;
        colA %= blockLength;
        colB %= blockLength;
        for (int i = A.row0; i < A.row1; i += blockLength) {
            final int height = Math.min(blockLength, A.row1 - i);
            int indexA = i * A.original.numCols + height * colBlockA + colA;
            int indexB = i * A.original.numCols + height * colBlockB + colB;
            if (i == A.row0) {
                indexA += widthA * (colA + 1);
                indexB += widthB * colA;
                total = data[indexB];
                indexB += widthB;
                for (int endA = indexA + (height - colA - 1) * widthA; indexA != endA; indexA += widthA, indexB += widthB) {
                    total += data[indexA] * data[indexB];
                }
            }
            else {
                for (int endA = indexA + widthA * height; indexA != endA; indexA += widthA, indexB += widthB) {
                    total += data[indexA] * data[indexB];
                }
            }
        }
        return total;
    }
    
    public static double innerProdRow(final int blockLength, final D1Submatrix64F A, final int rowA, final D1Submatrix64F B, final int rowB, final int zeroOffset) {
        final int offset = rowA + zeroOffset;
        if (offset + B.col0 >= B.col1) {
            return 0.0;
        }
        double total = B.get(rowB, offset);
        total += BlockVectorOps.dot_row(blockLength, A, rowA, B, rowB, offset + 1, A.col1 - A.col0);
        return total;
    }
    
    public static void add_row(final int blockLength, final D1Submatrix64F A, final int rowA, final double alpha, final D1Submatrix64F B, final int rowB, final double beta, final D1Submatrix64F C, final int rowC, final int zeroOffset, final int end) {
        final int offset = rowA + zeroOffset;
        if (C.col0 + offset >= C.col1) {
            return;
        }
        C.set(rowC, offset, alpha + B.get(rowB, offset) * beta);
        BlockVectorOps.add_row(blockLength, A, rowA, alpha, B, rowB, beta, C, rowC, offset + 1, end);
    }
    
    public static void divideElementsCol(final int blockLength, final D1Submatrix64F Y, final int col, final double val) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);
        final double[] dataY = Y.original.data;
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int height = Math.min(blockLength, Y.row1 - i);
            int index = i * Y.original.numCols + height * Y.col0 + col;
            if (i == Y.row0) {
                index += width * (col + 1);
                for (int k = col + 1; k < height; ++k, index += width) {
                    final double[] array = dataY;
                    final int n = index;
                    array[n] /= val;
                }
            }
            else {
                for (int endIndex = index + width * height; index != endIndex; index += width) {
                    final double[] array2 = dataY;
                    final int n2 = index;
                    array2[n2] /= val;
                }
            }
        }
    }
    
    public static void scale_row(final int blockLength, final D1Submatrix64F Y, final D1Submatrix64F W, final int row, final int zeroOffset, final double val) {
        final int offset = row + zeroOffset;
        if (offset >= W.col1 - W.col0) {
            return;
        }
        W.set(row, offset, val);
        BlockVectorOps.scale_row(blockLength, Y, row, val, W, row, offset + 1, Y.col1 - Y.col0);
    }
    
    public static double computeTauAndDivideCol(final int blockLength, final D1Submatrix64F Y, final int col, final double max) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);
        final double[] dataY = Y.original.data;
        double top = 0.0;
        double norm2 = 0.0;
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int height = Math.min(blockLength, Y.row1 - i);
            int index = i * Y.original.numCols + height * Y.col0 + col;
            if (i == Y.row0) {
                index += width * col;
                final double[] array = dataY;
                final int n = index;
                final double n2 = array[n] / max;
                array[n] = n2;
                top = n2;
                norm2 += top * top;
                index += width;
                for (int k = col + 1; k < height; ++k, index += width) {
                    final double[] array2 = dataY;
                    final int n3 = index;
                    final double n4 = array2[n3] / max;
                    array2[n3] = n4;
                    final double val = n4;
                    norm2 += val * val;
                }
            }
            else {
                for (int k = 0; k < height; ++k, index += width) {
                    final double[] array3 = dataY;
                    final int n5 = index;
                    final double n6 = array3[n5] / max;
                    array3[n5] = n6;
                    final double val = n6;
                    norm2 += val * val;
                }
            }
        }
        norm2 = Math.sqrt(norm2);
        if (top < 0.0) {
            norm2 = -norm2;
        }
        return norm2;
    }
    
    public static double computeTauAndDivideRow(final int blockLength, final D1Submatrix64F Y, final int row, int colStart, final double max) {
        final int height = Math.min(blockLength, Y.row1 - Y.row0);
        final double[] dataY = Y.original.data;
        double top = 0.0;
        double norm2 = 0.0;
        final int startJ = Y.col0 + colStart - colStart % blockLength;
        colStart %= blockLength;
        for (int j = startJ; j < Y.col1; j += blockLength) {
            final int width = Math.min(blockLength, Y.col1 - j);
            int index = Y.row0 * Y.original.numCols + height * j + row * width;
            if (j == startJ) {
                index += colStart;
                final double[] array = dataY;
                final int n = index;
                final double n2 = array[n] / max;
                array[n] = n2;
                top = n2;
                norm2 += top * top;
                ++index;
                for (int k = colStart + 1; k < width; ++k) {
                    final double[] array2 = dataY;
                    final int n3 = index++;
                    final double n4 = array2[n3] / max;
                    array2[n3] = n4;
                    final double val = n4;
                    norm2 += val * val;
                }
            }
            else {
                for (int k = 0; k < width; ++k) {
                    final double[] array3 = dataY;
                    final int n5 = index++;
                    final double n6 = array3[n5] / max;
                    array3[n5] = n6;
                    final double val = n6;
                    norm2 += val * val;
                }
            }
        }
        norm2 = Math.sqrt(norm2);
        if (top < 0.0) {
            norm2 = -norm2;
        }
        return norm2;
    }
    
    public static double findMaxCol(final int blockLength, final D1Submatrix64F Y, final int col) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);
        final double[] dataY = Y.original.data;
        double max = 0.0;
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int height = Math.min(blockLength, Y.row1 - i);
            int index = i * Y.original.numCols + height * Y.col0 + col;
            if (i == Y.row0) {
                index += width * col;
                for (int k = col; k < height; ++k, index += width) {
                    final double v = Math.abs(dataY[index]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
            else {
                for (int k = 0; k < height; ++k, index += width) {
                    final double v = Math.abs(dataY[index]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
        }
        return max;
    }
    
    public static double findMaxRow(final int blockLength, final D1Submatrix64F Y, final int row, final int colStart) {
        final int height = Math.min(blockLength, Y.row1 - Y.row0);
        final double[] dataY = Y.original.data;
        double max = 0.0;
        for (int j = Y.col0; j < Y.col1; j += blockLength) {
            final int width = Math.min(blockLength, Y.col1 - j);
            int index = Y.row0 * Y.original.numCols + height * j + row * width;
            if (j == Y.col0) {
                index += colStart;
                for (int k = colStart; k < width; ++k) {
                    final double v = Math.abs(dataY[index++]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
            else {
                for (int k = 0; k < width; ++k) {
                    final double v = Math.abs(dataY[index++]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
        }
        return max;
    }
    
    public static void computeW_Column(final int blockLength, final D1Submatrix64F Y, final D1Submatrix64F W, final double[] temp, final double[] beta, int betaIndex) {
        final int widthB = W.col1 - W.col0;
        initializeW(blockLength, W, Y, widthB, beta[betaIndex++]);
        for (int min = Math.min(widthB, W.row1 - W.row0), j = 1; j < min; ++j) {
            computeY_t_V(blockLength, Y, j, temp);
            computeZ(blockLength, Y, W, j, temp, beta[betaIndex++]);
        }
    }
    
    public static void initializeW(final int blockLength, final D1Submatrix64F W, final D1Submatrix64F Y, final int widthB, final double b) {
        final double[] dataW = W.original.data;
        final double[] dataY = Y.original.data;
        for (int i = W.row0; i < W.row1; i += blockLength) {
            final int heightW = Math.min(blockLength, W.row1 - i);
            int indexW = i * W.original.numCols + heightW * W.col0;
            int indexY = i * Y.original.numCols + heightW * Y.col0;
            if (i == W.row0) {
                dataW[indexW] = -b;
                indexW += widthB;
                indexY += widthB;
                for (int k = 1; k < heightW; ++k, indexW += widthB, indexY += widthB) {
                    dataW[indexW] = -b * dataY[indexY];
                }
            }
            else {
                for (int k = 0; k < heightW; ++k, indexW += widthB, indexY += widthB) {
                    dataW[indexW] = -b * dataY[indexY];
                }
            }
        }
    }
    
    public static void computeZ(final int blockLength, final D1Submatrix64F Y, final D1Submatrix64F W, final int col, final double[] temp, final double beta) {
        final int width = Y.col1 - Y.col0;
        final double[] dataW = W.original.data;
        final double[] dataY = Y.original.data;
        final int colsW = W.original.numCols;
        final double beta_neg = -beta;
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int heightW = Math.min(blockLength, Y.row1 - i);
            int indexW = i * colsW + heightW * W.col0;
            int indexZ = i * colsW + heightW * W.col0 + col;
            int indexV = i * Y.original.numCols + heightW * Y.col0 + col;
            if (i == Y.row0) {
                for (int k = 0; k < heightW; ++k, indexZ += width, indexW += width, indexV += width) {
                    double total = 0.0;
                    for (int j = 0; j < col; ++j) {
                        total += dataW[indexW + j] * temp[j];
                    }
                    if (k < col) {
                        dataW[indexZ] = -beta * total;
                    }
                    else if (k == col) {
                        dataW[indexZ] = beta_neg * (1.0 + total);
                    }
                    else {
                        dataW[indexZ] = beta_neg * (dataY[indexV] + total);
                    }
                }
            }
            else {
                for (int endZ = indexZ + width * heightW; indexZ != endZ; indexZ += width, indexW += width, indexV += width) {
                    double total = 0.0;
                    for (int j = 0; j < col; ++j) {
                        total += dataW[indexW + j] * temp[j];
                    }
                    dataW[indexZ] = beta_neg * (dataY[indexV] + total);
                }
            }
        }
    }
    
    public static void computeY_t_V(final int blockLength, final D1Submatrix64F Y, final int col, final double[] temp) {
        final int widthB = Y.col1 - Y.col0;
        for (int j = 0; j < col; ++j) {
            temp[j] = innerProdCol(blockLength, Y, col, widthB, j, widthB);
        }
    }
    
    public static void multAdd_zeros(final int blockLength, final D1Submatrix64F Y, final D1Submatrix64F B, final D1Submatrix64F C) {
        final int widthY = Y.col1 - Y.col0;
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int heightY = Math.min(blockLength, Y.row1 - i);
            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);
                final int indexC = (i - Y.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightY;
                for (int k = Y.col0; k < Y.col1; k += blockLength) {
                    final int indexY = i * Y.original.numCols + k * heightY;
                    final int indexB = (k - Y.col0 + B.row0) * B.original.numCols + j * widthY;
                    if (i == Y.row0) {
                        multBlockAdd_zerosone(Y.original.data, B.original.data, C.original.data, indexY, indexB, indexC, heightY, widthY, widthB);
                    }
                    else {
                        BlockInnerMultiplication.blockMultPlus(Y.original.data, B.original.data, C.original.data, indexY, indexB, indexC, heightY, widthY, widthB);
                    }
                }
            }
        }
    }
    
    public static void multBlockAdd_zerosone(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = (i < widthA) ? dataB[i * widthC + j + indexB] : 0.0;
                for (int end = Math.min(i, widthA), k = 0; k < end; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[k * widthC + j + indexB];
                }
                final int n = i * widthC + j + indexC;
                dataC[n] += val;
            }
        }
    }
    
    public static void multTransA_vecCol(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F B, final D1Submatrix64F C) {
        final int widthA = A.col1 - A.col0;
        if (widthA > blockLength) {
            throw new IllegalArgumentException("A is expected to be at most one block wide.");
        }
        for (int j = B.col0; j < B.col1; j += blockLength) {
            final int widthB = Math.min(blockLength, B.col1 - j);
            final int indexC = C.row0 * C.original.numCols + (j - B.col0 + C.col0) * widthA;
            for (int k = A.row0; k < A.row1; k += blockLength) {
                final int heightA = Math.min(blockLength, A.row1 - k);
                final int indexA = k * A.original.numCols + A.col0 * heightA;
                final int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
                if (k == A.row0) {
                    multTransABlockSet_lowerTriag(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
                else {
                    BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
                }
            }
        }
    }
    
    protected static void multTransABlockSet_lowerTriag(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < widthA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = (i < heightA) ? dataB[i * widthC + j + indexB] : 0.0;
                for (int k = i + 1; k < heightA; ++k) {
                    val += dataA[k * widthA + i + indexA] * dataB[k * widthC + j + indexB];
                }
                dataC[i * widthC + j + indexC] = val;
            }
        }
    }
}
