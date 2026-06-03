package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverChol extends LinearSolverAbstract {
  CholeskyDecompositionCommon_D64 decomp;
  
  int n;
  
  double[] vv;
  
  double[] t;
  
  public LinearSolverChol(CholeskyDecompositionCommon_D64 decomp) {
    this.decomp = decomp;
  }
  
  public boolean setA(DenseMatrix64F A) {
    if (A.numRows != A.numCols)
      throw new IllegalArgumentException("Matrix must be square"); 
    _setA(A);
    if (this.decomp.decompose(A)) {
      this.n = A.numCols;
      this.vv = this.decomp._getVV();
      this.t = (this.decomp.getT()).data;
      return true;
    } 
    return false;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.decomp.getT());
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (B.numCols != X.numCols || B.numRows != this.n || X.numRows != this.n)
      throw new IllegalArgumentException("Unexpected matrix size"); 
    int numCols = B.numCols;
    double[] dataB = B.data;
    double[] dataX = X.data;
    if (this.decomp.isLower()) {
      for (int j = 0; j < numCols; j++) {
        int i;
        for (i = 0; i < this.n; ) {
          this.vv[i] = dataB[i * numCols + j];
          i++;
        } 
        solveInternalL();
        for (i = 0; i < this.n; ) {
          dataX[i * numCols + j] = this.vv[i];
          i++;
        } 
      } 
    } else {
      throw new RuntimeException("Implement");
    } 
  }
  
  private void solveInternalL() {
    TriangularSolver.solveL(this.t, this.vv, this.n);
    TriangularSolver.solveTranL(this.t, this.vv, this.n);
  }
  
  public void invert(DenseMatrix64F inv) {
    if (inv.numRows != this.n || inv.numCols != this.n)
      throw new RuntimeException("Unexpected matrix dimension"); 
    if (inv.data == this.t)
      throw new IllegalArgumentException("Passing in the same matrix that was decomposed."); 
    double[] a = inv.data;
    if (this.decomp.isLower()) {
      setToInverseL(a);
    } else {
      throw new RuntimeException("Implement");
    } 
  }
  
  public void setToInverseL(double[] a) {
    int i;
    for (i = 0; i < this.n; i++) {
      double el_ii = this.t[i * this.n + i];
      for (int j = 0; j <= i; j++) {
        double sum = (i == j) ? 1.0D : 0.0D;
        for (int k = i - 1; k >= j; k--)
          sum -= this.t[i * this.n + k] * a[j * this.n + k]; 
        a[j * this.n + i] = sum / el_ii;
      } 
    } 
    for (i = this.n - 1; i >= 0; i--) {
      double el_ii = this.t[i * this.n + i];
      for (int j = 0; j <= i; j++) {
        double sum = (i < j) ? 0.0D : a[j * this.n + i];
        for (int k = i + 1; k < this.n; k++)
          sum -= this.t[k * this.n + i] * a[j * this.n + k]; 
        a[j * this.n + i] = sum / el_ii;
        a[i * this.n + j] = sum / el_ii;
      } 
    } 
  }
  
  public boolean modifiesA() {
    return this.decomp.inputModified();
  }
  
  public boolean modifiesB() {
    return false;
  }
}
