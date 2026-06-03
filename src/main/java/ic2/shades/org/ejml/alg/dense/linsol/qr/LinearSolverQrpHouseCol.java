package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;

public class LinearSolverQrpHouseCol extends BaseLinearSolverQrp {
  private QRColPivDecompositionHouseholderColumn_D64 decomposition;
  
  private DenseMatrix64F x_basic = new DenseMatrix64F(1, 1);
  
  public LinearSolverQrpHouseCol(QRColPivDecompositionHouseholderColumn_D64 decomposition, boolean norm2Solution) {
    super((QRPDecomposition<DenseMatrix64F>)decomposition, norm2Solution);
    this.decomposition = decomposition;
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (X.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for X"); 
    if (B.numRows != this.numRows || B.numCols != X.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for B"); 
    int BnumCols = B.numCols;
    int[] pivots = this.decomposition.getPivots();
    double[][] qr = this.decomposition.getQR();
    double[] gammas = this.decomposition.getGammas();
    for (int colB = 0; colB < BnumCols; colB++) {
      this.x_basic.reshape(this.numRows, 1);
      this.Y.reshape(this.numRows, 1);
      int i;
      for (i = 0; i < this.numRows; i++)
        this.x_basic.data[i] = B.get(i, colB); 
      for (i = 0; i < this.rank; i++) {
        double[] u = qr[i];
        double vv = u[i];
        u[i] = 1.0D;
        QrHelperFunctions.rank1UpdateMultR(this.x_basic, u, gammas[i], 0, i, this.numRows, this.Y.data);
        u[i] = vv;
      } 
      TriangularSolver.solveU(this.R11.data, this.x_basic.data, this.rank);
      this.x_basic.reshape(this.numCols, 1, true);
      for (i = this.rank; i < this.numCols; i++)
        this.x_basic.data[i] = 0.0D; 
      if (this.norm2Solution && this.rank < this.numCols)
        upgradeSolution(this.x_basic); 
      for (i = 0; i < this.numCols; i++)
        X.set(pivots[i], colB, this.x_basic.data[i]); 
    } 
  }
  
  public boolean modifiesA() {
    return this.decomposition.inputModified();
  }
  
  public boolean modifiesB() {
    return false;
  }
}
