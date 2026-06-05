package ic2.shades.org.ejml.alg.dense.linsol.svd;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;

public class SolvePseudoInverseSvd implements LinearSolver<DenseMatrix64F> {
   private SingularValueDecomposition<DenseMatrix64F> svd;
   private DenseMatrix64F pinv = new DenseMatrix64F(1, 1);
   private double threshold = UtilEjml.EPS;

   public SolvePseudoInverseSvd(int maxRows, int maxCols) {
      this.svd = DecompositionFactory.svd(maxRows, maxCols, true, true, true);
   }

   public SolvePseudoInverseSvd() {
      this(100, 100);
   }

   public boolean setA(DenseMatrix64F A) {
      this.pinv.reshape(A.numCols, A.numRows, false);
      if (!this.svd.decompose(A)) {
         return false;
      }

      DenseMatrix64F U_t = this.svd.getU(null, true);
      DenseMatrix64F V = this.svd.getV(null, false);
      double[] S = this.svd.getSingularValues();
      int N = Math.min(A.numRows, A.numCols);
      double maxSingular = 0.0;

      for (int i = 0; i < N; i++) {
         if (S[i] > maxSingular) {
            maxSingular = S[i];
         }
      }

      double tau = this.threshold * Math.max(A.numCols, A.numRows) * maxSingular;
      if (maxSingular != 0.0) {
         for (int i = 0; i < N; i++) {
            double s = S[i];
            if (s < tau) {
               S[i] = 0.0;
            } else {
               S[i] = 1.0 / S[i];
            }
         }
      }

      for (int i = 0; i < V.numRows; i++) {
         int index = i * V.numCols;

         for (int j = 0; j < V.numCols; j++) {
            int var10001 = index++;
            V.data[var10001] = V.data[var10001] * S[j];
         }
      }

      CommonOps.mult(V, U_t, this.pinv);
      return true;
   }

   @Override
   public double quality() {
      throw new IllegalArgumentException("Not supported by this solver.");
   }

   public void solve(DenseMatrix64F b, DenseMatrix64F x) {
      CommonOps.mult(this.pinv, b, x);
   }

   public void invert(DenseMatrix64F A_inv) {
      A_inv.set(this.pinv);
   }

   @Override
   public boolean modifiesA() {
      return this.svd.inputModified();
   }

   @Override
   public boolean modifiesB() {
      return false;
   }

   public void setThreshold(double threshold) {
      this.threshold = threshold;
   }

   public SingularValueDecomposition<DenseMatrix64F> getDecomposer() {
      return this.svd;
   }
}
