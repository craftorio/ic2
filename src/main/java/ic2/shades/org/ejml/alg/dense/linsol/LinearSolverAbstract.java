package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public abstract class LinearSolverAbstract implements LinearSolver<DenseMatrix64F> {
  protected DenseMatrix64F A;
  
  protected int numRows;
  
  protected int numCols;
  
  public DenseMatrix64F getA() {
    return this.A;
  }
  
  protected void _setA(DenseMatrix64F A) {
    this.A = A;
    this.numRows = A.numRows;
    this.numCols = A.numCols;
  }
  
  public void invert(DenseMatrix64F A_inv) {
    InvertUsingSolve.invert(this, (RowD1Matrix64F)this.A, A_inv);
  }
}
