// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockTriangularSolver
{
    public static void invert(final int blockLength, final boolean upper, final D1Submatrix64F T, final D1Submatrix64F T_inv, final double[] temp) {
        if (upper) {
            throw new IllegalArgumentException("Upper triangular matrices not supported yet");
        }
        if (temp.length < blockLength * blockLength) {
            throw new IllegalArgumentException("Temp must be at least blockLength*blockLength long.");
        }
        if (T.row0 != T_inv.row0 || T.row1 != T_inv.row1 || T.col0 != T_inv.col0 || T.col1 != T_inv.col1) {
            throw new IllegalArgumentException("T and T_inv must be at the same elements in the matrix");
        }
        final int M = T.row1 - T.row0;
        final double[] dataT = T.original.data;
        final double[] dataX = T_inv.original.data;
        final int offsetT = T.row0 * T.original.numCols + M * T.col0;
        for (int i = 0; i < M; i += blockLength) {
            final int heightT = Math.min(T.row1 - (i + T.row0), blockLength);
            final int indexII = offsetT + T.original.numCols * (i + T.row0) + heightT * (i + T.col0);
            for (int j = 0; j < i; j += blockLength) {
                final int widthX = Math.min(T.col1 - (j + T.col0), blockLength);
                for (int w = 0; w < temp.length; ++w) {
                    temp[w] = 0.0;
                }
                for (int k = j; k < i; k += blockLength) {
                    final int widthT = Math.min(T.col1 - (k + T.col0), blockLength);
                    final int indexL = offsetT + T.original.numCols * (i + T.row0) + heightT * (k + T.col0);
                    final int indexX = offsetT + T.original.numCols * (k + T.row0) + widthT * (j + T.col0);
                    BlockInnerMultiplication.blockMultMinus(dataT, dataX, temp, indexL, indexX, 0, heightT, widthT, widthX);
                }
                final int indexX2 = offsetT + T.original.numCols * (i + T.row0) + heightT * (j + T.col0);
                BlockInnerTriangularSolver.solveL(dataT, temp, heightT, widthX, heightT, indexII, 0);
                System.arraycopy(temp, 0, dataX, indexX2, widthX * heightT);
            }
            BlockInnerTriangularSolver.invertLower(dataT, dataX, heightT, indexII, indexII);
        }
    }
    
    public static void invert(final int blockLength, final boolean upper, final D1Submatrix64F T, final double[] temp) {
        if (upper) {
            throw new IllegalArgumentException("Upper triangular matrices not supported yet");
        }
        if (temp.length < blockLength * blockLength) {
            throw new IllegalArgumentException("Temp must be at least blockLength*blockLength long.");
        }
        final int M = T.row1 - T.row0;
        final double[] dataT = T.original.data;
        final int offsetT = T.row0 * T.original.numCols + M * T.col0;
        for (int i = 0; i < M; i += blockLength) {
            final int heightT = Math.min(T.row1 - (i + T.row0), blockLength);
            final int indexII = offsetT + T.original.numCols * (i + T.row0) + heightT * (i + T.col0);
            for (int j = 0; j < i; j += blockLength) {
                final int widthX = Math.min(T.col1 - (j + T.col0), blockLength);
                for (int w = 0; w < temp.length; ++w) {
                    temp[w] = 0.0;
                }
                for (int k = j; k < i; k += blockLength) {
                    final int widthT = Math.min(T.col1 - (k + T.col0), blockLength);
                    final int indexL = offsetT + T.original.numCols * (i + T.row0) + heightT * (k + T.col0);
                    final int indexX = offsetT + T.original.numCols * (k + T.row0) + widthT * (j + T.col0);
                    BlockInnerMultiplication.blockMultMinus(dataT, dataT, temp, indexL, indexX, 0, heightT, widthT, widthX);
                }
                final int indexX2 = offsetT + T.original.numCols * (i + T.row0) + heightT * (j + T.col0);
                BlockInnerTriangularSolver.solveL(dataT, temp, heightT, widthX, heightT, indexII, 0);
                System.arraycopy(temp, 0, dataT, indexX2, widthX * heightT);
            }
            BlockInnerTriangularSolver.invertLower(dataT, heightT, indexII);
        }
    }
    
    public static void solve(final int blockLength, final boolean upper, final D1Submatrix64F T, final D1Submatrix64F B, final boolean transT) {
        if (upper) {
            solveR(blockLength, T, B, transT);
        }
        else {
            solveL(blockLength, T, B, transT);
        }
    }
    
    public static void solveBlock(final int blockLength, final boolean upper, final D1Submatrix64F T, final D1Submatrix64F B, final boolean transT, final boolean transB) {
        final int Trows = T.row1 - T.row0;
        if (Trows > blockLength) {
            throw new IllegalArgumentException("T can be at most the size of a block");
        }
        final int blockT_rows = Math.min(blockLength, T.original.numRows - T.row0);
        final int blockT_cols = Math.min(blockLength, T.original.numCols - T.col0);
        final int offsetT = T.row0 * T.original.numCols + blockT_rows * T.col0;
        final double[] dataT = T.original.data;
        final double[] dataB = B.original.data;
        if (transB) {
            if (upper) {
                if (transT) {
                    throw new IllegalArgumentException("Operation not yet supported");
                }
                throw new IllegalArgumentException("Operation not yet supported");
            }
            else {
                if (transT) {
                    throw new IllegalArgumentException("Operation not yet supported");
                }
                for (int i = B.row0; i < B.row1; i += blockLength) {
                    final int N = Math.min(B.row1, i + blockLength) - i;
                    final int offsetB = i * B.original.numCols + N * B.col0;
                    BlockInnerTriangularSolver.solveLTransB(dataT, dataB, blockT_rows, N, blockT_rows, offsetT, offsetB);
                }
            }
        }
        else {
            if (Trows != B.row1 - B.row0) {
                throw new IllegalArgumentException("T and B must have the same number of rows.");
            }
            if (upper) {
                if (transT) {
                    for (int i = B.col0; i < B.col1; i += blockLength) {
                        final int offsetB2 = B.row0 * B.original.numCols + Trows * i;
                        final int N2 = Math.min(B.col1, i + blockLength) - i;
                        BlockInnerTriangularSolver.solveTransU(dataT, dataB, Trows, N2, Trows, offsetT, offsetB2);
                    }
                }
                else {
                    for (int i = B.col0; i < B.col1; i += blockLength) {
                        final int offsetB2 = B.row0 * B.original.numCols + Trows * i;
                        final int N2 = Math.min(B.col1, i + blockLength) - i;
                        BlockInnerTriangularSolver.solveU(dataT, dataB, Trows, N2, Trows, offsetT, offsetB2);
                    }
                }
            }
            else if (transT) {
                for (int i = B.col0; i < B.col1; i += blockLength) {
                    final int offsetB2 = B.row0 * B.original.numCols + Trows * i;
                    final int N2 = Math.min(B.col1, i + blockLength) - i;
                    BlockInnerTriangularSolver.solveTransL(dataT, dataB, Trows, N2, blockT_cols, offsetT, offsetB2);
                }
            }
            else {
                for (int i = B.col0; i < B.col1; i += blockLength) {
                    final int offsetB2 = B.row0 * B.original.numCols + Trows * i;
                    final int N2 = Math.min(B.col1, i + blockLength) - i;
                    BlockInnerTriangularSolver.solveL(dataT, dataB, Trows, N2, blockT_cols, offsetT, offsetB2);
                }
            }
        }
    }
    
    public static void solveL(final int blockLength, final D1Submatrix64F L, final D1Submatrix64F B, final boolean transL) {
        final D1Submatrix64F Y = new D1Submatrix64F(B.original);
        final D1Submatrix64F Linner = new D1Submatrix64F(L.original);
        final D1Submatrix64F Binner = new D1Submatrix64F(B.original);
        final int lengthL = B.row1 - B.row0;
        int startI;
        int stepI;
        if (transL) {
            startI = lengthL - lengthL % blockLength;
            if (startI == lengthL && lengthL >= blockLength) {
                startI -= blockLength;
            }
            stepI = -blockLength;
        }
        else {
            startI = 0;
            stepI = blockLength;
        }
        int i = startI;
        while (true) {
            if (transL) {
                if (i < 0) {
                    break;
                }
            }
            else if (i >= lengthL) {
                break;
            }
            final int widthT = Math.min(blockLength, lengthL - i);
            Linner.col0 = L.col0 + i;
            Linner.col1 = Linner.col0 + widthT;
            Linner.row0 = L.row0 + i;
            Linner.row1 = Linner.row0 + widthT;
            Binner.col0 = B.col0;
            Binner.col1 = B.col1;
            Binner.row0 = B.row0 + i;
            Binner.row1 = Binner.row0 + widthT;
            solveBlock(blockLength, false, Linner, Binner, transL, false);
            boolean updateY;
            if (transL) {
                updateY = (Linner.row0 > 0);
            }
            else {
                updateY = (Linner.row1 < L.row1);
            }
            if (updateY) {
                if (transL) {
                    Linner.col1 = Linner.col0;
                    Linner.col0 = Linner.col1 - blockLength;
                    Linner.row1 = L.row1;
                    Binner.row1 = B.row1;
                    Y.row0 = Binner.row0 - blockLength;
                    Y.row1 = Binner.row0;
                }
                else {
                    Linner.row0 = Linner.row1;
                    Linner.row1 = Math.min(Linner.row0 + blockLength, L.row1);
                    Linner.col0 = L.col0;
                    Binner.row0 = B.row0;
                    Y.row0 = Binner.row1;
                    Y.row1 = Math.min(Y.row0 + blockLength, B.row1);
                }
                for (int k = B.col0; k < B.col1; k += blockLength) {
                    Binner.col0 = k;
                    Binner.col1 = Math.min(k + blockLength, B.col1);
                    Y.col0 = Binner.col0;
                    Y.col1 = Binner.col1;
                    if (transL) {
                        BlockMultiplication.multMinusTransA(blockLength, Linner, Binner, Y);
                    }
                    else {
                        BlockMultiplication.multMinus(blockLength, Linner, Binner, Y);
                    }
                }
            }
            i += stepI;
        }
    }
    
    public static void solveR(final int blockLength, final D1Submatrix64F R, final D1Submatrix64F B, final boolean transR) {
        final int lengthR = B.row1 - B.row0;
        if (R.getCols() != lengthR) {
            throw new IllegalArgumentException("Number of columns in R must be equal to the number of rows in B");
        }
        if (R.getRows() != lengthR) {
            throw new IllegalArgumentException("Number of rows in R must be equal to the number of rows in B");
        }
        final D1Submatrix64F Y = new D1Submatrix64F(B.original);
        final D1Submatrix64F Rinner = new D1Submatrix64F(R.original);
        final D1Submatrix64F Binner = new D1Submatrix64F(B.original);
        int startI;
        int stepI;
        if (transR) {
            startI = 0;
            stepI = blockLength;
        }
        else {
            startI = lengthR - lengthR % blockLength;
            if (startI == lengthR && lengthR >= blockLength) {
                startI -= blockLength;
            }
            stepI = -blockLength;
        }
        int i = startI;
        while (true) {
            if (transR) {
                if (i >= lengthR) {
                    break;
                }
            }
            else if (i < 0) {
                break;
            }
            final int widthT = Math.min(blockLength, lengthR - i);
            Rinner.col0 = R.col0 + i;
            Rinner.col1 = Rinner.col0 + widthT;
            Rinner.row0 = R.row0 + i;
            Rinner.row1 = Rinner.row0 + widthT;
            Binner.col0 = B.col0;
            Binner.col1 = B.col1;
            Binner.row0 = B.row0 + i;
            Binner.row1 = Binner.row0 + widthT;
            solveBlock(blockLength, true, Rinner, Binner, transR, false);
            boolean updateY;
            if (transR) {
                updateY = (Rinner.row1 < R.row1);
            }
            else {
                updateY = (Rinner.row0 > 0);
            }
            if (updateY) {
                if (transR) {
                    Rinner.col0 = Rinner.col1;
                    Rinner.col1 = Math.min(Rinner.col0 + blockLength, R.col1);
                    Rinner.row0 = R.row0;
                    Binner.row0 = B.row0;
                    Y.row0 = Binner.row1;
                    Y.row1 = Math.min(Y.row0 + blockLength, B.row1);
                }
                else {
                    Rinner.row1 = Rinner.row0;
                    Rinner.row0 = Rinner.row1 - blockLength;
                    Rinner.col1 = R.col1;
                    Binner.row1 = B.row1;
                    Y.row0 = Binner.row0 - blockLength;
                    Y.row1 = Binner.row0;
                }
                for (int k = B.col0; k < B.col1; k += blockLength) {
                    Binner.col0 = k;
                    Binner.col1 = Math.min(k + blockLength, B.col1);
                    Y.col0 = Binner.col0;
                    Y.col1 = Binner.col1;
                    if (transR) {
                        BlockMultiplication.multMinusTransA(blockLength, Rinner, Binner, Y);
                    }
                    else {
                        BlockMultiplication.multMinus(blockLength, Rinner, Binner, Y);
                    }
                }
            }
            i += stepI;
        }
    }
}
