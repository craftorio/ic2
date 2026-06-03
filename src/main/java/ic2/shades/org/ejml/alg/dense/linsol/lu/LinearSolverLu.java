package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;

public class LinearSolverLu extends LinearSolverLuBase {
  boolean doImprove = false;
  
  public LinearSolverLu(LUDecompositionBase_D64 decomp) {
    super(decomp);
  }
  
  public LinearSolverLu(LUDecompositionBase_D64 decomp, boolean doImprove) {
    super(decomp);
    this.doImprove = doImprove;
  }
  
  public void solve(DenseMatrix64F b, DenseMatrix64F x) {
    if (b.numCols != x.numCols || b.numRows != this.numRows || x.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected matrix size"); 
    int numCols = b.numCols;
    double[] dataB = b.data;
    double[] dataX = x.data;
    double[] vv = this.decomp._getVV();
    for (int j = 0; j < numCols; j++) {
      int index = j;
      int i;
      for (i = 0; i < this.numCols; ) {
        vv[i] = dataB[index];
        i++;
        index += numCols;
      } 
      this.decomp._solveVectorInternal(vv);
      index = j;
      for (i = 0; i < this.numCols; ) {
        dataX[index] = vv[i];
        i++;
        index += numCols;
      } 
    } 
    if (this.doImprove)
      improveSol(b, x); 
  }
}
