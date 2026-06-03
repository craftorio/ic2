package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;

public abstract class LinearSolverLuBase extends LinearSolverAbstract {
  protected LUDecompositionBase_D64 decomp;
  
  public LinearSolverLuBase(LUDecompositionBase_D64 decomp) {
    this.decomp = decomp;
  }
  
  public boolean setA(DenseMatrix64F A) {
    _setA(A);
    return this.decomp.decompose((Matrix64F)A);
  }
  
  public double quality() {
    return this.decomp.quality();
  }
  
  public void invert(DenseMatrix64F A_inv) {
    double[] vv = this.decomp._getVV();
    DenseMatrix64F LU = this.decomp.getLU();
    if (A_inv.numCols != LU.numCols || A_inv.numRows != LU.numRows)
      throw new IllegalArgumentException("Unexpected matrix dimension"); 
    int n = this.A.numCols;
    double[] dataInv = A_inv.data;
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < n; ) {
        vv[i] = (i == j) ? 1.0D : 0.0D;
        i++;
      } 
      this.decomp._solveVectorInternal(vv);
      int index = j;
      for (int k = 0; k < n; ) {
        dataInv[index] = vv[k];
        k++;
        index += n;
      } 
    } 
  }
  
  public void improveSol(DenseMatrix64F b, DenseMatrix64F x) {
    if (b.numCols != x.numCols)
      throw new IllegalArgumentException("bad shapes"); 
    double[] dataA = this.A.data;
    double[] dataB = b.data;
    double[] dataX = x.data;
    int nc = b.numCols;
    int n = b.numCols;
    double[] vv = this.decomp._getVV();
    DenseMatrix64F LU = this.decomp.getLU();
    for (int k = 0; k < nc; k++) {
      int i;
      for (i = 0; i < n; i++) {
        double sdp = -dataB[i * nc + k];
        for (int j = 0; j < n; j++)
          sdp += dataA[i * n + j] * dataX[j * nc + k]; 
        vv[i] = sdp;
      } 
      this.decomp._solveVectorInternal(vv);
      for (i = 0; i < n; i++)
        dataX[i * nc + k] = dataX[i * nc + k] - vv[i]; 
    } 
  }
  
  public boolean modifiesA() {
    return false;
  }
  
  public boolean modifiesB() {
    return false;
  }
  
  public LUDecompositionBase_D64 getDecomposer() {
    return this.decomp;
  }
}
