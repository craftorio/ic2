package ic2.shades.org.ejml.alg.block;

public class BlockInnerTriangularSolver {
   public static void invertLower(double[] L, double[] L_inv, int m, int offsetL, int offsetL_inv) {
      for (int i = 0; i < m; i++) {
         double L_ii = L[offsetL + i * m + i];

         for (int j = 0; j < i; j++) {
            double val = 0.0;

            for (int k = j; k < i; k++) {
               val += L[offsetL + i * m + k] * L_inv[offsetL_inv + k * m + j];
            }

            L_inv[offsetL_inv + i * m + j] = -val / L_ii;
         }

         L_inv[offsetL_inv + i * m + i] = 1.0 / L_ii;
      }
   }

   public static void invertLower(double[] L, int m, int offsetL) {
      for (int i = 0; i < m; i++) {
         double L_ii = L[offsetL + i * m + i];

         for (int j = 0; j < i; j++) {
            double val = 0.0;

            for (int k = j; k < i; k++) {
               val += L[offsetL + i * m + k] * L[offsetL + k * m + j];
            }

            L[offsetL + i * m + j] = -val / L_ii;
         }

         L[offsetL + i * m + i] = 1.0 / L_ii;
      }
   }

   public static void solveL(double[] L, double[] b, int m, int n, int strideL, int offsetL, int offsetB) {
      for (int j = 0; j < n; j++) {
         for (int i = 0; i < m; i++) {
            double sum = b[offsetB + i * n + j];

            for (int k = 0; k < i; k++) {
               sum -= L[offsetL + i * strideL + k] * b[offsetB + k * n + j];
            }

            b[offsetB + i * n + j] = sum / L[offsetL + i * strideL + i];
         }
      }
   }

   public static void solveTransL(double[] L, double[] b, int m, int n, int strideL, int offsetL, int offsetB) {
      for (int j = 0; j < n; j++) {
         for (int i = m - 1; i >= 0; i--) {
            double sum = b[offsetB + i * n + j];

            for (int k = i + 1; k < m; k++) {
               sum -= L[offsetL + k * strideL + i] * b[offsetB + k * n + j];
            }

            b[offsetB + i * n + j] = sum / L[offsetL + i * strideL + i];
         }
      }
   }

   public static void solveLTransB(double[] L, double[] b, int m, int n, int strideL, int offsetL, int offsetB) {
      for (int j = 0; j < n; j++) {
         for (int i = 0; i < m; i++) {
            double sum = b[offsetB + j * m + i];
            int l = offsetL + i * strideL;
            int bb = offsetB + j * m;
            int endL = l + i;

            while (l != endL) {
               sum -= L[l++] * b[bb++];
            }

            b[offsetB + j * m + i] = sum / L[offsetL + i * strideL + i];
         }
      }
   }

   public static void solveU(double[] U, double[] b, int m, int n, int strideU, int offsetU, int offsetB) {
      for (int j = 0; j < n; j++) {
         for (int i = m - 1; i >= 0; i--) {
            double sum = b[offsetB + i * n + j];

            for (int k = i + 1; k < m; k++) {
               sum -= U[offsetU + i * strideU + k] * b[offsetB + k * n + j];
            }

            b[offsetB + i * n + j] = sum / U[offsetU + i * strideU + i];
         }
      }
   }

   public static void solveTransU(double[] U, double[] b, int m, int n, int strideU, int offsetU, int offsetB) {
      for (int j = 0; j < n; j++) {
         for (int i = 0; i < m; i++) {
            double sum = b[offsetB + i * n + j];

            for (int k = 0; k < i; k++) {
               sum -= U[offsetU + k * strideU + i] * b[offsetB + k * n + j];
            }

            b[offsetB + i * n + j] = sum / U[offsetU + i * strideU + i];
         }
      }
   }
}
