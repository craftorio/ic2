package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import ic2.shades.org.ejml.alg.dense.misc.ImplCommonOps_DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.misc.ImplCommonOps_Matrix64F;
import ic2.shades.org.ejml.alg.dense.misc.RrefGaussJordanRowPivot;
import ic2.shades.org.ejml.alg.dense.misc.TransposeAlgs;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledDeterminantFromMinor;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.alg.dense.mult.MatrixMatrixMult;
import ic2.shades.org.ejml.alg.dense.mult.MatrixMultProduct;
import ic2.shades.org.ejml.alg.dense.mult.MatrixVectorMult;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.interfaces.linsol.ReducedRowEchelonForm;
import java.util.Arrays;

public class CommonOps {
   public static void mult(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols == 1) {
         MatrixVectorMult.mult(a, b, c);
      } else if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.mult_reorder(a, b, c);
      } else {
         MatrixMatrixMult.mult_small(a, b, c);
      }
   }

   public static void mult(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.mult_reorder(alpha, a, b, c);
      } else {
         MatrixMatrixMult.mult_small(alpha, a, b, c);
      }
   }

   public static void multTransA(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols == 1) {
         if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixVectorMult.multTransA_reorder(a, b, c);
         } else {
            MatrixVectorMult.multTransA_small(a, b, c);
         }
      } else if (a.numCols < EjmlParameters.MULT_COLUMN_SWITCH && b.numCols < EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multTransA_small(a, b, c);
      } else {
         MatrixMatrixMult.multTransA_reorder(a, b, c);
      }
   }

   public static void multTransA(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (a.numCols < EjmlParameters.MULT_COLUMN_SWITCH && b.numCols < EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multTransA_small(alpha, a, b, c);
      } else {
         MatrixMatrixMult.multTransA_reorder(alpha, a, b, c);
      }
   }

   public static void multTransB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numRows == 1) {
         MatrixVectorMult.mult(a, b, c);
      } else {
         MatrixMatrixMult.multTransB(a, b, c);
      }
   }

   public static void multTransB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      MatrixMatrixMult.multTransB(alpha, a, b, c);
   }

   public static void multTransAB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numRows == 1) {
         if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixVectorMult.multTransA_reorder(a, b, c);
         } else {
            MatrixVectorMult.multTransA_small(a, b, c);
         }
      } else if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
         MatrixMatrixMult.multTransAB_aux(a, b, c, null);
      } else {
         MatrixMatrixMult.multTransAB(a, b, c);
      }
   }

   public static void multTransAB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
         MatrixMatrixMult.multTransAB_aux(alpha, a, b, c, null);
      } else {
         MatrixMatrixMult.multTransAB(alpha, a, b, c);
      }
   }

   public static void multInner(RowD1Matrix64F a, RowD1Matrix64F c) {
      if (a.numCols == c.numCols && a.numCols == c.numRows) {
         if (a.numCols >= EjmlParameters.MULT_INNER_SWITCH) {
            MatrixMultProduct.inner_small(a, c);
         } else {
            MatrixMultProduct.inner_reorder(a, c);
         }
      } else {
         throw new IllegalArgumentException("Rows and columns of 'c' must be the same as the columns in 'a'");
      }
   }

   public static void multOuter(RowD1Matrix64F a, RowD1Matrix64F c) {
      if (a.numRows == c.numCols && a.numRows == c.numRows) {
         MatrixMultProduct.outer(a, c);
      } else {
         throw new IllegalArgumentException("Rows and columns of 'c' must be the same as the rows in 'a'");
      }
   }

   public static void multAdd(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols == 1) {
         MatrixVectorMult.multAdd(a, b, c);
      } else if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multAdd_reorder(a, b, c);
      } else {
         MatrixMatrixMult.multAdd_small(a, b, c);
      }
   }

   public static void multAdd(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multAdd_reorder(alpha, a, b, c);
      } else {
         MatrixMatrixMult.multAdd_small(alpha, a, b, c);
      }
   }

   public static void multAddTransA(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numCols == 1) {
         if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixVectorMult.multAddTransA_reorder(a, b, c);
         } else {
            MatrixVectorMult.multAddTransA_small(a, b, c);
         }
      } else if (a.numCols < EjmlParameters.MULT_COLUMN_SWITCH && b.numCols < EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multAddTransA_small(a, b, c);
      } else {
         MatrixMatrixMult.multAddTransA_reorder(a, b, c);
      }
   }

   public static void multAddTransA(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (a.numCols < EjmlParameters.MULT_COLUMN_SWITCH && b.numCols < EjmlParameters.MULT_COLUMN_SWITCH) {
         MatrixMatrixMult.multAddTransA_small(alpha, a, b, c);
      } else {
         MatrixMatrixMult.multAddTransA_reorder(alpha, a, b, c);
      }
   }

   public static void multAddTransB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      MatrixMatrixMult.multAddTransB(a, b, c);
   }

   public static void multAddTransB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      MatrixMatrixMult.multAddTransB(alpha, a, b, c);
   }

   public static void multAddTransAB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (b.numRows == 1) {
         if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixVectorMult.multAddTransA_reorder(a, b, c);
         } else {
            MatrixVectorMult.multAddTransA_small(a, b, c);
         }
      } else if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
         MatrixMatrixMult.multAddTransAB_aux(a, b, c, null);
      } else {
         MatrixMatrixMult.multAddTransAB(a, b, c);
      }
   }

   public static void multAddTransAB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c) {
      if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
         MatrixMatrixMult.multAddTransAB_aux(alpha, a, b, c, null);
      } else {
         MatrixMatrixMult.multAddTransAB(alpha, a, b, c);
      }
   }

   public static boolean solve(DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F x) {
      LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.general(a.numRows, a.numCols);
      solver = new LinearSolverSafe<>(solver);
      if (!solver.setA(a)) {
         return false;
      }

      solver.solve(b, x);
      return true;
   }

   public static void transpose(DenseMatrix64F mat) {
      if (mat.numCols == mat.numRows) {
         TransposeAlgs.square(mat);
      } else {
         DenseMatrix64F b = new DenseMatrix64F(mat.numCols, mat.numRows);
         transpose(mat, b);
         mat.setReshape(b);
      }
   }

   public static DenseMatrix64F transpose(DenseMatrix64F A, DenseMatrix64F A_tran) {
      if (A_tran == null) {
         A_tran = new DenseMatrix64F(A.numCols, A.numRows);
      } else if (A.numRows != A_tran.numCols || A.numCols != A_tran.numRows) {
         throw new IllegalArgumentException("Incompatible matrix dimensions");
      }

      if (A.numRows > EjmlParameters.TRANSPOSE_SWITCH && A.numCols > EjmlParameters.TRANSPOSE_SWITCH) {
         TransposeAlgs.block(A, A_tran, EjmlParameters.BLOCK_WIDTH);
      } else {
         TransposeAlgs.standard(A, A_tran);
      }

      return A_tran;
   }

   public static double trace(RowD1Matrix64F a) {
      int N = Math.min(a.numRows, a.numCols);
      double sum = 0.0;
      int index = 0;

      for (int i = 0; i < N; i++) {
         sum += a.get(index);
         index += 1 + a.numCols;
      }

      return sum;
   }

   public static double det(DenseMatrix64F mat) {
      int numCol = mat.getNumCols();
      int numRow = mat.getNumRows();
      if (numCol != numRow) {
         throw new IllegalArgumentException("Must be a square matrix.");
      }

      if (numCol <= 6) {
         return numCol >= 2 ? UnrolledDeterminantFromMinor.det(mat) : mat.get(0);
      }

      LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
      if (alg.inputModified()) {
         mat = mat.copy();
      }

      return !alg.decompose(mat) ? 0.0 : alg.computeDeterminant();
   }

   public static boolean invert(DenseMatrix64F mat) {
      if (mat.numCols <= 5) {
         if (mat.numCols != mat.numRows) {
            throw new IllegalArgumentException("Must be a square matrix.");
         }

         if (mat.numCols >= 2) {
            UnrolledInverseFromMinor.inv(mat, mat);
         } else {
            mat.set(0, 1.0 / mat.get(0));
         }
      } else {
         LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
         LinearSolverLu solver = new LinearSolverLu(alg);
         if (!solver.setA(mat)) {
            return false;
         }

         solver.invert(mat);
      }

      return true;
   }

   public static boolean invert(DenseMatrix64F mat, DenseMatrix64F result) {
      if (mat.numCols <= 5) {
         if (mat.numCols != mat.numRows) {
            throw new IllegalArgumentException("Must be a square matrix.");
         }

         if (result.numCols >= 2) {
            UnrolledInverseFromMinor.inv(mat, result);
         } else {
            result.set(0, 1.0 / mat.get(0));
         }
      } else {
         LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
         LinearSolverLu solver = new LinearSolverLu(alg);
         if (solver.modifiesA()) {
            mat = mat.copy();
         }

         if (!solver.setA(mat)) {
            return false;
         }

         solver.invert(result);
      }

      return true;
   }

   public static void pinv(DenseMatrix64F A, DenseMatrix64F invA) {
      LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.pseudoInverse(true);
      if (solver.modifiesA()) {
         A = A.copy();
      }

      if (!solver.setA(A)) {
         throw new IllegalArgumentException("Invert failed, maybe a bug?");
      }

      solver.invert(invA);
   }

   public static DenseMatrix64F[] columnsToVector(DenseMatrix64F A, DenseMatrix64F[] v) {
      DenseMatrix64F[] ret;
      if (v != null && v.length >= A.numCols) {
         ret = v;
      } else {
         ret = new DenseMatrix64F[A.numCols];
      }

      for (int i = 0; i < ret.length; i++) {
         if (ret[i] == null) {
            ret[i] = new DenseMatrix64F(A.numRows, 1);
         } else {
            ret[i].reshape(A.numRows, 1, false);
         }

         DenseMatrix64F u = ret[i];

         for (int j = 0; j < A.numRows; j++) {
            u.set(j, 0, A.get(j, i));
         }
      }

      return ret;
   }

   public static DenseMatrix64F[] rowsToVector(DenseMatrix64F A, DenseMatrix64F[] v) {
      DenseMatrix64F[] ret;
      if (v != null && v.length >= A.numRows) {
         ret = v;
      } else {
         ret = new DenseMatrix64F[A.numRows];
      }

      for (int i = 0; i < ret.length; i++) {
         if (ret[i] == null) {
            ret[i] = new DenseMatrix64F(A.numCols, 1);
         } else {
            ret[i].reshape(A.numCols, 1, false);
         }

         DenseMatrix64F u = ret[i];

         for (int j = 0; j < A.numCols; j++) {
            u.set(j, 0, A.get(i, j));
         }
      }

      return ret;
   }

   public static void setIdentity(RowD1Matrix64F mat) {
      int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;
      Arrays.fill(mat.data, 0, mat.getNumElements(), 0.0);
      int index = 0;

      for (int i = 0; i < width; index += mat.numCols + 1) {
         mat.data[index] = 1.0;
         i++;
      }
   }

   public static DenseMatrix64F identity(int width) {
      DenseMatrix64F ret = new DenseMatrix64F(width, width);

      for (int i = 0; i < width; i++) {
         ret.set(i, i, 1.0);
      }

      return ret;
   }

   public static DenseMatrix64F identity(int numRows, int numCols) {
      DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
      int small = numRows < numCols ? numRows : numCols;

      for (int i = 0; i < small; i++) {
         ret.set(i, i, 1.0);
      }

      return ret;
   }

   public static DenseMatrix64F diag(double... diagEl) {
      return diag(null, diagEl.length, diagEl);
   }

   public static DenseMatrix64F diag(DenseMatrix64F ret, int width, double... diagEl) {
      if (ret == null) {
         ret = new DenseMatrix64F(width, width);
      } else {
         if (ret.numRows != width || ret.numCols != width) {
            throw new IllegalArgumentException("Unexpected matrix size");
         }

         fill(ret, 0.0);
      }

      for (int i = 0; i < width; i++) {
         ret.unsafe_set(i, i, diagEl[i]);
      }

      return ret;
   }

   public static DenseMatrix64F diagR(int numRows, int numCols, double... diagEl) {
      DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
      int o = Math.min(numRows, numCols);

      for (int i = 0; i < o; i++) {
         ret.set(i, i, diagEl[i]);
      }

      return ret;
   }

   public static void kron(DenseMatrix64F A, DenseMatrix64F B, DenseMatrix64F C) {
      int numColsC = A.numCols * B.numCols;
      int numRowsC = A.numRows * B.numRows;
      if (C.numCols == numColsC && C.numRows == numRowsC) {
         for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
               double a = A.get(i, j);

               for (int rowB = 0; rowB < B.numRows; rowB++) {
                  for (int colB = 0; colB < B.numCols; colB++) {
                     double val = a * B.get(rowB, colB);
                     C.set(i * B.numRows + rowB, j * B.numCols + colB, val);
                  }
               }
            }
         }
      } else {
         throw new IllegalArgumentException("C does not have the expected dimensions");
      }
   }

   public static void extract(ReshapeMatrix64F src, int srcY0, int srcY1, int srcX0, int srcX1, ReshapeMatrix64F dst, int dstY0, int dstX0) {
      if (srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows) {
         throw new IllegalArgumentException("srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows");
      }

      if (srcX1 >= srcX0 && srcX0 >= 0 && srcX1 <= src.numCols) {
         int w = srcX1 - srcX0;
         int h = srcY1 - srcY0;
         if (dstY0 + h > dst.numRows) {
            throw new IllegalArgumentException("dst is too small in rows");
         }

         if (dstX0 + w > dst.numCols) {
            throw new IllegalArgumentException("dst is too small in columns");
         }

         if (src instanceof DenseMatrix64F && dst instanceof DenseMatrix64F) {
            ImplCommonOps_DenseMatrix64F.extract((DenseMatrix64F)src, srcY0, srcX0, (DenseMatrix64F)dst, dstY0, dstX0, h, w);
         } else {
            ImplCommonOps_Matrix64F.extract(src, srcY0, srcX0, dst, dstY0, dstX0, h, w);
         }
      } else {
         throw new IllegalArgumentException("srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.numCols");
      }
   }

   public static DenseMatrix64F extract(DenseMatrix64F src, int srcY0, int srcY1, int srcX0, int srcX1) {
      if (srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows) {
         throw new IllegalArgumentException("srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows");
      } else if (srcX1 > srcX0 && srcX0 >= 0 && srcX1 <= src.numCols) {
         int w = srcX1 - srcX0;
         int h = srcY1 - srcY0;
         DenseMatrix64F dst = new DenseMatrix64F(h, w);
         ImplCommonOps_DenseMatrix64F.extract(src, srcY0, srcX0, dst, 0, 0, h, w);
         return dst;
      } else {
         throw new IllegalArgumentException("srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols");
      }
   }

   public static void extractDiag(DenseMatrix64F src, DenseMatrix64F dst) {
      int N = Math.min(src.numRows, src.numCols);
      if (!MatrixFeatures.isVector(dst)) {
         throw new IllegalArgumentException("Expected a vector for dst.");
      }

      if (dst.getNumElements() != N) {
         throw new IllegalArgumentException("Expected " + N + " elements in dst.");
      }

      for (int i = 0; i < N; i++) {
         dst.set(i, src.unsafe_get(i, i));
      }
   }

   public static void insert(ReshapeMatrix64F src, ReshapeMatrix64F dest, int destY0, int destX0) {
      extract(src, 0, src.numRows, 0, src.numCols, dest, destY0, destX0);
   }

   public static double elementMax(D1Matrix64F a) {
      int size = a.getNumElements();
      double max = a.get(0);

      for (int i = 1; i < size; i++) {
         double val = a.get(i);
         if (val >= max) {
            max = val;
         }
      }

      return max;
   }

   public static double elementMaxAbs(D1Matrix64F a) {
      int size = a.getNumElements();
      double max = 0.0;

      for (int i = 0; i < size; i++) {
         double val = Math.abs(a.get(i));
         if (val > max) {
            max = val;
         }
      }

      return max;
   }

   public static double elementMin(D1Matrix64F a) {
      int size = a.getNumElements();
      double min = a.get(0);

      for (int i = 1; i < size; i++) {
         double val = a.get(i);
         if (val < min) {
            min = val;
         }
      }

      return min;
   }

   public static double elementMinAbs(D1Matrix64F a) {
      int size = a.getNumElements();
      double min = Double.MAX_VALUE;

      for (int i = 0; i < size; i++) {
         double val = Math.abs(a.get(i));
         if (val < min) {
            min = val;
         }
      }

      return min;
   }

   public static void elementMult(D1Matrix64F a, D1Matrix64F b) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            a.times(i, b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void elementMult(D1Matrix64F a, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numRows == c.numRows && a.numCols == c.numCols) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, a.get(i) * b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void elementDiv(D1Matrix64F a, D1Matrix64F b) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            a.div(i, b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void elementDiv(D1Matrix64F a, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numRows == c.numRows && a.numCols == c.numCols) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, a.get(i) / b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static double elementSum(D1Matrix64F mat) {
      double total = 0.0;
      int size = mat.getNumElements();

      for (int i = 0; i < size; i++) {
         total += mat.get(i);
      }

      return total;
   }

   public static double elementSumAbs(D1Matrix64F mat) {
      double total = 0.0;
      int size = mat.getNumElements();

      for (int i = 0; i < size; i++) {
         total += Math.abs(mat.get(i));
      }

      return total;
   }

   public static void elementPower(D1Matrix64F A, D1Matrix64F B, D1Matrix64F C) {
      if (A.numRows == B.numRows && A.numRows == C.numRows && A.numCols == B.numCols && A.numCols == C.numCols) {
         int size = A.getNumElements();

         for (int i = 0; i < size; i++) {
            C.data[i] = Math.pow(A.data[i], B.data[i]);
         }
      } else {
         throw new IllegalArgumentException("All matrices must be the same shape");
      }
   }

   public static void elementPower(double a, D1Matrix64F B, D1Matrix64F C) {
      if (B.numRows == C.numRows && B.numCols == C.numCols) {
         int size = B.getNumElements();

         for (int i = 0; i < size; i++) {
            C.data[i] = Math.pow(a, B.data[i]);
         }
      } else {
         throw new IllegalArgumentException("All matrices must be the same shape");
      }
   }

   public static void elementPower(D1Matrix64F A, double b, D1Matrix64F C) {
      if (A.numRows == C.numRows && A.numCols == C.numCols) {
         int size = A.getNumElements();

         for (int i = 0; i < size; i++) {
            C.data[i] = Math.pow(A.data[i], b);
         }
      } else {
         throw new IllegalArgumentException("All matrices must be the same shape");
      }
   }

   public static void elementLog(D1Matrix64F A, D1Matrix64F C) {
      if (A.numCols == C.numCols && A.numRows == C.numRows) {
         int size = A.getNumElements();

         for (int i = 0; i < size; i++) {
            C.data[i] = Math.log(A.data[i]);
         }
      } else {
         throw new IllegalArgumentException("All matrices must be the same shape");
      }
   }

   public static void elementExp(D1Matrix64F A, D1Matrix64F C) {
      if (A.numCols == C.numCols && A.numRows == C.numRows) {
         int size = A.getNumElements();

         for (int i = 0; i < size; i++) {
            C.data[i] = Math.exp(A.data[i]);
         }
      } else {
         throw new IllegalArgumentException("All matrices must be the same shape");
      }
   }

   public static DenseMatrix64F sumRows(DenseMatrix64F input, DenseMatrix64F output) {
      if (output == null) {
         output = new DenseMatrix64F(input.numRows, 1);
      } else if (output.getNumElements() != input.numRows) {
         throw new IllegalArgumentException("Output does not have enough elements to store the results");
      }

      for (int row = 0; row < input.numRows; row++) {
         double total = 0.0;
         int end = (row + 1) * input.numCols;

         for (int index = row * input.numCols; index < end; index++) {
            total += input.data[index];
         }

         output.set(row, total);
      }

      return output;
   }

   public static DenseMatrix64F sumCols(DenseMatrix64F input, DenseMatrix64F output) {
      if (output == null) {
         output = new DenseMatrix64F(1, input.numCols);
      } else if (output.getNumElements() != input.numCols) {
         throw new IllegalArgumentException("Output does not have enough elements to store the results");
      }

      for (int cols = 0; cols < input.numCols; cols++) {
         double total = 0.0;
         int index = cols;

         for (int end = index + input.numCols * input.numRows; index < end; index += input.numCols) {
            total += input.data[index];
         }

         output.set(cols, total);
      }

      return output;
   }

   public static void addEquals(D1Matrix64F a, D1Matrix64F b) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            a.plus(i, b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void addEquals(D1Matrix64F a, double beta, D1Matrix64F b) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            a.plus(i, beta * b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void add(D1Matrix64F a, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numCols == c.numCols && a.numRows == c.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, a.get(i) + b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The matrices are not all the same dimension.");
      }
   }

   public static void add(D1Matrix64F a, double beta, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numCols == c.numCols && a.numRows == c.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, a.get(i) + beta * b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The matrices are not all the same dimension.");
      }
   }

   public static void add(double alpha, D1Matrix64F a, double beta, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numCols == c.numCols && a.numRows == c.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, alpha * a.get(i) + beta * b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The matrices are not all the same dimension.");
      }
   }

   public static void add(double alpha, D1Matrix64F a, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows && a.numCols == c.numCols && a.numRows == c.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.set(i, alpha * a.get(i) + b.get(i));
         }
      } else {
         throw new IllegalArgumentException("The matrices are not all the same dimension.");
      }
   }

   public static void add(D1Matrix64F a, double val) {
      int length = a.getNumElements();

      for (int i = 0; i < length; i++) {
         a.plus(i, val);
      }
   }

   public static void add(D1Matrix64F a, double val, D1Matrix64F c) {
      if (a.numRows == c.numRows && a.numCols == c.numCols) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.data[i] = a.data[i] + val;
         }
      } else {
         throw new IllegalArgumentException("Dimensions of a and c do not match.");
      }
   }

   public static void subtract(D1Matrix64F a, double val, D1Matrix64F c) {
      if (a.numRows == c.numRows && a.numCols == c.numCols) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.data[i] = a.data[i] - val;
         }
      } else {
         throw new IllegalArgumentException("Dimensions of a and c do not match.");
      }
   }

   public static void subtract(double val, D1Matrix64F a, D1Matrix64F c) {
      if (a.numRows == c.numRows && a.numCols == c.numCols) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.data[i] = val - a.data[i];
         }
      } else {
         throw new IllegalArgumentException("Dimensions of a and c do not match.");
      }
   }

   public static void subtractEquals(D1Matrix64F a, D1Matrix64F b) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            a.data[i] = a.data[i] - b.data[i];
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void subtract(D1Matrix64F a, D1Matrix64F b, D1Matrix64F c) {
      if (a.numCols == b.numCols && a.numRows == b.numRows) {
         int length = a.getNumElements();

         for (int i = 0; i < length; i++) {
            c.data[i] = a.data[i] - b.data[i];
         }
      } else {
         throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
      }
   }

   public static void scale(double alpha, D1Matrix64F a) {
      int size = a.getNumElements();

      for (int i = 0; i < size; i++) {
         a.data[i] = a.data[i] * alpha;
      }
   }

   public static void scale(double alpha, D1Matrix64F a, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();

         for (int i = 0; i < size; i++) {
            b.data[i] = a.data[i] * alpha;
         }
      } else {
         throw new IllegalArgumentException("Matrices must have the same shape");
      }
   }

   public static void divide(double alpha, D1Matrix64F a) {
      int size = a.getNumElements();

      for (int i = 0; i < size; i++) {
         a.data[i] = alpha / a.data[i];
      }
   }

   public static void divide(D1Matrix64F a, double alpha) {
      int size = a.getNumElements();

      for (int i = 0; i < size; i++) {
         a.data[i] = a.data[i] / alpha;
      }
   }

   public static void divide(double alpha, D1Matrix64F a, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();

         for (int i = 0; i < size; i++) {
            b.data[i] = alpha / a.data[i];
         }
      } else {
         throw new IllegalArgumentException("Matrices must have the same shape");
      }
   }

   public static void divide(D1Matrix64F a, double alpha, D1Matrix64F b) {
      if (a.numRows == b.numRows && a.numCols == b.numCols) {
         int size = a.getNumElements();

         for (int i = 0; i < size; i++) {
            b.data[i] = a.data[i] / alpha;
         }
      } else {
         throw new IllegalArgumentException("Matrices must have the same shape");
      }
   }

   public static void changeSign(D1Matrix64F a) {
      int size = a.getNumElements();

      for (int i = 0; i < size; i++) {
         a.data[i] = -a.data[i];
      }
   }

   public static void changeSign(D1Matrix64F input, D1Matrix64F output) {
      if (input.numRows == output.numRows && input.numCols == output.numCols) {
         int size = input.getNumElements();

         for (int i = 0; i < size; i++) {
            output.data[i] = -input.data[i];
         }
      } else {
         throw new IllegalArgumentException("Matrices must have the same shape");
      }
   }

   public static void fill(D1Matrix64F a, double value) {
      Arrays.fill(a.data, 0, a.getNumElements(), value);
   }

   public static DenseMatrix64F rref(DenseMatrix64F A, int numUnknowns, DenseMatrix64F reduced) {
      if (reduced == null) {
         reduced = new DenseMatrix64F(A.numRows, A.numCols);
      } else if (reduced.numCols != A.numCols || reduced.numRows != A.numRows) {
         throw new IllegalArgumentException("'re' must have the same shape as the original input matrix");
      }

      if (numUnknowns <= 0) {
         numUnknowns = Math.min(A.numCols, A.numRows);
      }

      ReducedRowEchelonForm<DenseMatrix64F> alg = new RrefGaussJordanRowPivot();
      alg.setTolerance(elementMaxAbs(A) * UtilEjml.EPS * Math.max(A.numRows, A.numCols));
      reduced.set(A);
      alg.reduce(reduced, numUnknowns);
      return reduced;
   }
}
