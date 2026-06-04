// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface QRDecomposition<T extends Matrix64F> extends DecompositionInterface<T>
{
    T getQ(final T p0, final boolean p1);
    
    T getR(final T p0, final boolean p1);
}
