// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.hessenberg;

import ic2.shades.org.ejml.alg.block.BlockVectorOps;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.alg.block.decomposition.qr.BlockHouseHolder;
import ic2.shades.org.ejml.data.D1Submatrix64F;

public class TridiagonalHelper_B64
{
    public static void tridiagUpperRow(final int blockLength, final D1Submatrix64F A, final double[] gammas, final D1Submatrix64F V) {
        final int blockHeight = Math.min(blockLength, A.row1 - A.row0);
        if (blockHeight <= 1) {
            return;
        }
        final int width = A.col1 - A.col0;
        final int num = Math.min(width - 1, blockHeight);
        final int applyIndex = Math.min(width, blockHeight);
        for (int i = 0; i < num; ++i) {
            BlockHouseHolder.computeHouseHolderRow(blockLength, A, gammas, i);
            final double gamma = gammas[A.row0 + i];
            computeY(blockLength, A, V, i, gamma);
            computeRowOfV(blockLength, A, V, i, gamma);
            if (i + 1 < applyIndex) {
                applyReflectorsToRow(blockLength, A, V, i + 1);
            }
        }
    }
    
    public static void computeW_row(final int blockLength, final D1Submatrix64F Y, final D1Submatrix64F W, final double[] beta, int betaIndex) {
        final int heightY = Y.row1 - Y.row0;
        CommonOps.fill(W.original, 0.0);
        BlockHouseHolder.scale_row(blockLength, Y, W, 0, 1, -beta[betaIndex++]);
        for (int min = Math.min(heightY, W.col1 - W.col0), i = 1; i < min; ++i) {
            final double b = -beta[betaIndex++];
            for (int j = 0; j < i; ++j) {
                final double yv = BlockHouseHolder.innerProdRow(blockLength, Y, i, Y, j, 1);
                BlockVectorOps.add_row(blockLength, W, i, 1.0, W, j, b * yv, W, i, 1, Y.col1 - Y.col0);
            }
            BlockHouseHolder.add_row(blockLength, Y, i, b, W, i, 1.0, W, i, 1, Y.col1 - Y.col0);
        }
    }
    
    public static void computeV_blockVector(final int blockLength, final D1Submatrix64F A, final double[] gammas, final D1Submatrix64F V) {
        final int blockHeight = Math.min(blockLength, A.row1 - A.row0);
        if (blockHeight <= 1) {
            return;
        }
        final int width = A.col1 - A.col0;
        for (int num = Math.min(width - 1, blockHeight), i = 0; i < num; ++i) {
            final double gamma = gammas[A.row0 + i];
            computeY(blockLength, A, V, i, gamma);
            computeRowOfV(blockLength, A, V, i, gamma);
        }
    }
    
    public static void applyReflectorsToRow(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F V, final int row) {
        final int height = Math.min(blockLength, A.row1 - A.row0);
        final double[] dataA = A.original.data;
        final double[] dataV = V.original.data;
        for (int i = 0; i < row; ++i) {
            final int width = Math.min(blockLength, A.col1 - A.col0);
            final int indexU = A.original.numCols * A.row0 + height * A.col0 + i * width + row;
            final int indexV = V.original.numCols * V.row0 + height * V.col0 + i * width + row;
            final double u_row = (i + 1 == row) ? 1.0 : dataA[indexU];
            final double v_row = dataV[indexV];
            final double before = A.get(i, i + 1);
            A.set(i, i + 1, 1.0);
            BlockVectorOps.add_row(blockLength, A, row, 1.0, V, i, u_row, A, row, row, A.col1 - A.col0);
            BlockVectorOps.add_row(blockLength, A, row, 1.0, A, i, v_row, A, row, row, A.col1 - A.col0);
            A.set(i, i + 1, before);
        }
    }
    
    public static void computeY(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F V, final int row, final double gamma) {
        multA_u(blockLength, A, V, row);
        for (int i = 0; i < row; ++i) {
            final double dot_v_u = BlockHouseHolder.innerProdRow(blockLength, A, row, V, i, 1);
            final double dot_u_u = BlockHouseHolder.innerProdRow(blockLength, A, row, A, i, 1);
            BlockVectorOps.add_row(blockLength, V, row, 1.0, A, i, dot_v_u, V, row, row + 1, A.col1 - A.col0);
            BlockVectorOps.add_row(blockLength, V, row, 1.0, V, i, dot_u_u, V, row, row + 1, A.col1 - A.col0);
        }
        BlockVectorOps.scale_row(blockLength, V, row, -gamma, V, row, row + 1, V.col1 - V.col0);
    }
    
    public static void multA_u(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F V, final int row) {
        for (int heightMatA = A.row1 - A.row0, i = row + 1; i < heightMatA; ++i) {
            final double val = innerProdRowSymm(blockLength, A, row, A, i, 1);
            V.set(row, i, val);
        }
    }
    
    public static double innerProdRowSymm(final int blockLength, final D1Submatrix64F A, final int rowA, final D1Submatrix64F B, final int rowB, final int zeroOffset) {
        final int offset = rowA + zeroOffset;
        if (offset + B.col0 >= B.col1) {
            return 0.0;
        }
        if (offset < rowB) {
            double total = B.get(offset, rowB);
            total += BlockVectorOps.dot_row_col(blockLength, A, rowA, B, rowB, offset + 1, rowB);
            total += BlockVectorOps.dot_row(blockLength, A, rowA, B, rowB, rowB, A.col1 - A.col0);
            return total;
        }
        double total = B.get(rowB, offset);
        total += BlockVectorOps.dot_row(blockLength, A, rowA, B, rowB, offset + 1, A.col1 - A.col0);
        return total;
    }
    
    public static void computeRowOfV(final int blockLength, final D1Submatrix64F A, final D1Submatrix64F V, final int row, final double gamma) {
        final double val = BlockHouseHolder.innerProdRow(blockLength, A, row, V, row, 1);
        final double before = A.get(row, row + 1);
        A.set(row, row + 1, 1.0);
        BlockVectorOps.add_row(blockLength, V, row, 1.0, A, row, -0.5 * gamma * val, V, row, row + 1, A.col1 - A.col0);
        A.set(row, row + 1, before);
    }
}
