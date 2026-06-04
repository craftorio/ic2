// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.bidiagonal;

import ic2.shades.org.ejml.alg.block.decomposition.qr.BlockHouseHolder;
import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BidiagonalHelper
{
    public static boolean bidiagOuterBlocks(final int blockLength, final D1Submatrix64F A, final double[] gammasU, final double[] gammasV) {
        final int width = Math.min(blockLength, A.col1 - A.col0);
        final int height = Math.min(blockLength, A.row1 - A.row0);
        for (int min = Math.min(width, height), i = 0; i < min; ++i) {
            if (!BlockHouseHolder.computeHouseHolderCol(blockLength, A, gammasU, i)) {
                return false;
            }
            BlockHouseHolder.rank1UpdateMultR_Col(blockLength, A, i, gammasU[A.col0 + i]);
            BlockHouseHolder.rank1UpdateMultR_TopRow(blockLength, A, i, gammasU[A.col0 + i]);
            System.out.println("After column stuff");
            A.original.print();
            if (!BlockHouseHolder.computeHouseHolderRow(blockLength, A, gammasV, i)) {
                return false;
            }
            BlockHouseHolder.rank1UpdateMultL_Row(blockLength, A, i, i + 1, gammasV[A.row0 + i]);
            System.out.println("After update row");
            A.original.print();
            System.out.println("After row stuff");
            A.original.print();
        }
        return true;
    }
}
