// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol.lu;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverAbstract;

public abstract class LinearSolverLuBase extends LinearSolverAbstract
{
    protected LUDecompositionBase_D64 decomp;
    
    public LinearSolverLuBase(final LUDecompositionBase_D64 decomp) {
        this.decomp = decomp;
    }
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        this._setA(A);
        return this.decomp.decompose(A);
    }
    
    @Override
    public double quality() {
        return this.decomp.quality();
    }
    
    @Override
    public void invert(final DenseMatrix64F A_inv) {
        final double[] vv = this.decomp._getVV();
        final DenseMatrix64F LU = this.decomp.getLU();
        if (A_inv.numCols != LU.numCols || A_inv.numRows != LU.numRows) {
            throw new IllegalArgumentException("Unexpected matrix dimension");
        }
        final int n = this.A.numCols;
        final double[] dataInv = A_inv.data;
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < n; ++i) {
                vv[i] = ((i == j) ? 1.0 : 0.0);
            }
            this.decomp._solveVectorInternal(vv);
            for (int index = j, k = 0; k < n; ++k, index += n) {
                dataInv[index] = vv[k];
            }
        }
    }
    
    public void improveSol(final DenseMatrix64F b, final DenseMatrix64F x) {
        if (b.numCols != x.numCols) {
            throw new IllegalArgumentException("bad shapes");
        }
        final double[] dataA = this.A.data;
        final double[] dataB = b.data;
        final double[] dataX = x.data;
        final int nc = b.numCols;
        final int n = b.numCols;
        final double[] vv = this.decomp._getVV();
        final DenseMatrix64F LU = this.decomp.getLU();
        for (int k = 0; k < nc; ++k) {
            for (int i = 0; i < n; ++i) {
                double sdp = -dataB[i * nc + k];
                for (int j = 0; j < n; ++j) {
                    sdp += dataA[i * n + j] * dataX[j * nc + k];
                }
                vv[i] = sdp;
            }
            this.decomp._solveVectorInternal(vv);
            for (int i = 0; i < n; ++i) {
                final double[] array = dataX;
                final int n2 = i * nc + k;
                array[n2] -= vv[i];
            }
        }
    }
    
    @Override
    public boolean modifiesA() {
        return false;
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
    
    public LUDecompositionBase_D64 getDecomposer() {
        return this.decomp;
    }
}
