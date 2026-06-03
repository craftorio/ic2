package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class ImplCommonOps_DenseMatrix64F {
  public static void extract(DenseMatrix64F src, int srcY0, int srcX0, DenseMatrix64F dst, int dstY0, int dstX0, int numRows, int numCols) {
    for (int y = 0; y < numRows; y++) {
      int indexSrc = src.getIndex(y + srcY0, srcX0);
      int indexDst = dst.getIndex(y + dstY0, dstX0);
      System.arraycopy(src.data, indexSrc, dst.data, indexDst, numCols);
    } 
  }
}
