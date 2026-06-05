package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenPowerMethod;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Eigenpair;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class EigenOps {
   public static double computeEigenValue(DenseMatrix64F A, DenseMatrix64F eigenVector) {
      double bottom = VectorVectorMult.innerProd(eigenVector, eigenVector);
      double top = VectorVectorMult.innerProdA(eigenVector, A, eigenVector);
      return top / bottom;
   }

   public static Eigenpair computeEigenVector(DenseMatrix64F A, double eigenvalue) {
      if (A.numRows != A.numCols) {
         throw new IllegalArgumentException("Must be a square matrix.");
      }

      DenseMatrix64F M = new DenseMatrix64F(A.numRows, A.numCols);
      DenseMatrix64F x = new DenseMatrix64F(A.numRows, 1);
      DenseMatrix64F b = new DenseMatrix64F(A.numRows, 1);
      CommonOps.fill(b, 1.0);
      double origEigenvalue = eigenvalue;
      SpecializedOps.addIdentity(A, M, -eigenvalue);
      double threshold = NormOps.normPInf(A) * UtilEjml.EPS;
      double prevError = Double.MAX_VALUE;
      boolean hasWorked = false;
      LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.linear(M.numRows);
      double perp = 1.0E-4;

      for (int i = 0; i < 200; i++) {
         boolean failed = false;
         if (!solver.setA(M)) {
            failed = true;
         } else {
            solver.solve(b, x);
         }

         if (MatrixFeatures.hasUncountable(x)) {
            failed = true;
         }

         if (failed) {
            if (hasWorked) {
               return new Eigenpair(eigenvalue, b);
            }

            double val = i % 2 == 0 ? 1.0 - perp : 1.0 + perp;
            eigenvalue = origEigenvalue * Math.pow(val, i / 2 + 1);
            SpecializedOps.addIdentity(A, M, -eigenvalue);
         } else {
            hasWorked = true;
            b.set(x);
            NormOps.normalizeF(b);
            CommonOps.mult(M, b, x);
            double error = NormOps.normPInf(x);
            if (error - prevError > UtilEjml.EPS * 10.0) {
               prevError = Double.MAX_VALUE;
               hasWorked = false;
               double val = i % 2 == 0 ? 1.0 - perp : 1.0 + perp;
               eigenvalue = origEigenvalue * Math.pow(val, 1.0);
            } else {
               if (error <= threshold || Math.abs(prevError - error) <= UtilEjml.EPS) {
                  return new Eigenpair(eigenvalue, b);
               }

               prevError = error;
               eigenvalue = VectorVectorMult.innerProdA(b, A, b);
            }

            SpecializedOps.addIdentity(A, M, -eigenvalue);
         }
      }

      return null;
   }

   public static Eigenpair dominantEigenpair(DenseMatrix64F A) {
      EigenPowerMethod power = new EigenPowerMethod(A.numRows);
      return !power.computeShiftInvert(A, 0.1) ? null : null;
   }

   public static double[] boundLargestEigenValue(DenseMatrix64F A, double[] bound) {
      if (A.numRows != A.numCols) {
         throw new IllegalArgumentException("A must be a square matrix.");
      }

      double min = Double.MAX_VALUE;
      double max = 0.0;
      int n = A.numRows;

      for (int i = 0; i < n; i++) {
         double total = 0.0;

         for (int j = 0; j < n; j++) {
            double v = A.get(i, j);
            if (v < 0.0) {
               throw new IllegalArgumentException("Matrix must be positive");
            }

            total += v;
         }

         if (total < min) {
            min = total;
         }

         if (total > max) {
            max = total;
         }
      }

      if (bound == null) {
         bound = new double[2];
      }

      bound[0] = min;
      bound[1] = max;
      return bound;
   }

   public static DenseMatrix64F createMatrixD(EigenDecomposition eig) {
      int N = eig.getNumberOfEigenvalues();
      DenseMatrix64F D = new DenseMatrix64F(N, N);

      for (int i = 0; i < N; i++) {
         Complex64F c = eig.getEigenvalue(i);
         if (c.isReal()) {
            D.set(i, i, c.real);
         }
      }

      return D;
   }

   public static DenseMatrix64F createMatrixV(EigenDecomposition<DenseMatrix64F> eig) {
      int N = eig.getNumberOfEigenvalues();
      DenseMatrix64F V = new DenseMatrix64F(N, N);

      for (int i = 0; i < N; i++) {
         Complex64F c = eig.getEigenvalue(i);
         if (c.isReal()) {
            DenseMatrix64F v = eig.getEigenVector(i);
            if (v != null) {
               for (int j = 0; j < N; j++) {
                  V.set(j, i, v.get(j, 0));
               }
            }
         }
      }

      return V;
   }
}
