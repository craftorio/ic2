// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.alg.dense.mult.SubmatrixOps;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.Random;

public class RandomMatrices
{
    public static DenseMatrix64F[] createSpan(final int dimen, final int numVectors, final Random rand) {
        if (dimen < numVectors) {
            throw new IllegalArgumentException("The number of vectors must be less than or equal to the dimension");
        }
        final DenseMatrix64F[] u = new DenseMatrix64F[numVectors];
        NormOps.normalizeF(u[0] = createRandom(dimen, 1, -1.0, 1.0, rand));
        for (int i = 1; i < numVectors; ++i) {
            DenseMatrix64F a = new DenseMatrix64F(dimen, 1);
            DenseMatrix64F r = null;
            for (int j = 0; j < i; ++j) {
                if (j == 0) {
                    r = createRandom(dimen, 1, -1.0, 1.0, rand);
                }
                a.set(r);
                VectorVectorMult.householder(-2.0, u[j], r, a);
                CommonOps.add(r, a, a);
                CommonOps.scale(0.5, a);
                final DenseMatrix64F t = a;
                a = r;
                r = t;
                final double val = NormOps.normF(r);
                if (val == 0.0 || Double.isNaN(val) || Double.isInfinite(val)) {
                    throw new RuntimeException("Failed sanity check");
                }
                CommonOps.divide(r, val);
            }
            u[i] = r;
        }
        return u;
    }
    
    public static DenseMatrix64F createInSpan(final DenseMatrix64F[] span, final double min, final double max, final Random rand) {
        final DenseMatrix64F A = new DenseMatrix64F(span.length, 1);
        final DenseMatrix64F B = new DenseMatrix64F(span[0].getNumElements(), 1);
        for (int i = 0; i < span.length; ++i) {
            B.set(span[i]);
            final double val = rand.nextDouble() * (max - min) + min;
            CommonOps.scale(val, B);
            CommonOps.add(A, B, A);
        }
        return A;
    }
    
    public static DenseMatrix64F createOrthogonal(final int numRows, final int numCols, final Random rand) {
        if (numRows < numCols) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }
        final DenseMatrix64F[] u = createSpan(numRows, numCols, rand);
        final DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
        for (int i = 0; i < numCols; ++i) {
            SubmatrixOps.setSubMatrix(u[i], ret, 0, 0, 0, i, numRows, 1);
        }
        return ret;
    }
    
    public static DenseMatrix64F createDiagonal(final int N, final double min, final double max, final Random rand) {
        return createDiagonal(N, N, min, max, rand);
    }
    
    public static DenseMatrix64F createDiagonal(final int numRows, final int numCols, final double min, final double max, final Random rand) {
        if (max < min) {
            throw new IllegalArgumentException("The max must be >= the min");
        }
        final DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
        final int N = Math.min(numRows, numCols);
        final double r = max - min;
        for (int i = 0; i < N; ++i) {
            ret.set(i, i, rand.nextDouble() * r + min);
        }
        return ret;
    }
    
    public static DenseMatrix64F createSingularValues(final int numRows, final int numCols, final Random rand, final double... sv) {
        final DenseMatrix64F U = createOrthogonal(numRows, numRows, rand);
        final DenseMatrix64F V = createOrthogonal(numCols, numCols, rand);
        final DenseMatrix64F S = new DenseMatrix64F(numRows, numCols);
        int min = Math.min(numRows, numCols);
        min = Math.min(min, sv.length);
        for (int i = 0; i < min; ++i) {
            S.set(i, i, sv[i]);
        }
        final DenseMatrix64F tmp = new DenseMatrix64F(numRows, numCols);
        CommonOps.mult(U, S, tmp);
        CommonOps.multTransB(tmp, V, S);
        return S;
    }
    
    public static DenseMatrix64F createEigenvaluesSymm(final int num, final Random rand, final double... eigenvalues) {
        final DenseMatrix64F V = createOrthogonal(num, num, rand);
        final DenseMatrix64F D = CommonOps.diag(eigenvalues);
        final DenseMatrix64F temp = new DenseMatrix64F(num, num);
        CommonOps.mult(V, D, temp);
        CommonOps.multTransB(temp, V, D);
        return D;
    }
    
    public static DenseMatrix64F createRandom(final int numRow, final int numCol, final Random rand) {
        final DenseMatrix64F mat = new DenseMatrix64F(numRow, numCol);
        setRandom(mat, 0.0, 1.0, rand);
        return mat;
    }
    
    public static void addRandom(final DenseMatrix64F A, final double min, final double max, final Random rand) {
        final double[] d = A.getData();
        final int size = A.getNumElements();
        final double r = max - min;
        for (int i = 0; i < size; ++i) {
            final double[] array = d;
            final int n = i;
            array[n] += r * rand.nextDouble() + min;
        }
    }
    
    public static DenseMatrix64F createRandom(final int numRow, final int numCol, final double min, final double max, final Random rand) {
        final DenseMatrix64F mat = new DenseMatrix64F(numRow, numCol);
        setRandom(mat, min, max, rand);
        return mat;
    }
    
    public static void setRandom(final DenseMatrix64F mat, final Random rand) {
        setRandom(mat, 0.0, 1.0, rand);
    }
    
    public static void setRandom(final D1Matrix64F mat, final double min, final double max, final Random rand) {
        final double[] d = mat.getData();
        final int size = mat.getNumElements();
        final double r = max - min;
        for (int i = 0; i < size; ++i) {
            d[i] = r * rand.nextDouble() + min;
        }
    }
    
    public static DenseMatrix64F createSymmPosDef(final int width, final Random rand) {
        final DenseMatrix64F a = new DenseMatrix64F(width, 1);
        final DenseMatrix64F b = new DenseMatrix64F(width, width);
        for (int i = 0; i < width; ++i) {
            a.set(i, 0, rand.nextDouble());
        }
        CommonOps.multTransB(a, a, b);
        for (int i = 0; i < width; ++i) {
            b.add(i, i, 1.0);
        }
        return b;
    }
    
    public static DenseMatrix64F createSymmetric(final int length, final double min, final double max, final Random rand) {
        final DenseMatrix64F A = new DenseMatrix64F(length, length);
        createSymmetric(A, min, max, rand);
        return A;
    }
    
    public static void createSymmetric(final DenseMatrix64F A, final double min, final double max, final Random rand) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("A must be a square matrix");
        }
        final double range = max - min;
        for (int length = A.numRows, i = 0; i < length; ++i) {
            for (int j = i; j < length; ++j) {
                final double val = rand.nextDouble() * range + min;
                A.set(i, j, val);
                A.set(j, i, val);
            }
        }
    }
    
    public static DenseMatrix64F createUpperTriangle(final int dimen, final int hessenberg, final double min, final double max, final Random rand) {
        if (hessenberg < 0) {
            throw new RuntimeException("hessenberg must be more than or equal to 0");
        }
        final double range = max - min;
        final DenseMatrix64F A = new DenseMatrix64F(dimen, dimen);
        for (int i = 0; i < dimen; ++i) {
            int j;
            for (int start = j = ((i <= hessenberg) ? 0 : (i - hessenberg)); j < dimen; ++j) {
                A.set(i, j, rand.nextDouble() * range + min);
            }
        }
        return A;
    }
}
