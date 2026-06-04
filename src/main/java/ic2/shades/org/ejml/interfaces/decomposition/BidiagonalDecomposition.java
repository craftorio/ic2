// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public interface BidiagonalDecomposition<T extends ReshapeMatrix64F> extends DecompositionInterface<T>
{
    T getB(final T p0, final boolean p1);
    
    T getU(final T p0, final boolean p1, final boolean p2);
    
    T getV(final T p0, final boolean p1, final boolean p2);
    
    void getDiagonal(final double[] p0, final double[] p1);
}
