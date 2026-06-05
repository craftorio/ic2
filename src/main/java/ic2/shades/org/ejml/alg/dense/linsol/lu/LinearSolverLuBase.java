package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public abstract class LinearSolverLuBase extends LinearSolverAbstract {
   protected LUDecompositionBase_D64 decomp;

   public LinearSolverLuBase(LUDecompositionBase_D64 decomp) {
      this.decomp = decomp;
   }

   public boolean setA(DenseMatrix64F A) {
      this._setA(A);
      return this.decomp.decompose(A);
   }

   @Override
   public double quality() {
      return this.decomp.quality();
   }

   @Override
   public void invert(DenseMatrix64F A_inv) {
      double[] vv = this.decomp._getVV();
      DenseMatrix64F LU = this.decomp.getLU();
      if (A_inv.numCols == LU.numCols && A_inv.numRows == LU.numRows) {
         int n = this.A.numCols;
         double[] dataInv = A_inv.data;

         for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
               vv[i] = i == j ? 1.0 : 0.0;
            }

            this.decomp._solveVectorInternal(vv);
            int index = j;

            for (int i = 0; i < n; index += n) {
               dataInv[index] = vv[i];
               i++;
            }
         }
      } else {
         throw new IllegalArgumentException("Unexpected matrix dimension");
      }
   }

   public void improveSol(DenseMatrix64F b, DenseMatrix64F x) {
      if (b.numCols != x.numCols) {
         throw new IllegalArgumentException("bad shapes");
      }

      double[] dataA = this.A.data;
      double[] dataB = b.data;
      double[] dataX = x.data;
      int nc = b.numCols;
      int n = b.numCols;
      double[] vv = this.decomp._getVV();
      DenseMatrix64F LU = this.decomp.getLU();

      for (int k = 0; k < nc; k++) {
         for (int i = 0; i < n; i++) {
            double sdp = -dataB[i * nc + k];

            for (int j = 0; j < n; j++) {
               sdp += dataA[i * n + j] * dataX[j * nc + k];
            }

            vv[i] = sdp;
         }

         this.decomp._solveVectorInternal(vv);

         for (int i = 0; i < n; i++) {
            dataX[i * nc + k] = dataX[i * nc + k] - vv[i];
         }
      }
   }

   @Override
   public boolean modifiesA() {
      return false;
   }

   @Override
   public boolean modifiesB() {
      return false;
   }

   public LUDecompositionBase_D64 getDecomposer() {
      return this.decomp;
   }
}
