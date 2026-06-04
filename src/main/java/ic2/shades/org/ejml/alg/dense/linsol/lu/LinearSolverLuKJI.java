// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;

public class LinearSolverLuKJI extends LinearSolverLuBase
{
    private double[] dataLU;
    private int[] pivot;
    
    public LinearSolverLuKJI(final LUDecompositionBase_D64 decomp) {
        super(decomp);
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        final boolean ret = super.setA(A);
        this.pivot = this.decomp.getPivot();
        this.dataLU = this.decomp.getLU().data;
        return ret;
    }
    
    @Override
    public void solve(final DenseMatrix64F b, final DenseMatrix64F x) {
        if (b.numCols != x.numCols || b.numRows != this.numRows || x.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        if (b != x) {
            SpecializedOps.copyChangeRow(this.pivot, b, x);
            final int nx = b.numCols;
            final double[] dataX = x.data;
            for (int k = 0; k < this.numCols; ++k) {
                for (int i = k + 1; i < this.numCols; ++i) {
                    for (int j = 0; j < nx; ++j) {
                        final double[] array = dataX;
                        final int n = i * nx + j;
                        array[n] -= dataX[k * nx + j] * this.dataLU[i * this.numCols + k];
                    }
                }
            }
            for (int k = this.numCols - 1; k >= 0; --k) {
                for (int l = 0; l < nx; ++l) {
                    final double[] array2 = dataX;
                    final int n2 = k * nx + l;
                    array2[n2] /= this.dataLU[k * this.numCols + k];
                }
                for (int i = 0; i < k; ++i) {
                    for (int j = 0; j < nx; ++j) {
                        final double[] array3 = dataX;
                        final int n3 = i * nx + j;
                        array3[n3] -= dataX[k * nx + j] * this.dataLU[i * this.numCols + k];
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Current doesn't support using the same matrix instance");
    }
}
