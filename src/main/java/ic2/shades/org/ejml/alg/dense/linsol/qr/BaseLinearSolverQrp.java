package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

public abstract class BaseLinearSolverQrp extends LinearSolverAbstract {
  QRPDecomposition<DenseMatrix64F> decomposition;
  
  protected boolean norm2Solution;
  
  protected DenseMatrix64F Y = new DenseMatrix64F(1, 1);
  
  protected DenseMatrix64F R = new DenseMatrix64F(1, 1);
  
  protected DenseMatrix64F R11 = new DenseMatrix64F(1, 1);
  
  protected DenseMatrix64F I = new DenseMatrix64F(1, 1);
  
  protected int rank;
  
  protected LinearSolver<DenseMatrix64F> internalSolver = LinearSolverFactory.leastSquares(1, 1);
  
  private DenseMatrix64F W = new DenseMatrix64F(1, 1);
  
  protected BaseLinearSolverQrp(QRPDecomposition<DenseMatrix64F> decomposition, boolean norm2Solution) {
    this.decomposition = decomposition;
    this.norm2Solution = norm2Solution;
    if (this.internalSolver.modifiesA())
      this.internalSolver = (LinearSolver<DenseMatrix64F>)new LinearSolverSafe(this.internalSolver); 
  }
  
  public boolean setA(DenseMatrix64F A) {
    _setA(A);
    if (!this.decomposition.decompose((Matrix64F)A))
      return false; 
    this.rank = this.decomposition.getRank();
    this.R.reshape(this.numRows, this.numCols);
    this.decomposition.getR((Matrix64F)this.R, false);
    this.R11.reshape(this.rank, this.rank);
    CommonOps.extract((ReshapeMatrix64F)this.R, 0, this.rank, 0, this.rank, (ReshapeMatrix64F)this.R11, 0, 0);
    if (this.norm2Solution && this.rank < this.numCols) {
      this.W.reshape(this.rank, this.numCols - this.rank);
      CommonOps.extract((ReshapeMatrix64F)this.R, 0, this.rank, this.rank, this.numCols, (ReshapeMatrix64F)this.W, 0, 0);
      TriangularSolver.solveU(this.R11.data, 0, this.R11.numCols, this.R11.numCols, this.W.data, 0, this.W.numCols, this.W.numCols);
      this.W.reshape(this.numCols, this.W.numCols, true);
      for (int i = 0; i < this.numCols - this.rank; i++) {
        for (int j = 0; j < this.numCols - this.rank; j++) {
          if (i == j) {
            this.W.set(i + this.rank, j, -1.0D);
          } else {
            this.W.set(i + this.rank, j, 0.0D);
          } 
        } 
      } 
    } 
    return true;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.R);
  }
  
  protected void upgradeSolution(DenseMatrix64F X) {
    DenseMatrix64F z = this.Y;
    if (!this.internalSolver.setA((Matrix64F)this.W))
      throw new RuntimeException("This should never happen.  Is input NaN?"); 
    z.reshape(this.numCols - this.rank, 1);
    this.internalSolver.solve((Matrix64F)X, (Matrix64F)z);
    CommonOps.multAdd(-1.0D, (RowD1Matrix64F)this.W, (RowD1Matrix64F)z, (RowD1Matrix64F)X);
  }
  
  public void invert(DenseMatrix64F A_inv) {
    if (A_inv.numCols != this.numRows || A_inv.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for A_inv"); 
    this.I.reshape(this.numRows, this.numRows);
    CommonOps.setIdentity((RowD1Matrix64F)this.I);
    solve((Matrix64F)this.I, (Matrix64F)A_inv);
  }
  
  public QRPDecomposition<DenseMatrix64F> getDecomposition() {
    return this.decomposition;
  }
}
