package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class VectorVectorMult {
   public static void mult(DenseMatrix64F x, DenseMatrix64F y, DenseMatrix64F A) {
   }

   public static double innerProd(D1Matrix64F x, D1Matrix64F y) {
      int m = x.getNumElements();
      double total = 0.0;

      for (int i = 0; i < m; i++) {
         total += x.get(i) * y.get(i);
      }

      return total;
   }

   public static double innerProdA(D1Matrix64F x, D1Matrix64F A, D1Matrix64F y) {
      int n = A.numRows;
      int m = A.numCols;
      if (x.getNumElements() != n) {
         throw new IllegalArgumentException("Unexpected number of elements in x");
      }

      if (y.getNumElements() != m) {
         throw new IllegalArgumentException("Unexpected number of elements in y");
      }

      double result = 0.0;

      for (int i = 0; i < m; i++) {
         double total = 0.0;

         for (int j = 0; j < n; j++) {
            total += x.get(j) * A.unsafe_get(j, i);
         }

         result += total * y.get(i);
      }

      return result;
   }

   public static double innerProdTranA(D1Matrix64F x, D1Matrix64F A, D1Matrix64F y) {
      int n = A.numRows;
      if (n != A.numCols) {
         throw new IllegalArgumentException("A must be square");
      }

      if (x.getNumElements() != n) {
         throw new IllegalArgumentException("Unexpected number of elements in x");
      }

      if (y.getNumElements() != n) {
         throw new IllegalArgumentException("Unexpected number of elements in y");
      }

      double result = 0.0;

      for (int i = 0; i < n; i++) {
         double total = 0.0;

         for (int j = 0; j < n; j++) {
            total += x.get(j) * A.unsafe_get(i, j);
         }

         result += total * y.get(i);
      }

      return result;
   }

   public static void outerProd(D1Matrix64F x, D1Matrix64F y, RowD1Matrix64F A) {
      int m = A.numRows;
      int n = A.numCols;
      int index = 0;

      for (int i = 0; i < m; i++) {
         double xdat = x.get(i);

         for (int j = 0; j < n; j++) {
            A.set(index++, xdat * y.get(j));
         }
      }
   }

   public static void addOuterProd(double gamma, D1Matrix64F x, D1Matrix64F y, RowD1Matrix64F A) {
      int m = A.numRows;
      int n = A.numCols;
      int index = 0;
      if (gamma == 1.0) {
         for (int i = 0; i < m; i++) {
            double xdat = x.get(i);

            for (int j = 0; j < n; j++) {
               A.plus(index++, xdat * y.get(j));
            }
         }
      } else {
         for (int i = 0; i < m; i++) {
            double xdat = x.get(i);

            for (int j = 0; j < n; j++) {
               A.plus(index++, gamma * xdat * y.get(j));
            }
         }
      }
   }

   public static void householder(double gamma, D1Matrix64F u, D1Matrix64F x, D1Matrix64F y) {
      int n = u.getNumElements();
      double sum = 0.0;

      for (int i = 0; i < n; i++) {
         sum += u.get(i) * x.get(i);
      }

      for (int i = 0; i < n; i++) {
         y.set(i, x.get(i) + gamma * u.get(i) * sum);
      }
   }

   public static void rank1Update(double gamma, DenseMatrix64F A, DenseMatrix64F u, DenseMatrix64F w, DenseMatrix64F B) {
      int n = u.getNumElements();
      int matrixIndex = 0;

      for (int i = 0; i < n; i++) {
         double elementU = u.data[i];

         for (int j = 0; j < n; matrixIndex++) {
            B.data[matrixIndex] = A.data[matrixIndex] + gamma * elementU * w.data[j];
            j++;
         }
      }
   }

   public static void rank1Update(double gamma, DenseMatrix64F A, DenseMatrix64F u, DenseMatrix64F w) {
      int n = u.getNumElements();
      int matrixIndex = 0;

      for (int i = 0; i < n; i++) {
         double elementU = u.data[i];

         for (int j = 0; j < n; j++) {
            int var10001 = matrixIndex++;
            A.data[var10001] = A.data[var10001] + gamma * elementU * w.data[j];
         }
      }
   }
}
