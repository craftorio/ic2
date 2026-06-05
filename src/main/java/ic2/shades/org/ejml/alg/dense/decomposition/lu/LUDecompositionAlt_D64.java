package ic2.shades.org.ejml.alg.dense.decomposition.lu;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class LUDecompositionAlt_D64 extends LUDecompositionBase_D64 {
   public boolean decompose(DenseMatrix64F a) {
      this.decomposeCommonInit(a);
      double[] LUcolj = this.vv;

      for (int j = 0; j < this.n; j++) {
         for (int i = 0; i < this.m; i++) {
            LUcolj[i] = this.dataLU[i * this.n + j];
         }

         for (int i = 0; i < this.m; i++) {
            int rowIndex = i * this.n;
            int kmax = i < j ? i : j;
            double s = 0.0;

            for (int k = 0; k < kmax; k++) {
               s += this.dataLU[rowIndex + k] * LUcolj[k];
            }

            this.dataLU[rowIndex + j] = LUcolj[i] -= s;
         }

         int p = j;
         double max = Math.abs(LUcolj[p]);

         for (int i = j + 1; i < this.m; i++) {
            double v = Math.abs(LUcolj[i]);
            if (v > max) {
               p = i;
               max = v;
            }
         }

         if (p != j) {
            int rowP = p * this.n;
            int rowJ = j * this.n;

            for (int endP = rowP + this.n; rowP < endP; rowJ++) {
               double t = this.dataLU[rowP];
               this.dataLU[rowP] = this.dataLU[rowJ];
               this.dataLU[rowJ] = t;
               rowP++;
            }

            int k = this.pivot[p];
            this.pivot[p] = this.pivot[j];
            this.pivot[j] = k;
            this.pivsign = -this.pivsign;
         }

         this.indx[j] = p;
         if (j < this.m) {
            double lujj = this.dataLU[j * this.n + j];
            if (lujj != 0.0) {
               for (int i = j + 1; i < this.m; i++) {
                  this.dataLU[i * this.n + j] = this.dataLU[i * this.n + j] / lujj;
               }
            }
         }
      }

      return true;
   }
}
