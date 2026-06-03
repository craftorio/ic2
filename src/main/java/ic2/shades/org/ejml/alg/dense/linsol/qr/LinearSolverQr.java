package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverQr extends LinearSolverAbstract {
  private QRDecomposition<DenseMatrix64F> decomposer;
  
  protected int maxRows = -1;
  
  protected int maxCols = -1;
  
  protected DenseMatrix64F Q;
  
  protected DenseMatrix64F R;
  
  private DenseMatrix64F Y;
  
  private DenseMatrix64F Z;
  
  public LinearSolverQr(QRDecomposition<DenseMatrix64F> decomposer) {
    this.decomposer = decomposer;
  }
  
  public void setMaxSize(int maxRows, int maxCols) {
    this.maxRows = maxRows;
    this.maxCols = maxCols;
    this.Q = new DenseMatrix64F(maxRows, maxRows);
    this.R = new DenseMatrix64F(maxRows, maxCols);
    this.Y = new DenseMatrix64F(maxRows, 1);
    this.Z = new DenseMatrix64F(maxRows, 1);
  }
  
  public boolean setA(DenseMatrix64F A) {
    if (A.numRows > this.maxRows || A.numCols > this.maxCols)
      setMaxSize(A.numRows, A.numCols); 
    _setA(A);
    if (!this.decomposer.decompose((Matrix64F)A))
      return false; 
    this.Q.reshape(this.numRows, this.numRows, false);
    this.R.reshape(this.numRows, this.numCols, false);
    this.decomposer.getQ((Matrix64F)this.Q, false);
    this.decomposer.getR((Matrix64F)this.R, false);
    return true;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.R);
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    if (X.numRows != this.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for X"); 
    if (B.numRows != this.numRows || B.numCols != X.numCols)
      throw new IllegalArgumentException("Unexpected dimensions for B"); 
    int BnumCols = B.numCols;
    this.Y.reshape(this.numRows, 1, false);
    this.Z.reshape(this.numRows, 1, false);
    for (int colB = 0; colB < BnumCols; colB++) {
      int i;
      for (i = 0; i < this.numRows; i++)
        this.Y.data[i] = B.get(i, colB); 
      CommonOps.multTransA((RowD1Matrix64F)this.Q, (RowD1Matrix64F)this.Y, (RowD1Matrix64F)this.Z);
      TriangularSolver.solveU(this.R.data, this.Z.data, this.numCols);
      for (i = 0; i < this.numCols; i++)
        X.set(i, colB, this.Z.data[i]); 
    } 
  }
  
  public boolean modifiesA() {
    return this.decomposer.inputModified();
  }
  
  public boolean modifiesB() {
    return false;
  }
  
  public QRDecomposition<DenseMatrix64F> getDecomposer() {
    return this.decomposer;
  }
  
  public DenseMatrix64F getQ() {
    return this.Q;
  }
  
  public DenseMatrix64F getR() {
    return this.R;
  }
}
