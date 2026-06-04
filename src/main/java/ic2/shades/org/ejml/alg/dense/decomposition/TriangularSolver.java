// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition;

public class TriangularSolver
{
    public static void invertLower(final double[] L, final int m) {
        for (int i = 0; i < m; ++i) {
            final double L_ii = L[i * m + i];
            for (int j = 0; j < i; ++j) {
                double val = 0.0;
                for (int k = j; k < i; ++k) {
                    val += L[i * m + k] * L[k * m + j];
                }
                L[i * m + j] = -val / L_ii;
            }
            L[i * m + i] = 1.0 / L_ii;
        }
    }
    
    public static void invertLower(final double[] L, final double[] L_inv, final int m) {
        for (int i = 0; i < m; ++i) {
            final double L_ii = L[i * m + i];
            for (int j = 0; j < i; ++j) {
                double val = 0.0;
                for (int k = j; k < i; ++k) {
                    val -= L[i * m + k] * L_inv[k * m + j];
                }
                L_inv[i * m + j] = val / L_ii;
            }
            L_inv[i * m + i] = 1.0 / L_ii;
        }
    }
    
    public static void solveL(final double[] L, final double[] b, final int n) {
        for (int i = 0; i < n; ++i) {
            double sum = b[i];
            int indexL = i * n;
            for (int k = 0; k < i; ++k) {
                sum -= L[indexL++] * b[k];
            }
            b[i] = sum / L[indexL];
        }
    }
    
    public static void solveL(final double[] L, final double[] b, final int m, final int n) {
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < m; ++i) {
                double sum = b[i * n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= L[i * m + k] * b[k * n + j];
                }
                b[i * n + j] = sum / L[i * m + i];
            }
        }
    }
    
    public static void solveTranL(final double[] L, final double[] b, final int n) {
        for (int i = n - 1; i >= 0; --i) {
            double sum = b[i];
            for (int k = i + 1; k < n; ++k) {
                sum -= L[k * n + i] * b[k];
            }
            b[i] = sum / L[i * n + i];
        }
    }
    
    public static void solveU(final double[] U, final double[] b, final int n) {
        for (int i = n - 1; i >= 0; --i) {
            double sum = b[i];
            int indexU = i * n + i + 1;
            for (int j = i + 1; j < n; ++j) {
                sum -= U[indexU++] * b[j];
            }
            b[i] = sum / U[i * n + i];
        }
    }
    
    public static void solveU(final double[] U, final double[] b, final int sideLength, final int minRow, final int maxRow) {
        for (int i = maxRow - 1; i >= minRow; --i) {
            double sum = b[i];
            int indexU = i * sideLength + i + 1;
            for (int j = i + 1; j < maxRow; ++j) {
                sum -= U[indexU++] * b[j];
            }
            b[i] = sum / U[i * sideLength + i];
        }
    }
    
    public static void solveU(final double[] U, final int startU, final int strideU, final int widthU, final double[] b, final int startB, final int strideB, final int widthB) {
        for (int colB = 0; colB < widthB; ++colB) {
            for (int i = widthU - 1; i >= 0; --i) {
                double sum = b[startB + i * strideB + colB];
                for (int j = i + 1; j < widthU; ++j) {
                    sum -= U[startU + i * strideU + j] * b[startB + j * strideB + colB];
                }
                b[startB + i * strideB + colB] = sum / U[startU + i * strideU + i];
            }
        }
    }
}
