// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;

public class LinearSolverLu extends LinearSolverLuBase
{
    boolean doImprove;
    
    public LinearSolverLu(final LUDecompositionBase_D64 decomp) {
        super(decomp);
        this.doImprove = false;
    }
    
    public LinearSolverLu(final LUDecompositionBase_D64 decomp, final boolean doImprove) {
        super(decomp);
        this.doImprove = false;
        this.doImprove = doImprove;
    }
    
    @Override
    public void solve(final DenseMatrix64F b, final DenseMatrix64F x) {
        if (b.numCols != x.numCols || b.numRows != this.numRows || x.numRows != this.numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        final int numCols = b.numCols;
        final double[] dataB = b.data;
        final double[] dataX = x.data;
        final double[] vv = this.decomp._getVV();
        for (int j = 0; j < numCols; ++j) {
            for (int index = j, i = 0; i < this.numCols; ++i, index += numCols) {
                vv[i] = dataB[index];
            }
            this.decomp._solveVectorInternal(vv);
            for (int index = j, i = 0; i < this.numCols; ++i, index += numCols) {
                dataX[index] = vv[i];
            }
        }
        if (this.doImprove) {
            this.improveSol(b, x);
        }
    }
}
