package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverChol extends LinearSolverAbstract {
   CholeskyDecompositionCommon_D64 decomp;
   int n;
   double[] vv;
   double[] t;

   public LinearSolverChol(CholeskyDecompositionCommon_D64 decomp) {
      this.decomp = decomp;
   }

   public boolean setA(DenseMatrix64F A) {
      if (A.numRows != A.numCols) {
         throw new IllegalArgumentException("Matrix must be square");
      } else {
         this._setA(A);
         if (this.decomp.decompose(A)) {
            this.n = A.numCols;
            this.vv = this.decomp._getVV();
            this.t = this.decomp.getT().data;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public double quality() {
      return SpecializedOps.qualityTriangular(true, this.decomp.getT());
   }

   public void solve(DenseMatrix64F B, DenseMatrix64F X) {
      if (B.numCols == X.numCols && B.numRows == this.n && X.numRows == this.n) {
         int numCols = B.numCols;
         double[] dataB = B.data;
         double[] dataX = X.data;
         if (!this.decomp.isLower()) {
            throw new RuntimeException("Implement");
         }

         for (int j = 0; j < numCols; j++) {
            for (int i = 0; i < this.n; i++) {
               this.vv[i] = dataB[i * numCols + j];
            }

            this.solveInternalL();

            for (int i = 0; i < this.n; i++) {
               dataX[i * numCols + j] = this.vv[i];
            }
         }
      } else {
         throw new IllegalArgumentException("Unexpected matrix size");
      }
   }

   private void solveInternalL() {
      TriangularSolver.solveL(this.t, this.vv, this.n);
      TriangularSolver.solveTranL(this.t, this.vv, this.n);
   }

   @Override
   public void invert(DenseMatrix64F inv) {
      if (inv.numRows != this.n || inv.numCols != this.n) {
         throw new RuntimeException("Unexpected matrix dimension");
      }

      if (inv.data == this.t) {
         throw new IllegalArgumentException("Passing in the same matrix that was decomposed.");
      }

      double[] a = inv.data;
      if (this.decomp.isLower()) {
         this.setToInverseL(a);
      } else {
         throw new RuntimeException("Implement");
      }
   }

   public void setToInverseL(double[] a) {
      for (int i = 0; i < this.n; i++) {
         double el_ii = this.t[i * this.n + i];

         for (int j = 0; j <= i; j++) {
            double sum = i == j ? 1.0 : 0.0;

            for (int k = i - 1; k >= j; k--) {
               sum -= this.t[i * this.n + k] * a[j * this.n + k];
            }

            a[j * this.n + i] = sum / el_ii;
         }
      }

      for (int i = this.n - 1; i >= 0; i--) {
         double el_ii = this.t[i * this.n + i];

         for (int j = 0; j <= i; j++) {
            double sum = i < j ? 0.0 : a[j * this.n + i];

            for (int k = i + 1; k < this.n; k++) {
               sum -= this.t[k * this.n + i] * a[j * this.n + k];
            }

            a[i * this.n + j] = a[j * this.n + i] = sum / el_ii;
         }
      }
   }

   @Override
   public boolean modifiesA() {
      return this.decomp.inputModified();
   }

   @Override
   public boolean modifiesB() {
      return false;
   }
}
