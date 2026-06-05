package ic2.shades.org.ejml.alg.dense.decomposition.chol;

public class CholeskyDecompositionInner_D64 extends CholeskyDecompositionCommon_D64 {
   public CholeskyDecompositionInner_D64() {
      super(true);
   }

   public CholeskyDecompositionInner_D64(boolean lower) {
      super(lower);
   }

   @Override
   protected boolean decomposeLower() {
      double div_el_ii = 0.0;

      for (int i = 0; i < this.n; i++) {
         for (int j = i; j < this.n; j++) {
            double sum = this.t[i * this.n + j];
            int iEl = i * this.n;
            int jEl = j * this.n;

            for (int end = iEl + i; iEl < end; jEl++) {
               sum -= this.t[iEl] * this.t[jEl];
               iEl++;
            }

            if (i == j) {
               if (sum <= 0.0) {
                  return false;
               }

               double el_ii = Math.sqrt(sum);
               this.t[i * this.n + i] = el_ii;
               div_el_ii = 1.0 / el_ii;
            } else {
               this.t[j * this.n + i] = sum * div_el_ii;
            }
         }
      }

      for (int i = 0; i < this.n; i++) {
         for (int j = i + 1; j < this.n; j++) {
            this.t[i * this.n + j] = 0.0;
         }
      }

      return true;
   }

   @Override
   protected boolean decomposeUpper() {
      double div_el_ii = 0.0;

      for (int i = 0; i < this.n; i++) {
         for (int j = i; j < this.n; j++) {
            double sum = this.t[i * this.n + j];

            for (int k = 0; k < i; k++) {
               sum -= this.t[k * this.n + i] * this.t[k * this.n + j];
            }

            if (i == j) {
               if (sum <= 0.0) {
                  return false;
               }

               double el_ii = Math.sqrt(sum);
               this.t[i * this.n + i] = el_ii;
               div_el_ii = 1.0 / el_ii;
            } else {
               this.t[i * this.n + j] = sum * div_el_ii;
            }
         }
      }

      for (int i = 0; i < this.n; i++) {
         for (int j = 0; j < i; j++) {
            this.t[i * this.n + j] = 0.0;
         }
      }

      return true;
   }
}
