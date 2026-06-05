package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class SingularOps {
   public static void descendingOrder(DenseMatrix64F U, boolean tranU, DenseMatrix64F W, DenseMatrix64F V, boolean tranV) {
      int numSingular = Math.min(W.numRows, W.numCols);
      checkSvdMatrixSize(U, tranU, W, V, tranV);

      for (int i = 0; i < numSingular; i++) {
         double bigValue = -1.0;
         int bigIndex = -1;

         for (int j = i; j < numSingular; j++) {
            double v = W.get(j, j);
            if (v > bigValue) {
               bigValue = v;
               bigIndex = j;
            }
         }

         if (bigIndex != i) {
            if (bigIndex == -1) {
               break;
            }

            double tmp = W.get(i, i);
            W.set(i, i, bigValue);
            W.set(bigIndex, bigIndex, tmp);
            if (V != null) {
               swapRowOrCol(V, tranV, i, bigIndex);
            }

            if (U != null) {
               swapRowOrCol(U, tranU, i, bigIndex);
            }
         }
      }
   }

   public static void descendingOrder(DenseMatrix64F U, boolean tranU, double[] singularValues, int numSingularValues, DenseMatrix64F V, boolean tranV) {
      for (int i = 0; i < numSingularValues; i++) {
         double bigValue = -1.0;
         int bigIndex = -1;

         for (int j = i; j < numSingularValues; j++) {
            double v = singularValues[j];
            if (v > bigValue) {
               bigValue = v;
               bigIndex = j;
            }
         }

         if (bigIndex != i) {
            if (bigIndex == -1) {
               break;
            }

            double tmp = singularValues[i];
            singularValues[i] = bigValue;
            singularValues[bigIndex] = tmp;
            if (V != null) {
               swapRowOrCol(V, tranV, i, bigIndex);
            }

            if (U != null) {
               swapRowOrCol(U, tranU, i, bigIndex);
            }
         }
      }
   }

   public static void checkSvdMatrixSize(DenseMatrix64F U, boolean tranU, DenseMatrix64F W, DenseMatrix64F V, boolean tranV) {
      int numSingular = Math.min(W.numRows, W.numCols);
      boolean compact = W.numRows == W.numCols;
      if (compact) {
         if (U != null) {
            if (tranU && U.numRows != numSingular) {
               throw new IllegalArgumentException("Unexpected size of matrix U");
            }

            if (!tranU && U.numCols != numSingular) {
               throw new IllegalArgumentException("Unexpected size of matrix U");
            }
         }

         if (V != null) {
            if (tranV && V.numRows != numSingular) {
               throw new IllegalArgumentException("Unexpected size of matrix V");
            }

            if (!tranV && V.numCols != numSingular) {
               throw new IllegalArgumentException("Unexpected size of matrix V");
            }
         }
      } else {
         if (U != null && U.numRows != U.numCols) {
            throw new IllegalArgumentException("Unexpected size of matrix U");
         }

         if (V != null && V.numRows != V.numCols) {
            throw new IllegalArgumentException("Unexpected size of matrix V");
         }

         if (U != null && U.numRows != W.numRows) {
            throw new IllegalArgumentException("Unexpected size of W");
         }

         if (V != null && V.numRows != W.numCols) {
            throw new IllegalArgumentException("Unexpected size of W");
         }
      }
   }

   private static void swapRowOrCol(DenseMatrix64F M, boolean tran, int i, int bigIndex) {
      if (tran) {
         for (int col = 0; col < M.numCols; col++) {
            double tmp = M.get(i, col);
            M.set(i, col, M.get(bigIndex, col));
            M.set(bigIndex, col, tmp);
         }
      } else {
         for (int row = 0; row < M.numRows; row++) {
            double tmp = M.get(row, i);
            M.set(row, i, M.get(row, bigIndex));
            M.set(row, bigIndex, tmp);
         }
      }
   }

   public static DenseMatrix64F nullSpace(SingularValueDecomposition<DenseMatrix64F> svd, DenseMatrix64F nullSpace, double tol) {
      int N = svd.numberOfSingularValues();
      double[] s = svd.getSingularValues();
      DenseMatrix64F V = svd.getV(null, true);
      if (V.numRows != svd.numCols()) {
         throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
      }

      int numVectors = svd.numCols() - N;

      for (int i = 0; i < N; i++) {
         if (s[i] <= tol) {
            numVectors++;
         }
      }

      if (nullSpace == null) {
         nullSpace = new DenseMatrix64F(numVectors, svd.numCols());
      } else {
         nullSpace.reshape(numVectors, svd.numCols());
      }

      int count = 0;

      for (int i = 0; i < N; i++) {
         if (s[i] <= tol) {
            CommonOps.extract(V, i, i + 1, 0, V.numCols, nullSpace, count++, 0);
         }
      }

      for (int i = N; i < svd.numCols(); i++) {
         CommonOps.extract(V, i, i + 1, 0, V.numCols, nullSpace, count++, 0);
      }

      CommonOps.transpose(nullSpace);
      return nullSpace;
   }

   public static DenseMatrix64F nullVector(SingularValueDecomposition<DenseMatrix64F> svd, boolean isRight, DenseMatrix64F nullVector) {
      int N = svd.numberOfSingularValues();
      double[] s = svd.getSingularValues();
      DenseMatrix64F A = isRight ? svd.getV(null, true) : svd.getU(null, false);
      if (isRight) {
         if (A.numRows != svd.numCols()) {
            throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
         }

         if (nullVector == null) {
            nullVector = new DenseMatrix64F(svd.numCols(), 1);
         }
      } else {
         if (A.numCols != svd.numRows()) {
            throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
         }

         if (nullVector == null) {
            nullVector = new DenseMatrix64F(svd.numRows(), 1);
         }
      }

      int smallestIndex = -1;
      if (isRight && svd.numCols() > svd.numRows()) {
         smallestIndex = svd.numCols() - 1;
      } else if (!isRight && svd.numCols() < svd.numRows()) {
         smallestIndex = svd.numRows() - 1;
      } else {
         double smallestValue = Double.MAX_VALUE;

         for (int i = 0; i < N; i++) {
            if (s[i] < smallestValue) {
               smallestValue = s[i];
               smallestIndex = i;
            }
         }
      }

      if (isRight) {
         SpecializedOps.subvector(A, smallestIndex, 0, A.numRows, true, 0, nullVector);
      } else {
         SpecializedOps.subvector(A, 0, smallestIndex, A.numRows, false, 0, nullVector);
      }

      return nullVector;
   }

   public static int rank(SingularValueDecomposition svd, double threshold) {
      int numRank = 0;
      double[] w = svd.getSingularValues();
      int N = svd.numberOfSingularValues();

      for (int j = 0; j < N; j++) {
         if (w[j] > threshold) {
            numRank++;
         }
      }

      return numRank;
   }

   public static int nullity(SingularValueDecomposition svd, double threshold) {
      int ret = 0;
      double[] w = svd.getSingularValues();
      int N = svd.numberOfSingularValues();
      int numCol = svd.numCols();

      for (int j = 0; j < N; j++) {
         if (w[j] <= threshold) {
            ret++;
         }
      }

      return ret + numCol - N;
   }
}
