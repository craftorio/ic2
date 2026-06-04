// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class ImplCommonOps_DenseMatrix64F
{
    public static void extract(final DenseMatrix64F src, final int srcY0, final int srcX0, final DenseMatrix64F dst, final int dstY0, final int dstX0, final int numRows, final int numCols) {
        for (int y = 0; y < numRows; ++y) {
            final int indexSrc = src.getIndex(y + srcY0, srcX0);
            final int indexDst = dst.getIndex(y + dstY0, dstX0);
            System.arraycopy(src.data, indexSrc, dst.data, indexDst, numCols);
        }
    }
}
