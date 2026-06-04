// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class QrHelperFunctions
{
    public static double findMax(final double[] u, final int startU, final int length) {
        double max = -1.0;
        for (int index = startU, stopIndex = startU + length; index < stopIndex; ++index) {
            double val = u[index];
            val = ((val < 0.0) ? (-val) : val);
            if (val > max) {
                max = val;
            }
        }
        return max;
    }
    
    public static void divideElements(final int j, final int numRows, final double[] u, final double u_0) {
        for (int i = j; i < numRows; ++i) {
            final int n = i;
            u[n] /= u_0;
        }
    }
    
    public static void divideElements(final int j, final int numRows, final double[] u, final int startU, final double u_0) {
        for (int i = j; i < numRows; ++i) {
            final int n = i + startU;
            u[n] /= u_0;
        }
    }
    
    public static void divideElements_Brow(final int j, final int numRows, final double[] u, final double[] b, final int startB, final double u_0) {
        for (int i = j; i < numRows; ++i) {
            final int n = i;
            final int n2 = i + startB;
            u[n] = (b[n2] /= u_0);
        }
    }
    
    public static void divideElements_Bcol(final int j, final int numRows, final int numCols, final double[] u, final double[] b, final int startB, final double u_0) {
        for (int indexB = j * numCols + startB, i = j; i < numRows; ++i, indexB += numCols) {
            final int n = indexB;
            final int n2 = i;
            b[n] = (u[n2] /= u_0);
        }
    }
    
    public static double computeTauAndDivide(final int j, final int numRows, final double[] u, final int startU, final double max) {
        double tau = 0.0;
        for (int i = j; i < numRows; ++i) {
            final int n = startU + i;
            final double n2 = u[n] / max;
            u[n] = n2;
            final double d = n2;
            tau += d * d;
        }
        tau = Math.sqrt(tau);
        if (u[startU + j] < 0.0) {
            tau = -tau;
        }
        return tau;
    }
    
    public static double computeTauAndDivide(final int j, final int numRows, final double[] u, final double max) {
        double tau = 0.0;
        for (int i = j; i < numRows; ++i) {
            final int n = i;
            final double n2 = u[n] / max;
            u[n] = n2;
            final double d = n2;
            tau += d * d;
        }
        tau = Math.sqrt(tau);
        if (u[j] < 0.0) {
            tau = -tau;
        }
        return tau;
    }
    
    public static void rank1UpdateMultR(final DenseMatrix64F A, final double[] u, final double gamma, final int colA0, final int w0, final int w1, final double[] _temp) {
        for (int i = colA0; i < A.numCols; ++i) {
            _temp[i] = u[w0] * A.data[w0 * A.numCols + i];
        }
        for (int k = w0 + 1; k < w1; ++k) {
            int indexA = k * A.numCols + colA0;
            final double valU = u[k];
            for (int j = colA0; j < A.numCols; ++j) {
                final int n = j;
                _temp[n] += valU * A.data[indexA++];
            }
        }
        for (int i = colA0; i < A.numCols; ++i) {
            final int n2 = i;
            _temp[n2] *= gamma;
        }
        for (int i = w0; i < w1; ++i) {
            final double valU2 = u[i];
            int indexA2 = i * A.numCols + colA0;
            for (int l = colA0; l < A.numCols; ++l) {
                final double[] data = A.data;
                final int n3 = indexA2++;
                data[n3] -= valU2 * _temp[l];
            }
        }
    }
    
    public static void rank1UpdateMultR(final DenseMatrix64F A, final double[] u, final int offsetU, final double gamma, final int colA0, final int w0, final int w1, final double[] _temp) {
        for (int i = colA0; i < A.numCols; ++i) {
            _temp[i] = u[w0 + offsetU] * A.data[w0 * A.numCols + i];
        }
        for (int k = w0 + 1; k < w1; ++k) {
            int indexA = k * A.numCols + colA0;
            final double valU = u[k + offsetU];
            for (int j = colA0; j < A.numCols; ++j) {
                final int n = j;
                _temp[n] += valU * A.data[indexA++];
            }
        }
        for (int i = colA0; i < A.numCols; ++i) {
            final int n2 = i;
            _temp[n2] *= gamma;
        }
        for (int i = w0; i < w1; ++i) {
            final double valU2 = u[i + offsetU];
            int indexA2 = i * A.numCols + colA0;
            for (int l = colA0; l < A.numCols; ++l) {
                final double[] data = A.data;
                final int n3 = indexA2++;
                data[n3] -= valU2 * _temp[l];
            }
        }
    }
    
    public static void rank1UpdateMultL(final DenseMatrix64F A, final double[] u, final double gamma, final int colA0, final int w0, final int w1) {
        for (int i = colA0; i < A.numRows; ++i) {
            final int startIndex = i * A.numCols + w0;
            double sum = 0.0;
            int rowIndex = startIndex;
            for (int j = w0; j < w1; ++j) {
                sum += A.data[rowIndex++] * u[j];
            }
            sum *= -gamma;
            rowIndex = startIndex;
            for (int j = w0; j < w1; ++j) {
                final double[] data = A.data;
                final int n = rowIndex++;
                data[n] += sum * u[j];
            }
        }
    }
}
