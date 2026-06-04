// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverUnrolled implements LinearSolver<DenseMatrix64F>
{
    DenseMatrix64F A;
    
    @Override
    public boolean setA(final DenseMatrix64F A) {
        if (A.numRows != A.numCols) {
            return false;
        }
        this.A = A;
        return A.numRows <= 5;
    }
    
    @Override
    public double quality() {
        throw new IllegalArgumentException("Not supported by this solver.");
    }
    
    @Override
    public void solve(final DenseMatrix64F B, final DenseMatrix64F X) {
        throw new RuntimeException("Not supported");
    }
    
    @Override
    public void invert(final DenseMatrix64F A_inv) {
        if (this.A.numRows == 1) {
            A_inv.set(0, 1.0 / this.A.get(0));
        }
        UnrolledInverseFromMinor.inv(this.A, A_inv);
    }
    
    @Override
    public boolean modifiesA() {
        return false;
    }
    
    @Override
    public boolean modifiesB() {
        return false;
    }
}
