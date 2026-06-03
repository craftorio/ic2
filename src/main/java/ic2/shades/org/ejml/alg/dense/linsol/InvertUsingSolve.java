package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;

public class InvertUsingSolve {
  public static void invert(LinearSolver<DenseMatrix64F> solver, RowD1Matrix64F A, DenseMatrix64F A_inv, DenseMatrix64F storage) {
    if (A.numRows != A_inv.numRows || A.numCols != A_inv.numCols)
      throw new IllegalArgumentException("A and A_inv must have the same dimensions"); 
    CommonOps.setIdentity((RowD1Matrix64F)storage);
    solver.solve((Matrix64F)storage, (Matrix64F)A_inv);
  }
  
  public static void invert(LinearSolver<DenseMatrix64F> solver, RowD1Matrix64F A, DenseMatrix64F A_inv) {
    if (A.numRows != A_inv.numRows || A.numCols != A_inv.numCols)
      throw new IllegalArgumentException("A and A_inv must have the same dimensions"); 
    CommonOps.setIdentity((RowD1Matrix64F)A_inv);
    solver.solve((Matrix64F)A_inv, (Matrix64F)A_inv);
  }
}
