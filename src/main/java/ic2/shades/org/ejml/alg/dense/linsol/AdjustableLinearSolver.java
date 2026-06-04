// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public interface AdjustableLinearSolver extends LinearSolver<DenseMatrix64F>
{
    boolean addRowToA(final double[] p0, final int p1);
    
    boolean removeRowFromA(final int p0);
}
