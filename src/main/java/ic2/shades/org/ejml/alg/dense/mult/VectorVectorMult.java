// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class VectorVectorMult
{
    public static void mult(final DenseMatrix64F x, final DenseMatrix64F y, final DenseMatrix64F A) {
    }
    
    public static double innerProd(final D1Matrix64F x, final D1Matrix64F y) {
        final int m = x.getNumElements();
        double total = 0.0;
        for (int i = 0; i < m; ++i) {
            total += x.get(i) * y.get(i);
        }
        return total;
    }
    
    public static double innerProdA(final D1Matrix64F x, final D1Matrix64F A, final D1Matrix64F y) {
        final int n = A.numRows;
        final int m = A.numCols;
        if (x.getNumElements() != n) {
            throw new IllegalArgumentException("Unexpected number of elements in x");
        }
        if (y.getNumElements() != m) {
            throw new IllegalArgumentException("Unexpected number of elements in y");
        }
        double result = 0.0;
        for (int i = 0; i < m; ++i) {
            double total = 0.0;
            for (int j = 0; j < n; ++j) {
                total += x.get(j) * A.unsafe_get(j, i);
            }
            result += total * y.get(i);
        }
        return result;
    }
    
    public static double innerProdTranA(final D1Matrix64F x, final D1Matrix64F A, final D1Matrix64F y) {
        final int n = A.numRows;
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
        for (int i = 0; i < n; ++i) {
            double total = 0.0;
            for (int j = 0; j < n; ++j) {
                total += x.get(j) * A.unsafe_get(i, j);
            }
            result += total * y.get(i);
        }
        return result;
    }
    
    public static void outerProd(final D1Matrix64F x, final D1Matrix64F y, final RowD1Matrix64F A) {
        final int m = A.numRows;
        final int n = A.numCols;
        int index = 0;
        for (int i = 0; i < m; ++i) {
            final double xdat = x.get(i);
            for (int j = 0; j < n; ++j) {
                A.set(index++, xdat * y.get(j));
            }
        }
    }
    
    public static void addOuterProd(final double gamma, final D1Matrix64F x, final D1Matrix64F y, final RowD1Matrix64F A) {
        final int m = A.numRows;
        final int n = A.numCols;
        int index = 0;
        if (gamma == 1.0) {
            for (int i = 0; i < m; ++i) {
                final double xdat = x.get(i);
                for (int j = 0; j < n; ++j) {
                    A.plus(index++, xdat * y.get(j));
                }
            }
        }
        else {
            for (int i = 0; i < m; ++i) {
                final double xdat = x.get(i);
                for (int j = 0; j < n; ++j) {
                    A.plus(index++, gamma * xdat * y.get(j));
                }
            }
        }
    }
    
    public static void householder(final double gamma, final D1Matrix64F u, final D1Matrix64F x, final D1Matrix64F y) {
        final int n = u.getNumElements();
        double sum = 0.0;
        for (int i = 0; i < n; ++i) {
            sum += u.get(i) * x.get(i);
        }
        for (int i = 0; i < n; ++i) {
            y.set(i, x.get(i) + gamma * u.get(i) * sum);
        }
    }
    
    public static void rank1Update(final double gamma, final DenseMatrix64F A, final DenseMatrix64F u, final DenseMatrix64F w, final DenseMatrix64F B) {
        final int n = u.getNumElements();
        int matrixIndex = 0;
        for (int i = 0; i < n; ++i) {
            final double elementU = u.data[i];
            for (int j = 0; j < n; ++j, ++matrixIndex) {
                B.data[matrixIndex] = A.data[matrixIndex] + gamma * elementU * w.data[j];
            }
        }
    }
    
    public static void rank1Update(final double gamma, final DenseMatrix64F A, final DenseMatrix64F u, final DenseMatrix64F w) {
        final int n = u.getNumElements();
        int matrixIndex = 0;
        for (int i = 0; i < n; ++i) {
            final double elementU = u.data[i];
            for (int j = 0; j < n; ++j) {
                final double[] data = A.data;
                final int n2 = matrixIndex++;
                data[n2] += gamma * elementU * w.data[j];
            }
        }
    }
}
