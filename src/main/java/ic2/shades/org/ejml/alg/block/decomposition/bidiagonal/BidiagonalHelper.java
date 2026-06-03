package ic2.shades.org.ejml.alg.block.decomposition.bidiagonal;

import ic2.shades.org.ejml.alg.block.decomposition.qr.BlockHouseHolder;
import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BidiagonalHelper {
  public static boolean bidiagOuterBlocks(int blockLength, D1Submatrix64F A, double[] gammasU, double[] gammasV) {
    int width = Math.min(blockLength, A.col1 - A.col0);
    int height = Math.min(blockLength, A.row1 - A.row0);
    int min = Math.min(width, height);
    for (int i = 0; i < min; i++) {
      if (!BlockHouseHolder.computeHouseHolderCol(blockLength, A, gammasU, i))
        return false; 
      BlockHouseHolder.rank1UpdateMultR_Col(blockLength, A, i, gammasU[A.col0 + i]);
      BlockHouseHolder.rank1UpdateMultR_TopRow(blockLength, A, i, gammasU[A.col0 + i]);
      System.out.println("After column stuff");
      A.original.print();
      if (!BlockHouseHolder.computeHouseHolderRow(blockLength, A, gammasV, i))
        return false; 
      BlockHouseHolder.rank1UpdateMultL_Row(blockLength, A, i, i + 1, gammasV[A.row0 + i]);
      System.out.println("After update row");
      A.original.print();
      System.out.println("After row stuff");
      A.original.print();
    } 
    return true;
  }
}
