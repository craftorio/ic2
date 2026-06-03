package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverQrHouseCol extends LinearSolverAbstract {
  private QRDecompositionHouseholderColumn_D64 decomposer;
  
  private DenseMatrix64F a = new DenseMatrix64F(1, 1);
  
  private DenseMatrix64F temp = new DenseMatrix64F(1, 1);
  
  protected int maxRows = -1;
  
  protected int maxCols = -1;
  
  private double[][] QR;
  
  private DenseMatrix64F R = new DenseMatrix64F(1, 1);
  
  private double[] gammas;
  
  public LinearSolverQrHouseCol() {
    this.decomposer = new QRDecompositionHouseholderColumn_D64();
  }
  
  public void setMaxSize(int maxRows, int maxCols) {
    this.maxRows = maxRows;
    this.maxCols = maxCols;
  }
  
  public boolean setA(DenseMatrix64F A) {
    if (A.numRows > this.maxRows || A.numCols > this.maxCols)
      setMaxSize(A.numRows, A.numCols); 
    this.R.reshape(A.numCols, A.numCols);
    this.a.reshape(A.numRows, 1);
    this.temp.reshape(A.numRows, 1);
    _setA(A);
    if (!this.decomposer.decompose(A))
      return false; 
    this.gammas = this.decomposer.getGammas();
    this.QR = this.decomposer.getQR();
    this.decomposer.getR(this.R, true);
    return true;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.R);
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (X.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for X: X rows = " + X.numRows + " expected = " + this.numCols); 
    if (B.numRows != this.numRows || B.numCols != X.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for B"); 
    int BnumCols = B.numCols;
    for (int colB = 0; colB < BnumCols; colB++) {
      for (int j = 0; j < this.numRows; j++)
        this.a.data[j] = B.data[j * BnumCols + colB]; 
      for (int n = 0; n < this.numCols; n++) {
        double[] u = this.QR[n];
        double vv = u[n];
        u[n] = 1.0D;
        QrHelperFunctions.rank1UpdateMultR(this.a, u, this.gammas[n], 0, n, this.numRows, this.temp.data);
        u[n] = vv;
      } 
      TriangularSolver.solveU(this.R.data, this.a.data, this.numCols);
      for (int i = 0; i < this.numCols; i++)
        X.data[i * X.numCols + colB] = this.a.data[i]; 
    } 
  }
  
  public boolean modifiesA() {
    return false;
  }
  
  public boolean modifiesB() {
    return false;
  }
}
