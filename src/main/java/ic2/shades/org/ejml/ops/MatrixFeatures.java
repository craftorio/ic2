// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.interfaces.decomposition.LUDecomposition;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;

public class MatrixFeatures
{
    public static boolean hasNaN(final D1Matrix64F m) {
        for (int length = m.getNumElements(), i = 0; i < length; ++i) {
            if (Double.isNaN(m.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasUncountable(final D1Matrix64F m) {
        for (int length = m.getNumElements(), i = 0; i < length; ++i) {
            final double a = m.get(i);
            if (Double.isNaN(a) || Double.isInfinite(a)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isZeros(final D1Matrix64F m, final double tol) {
        for (int length = m.getNumElements(), i = 0; i < length; ++i) {
            if (Math.abs(m.get(i)) > tol) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isVector(final D1Matrix64F mat) {
        return mat.numCols == 1 || mat.numRows == 1;
    }
    
    public static boolean isPositiveDefinite(DenseMatrix64F A) {
        if (!isSquare(A)) {
            return false;
        }
        final CholeskyDecompositionInner_D64 chol = new CholeskyDecompositionInner_D64(true);
        if (chol.inputModified()) {
            A = A.copy();
        }
        return chol.decompose(A);
    }
    
    public static boolean isPositiveSemidefinite(DenseMatrix64F A) {
        if (!isSquare(A)) {
            return false;
        }
        final EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(A.numCols, false);
        if (eig.inputModified()) {
            A = A.copy();
        }
        eig.decompose(A);
        for (int i = 0; i < A.numRows; ++i) {
            final Complex64F v = eig.getEigenvalue(i);
            if (v.getReal() < 0.0) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isSquare(final D1Matrix64F mat) {
        return mat.numCols == mat.numRows;
    }
    
    public static boolean isSymmetric(final DenseMatrix64F m, final double tol) {
        if (m.numCols != m.numRows) {
            return false;
        }
        final double max = CommonOps.elementMaxAbs(m);
        for (int i = 0; i < m.numRows; ++i) {
            for (int j = 0; j < i; ++j) {
                final double a = m.get(i, j) / max;
                final double b = m.get(j, i) / max;
                final double diff = Math.abs(a - b);
                if (diff > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isSymmetric(final DenseMatrix64F m) {
        return isSymmetric(m, 0.0);
    }
    
    public static boolean isSkewSymmetric(final DenseMatrix64F A, final double tol) {
        if (A.numCols != A.numRows) {
            return false;
        }
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < i; ++j) {
                final double a = A.get(i, j);
                final double b = A.get(j, i);
                final double diff = Math.abs(a + b);
                if (diff > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isInverse(final DenseMatrix64F a, final DenseMatrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        final int numRows = a.numRows;
        final int numCols = a.numCols;
        for (int i = 0; i < numRows; ++i) {
            for (int j = 0; j < numCols; ++j) {
                double total = 0.0;
                for (int k = 0; k < numCols; ++k) {
                    total += a.get(i, k) * b.get(k, j);
                }
                if (i == j) {
                    if (Math.abs(total - 1.0) > tol) {
                        return false;
                    }
                }
                else if (Math.abs(total) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isEquals(final D1Matrix64F a, final D1Matrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        if (tol == 0.0) {
            return isEquals(a, b);
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            if (tol < Math.abs(a.get(i) - b.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isEqualsTriangle(final ReshapeMatrix64F a, final ReshapeMatrix64F b, final boolean upper, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        if (upper) {
            for (int i = 0; i < a.numRows; ++i) {
                for (int j = i; j < a.numCols; ++j) {
                    if (Math.abs(a.get(i, j) - b.get(i, j)) > tol) {
                        return false;
                    }
                }
            }
        }
        else {
            for (int i = 0; i < a.numRows; ++i) {
                for (int end = Math.min(i, a.numCols - 1), k = 0; k <= end; ++k) {
                    if (Math.abs(a.get(i, k) - b.get(i, k)) > tol) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static boolean isEquals(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isIdentical(final D1Matrix64F a, final D1Matrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        if (tol < 0.0) {
            throw new IllegalArgumentException("Tolerance must be greater than or equal to zero.");
        }
        final int length = a.getNumElements();
        int i = 0;
        while (i < length) {
            final double valA = a.get(i);
            final double valB = b.get(i);
            final double diff = Math.abs(valA - valB);
            if (tol >= diff) {
                ++i;
            }
            else {
                if (Double.isNaN(valA)) {
                    return Double.isNaN(valB);
                }
                return Double.isInfinite(valA) && valA == valB;
            }
        }
        return true;
    }
    
    public static boolean isOrthogonal(final DenseMatrix64F Q, final double tol) {
        if (Q.numRows < Q.numCols) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }
        final DenseMatrix64F[] u = CommonOps.columnsToVector(Q, null);
        for (int i = 0; i < u.length; ++i) {
            final DenseMatrix64F a = u[i];
            for (int j = i + 1; j < u.length; ++j) {
                final double val = VectorVectorMult.innerProd(a, u[j]);
                if (Math.abs(val) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isRowsLinearIndependent(DenseMatrix64F A) {
        final LUDecomposition<DenseMatrix64F> lu = DecompositionFactory.lu(A.numRows, A.numCols);
        if (lu.inputModified()) {
            A = A.copy();
        }
        if (!lu.decompose(A)) {
            throw new RuntimeException("Decompositon failed?");
        }
        return !lu.isSingular();
    }
    
    public static boolean isIdentity(final DenseMatrix64F mat, final double tol) {
        int index = 0;
        for (int i = 0; i < mat.numRows; ++i) {
            for (int j = 0; j < mat.numCols; ++j) {
                if (i == j) {
                    if (Math.abs(mat.get(index++) - 1.0) > tol) {
                        return false;
                    }
                }
                else if (Math.abs(mat.get(index++)) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isConstantVal(final DenseMatrix64F mat, final double val, final double tol) {
        int index = 0;
        for (int i = 0; i < mat.numRows; ++i) {
            for (int j = 0; j < mat.numCols; ++j) {
                if (Math.abs(mat.get(index++) - val) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isDiagonalPositive(final DenseMatrix64F a) {
        for (int i = 0; i < a.numRows; ++i) {
            if (a.get(i, i) < 0.0) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isFullRank(final DenseMatrix64F a) {
        throw new RuntimeException("Implement");
    }
    
    public static boolean isNegative(final D1Matrix64F a, final D1Matrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Matrix dimensions must match");
        }
        for (int length = a.getNumElements(), i = 0; i < length; ++i) {
            if (Math.abs(a.get(i) + b.get(i)) > tol) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isUpperTriangle(final DenseMatrix64F A, final int hessenberg, final double tol) {
        if (A.numRows != A.numCols) {
            return false;
        }
        for (int i = hessenberg + 1; i < A.numRows; ++i) {
            for (int j = 0; j < i - hessenberg; ++j) {
                if (Math.abs(A.get(i, j)) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static int rank(final DenseMatrix64F A) {
        return rank(A, UtilEjml.EPS * 100.0);
    }
    
    public static int rank(DenseMatrix64F A, final double threshold) {
        final SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
        if (svd.inputModified()) {
            A = A.copy();
        }
        if (!svd.decompose(A)) {
            throw new RuntimeException("Decomposition failed");
        }
        return SingularOps.rank(svd, threshold);
    }
    
    public static int nullity(final DenseMatrix64F A) {
        return nullity(A, UtilEjml.EPS * 100.0);
    }
    
    public static int nullity(DenseMatrix64F A, final double threshold) {
        final SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
        if (svd.inputModified()) {
            A = A.copy();
        }
        if (!svd.decompose(A)) {
            throw new RuntimeException("Decomposition failed");
        }
        return SingularOps.nullity(svd, threshold);
    }
}
