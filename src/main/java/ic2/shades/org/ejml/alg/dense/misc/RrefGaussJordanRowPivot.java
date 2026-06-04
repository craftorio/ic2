// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.ReducedRowEchelonForm;

public class RrefGaussJordanRowPivot implements ReducedRowEchelonForm<DenseMatrix64F>
{
    double tol;
    
    @Override
    public void setTolerance(final double tol) {
        this.tol = tol;
    }
    
    @Override
    public void reduce(final DenseMatrix64F A, final int coefficientColumns) {
        if (A.numCols < coefficientColumns) {
            throw new IllegalArgumentException("The system must be at least as wide as A");
        }
        int leadIndex = 0;
        for (int i = 0; i < coefficientColumns; ++i) {
            int pivotRow = -1;
            double maxValue = this.tol;
            for (int row = leadIndex; row < A.numRows; ++row) {
                final double v = Math.abs(A.data[row * A.numCols + i]);
                if (v > maxValue) {
                    maxValue = v;
                    pivotRow = row;
                }
            }
            if (pivotRow != -1) {
                if (leadIndex != pivotRow) {
                    swapRows(A, leadIndex, pivotRow);
                }
                for (int row = 0; row < A.numRows; ++row) {
                    if (row != leadIndex) {
                        int indexPivot = leadIndex * A.numCols + i;
                        int indexTarget = row * A.numCols + i;
                        final double alpha = A.data[indexTarget] / A.data[indexPivot++];
                        A.data[indexTarget++] = 0.0;
                        for (int col = i + 1; col < A.numCols; ++col) {
                            final double[] data = A.data;
                            final int n = indexTarget++;
                            data[n] -= A.data[indexPivot++] * alpha;
                        }
                    }
                }
                int indexPivot2 = leadIndex * A.numCols + i;
                final double alpha2 = 1.0 / A.data[indexPivot2];
                A.data[indexPivot2++] = 1.0;
                for (int col2 = i + 1; col2 < A.numCols; ++col2) {
                    final double[] data2 = A.data;
                    final int n2 = indexPivot2++;
                    data2[n2] *= alpha2;
                }
                ++leadIndex;
            }
        }
    }
    
    protected static void swapRows(final DenseMatrix64F A, final int rowA, final int rowB) {
        for (int indexA = rowA * A.numCols, indexB = rowB * A.numCols, i = 0; i < A.numCols; ++i, ++indexA, ++indexB) {
            final double temp = A.data[indexA];
            A.data[indexA] = A.data[indexB];
            A.data[indexB] = temp;
        }
    }
}
