// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.interfaces.linsol.ReducedRowEchelonForm;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.misc.RrefGaussJordanRowPivot;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.misc.ImplCommonOps_Matrix64F;
import ic2.shades.org.ejml.alg.dense.misc.ImplCommonOps_DenseMatrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import java.util.Arrays;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledDeterminantFromMinor;
import ic2.shades.org.ejml.alg.dense.misc.TransposeAlgs;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.mult.MatrixMultProduct;
import ic2.shades.org.ejml.alg.dense.mult.MatrixMatrixMult;
import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.alg.dense.mult.MatrixVectorMult;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class CommonOps
{
    public static void mult(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols == 1) {
            MatrixVectorMult.mult(a, b, c);
        }
        else if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.mult_reorder(a, b, c);
        }
        else {
            MatrixMatrixMult.mult_small(a, b, c);
        }
    }
    
    public static void mult(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.mult_reorder(alpha, a, b, c);
        }
        else {
            MatrixMatrixMult.mult_small(alpha, a, b, c);
        }
    }
    
    public static void multTransA(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols == 1) {
            if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
                MatrixVectorMult.multTransA_reorder(a, b, c);
            }
            else {
                MatrixVectorMult.multTransA_small(a, b, c);
            }
        }
        else if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH || b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multTransA_reorder(a, b, c);
        }
        else {
            MatrixMatrixMult.multTransA_small(a, b, c);
        }
    }
    
    public static void multTransA(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH || b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multTransA_reorder(alpha, a, b, c);
        }
        else {
            MatrixMatrixMult.multTransA_small(alpha, a, b, c);
        }
    }
    
    public static void multTransB(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numRows == 1) {
            MatrixVectorMult.mult(a, b, c);
        }
        else {
            MatrixMatrixMult.multTransB(a, b, c);
        }
    }
    
    public static void multTransB(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        MatrixMatrixMult.multTransB(alpha, a, b, c);
    }
    
    public static void multTransAB(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numRows == 1) {
            if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
                MatrixVectorMult.multTransA_reorder(a, b, c);
            }
            else {
                MatrixVectorMult.multTransA_small(a, b, c);
            }
        }
        else if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
            MatrixMatrixMult.multTransAB_aux(a, b, c, null);
        }
        else {
            MatrixMatrixMult.multTransAB(a, b, c);
        }
    }
    
    public static void multTransAB(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
            MatrixMatrixMult.multTransAB_aux(alpha, a, b, c, null);
        }
        else {
            MatrixMatrixMult.multTransAB(alpha, a, b, c);
        }
    }
    
    public static void multInner(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        if (a.numCols != c.numCols || a.numCols != c.numRows) {
            throw new IllegalArgumentException("Rows and columns of 'c' must be the same as the columns in 'a'");
        }
        if (a.numCols >= EjmlParameters.MULT_INNER_SWITCH) {
            MatrixMultProduct.inner_small(a, c);
        }
        else {
            MatrixMultProduct.inner_reorder(a, c);
        }
    }
    
    public static void multOuter(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        if (a.numRows != c.numCols || a.numRows != c.numRows) {
            throw new IllegalArgumentException("Rows and columns of 'c' must be the same as the rows in 'a'");
        }
        MatrixMultProduct.outer(a, c);
    }
    
    public static void multAdd(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols == 1) {
            MatrixVectorMult.multAdd(a, b, c);
        }
        else if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multAdd_reorder(a, b, c);
        }
        else {
            MatrixMatrixMult.multAdd_small(a, b, c);
        }
    }
    
    public static void multAdd(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multAdd_reorder(alpha, a, b, c);
        }
        else {
            MatrixMatrixMult.multAdd_small(alpha, a, b, c);
        }
    }
    
    public static void multAddTransA(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numCols == 1) {
            if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
                MatrixVectorMult.multAddTransA_reorder(a, b, c);
            }
            else {
                MatrixVectorMult.multAddTransA_small(a, b, c);
            }
        }
        else if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH || b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multAddTransA_reorder(a, b, c);
        }
        else {
            MatrixMatrixMult.multAddTransA_small(a, b, c);
        }
    }
    
    public static void multAddTransA(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH || b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult.multAddTransA_reorder(alpha, a, b, c);
        }
        else {
            MatrixMatrixMult.multAddTransA_small(alpha, a, b, c);
        }
    }
    
    public static void multAddTransB(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        MatrixMatrixMult.multAddTransB(a, b, c);
    }
    
    public static void multAddTransB(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        MatrixMatrixMult.multAddTransB(alpha, a, b, c);
    }
    
    public static void multAddTransAB(final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (b.numRows == 1) {
            if (a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
                MatrixVectorMult.multAddTransA_reorder(a, b, c);
            }
            else {
                MatrixVectorMult.multAddTransA_small(a, b, c);
            }
        }
        else if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
            MatrixMatrixMult.multAddTransAB_aux(a, b, c, null);
        }
        else {
            MatrixMatrixMult.multAddTransAB(a, b, c);
        }
    }
    
    public static void multAddTransAB(final double alpha, final RowD1Matrix64F a, final RowD1Matrix64F b, final RowD1Matrix64F c) {
        if (a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH) {
            MatrixMatrixMult.multAddTransAB_aux(alpha, a, b, c, null);
        }
        else {
            MatrixMatrixMult.multAddTransAB(alpha, a, b, c);
        }
    }
    
    public static boolean solve(final DenseMatrix64F a, final DenseMatrix64F b, final DenseMatrix64F x) {
        LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.general(a.numRows, a.numCols);
        solver = new LinearSolverSafe<DenseMatrix64F>(solver);
        if (!solver.setA(a)) {
            return false;
        }
        solver.solve(b, x);
        return true;
    }
    
    public static void transpose(final DenseMatrix64F mat) {
        if (mat.numCols == mat.numRows) {
            TransposeAlgs.square(mat);
        }
        else {
            final DenseMatrix64F b = new DenseMatrix64F(mat.numCols, mat.numRows);
            transpose(mat, b);
            mat.setReshape(b);
        }
    }
    
    public static DenseMatrix64F transpose(final DenseMatrix64F A, DenseMatrix64F A_tran) {
        if (A_tran == null) {
            A_tran = new DenseMatrix64F(A.numCols, A.numRows);
        }
        else if (A.numRows != A_tran.numCols || A.numCols != A_tran.numRows) {
            throw new IllegalArgumentException("Incompatible matrix dimensions");
        }
        if (A.numRows > EjmlParameters.TRANSPOSE_SWITCH && A.numCols > EjmlParameters.TRANSPOSE_SWITCH) {
            TransposeAlgs.block(A, A_tran, EjmlParameters.BLOCK_WIDTH);
        }
        else {
            TransposeAlgs.standard(A, A_tran);
        }
        return A_tran;
    }
    
    public static double trace(final RowD1Matrix64F a) {
        final int N = Math.min(a.numRows, a.numCols);
        double sum = 0.0;
        int index = 0;
        for (int i = 0; i < N; ++i) {
            sum += a.get(index);
            index += 1 + a.numCols;
        }
        return sum;
    }
    
    public static double det(DenseMatrix64F mat) {
        final int numCol = mat.getNumCols();
        final int numRow = mat.getNumRows();
        if (numCol != numRow) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }
        if (numCol <= 6) {
            if (numCol >= 2) {
                return UnrolledDeterminantFromMinor.det(mat);
            }
            return mat.get(0);
        }
        else {
            final LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            if (alg.inputModified()) {
                mat = mat.copy();
            }
            if (!alg.decompose(mat)) {
                return 0.0;
            }
            return alg.computeDeterminant();
        }
    }
    
    public static boolean invert(final DenseMatrix64F mat) {
        if (mat.numCols <= 5) {
            if (mat.numCols != mat.numRows) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }
            if (mat.numCols >= 2) {
                UnrolledInverseFromMinor.inv(mat, mat);
            }
            else {
                mat.set(0, 1.0 / mat.get(0));
            }
        }
        else {
            final LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            final LinearSolverLu solver = new LinearSolverLu(alg);
            if (!solver.setA(mat)) {
                return false;
            }
            solver.invert(mat);
        }
        return true;
    }
    
    public static boolean invert(DenseMatrix64F mat, final DenseMatrix64F result) {
        if (mat.numCols <= 5) {
            if (mat.numCols != mat.numRows) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }
            if (result.numCols >= 2) {
                UnrolledInverseFromMinor.inv(mat, result);
            }
            else {
                result.set(0, 1.0 / mat.get(0));
            }
        }
        else {
            final LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            final LinearSolverLu solver = new LinearSolverLu(alg);
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
    
    public static void pinv(DenseMatrix64F A, final DenseMatrix64F invA) {
        final LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.pseudoInverse(true);
        if (solver.modifiesA()) {
            A = A.copy();
        }
        if (!solver.setA(A)) {
            throw new IllegalArgumentException("Invert failed, maybe a bug?");
        }
        solver.invert(invA);
    }
    
    public static DenseMatrix64F[] columnsToVector(final DenseMatrix64F A, final DenseMatrix64F[] v) {
        DenseMatrix64F[] ret;
        if (v == null || v.length < A.numCols) {
            ret = new DenseMatrix64F[A.numCols];
        }
        else {
            ret = v;
        }
        for (int i = 0; i < ret.length; ++i) {
            if (ret[i] == null) {
                ret[i] = new DenseMatrix64F(A.numRows, 1);
            }
            else {
                ret[i].reshape(A.numRows, 1, false);
            }
            final DenseMatrix64F u = ret[i];
            for (int j = 0; j < A.numRows; ++j) {
                u.set(j, 0, A.get(j, i));
            }
        }
        return ret;
    }
    
    public static DenseMatrix64F[] rowsToVector(final DenseMatrix64F A, final DenseMatrix64F[] v) {
        DenseMatrix64F[] ret;
        if (v == null || v.length < A.numRows) {
            ret = new DenseMatrix64F[A.numRows];
        }
        else {
            ret = v;
        }
        for (int i = 0; i < ret.length; ++i) {
            if (ret[i] == null) {
                ret[i] = new DenseMatrix64F(A.numCols, 1);
            }
            else {
                ret[i].reshape(A.numCols, 1, false);
            }
            final DenseMatrix64F u = ret[i];
            for (int j = 0; j < A.numCols; ++j) {
                u.set(j, 0, A.get(i, j));
            }
        }
        return ret;
    }
    
    public static void setIdentity(final RowD1Matrix64F mat) {
        final int width = (mat.numRows < mat.numCols) ? mat.numRows : mat.numCols;
        Arrays.fill(mat.data, 0, mat.getNumElements(), 0.0);
        for (int index = 0, i = 0; i < width; ++i, index += mat.numCols + 1) {
            mat.data[index] = 1.0;
        }
    }
    
    public static DenseMatrix64F identity(final int width) {
        final DenseMatrix64F ret = new DenseMatrix64F(width, width);
        for (int i = 0; i < width; ++i) {
            ret.set(i, i, 1.0);
        }
        return ret;
    }
    
    public static DenseMatrix64F identity(final int numRows, final int numCols) {
        final DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
        for (int small = (numRows < numCols) ? numRows : numCols, i = 0; i < small; ++i) {
            ret.set(i, i, 1.0);
        }
        return ret;
    }
    
    public static DenseMatrix64F diag(final double... diagEl) {
        return diag(null, diagEl.length, diagEl);
    }
    
    public static DenseMatrix64F diag(DenseMatrix64F ret, final int width, final double... diagEl) {
        if (ret == null) {
            ret = new DenseMatrix64F(width, width);
        }
        else {
            if (ret.numRows != width || ret.numCols != width) {
                throw new IllegalArgumentException("Unexpected matrix size");
            }
            fill(ret, 0.0);
        }
        for (int i = 0; i < width; ++i) {
            ret.unsafe_set(i, i, diagEl[i]);
        }
        return ret;
    }
    
    public static DenseMatrix64F diagR(final int numRows, final int numCols, final double... diagEl) {
        final DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
        for (int o = Math.min(numRows, numCols), i = 0; i < o; ++i) {
            ret.set(i, i, diagEl[i]);
        }
        return ret;
    }
    
    public static void kron(final DenseMatrix64F A, final DenseMatrix64F B, final DenseMatrix64F C) {
        final int numColsC = A.numCols * B.numCols;
        final int numRowsC = A.numRows * B.numRows;
        if (C.numCols != numColsC || C.numRows != numRowsC) {
            throw new IllegalArgumentException("C does not have the expected dimensions");
        }
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                final double a = A.get(i, j);
                for (int rowB = 0; rowB < B.numRows; ++rowB) {
                    for (int colB = 0; colB < B.numCols; ++colB) {
                        final double val = a * B.get(rowB, colB);
                        C.set(i * B.numRows + rowB, j * B.numCols + colB, val);
                    }
                }
            }
        }
    }
    
    public static void extract(final ReshapeMatrix64F src, final int srcY0, final int srcY1, final int srcX0, final int srcX1, final ReshapeMatrix64F dst, final int dstY0, final int dstX0) {
        if (srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows) {
            throw new IllegalArgumentException("srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows");
        }
        if (srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.numCols) {
            throw new IllegalArgumentException("srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.numCols");
        }
        final int w = srcX1 - srcX0;
        final int h = srcY1 - srcY0;
        if (dstY0 + h > dst.numRows) {
            throw new IllegalArgumentException("dst is too small in rows");
        }
        if (dstX0 + w > dst.numCols) {
            throw new IllegalArgumentException("dst is too small in columns");
        }
        if (src instanceof DenseMatrix64F && dst instanceof DenseMatrix64F) {
            ImplCommonOps_DenseMatrix64F.extract((DenseMatrix64F)src, srcY0, srcX0, (DenseMatrix64F)dst, dstY0, dstX0, h, w);
        }
        else {
            ImplCommonOps_Matrix64F.extract(src, srcY0, srcX0, dst, dstY0, dstX0, h, w);
        }
    }
    
    public static DenseMatrix64F extract(final DenseMatrix64F src, final int srcY0, final int srcY1, final int srcX0, final int srcX1) {
        if (srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows) {
            throw new IllegalArgumentException("srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows");
        }
        if (srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols) {
            throw new IllegalArgumentException("srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols");
        }
        final int w = srcX1 - srcX0;
        final int h = srcY1 - srcY0;
        final DenseMatrix64F dst = new DenseMatrix64F(h, w);
        ImplCommonOps_DenseMatrix64F.extract(src, srcY0, srcX0, dst, 0, 0, h, w);
        return dst;
    }
    
    public static void extractDiag(final DenseMatrix64F src, final DenseMatrix64F dst) {
        final int N = Math.min(src.numRows, src.numCols);
        if (!MatrixFeatures.isVector(dst)) {
            throw new IllegalArgumentException("Expected a vector for dst.");
        }
        if (dst.getNumElements() != N) {
            throw new IllegalArgumentException("Expected " + N + " elements in dst.");
        }
        for (int i = 0; i < N; ++i) {
            dst.set(i, src.unsafe_get(i, i));
        }
    }
    
    public static void insert(final ReshapeMatrix64F src, final ReshapeMatrix64F dest, final int destY0, final int destX0) {
        extract(src, 0, src.numRows, 0, src.numCols, dest, destY0, destX0);
    }
    
    public static double elementMax(final D1Matrix64F a) {
        final int size = a.getNumElements();
        double max = a.get(0);
        for (int i = 1; i < size; ++i) {
            final double val = a.get(i);
            if (val >= max) {
                max = val;
            }
        }
        return max;
    }
    
    public static double elementMaxAbs(final D1Matrix64F a) {
        final int size = a.getNumElements();
        double max = 0.0;
        for (int i = 0; i < size; ++i) {
            final double val = Math.abs(a.get(i));
            if (val > max) {
                max = val;
            }
        }
        return max;
    }
    
    public static double elementMin(final D1Matrix64F a) {
        final int size = a.getNumElements();
        double min = a.get(0);
        for (int i = 1; i < size; ++i) {
            final double val = a.get(i);
            if (val < min) {
                min = val;
            }
        }
        return min;
    }
    
    public static double elementMinAbs(final D1Matrix64F a) {
        final int size = a.getNumElements();
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; ++i) {
            final double val = Math.abs(a.get(i));
            if (val < min) {
                min = val;
            }
        }
        return min;
    }
    
    public static void elementMult(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            a.times(i, b.get(i));
        }
    }
    
    public static void elementMult(final D1Matrix64F a, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numRows != c.numRows || a.numCols != c.numCols) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, a.get(i) * b.get(i));
        }
    }
    
    public static void elementDiv(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            a.div(i, b.get(i));
        }
    }
    
    public static void elementDiv(final D1Matrix64F a, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numRows != c.numRows || a.numCols != c.numCols) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, a.get(i) / b.get(i));
        }
    }
    
    public static double elementSum(final D1Matrix64F mat) {
        double total = 0.0;
        for (int size = mat.getNumElements(), i = 0; i < size; ++i) {
            total += mat.get(i);
        }
        return total;
    }
    
    public static double elementSumAbs(final D1Matrix64F mat) {
        double total = 0.0;
        for (int size = mat.getNumElements(), i = 0; i < size; ++i) {
            total += Math.abs(mat.get(i));
        }
        return total;
    }
    
    public static void elementPower(final D1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (A.numRows != B.numRows || A.numRows != C.numRows || A.numCols != B.numCols || A.numCols != C.numCols) {
            throw new IllegalArgumentException("All matrices must be the same shape");
        }
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            C.data[i] = Math.pow(A.data[i], B.data[i]);
        }
    }
    
    public static void elementPower(final double a, final D1Matrix64F B, final D1Matrix64F C) {
        if (B.numRows != C.numRows || B.numCols != C.numCols) {
            throw new IllegalArgumentException("All matrices must be the same shape");
        }
        for (int size = B.getNumElements(), i = 0; i < size; ++i) {
            C.data[i] = Math.pow(a, B.data[i]);
        }
    }
    
    public static void elementPower(final D1Matrix64F A, final double b, final D1Matrix64F C) {
        if (A.numRows != C.numRows || A.numCols != C.numCols) {
            throw new IllegalArgumentException("All matrices must be the same shape");
        }
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            C.data[i] = Math.pow(A.data[i], b);
        }
    }
    
    public static void elementLog(final D1Matrix64F A, final D1Matrix64F C) {
        if (A.numCols != C.numCols || A.numRows != C.numRows) {
            throw new IllegalArgumentException("All matrices must be the same shape");
        }
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            C.data[i] = Math.log(A.data[i]);
        }
    }
    
    public static void elementExp(final D1Matrix64F A, final D1Matrix64F C) {
        if (A.numCols != C.numCols || A.numRows != C.numRows) {
            throw new IllegalArgumentException("All matrices must be the same shape");
        }
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            C.data[i] = Math.exp(A.data[i]);
        }
    }
    
    public static DenseMatrix64F sumRows(final DenseMatrix64F input, DenseMatrix64F output) {
        if (output == null) {
            output = new DenseMatrix64F(input.numRows, 1);
        }
        else if (output.getNumElements() != input.numRows) {
            throw new IllegalArgumentException("Output does not have enough elements to store the results");
        }
        for (int row = 0; row < input.numRows; ++row) {
            double total = 0.0;
            for (int end = (row + 1) * input.numCols, index = row * input.numCols; index < end; ++index) {
                total += input.data[index];
            }
            output.set(row, total);
        }
        return output;
    }
    
    public static DenseMatrix64F sumCols(final DenseMatrix64F input, DenseMatrix64F output) {
        if (output == null) {
            output = new DenseMatrix64F(1, input.numCols);
        }
        else if (output.getNumElements() != input.numCols) {
            throw new IllegalArgumentException("Output does not have enough elements to store the results");
        }
        for (int cols = 0; cols < input.numCols; ++cols) {
            double total = 0.0;
            for (int index = cols, end = index + input.numCols * input.numRows; index < end; index += input.numCols) {
                total += input.data[index];
            }
            output.set(cols, total);
        }
        return output;
    }
    
    public static void addEquals(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            a.plus(i, b.get(i));
        }
    }
    
    public static void addEquals(final D1Matrix64F a, final double beta, final D1Matrix64F b) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            a.plus(i, beta * b.get(i));
        }
    }
    
    public static void add(final D1Matrix64F a, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numCols != c.numCols || a.numRows != c.numRows) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, a.get(i) + b.get(i));
        }
    }
    
    public static void add(final D1Matrix64F a, final double beta, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numCols != c.numCols || a.numRows != c.numRows) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, a.get(i) + beta * b.get(i));
        }
    }
    
    public static void add(final double alpha, final D1Matrix64F a, final double beta, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numCols != c.numCols || a.numRows != c.numRows) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, alpha * a.get(i) + beta * b.get(i));
        }
    }
    
    public static void add(final double alpha, final D1Matrix64F a, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows || a.numCols != c.numCols || a.numRows != c.numRows) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.set(i, alpha * a.get(i) + b.get(i));
        }
    }
    
    public static void add(final D1Matrix64F a, final double val) {
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            a.plus(i, val);
        }
    }
    
    public static void add(final D1Matrix64F a, final double val, final D1Matrix64F c) {
        if (a.numRows != c.numRows || a.numCols != c.numCols) {
            throw new IllegalArgumentException("Dimensions of a and c do not match.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.data[i] = a.data[i] + val;
        }
    }
    
    public static void subtract(final D1Matrix64F a, final double val, final D1Matrix64F c) {
        if (a.numRows != c.numRows || a.numCols != c.numCols) {
            throw new IllegalArgumentException("Dimensions of a and c do not match.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.data[i] = a.data[i] - val;
        }
    }
    
    public static void subtract(final double val, final D1Matrix64F a, final D1Matrix64F c) {
        if (a.numRows != c.numRows || a.numCols != c.numCols) {
            throw new IllegalArgumentException("Dimensions of a and c do not match.");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.data[i] = val - a.data[i];
        }
    }
    
    public static void subtractEquals(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            final double[] data = a.data;
            final int n = i;
            data[n] -= b.data[i];
        }
    }
    
    public static void subtract(final D1Matrix64F a, final D1Matrix64F b, final D1Matrix64F c) {
        if (a.numCols != b.numCols || a.numRows != b.numRows) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            c.data[i] = a.data[i] - b.data[i];
        }
    }
    
    public static void scale(final double alpha, final D1Matrix64F a) {
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            final double[] data = a.data;
            final int n = i;
            data[n] *= alpha;
        }
    }
    
    public static void scale(final double alpha, final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Matrices must have the same shape");
        }
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            b.data[i] = a.data[i] * alpha;
        }
    }
    
    public static void divide(final double alpha, final D1Matrix64F a) {
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            a.data[i] = alpha / a.data[i];
        }
    }
    
    public static void divide(final D1Matrix64F a, final double alpha) {
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            final double[] data = a.data;
            final int n = i;
            data[n] /= alpha;
        }
    }
    
    public static void divide(final double alpha, final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Matrices must have the same shape");
        }
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            b.data[i] = alpha / a.data[i];
        }
    }
    
    public static void divide(final D1Matrix64F a, final double alpha, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Matrices must have the same shape");
        }
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            b.data[i] = a.data[i] / alpha;
        }
    }
    
    public static void changeSign(final D1Matrix64F a) {
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            a.data[i] = -a.data[i];
        }
    }
    
    public static void changeSign(final D1Matrix64F input, final D1Matrix64F output) {
        if (input.numRows != output.numRows || input.numCols != output.numCols) {
            throw new IllegalArgumentException("Matrices must have the same shape");
        }
        for (int size = input.getNumElements(), i = 0; i < size; ++i) {
            output.data[i] = -input.data[i];
        }
    }
    
    public static void fill(final D1Matrix64F a, final double value) {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }
    
    public static DenseMatrix64F rref(final DenseMatrix64F A, int numUnknowns, DenseMatrix64F reduced) {
        if (reduced == null) {
            reduced = new DenseMatrix64F(A.numRows, A.numCols);
        }
        else if (reduced.numCols != A.numCols || reduced.numRows != A.numRows) {
            throw new IllegalArgumentException("'re' must have the same shape as the original input matrix");
        }
        if (numUnknowns <= 0) {
            numUnknowns = Math.min(A.numCols, A.numRows);
        }
        final ReducedRowEchelonForm<DenseMatrix64F> alg = new RrefGaussJordanRowPivot();
        alg.setTolerance(elementMaxAbs(A) * UtilEjml.EPS * Math.max(A.numRows, A.numCols));
        reduced.set(A);
        alg.reduce(reduced, numUnknowns);
        return reduced;
    }
}
