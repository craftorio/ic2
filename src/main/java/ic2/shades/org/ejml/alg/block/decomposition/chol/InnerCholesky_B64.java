// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.chol;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class InnerCholesky_B64
{
    public static boolean upper(final D1Submatrix64F T) {
        final int n = T.row1 - T.row0;
        final int indexT = T.row0 * T.original.numCols + T.col0 * n;
        return upper(T.original.data, indexT, n);
    }
    
    public static boolean lower(final D1Submatrix64F T) {
        final int n = T.row1 - T.row0;
        final int indexT = T.row0 * T.original.numCols + T.col0 * n;
        return lower(T.original.data, indexT, n);
    }
    
    public static boolean upper(final double[] T, final int indexT, final int n) {
        double div_el_ii = 0.0;
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                double sum = T[indexT + i * n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= T[indexT + k * n + i] * T[indexT + k * n + j];
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        return false;
                    }
                    final double el_ii = Math.sqrt(sum);
                    T[indexT + i * n + i] = el_ii;
                    div_el_ii = 1.0 / el_ii;
                }
                else {
                    T[indexT + i * n + j] = sum * div_el_ii;
                }
            }
        }
        return true;
    }
    
    public static boolean lower(final double[] T, final int indexT, final int n) {
        double div_el_ii = 0.0;
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                double sum = T[indexT + j * n + i];
                for (int k = 0; k < i; ++k) {
                    sum -= T[indexT + i * n + k] * T[indexT + j * n + k];
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        return false;
                    }
                    final double el_ii = Math.sqrt(sum);
                    T[indexT + i * n + i] = el_ii;
                    div_el_ii = 1.0 / el_ii;
                }
                else {
                    T[indexT + j * n + i] = sum * div_el_ii;
                }
            }
        }
        return true;
    }
}
