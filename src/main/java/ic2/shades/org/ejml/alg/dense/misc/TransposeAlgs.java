// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class TransposeAlgs
{
    public static void square(final RowD1Matrix64F mat) {
        for (int index = 1, indexEnd = mat.numCols, i = 0; i < mat.numRows; ++i, index += i + 1, indexEnd += mat.numCols) {
            for (int indexOther = (i + 1) * mat.numCols + i; index < indexEnd; ++index, indexOther += mat.numCols) {
                final double val = mat.data[index];
                mat.data[index] = mat.data[indexOther];
                mat.data[indexOther] = val;
            }
        }
    }
    
    public static void block(final RowD1Matrix64F A, final RowD1Matrix64F A_tran, final int blockLength) {
        for (int i = 0; i < A.numRows; i += blockLength) {
            final int blockHeight = Math.min(blockLength, A.numRows - i);
            int indexSrc = i * A.numCols;
            int indexDst = i;
            for (int j = 0; j < A.numCols; j += blockLength) {
                final int blockWidth = Math.min(blockLength, A.numCols - j);
                for (int indexSrcEnd = indexSrc + blockWidth; indexSrc < indexSrcEnd; ++indexSrc) {
                    for (int rowSrc = indexSrc, rowDst = indexDst, end = rowDst + blockHeight; rowDst < end; A_tran.data[rowDst++] = A.data[rowSrc], rowSrc += A.numCols) {}
                    indexDst += A_tran.numCols;
                }
            }
        }
    }
    
    public static void standard(final RowD1Matrix64F A, final RowD1Matrix64F A_tran) {
        int index = 0;
        for (int i = 0; i < A_tran.numRows; ++i) {
            for (int index2 = i, end = index + A_tran.numCols; index < end; A_tran.data[index++] = A.data[index2], index2 += A.numCols) {}
        }
    }
}
