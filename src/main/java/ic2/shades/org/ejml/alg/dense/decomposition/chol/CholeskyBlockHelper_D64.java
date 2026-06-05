package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.DenseMatrix64F;

class CholeskyBlockHelper_D64 {
   private DenseMatrix64F L;
   private double[] el;

   public CholeskyBlockHelper_D64(int widthMax) {
      this.L = new DenseMatrix64F(widthMax, widthMax);
      this.el = this.L.data;
   }

   public boolean decompose(DenseMatrix64F mat, int indexStart, int n) {
      double[] m = mat.data;
      double div_el_ii = 0.0;

      for (int i = 0; i < n; i++) {
         for (int j = i; j < n; j++) {
            double sum = m[indexStart + i * mat.numCols + j];
            int iEl = i * n;
            int jEl = j * n;

            for (int end = iEl + i; iEl < end; jEl++) {
               sum -= this.el[iEl] * this.el[jEl];
               iEl++;
            }

            if (i == j) {
               if (sum <= 0.0) {
                  return false;
               }

               double el_ii = Math.sqrt(sum);
               this.el[i * n + i] = el_ii;
               m[indexStart + i * mat.numCols + i] = el_ii;
               div_el_ii = 1.0 / el_ii;
            } else {
               double v = sum * div_el_ii;
               this.el[j * n + i] = v;
               m[indexStart + j * mat.numCols + i] = v;
            }
         }
      }

      return true;
   }

   public DenseMatrix64F getL() {
      return this.L;
   }
}
