package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class LinearSolverLuKJI extends LinearSolverLuBase {
   private double[] dataLU;
   private int[] pivot;

   public LinearSolverLuKJI(LUDecompositionBase_D64 decomp) {
      super(decomp);
   }

   @Override
   public boolean setA(DenseMatrix64F A) {
      boolean ret = super.setA(A);
      this.pivot = this.decomp.getPivot();
      this.dataLU = this.decomp.getLU().data;
      return ret;
   }

   public void solve(DenseMatrix64F b, DenseMatrix64F x) {
      if (b.numCols == x.numCols && b.numRows == this.numRows && x.numRows == this.numCols) {
         if (b == x) {
            throw new IllegalArgumentException("Current doesn't support using the same matrix instance");
         }

         SpecializedOps.copyChangeRow(this.pivot, b, x);
         int nx = b.numCols;
         double[] dataX = x.data;

         for (int k = 0; k < this.numCols; k++) {
            for (int i = k + 1; i < this.numCols; i++) {
               for (int j = 0; j < nx; j++) {
                  dataX[i * nx + j] = dataX[i * nx + j] - dataX[k * nx + j] * this.dataLU[i * this.numCols + k];
               }
            }
         }

         for (int k = this.numCols - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
               dataX[k * nx + j] = dataX[k * nx + j] / this.dataLU[k * this.numCols + k];
            }

            for (int i = 0; i < k; i++) {
               for (int j = 0; j < nx; j++) {
                  dataX[i * nx + j] = dataX[i * nx + j] - dataX[k * nx + j] * this.dataLU[i * this.numCols + k];
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Unexpected matrix size");
      }
   }
}
