// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.Matrix64F;

public class ImplCommonOps_Matrix64F
{
    public static void extract(final Matrix64F src, final int srcY0, final int srcX0, final Matrix64F dst, final int dstY0, final int dstX0, final int numRows, final int numCols) {
        for (int y = 0; y < numRows; ++y) {
            for (int x = 0; x < numCols; ++x) {
                final double v = src.get(y + srcY0, x + srcX0);
                dst.set(dstY0 + y, dstX0 + x, v);
            }
        }
    }
}
