// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class MatrixMultProduct
{
    public static void outer(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        for (int i = 0; i < a.numRows; ++i) {
            int indexC2;
            for (int indexC1 = indexC2 = i * c.numCols + i, j = i; j < a.numRows; ++j, indexC2 += c.numCols) {
                int indexA = i * a.numCols;
                int indexB = j * a.numCols;
                double sum = 0.0;
                for (int end = indexA + a.numCols; indexA < end; ++indexA, ++indexB) {
                    sum += a.data[indexA] * a.data[indexB];
                }
                c.data[indexC2] = (c.data[indexC1++] = sum);
            }
        }
    }
    
    public static void inner_small(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        for (int i = 0; i < a.numCols; ++i) {
            for (int j = i; j < a.numCols; ++j) {
                final int indexC1 = i * c.numCols + j;
                final int indexC2 = j * c.numCols + i;
                int indexA = i;
                int indexB = j;
                double sum = 0.0;
                for (int end = indexA + a.numRows * a.numCols; indexA < end; indexA += a.numCols, indexB += a.numCols) {
                    sum += a.data[indexA] * a.data[indexB];
                }
                c.data[indexC1] = (c.data[indexC2] = sum);
            }
        }
    }
    
    public static void inner_reorder(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        for (int i = 0; i < a.numCols; ++i) {
            int indexC = i * c.numCols + i;
            double valAi = a.data[i];
            for (int j = i; j < a.numCols; ++j) {
                c.data[indexC++] = valAi * a.data[j];
            }
            for (int k = 1; k < a.numRows; ++k) {
                indexC = i * c.numCols + i;
                int indexB = k * a.numCols + i;
                valAi = a.data[indexB];
                for (int l = i; l < a.numCols; ++l) {
                    final double[] data = c.data;
                    final int n = indexC++;
                    data[n] += valAi * a.data[indexB++];
                }
            }
            int indexC2;
            indexC = (indexC2 = i * c.numCols + i);
            for (int m = i; m < a.numCols; ++m, indexC2 += c.numCols) {
                c.data[indexC2] = c.data[indexC++];
            }
        }
    }
    
    public static void inner_reorder_upper(final RowD1Matrix64F a, final RowD1Matrix64F c) {
        for (int i = 0; i < a.numCols; ++i) {
            int indexC = i * c.numCols + i;
            double valAi = a.data[i];
            for (int j = i; j < a.numCols; ++j) {
                c.data[indexC++] = valAi * a.data[j];
            }
            for (int k = 1; k < a.numRows; ++k) {
                indexC = i * c.numCols + i;
                int indexB = k * a.numCols + i;
                valAi = a.data[indexB];
                for (int l = i; l < a.numCols; ++l) {
                    final double[] data = c.data;
                    final int n = indexC++;
                    data[n] += valAi * a.data[indexB++];
                }
            }
        }
    }
}
