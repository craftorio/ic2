package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverCholLDL extends LinearSolverAbstract {
  private CholeskyDecompositionLDL_D64 decomp;
  
  private int n;
  
  private double[] vv;
  
  private double[] el;
  
  private double[] d;
  
  public LinearSolverCholLDL(CholeskyDecompositionLDL_D64 decomp) {
    this.decomp = decomp;
  }
  
  public LinearSolverCholLDL() {
    this.decomp = new CholeskyDecompositionLDL_D64();
  }
  
  public boolean setA(DenseMatrix64F A) {
    _setA(A);
    if (this.decomp.decompose(A)) {
      this.n = A.numCols;
      this.vv = this.decomp._getVV();
      this.el = (this.decomp.getL()).data;
      this.d = this.decomp.getDiagonal();
      return true;
    } 
    return false;
  }
  
  public double quality() {
    return Math.abs(SpecializedOps.diagProd((RowD1Matrix64F)this.decomp.getL()));
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (B.numCols != X.numCols && B.numRows != this.n && X.numRows != this.n)
      throw new IllegalArgumentException("Unexpected matrix size"); 
    int numCols = B.numCols;
    double[] dataB = B.data;
    double[] dataX = X.data;
    for (int j = 0; j < numCols; j++) {
      int i;
      for (i = 0; i < this.n; ) {
        this.vv[i] = dataB[i * numCols + j];
        i++;
      } 
      solveInternal();
      for (i = 0; i < this.n; ) {
        dataX[i * numCols + j] = this.vv[i];
        i++;
      } 
    } 
  }
  
  private void solveInternal() {
    TriangularSolver.solveL(this.el, this.vv, this.n);
    for (int i = 0; i < this.n; i++)
      this.vv[i] = this.vv[i] / this.d[i]; 
    TriangularSolver.solveTranL(this.el, this.vv, this.n);
  }
  
  public void invert(DenseMatrix64F inv) {
    if (inv.numRows != this.n || inv.numCols != this.n)
      throw new RuntimeException("Unexpected matrix dimension"); 
    double[] a = inv.data;
    int i;
    for (i = 0; i < this.n; i++) {
      for (int j = 0; j <= i; j++) {
        double sum = (i == j) ? 1.0D : 0.0D;
        for (int k = i - 1; k >= j; k--)
          sum -= this.el[i * this.n + k] * a[j * this.n + k]; 
        a[j * this.n + i] = sum;
      } 
    } 
    for (i = 0; i < this.n; i++) {
      double inv_d = 1.0D / this.d[i];
      for (int j = 0; j <= i; j++)
        a[j * this.n + i] = a[j * this.n + i] * inv_d; 
    } 
    for (i = this.n - 1; i >= 0; i--) {
      for (int j = 0; j <= i; j++) {
        double sum = (i < j) ? 0.0D : a[j * this.n + i];
        for (int k = i + 1; k < this.n; k++)
          sum -= this.el[k * this.n + i] * a[j * this.n + k]; 
        a[j * this.n + i] = sum;
        a[i * this.n + j] = sum;
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
