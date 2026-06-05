package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;

public class InvertUsingSolve {
   public static void invert(LinearSolver<DenseMatrix64F> solver, RowD1Matrix64F A, DenseMatrix64F A_inv, DenseMatrix64F storage) {
      if (A.numRows == A_inv.numRows && A.numCols == A_inv.numCols) {
         CommonOps.setIdentity(storage);
         solver.solve(storage, A_inv);
      } else {
         throw new IllegalArgumentException("A and A_inv must have the same dimensions");
      }
   }

   public static void invert(LinearSolver<DenseMatrix64F> solver, RowD1Matrix64F A, DenseMatrix64F A_inv) {
      if (A.numRows == A_inv.numRows && A.numCols == A_inv.numCols) {
         CommonOps.setIdentity(A_inv);
         solver.solve(A_inv, A_inv);
      } else {
         throw new IllegalArgumentException("A and A_inv must have the same dimensions");
      }
   }
}
