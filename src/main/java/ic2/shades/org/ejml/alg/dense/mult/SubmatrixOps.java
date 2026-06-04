// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class SubmatrixOps
{
    public static void setSubMatrix(final RowD1Matrix64F src, final RowD1Matrix64F dst, final int srcRow, final int srcCol, final int dstRow, final int dstCol, final int numSubRows, final int numSubCols) {
        for (int i = 0; i < numSubRows; ++i) {
            for (int j = 0; j < numSubCols; ++j) {
                final double val = src.get(i + srcRow, j + srcCol);
                dst.set(i + dstRow, j + dstCol, val);
            }
        }
    }
}
