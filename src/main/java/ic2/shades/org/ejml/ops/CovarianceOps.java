package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import java.util.Random;

public class CovarianceOps {
   public static double TOL = 1.0E-9;

   public static boolean isValidFast(DenseMatrix64F cov) {
      return MatrixFeatures.isDiagonalPositive(cov);
   }

   public static int isValid(DenseMatrix64F cov) {
      if (!MatrixFeatures.isDiagonalPositive(cov)) {
         return 1;
      } else if (!MatrixFeatures.isSymmetric(cov, TOL)) {
         return 2;
      } else {
         return !MatrixFeatures.isPositiveSemidefinite(cov) ? 3 : 0;
      }
   }

   public static boolean invert(DenseMatrix64F cov) {
      return invert(cov, cov);
   }

   public static boolean invert(DenseMatrix64F cov, DenseMatrix64F cov_inv) {
      if (cov.numCols <= 4) {
         if (cov.numCols != cov.numRows) {
            throw new IllegalArgumentException("Must be a square matrix.");
         }

         if (cov.numCols >= 2) {
            UnrolledInverseFromMinor.inv(cov, cov_inv);
         } else {
            cov_inv.data[0] = 1.0 / cov_inv.data[0];
         }
      } else {
         LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.symmPosDef(cov.numRows);
         solver = new LinearSolverSafe<>(solver);
         if (!solver.setA(cov)) {
            return false;
         }

         solver.invert(cov_inv);
      }

      return true;
   }

   public static void randomVector(DenseMatrix64F cov, DenseMatrix64F vector, Random rand) {
      CovarianceRandomDraw rng = new CovarianceRandomDraw(rand, cov);
      rng.next(vector);
   }
}
