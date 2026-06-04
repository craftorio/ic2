// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public class LinearSolverSafe<T extends ReshapeMatrix64F> implements LinearSolver<T>
{
    private LinearSolver<T> alg;
    private T A;
    private T B;
    
    public LinearSolverSafe(final LinearSolver<T> alg) {
        this.alg = alg;
    }
    
    @Override
    public boolean setA(final T A) {
        if (this.alg.modifiesA()) {
            if (this.A == null) {
                this.A = A.copy();
            }
            else {
                if (this.A.numRows != A.numRows || this.A.numCols != A.numCols) {
                    this.A.reshape(A.numRows, A.numCols, false);
                }
                this.A.set(A);
            }
            return this.alg.setA(this.A);
        }
        return this.alg.setA(A);
    }
    
    @Override
    public double quality() {
        return this.alg.quality();
    }
    
    @Override
    public void solve(T B, final T X) {
        if (this.alg.modifiesB()) {
            if (this.B == null) {
                this.B = B.copy();
            }
            else {
                if (this.B.numRows != B.numRows || this.B.numCols != B.numCols) {
                    this.B.reshape(this.A.numRows, B.numCols, false);
                }
                this.B.set(B);
            }
            B = this.B;
        }
        this.alg.solve(B, X);
    }
    
    @Override
    public void invert(final T A_inv) {
        this.alg.invert(A_inv);
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
