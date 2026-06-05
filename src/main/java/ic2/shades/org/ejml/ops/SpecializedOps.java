package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class SpecializedOps {
   public static DenseMatrix64F createReflector(RowD1Matrix64F u) {
      if (!MatrixFeatures.isVector(u)) {
         throw new IllegalArgumentException("u must be a vector");
      }

      double norm = NormOps.fastNormF(u);
      double gamma = -2.0 / (norm * norm);
      DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
      CommonOps.multAddTransB(gamma, u, u, Q);
      return Q;
   }

   public static DenseMatrix64F createReflector(DenseMatrix64F u, double gamma) {
      if (!MatrixFeatures.isVector(u)) {
         throw new IllegalArgumentException("u must be a vector");
      }

      DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
      CommonOps.multAddTransB(-gamma, u, u, Q);
      return Q;
   }

   public static DenseMatrix64F copyChangeRow(int[] order, DenseMatrix64F src, DenseMatrix64F dst) {
      if (dst == null) {
         dst = new DenseMatrix64F(src.numRows, src.numCols);
      } else if (src.numRows != dst.numRows || src.numCols != dst.numCols) {
         throw new IllegalArgumentException("src and dst must have the same dimensions.");
      }

      for (int i = 0; i < src.numRows; i++) {
         int indexDst = i * src.numCols;
         int indexSrc = order[i] * src.numCols;
         System.arraycopy(src.data, indexSrc, dst.data, indexDst, src.numCols);
      }

      return dst;
   }

   public static DenseMatrix64F copyTriangle(DenseMatrix64F src, DenseMatrix64F dst, boolean upper) {
      if (dst == null) {
         dst = new DenseMatrix64F(src.numRows, src.numCols);
      } else if (src.numRows != dst.numRows || src.numCols != dst.numCols) {
         throw new IllegalArgumentException("src and dst must have the same dimensions.");
      }

      if (upper) {
         int N = Math.min(src.numRows, src.numCols);

         for (int i = 0; i < N; i++) {
            int index = i * src.numCols + i;
            System.arraycopy(src.data, index, dst.data, index, src.numCols - i);
         }
      } else {
         for (int i = 0; i < src.numRows; i++) {
            int length = Math.min(i + 1, src.numCols);
            int index = i * src.numCols;
            System.arraycopy(src.data, index, dst.data, index, length);
         }
      }

      return dst;
   }

   public static double diffNormF(D1Matrix64F a, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();
         DenseMatrix64F diff = new DenseMatrix64F(size, 1);

         for (int i = 0; i < size; i++) {
            diff.set(i, b.get(i) - a.get(i));
         }

         return NormOps.normF(diff);
      } else {
         throw new IllegalArgumentException("Both matrices must have the same shape.");
      }
   }

   public static double diffNormF_fast(D1Matrix64F a, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();
         double total = 0.0;

         for (int i = 0; i < size; i++) {
            double diff = b.get(i) - a.get(i);
            total += diff * diff;
         }

         return Math.sqrt(total);
      } else {
         throw new IllegalArgumentException("Both matrices must have the same shape.");
      }
   }

   public static double diffNormP1(D1Matrix64F a, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();
         double total = 0.0;

         for (int i = 0; i < size; i++) {
            total += Math.abs(b.get(i) - a.get(i));
         }

         return total;
      } else {
         throw new IllegalArgumentException("Both matrices must have the same shape.");
      }
   }

   public static void addIdentity(RowD1Matrix64F A, RowD1Matrix64F B, double alpha) {
      if (A.numCols != A.numRows) {
         throw new IllegalArgumentException("A must be square");
      }

      if (B.numCols == A.numCols && B.numRows == A.numRows) {
         int n = A.numCols;
         int index = 0;

         for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; index++) {
               if (i == j) {
                  B.set(index, A.get(index) + alpha);
               } else {
                  B.set(index, A.get(index));
               }

               j++;
            }
         }
      } else {
         throw new IllegalArgumentException("B must be the same shape as A");
      }
   }

   public static void subvector(RowD1Matrix64F A, int rowA, int colA, int length, boolean row, int offsetV, RowD1Matrix64F v) {
      if (row) {
         for (int i = 0; i < length; i++) {
            v.set(offsetV + i, A.get(rowA, colA + i));
         }
      } else {
         for (int i = 0; i < length; i++) {
            v.set(offsetV + i, A.get(rowA + i, colA));
         }
      }
   }

   public static DenseMatrix64F[] splitIntoVectors(RowD1Matrix64F A, boolean column) {
      int w = column ? A.numCols : A.numRows;
      int M = column ? A.numRows : 1;
      int N = column ? 1 : A.numCols;
      int o = Math.max(M, N);
      DenseMatrix64F[] ret = new DenseMatrix64F[w];

      for (int i = 0; i < w; i++) {
         DenseMatrix64F a = new DenseMatrix64F(M, N);
         if (column) {
            subvector(A, 0, i, o, false, 0, a);
         } else {
            subvector(A, i, 0, o, true, 0, a);
         }

         ret[i] = a;
      }

      return ret;
   }

   public static DenseMatrix64F pivotMatrix(DenseMatrix64F ret, int[] pivots, int numPivots, boolean transposed) {
      if (ret == null) {
         ret = new DenseMatrix64F(numPivots, numPivots);
      } else {
         if (ret.numCols != numPivots || ret.numRows != numPivots) {
            throw new IllegalArgumentException("Unexpected matrix dimension");
         }

         CommonOps.fill(ret, 0.0);
      }

      if (transposed) {
         for (int i = 0; i < numPivots; i++) {
            ret.set(pivots[i], i, 1.0);
         }
      } else {
         for (int i = 0; i < numPivots; i++) {
            ret.set(i, pivots[i], 1.0);
         }
      }

      return ret;
   }

   public static double diagProd(RowD1Matrix64F T) {
      double prod = 1.0;
      int N = Math.min(T.numRows, T.numCols);

      for (int i = 0; i < N; i++) {
         prod *= T.unsafe_get(i, i);
      }

      return prod;
   }

   public static double qualityTriangular(boolean upper, D1Matrix64F T) {
      int N = Math.min(T.numRows, T.numCols);
      double max = CommonOps.elementMaxAbs(T);
      if (max == 0.0) {
         return 0.0;
      }

      double quality = 1.0;

      for (int i = 0; i < N; i++) {
         quality *= T.unsafe_get(i, i) / max;
      }

      return Math.abs(quality);
   }

   public static double elementSumSq(D1Matrix64F m) {
      double total = 0.0;
      int N = m.getNumElements();

      for (int i = 0; i < N; i++) {
         double d = m.data[i];
         total += d * d;
      }

      return total;
   }
}
