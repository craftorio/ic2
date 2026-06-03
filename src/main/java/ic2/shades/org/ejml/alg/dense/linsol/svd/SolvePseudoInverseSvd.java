package ic2.shades.org.ejml.alg.dense.linsol.svd;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;

public class SolvePseudoInverseSvd implements LinearSolver<DenseMatrix64F> {
  private SingularValueDecomposition<DenseMatrix64F> svd;
  
  private DenseMatrix64F pinv = new DenseMatrix64F(1, 1);
  
  private double threshold = UtilEjml.EPS;
  
  public SolvePseudoInverseSvd(int maxRows, int maxCols) {
    this.svd = DecompositionFactory.svd(maxRows, maxCols, true, true, true);
  }
  
  public SolvePseudoInverseSvd() {
    this(100, 100);
  }
  
  public boolean setA(DenseMatrix64F A) {
    this.pinv.reshape(A.numCols, A.numRows, false);
    if (!this.svd.decompose((Matrix64F)A))
      return false; 
    DenseMatrix64F U_t = (DenseMatrix64F)this.svd.getU(null, true);
    DenseMatrix64F V = (DenseMatrix64F)this.svd.getV(null, false);
    double[] S = this.svd.getSingularValues();
    int N = Math.min(A.numRows, A.numCols);
    double maxSingular = 0.0D;
    for (int i = 0; i < N; i++) {
      if (S[i] > maxSingular)
        maxSingular = S[i]; 
    } 
    double tau = this.threshold * Math.max(A.numCols, A.numRows) * maxSingular;
    if (maxSingular != 0.0D)
      for (int k = 0; k < N; k++) {
        double s = S[k];
        if (s < tau) {
          S[k] = 0.0D;
        } else {
          S[k] = 1.0D / S[k];
        } 
      }  
    for (int j = 0; j < V.numRows; j++) {
      int index = j * V.numCols;
      for (int k = 0; k < V.numCols; k++)
        V.data[index++] = V.data[index++] * S[k]; 
    } 
    CommonOps.mult((RowD1Matrix64F)V, (RowD1Matrix64F)U_t, (RowD1Matrix64F)this.pinv);
    return true;
  }
  
  public double quality() {
    throw new IllegalArgumentException("Not supported by this solver.");
  }
  
  public void solve(DenseMatrix64F b, DenseMatrix64F x) {
    CommonOps.mult((RowD1Matrix64F)this.pinv, (RowD1Matrix64F)b, (RowD1Matrix64F)x);
  }
  
  public void invert(DenseMatrix64F A_inv) {
    A_inv.set((D1Matrix64F)this.pinv);
  }
  
  public boolean modifiesA() {
    return this.svd.inputModified();
  }
  
  public boolean modifiesB() {
    return false;
  }
  
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }
  
  public SingularValueDecomposition<DenseMatrix64F> getDecomposer() {
    return this.svd;
  }
}
