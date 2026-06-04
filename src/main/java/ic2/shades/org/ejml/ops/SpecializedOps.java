// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class SpecializedOps
{
    public static DenseMatrix64F createReflector(final RowD1Matrix64F u) {
        if (!MatrixFeatures.isVector(u)) {
            throw new IllegalArgumentException("u must be a vector");
        }
        final double norm = NormOps.fastNormF(u);
        final double gamma = -2.0 / (norm * norm);
        final DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
        CommonOps.multAddTransB(gamma, u, u, Q);
        return Q;
    }
    
    public static DenseMatrix64F createReflector(final DenseMatrix64F u, final double gamma) {
        if (!MatrixFeatures.isVector(u)) {
            throw new IllegalArgumentException("u must be a vector");
        }
        final DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
        CommonOps.multAddTransB(-gamma, u, u, Q);
        return Q;
    }
    
    public static DenseMatrix64F copyChangeRow(final int[] order, final DenseMatrix64F src, DenseMatrix64F dst) {
        if (dst == null) {
            dst = new DenseMatrix64F(src.numRows, src.numCols);
        }
        else if (src.numRows != dst.numRows || src.numCols != dst.numCols) {
            throw new IllegalArgumentException("src and dst must have the same dimensions.");
        }
        for (int i = 0; i < src.numRows; ++i) {
            final int indexDst = i * src.numCols;
            final int indexSrc = order[i] * src.numCols;
            System.arraycopy(src.data, indexSrc, dst.data, indexDst, src.numCols);
        }
        return dst;
    }
    
    public static DenseMatrix64F copyTriangle(final DenseMatrix64F src, DenseMatrix64F dst, final boolean upper) {
        if (dst == null) {
            dst = new DenseMatrix64F(src.numRows, src.numCols);
        }
        else if (src.numRows != dst.numRows || src.numCols != dst.numCols) {
            throw new IllegalArgumentException("src and dst must have the same dimensions.");
        }
        if (upper) {
            for (int N = Math.min(src.numRows, src.numCols), i = 0; i < N; ++i) {
                final int index = i * src.numCols + i;
                System.arraycopy(src.data, index, dst.data, index, src.numCols - i);
            }
        }
        else {
            for (int j = 0; j < src.numRows; ++j) {
                final int length = Math.min(j + 1, src.numCols);
                final int index = j * src.numCols;
                System.arraycopy(src.data, index, dst.data, index, length);
            }
        }
        return dst;
    }
    
    public static double diffNormF(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }
        final int size = a.getNumElements();
        final DenseMatrix64F diff = new DenseMatrix64F(size, 1);
        for (int i = 0; i < size; ++i) {
            diff.set(i, b.get(i) - a.get(i));
        }
        return NormOps.normF(diff);
    }
    
    public static double diffNormF_fast(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }
        final int size = a.getNumElements();
        double total = 0.0;
        for (int i = 0; i < size; ++i) {
            final double diff = b.get(i) - a.get(i);
            total += diff * diff;
        }
        return Math.sqrt(total);
    }
    
    public static double diffNormP1(final D1Matrix64F a, final D1Matrix64F b) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }
        final int size = a.getNumElements();
        double total = 0.0;
        for (int i = 0; i < size; ++i) {
            total += Math.abs(b.get(i) - a.get(i));
        }
        return total;
    }
    
    public static void addIdentity(final RowD1Matrix64F A, final RowD1Matrix64F B, final double alpha) {
        if (A.numCols != A.numRows) {
            throw new IllegalArgumentException("A must be square");
        }
        if (B.numCols != A.numCols || B.numRows != A.numRows) {
            throw new IllegalArgumentException("B must be the same shape as A");
        }
        final int n = A.numCols;
        int index = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j, ++index) {
                if (i == j) {
                    B.set(index, A.get(index) + alpha);
                }
                else {
                    B.set(index, A.get(index));
                }
            }
        }
    }
    
    public static void subvector(final RowD1Matrix64F A, final int rowA, final int colA, final int length, final boolean row, final int offsetV, final RowD1Matrix64F v) {
        if (row) {
            for (int i = 0; i < length; ++i) {
                v.set(offsetV + i, A.get(rowA, colA + i));
            }
        }
        else {
            for (int i = 0; i < length; ++i) {
                v.set(offsetV + i, A.get(rowA + i, colA));
            }
        }
    }
    
    public static DenseMatrix64F[] splitIntoVectors(final RowD1Matrix64F A, final boolean column) {
        final int w = column ? A.numCols : A.numRows;
        final int M = column ? A.numRows : 1;
        final int N = column ? 1 : A.numCols;
        final int o = Math.max(M, N);
        final DenseMatrix64F[] ret = new DenseMatrix64F[w];
        for (int i = 0; i < w; ++i) {
            final DenseMatrix64F a = new DenseMatrix64F(M, N);
            if (column) {
                subvector(A, 0, i, o, false, 0, a);
            }
            else {
                subvector(A, i, 0, o, true, 0, a);
            }
            ret[i] = a;
        }
        return ret;
    }
    
    public static DenseMatrix64F pivotMatrix(DenseMatrix64F ret, final int[] pivots, final int numPivots, final boolean transposed) {
        if (ret == null) {
            ret = new DenseMatrix64F(numPivots, numPivots);
        }
        else {
            if (ret.numCols != numPivots || ret.numRows != numPivots) {
                throw new IllegalArgumentException("Unexpected matrix dimension");
            }
            CommonOps.fill(ret, 0.0);
        }
        if (transposed) {
            for (int i = 0; i < numPivots; ++i) {
                ret.set(pivots[i], i, 1.0);
            }
        }
        else {
            for (int i = 0; i < numPivots; ++i) {
                ret.set(i, pivots[i], 1.0);
            }
        }
        return ret;
    }
    
    public static double diagProd(final RowD1Matrix64F T) {
        double prod = 1.0;
        for (int N = Math.min(T.numRows, T.numCols), i = 0; i < N; ++i) {
            prod *= T.unsafe_get(i, i);
        }
        return prod;
    }
    
    public static double qualityTriangular(final boolean upper, final D1Matrix64F T) {
        final int N = Math.min(T.numRows, T.numCols);
        final double max = CommonOps.elementMaxAbs(T);
        if (max == 0.0) {
            return 0.0;
        }
        double quality = 1.0;
        for (int i = 0; i < N; ++i) {
            quality *= T.unsafe_get(i, i) / max;
        }
        return Math.abs(quality);
    }
    
    public static double elementSumSq(final D1Matrix64F m) {
        double total = 0.0;
        for (int N = m.getNumElements(), i = 0; i < N; ++i) {
            final double d = m.data[i];
            total += d * d;
        }
        return total;
    }
}
