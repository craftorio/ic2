// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class InvertUsingSolve
{
    public static void invert(final LinearSolver<DenseMatrix64F> solver, final RowD1Matrix64F A, final DenseMatrix64F A_inv, final DenseMatrix64F storage) {
        if (A.numRows != A_inv.numRows || A.numCols != A_inv.numCols) {
            throw new IllegalArgumentException("A and A_inv must have the same dimensions");
        }
        CommonOps.setIdentity(storage);
        solver.solve(storage, A_inv);
    }
    
    public static void invert(final LinearSolver<DenseMatrix64F> solver, final RowD1Matrix64F A, final DenseMatrix64F A_inv) {
        if (A.numRows != A_inv.numRows || A.numCols != A_inv.numCols) {
            throw new IllegalArgumentException("A and A_inv must have the same dimensions");
        }
        CommonOps.setIdentity(A_inv);
        solver.solve(A_inv, A_inv);
    }
}
