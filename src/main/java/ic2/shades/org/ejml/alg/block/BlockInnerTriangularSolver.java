// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

public class BlockInnerTriangularSolver
{
    public static void invertLower(final double[] L, final double[] L_inv, final int m, final int offsetL, final int offsetL_inv) {
        for (int i = 0; i < m; ++i) {
            final double L_ii = L[offsetL + i * m + i];
            for (int j = 0; j < i; ++j) {
                double val = 0.0;
                for (int k = j; k < i; ++k) {
                    val += L[offsetL + i * m + k] * L_inv[offsetL_inv + k * m + j];
                }
                L_inv[offsetL_inv + i * m + j] = -val / L_ii;
            }
            L_inv[offsetL_inv + i * m + i] = 1.0 / L_ii;
        }
    }
    
    public static void invertLower(final double[] L, final int m, final int offsetL) {
        for (int i = 0; i < m; ++i) {
            final double L_ii = L[offsetL + i * m + i];
            for (int j = 0; j < i; ++j) {
                double val = 0.0;
                for (int k = j; k < i; ++k) {
                    val += L[offsetL + i * m + k] * L[offsetL + k * m + j];
                }
                L[offsetL + i * m + j] = -val / L_ii;
            }
            L[offsetL + i * m + i] = 1.0 / L_ii;
        }
    }
    
    public static void solveL(final double[] L, final double[] b, final int m, final int n, final int strideL, final int offsetL, final int offsetB) {
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < m; ++i) {
                double sum = b[offsetB + i * n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= L[offsetL + i * strideL + k] * b[offsetB + k * n + j];
                }
                b[offsetB + i * n + j] = sum / L[offsetL + i * strideL + i];
            }
        }
    }
    
    public static void solveTransL(final double[] L, final double[] b, final int m, final int n, final int strideL, final int offsetL, final int offsetB) {
        for (int j = 0; j < n; ++j) {
            for (int i = m - 1; i >= 0; --i) {
                double sum = b[offsetB + i * n + j];
                for (int k = i + 1; k < m; ++k) {
                    sum -= L[offsetL + k * strideL + i] * b[offsetB + k * n + j];
                }
                b[offsetB + i * n + j] = sum / L[offsetL + i * strideL + i];
            }
        }
    }
    
    public static void solveLTransB(final double[] L, final double[] b, final int m, final int n, final int strideL, final int offsetL, final int offsetB) {
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < m; ++i) {
                double sum = b[offsetB + j * m + i];
                for (int l = offsetL + i * strideL, bb = offsetB + j * m, endL = l + i; l != endL; sum -= L[l++] * b[bb++]) {}
                b[offsetB + j * m + i] = sum / L[offsetL + i * strideL + i];
            }
        }
    }
    
    public static void solveU(final double[] U, final double[] b, final int m, final int n, final int strideU, final int offsetU, final int offsetB) {
        for (int j = 0; j < n; ++j) {
            for (int i = m - 1; i >= 0; --i) {
                double sum = b[offsetB + i * n + j];
                for (int k = i + 1; k < m; ++k) {
                    sum -= U[offsetU + i * strideU + k] * b[offsetB + k * n + j];
                }
                b[offsetB + i * n + j] = sum / U[offsetU + i * strideU + i];
            }
        }
    }
    
    public static void solveTransU(final double[] U, final double[] b, final int m, final int n, final int strideU, final int offsetU, final int offsetB) {
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < m; ++i) {
                double sum = b[offsetB + i * n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= U[offsetU + k * strideU + i] * b[offsetB + k * n + j];
                }
                b[offsetB + i * n + j] = sum / U[offsetU + i * strideU + i];
            }
        }
    }
}
