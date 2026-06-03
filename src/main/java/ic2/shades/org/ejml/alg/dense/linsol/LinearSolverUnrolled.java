package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverUnrolled implements LinearSolver<DenseMatrix64F> {
  DenseMatrix64F A;
  
  public boolean setA(DenseMatrix64F A) {
    if (A.numRows != A.numCols)
      return false; 
    this.A = A;
    return (A.numRows <= 5);
  }
  
  public double quality() {
    throw new IllegalArgumentException("Not supported by this solver.");
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    throw new RuntimeException("Not supported");
  }
  
  public void invert(DenseMatrix64F A_inv) {
    if (this.A.numRows == 1)
      A_inv.set(0, 1.0D / this.A.get(0)); 
    UnrolledInverseFromMinor.inv(this.A, A_inv);
  }
  
  public boolean modifiesA() {
    return false;
  }
  
  public boolean modifiesB() {
    return false;
  }
}
