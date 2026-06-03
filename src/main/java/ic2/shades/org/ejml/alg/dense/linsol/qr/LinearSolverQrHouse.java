package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverQrHouse extends LinearSolverAbstract {
  private QRDecompositionHouseholder_D64 decomposer;
  
  private double[] a;
  
  private double[] u;
  
  private int maxRows = -1;
  
  private DenseMatrix64F QR;
  
  private double[] gammas;
  
  public LinearSolverQrHouse() {
    this.decomposer = new QRDecompositionHouseholder_D64();
  }
  
  public void setMaxSize(int maxRows) {
    this.maxRows = maxRows;
    this.a = new double[maxRows];
    this.u = new double[maxRows];
  }
  
  public boolean setA(DenseMatrix64F A) {
    if (A.numRows > this.maxRows)
      setMaxSize(A.numRows); 
    _setA(A);
    if (!this.decomposer.decompose(A))
      return false; 
    this.gammas = this.decomposer.getGammas();
    this.QR = this.decomposer.getQR();
    return true;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.QR);
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (X.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for X"); 
    if (B.numRows != this.numRows || B.numCols != X.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for B"); 
    int BnumCols = B.numCols;
    for (int colB = 0; colB < BnumCols; colB++) {
      for (int j = 0; j < this.numRows; j++)
        this.a[j] = B.data[j * BnumCols + colB]; 
      for (int n = 0; n < this.numCols; n++) {
        this.u[n] = 1.0D;
        double ub = this.a[n];
        int k;
        for (k = n + 1; k < this.numRows; k++) {
          this.u[k] = this.QR.unsafe_get(k, n);
          ub += this.QR.unsafe_get(k, n) * this.a[k];
        } 
        ub *= this.gammas[n];
        for (k = n; k < this.numRows; k++)
          this.a[k] = this.a[k] - this.u[k] * ub; 
      } 
      TriangularSolver.solveU(this.QR.data, this.a, this.numCols);
      for (int i = 0; i < this.numCols; i++)
        X.data[i * X.numCols + colB] = this.a[i]; 
    } 
  }
  
  public boolean modifiesA() {
    return false;
  }
  
  public boolean modifiesB() {
    return false;
  }
}
