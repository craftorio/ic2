// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface LUDecomposition<T extends Matrix64F> extends DecompositionInterface<T>
{
    T getLower(final T p0);
    
    T getUpper(final T p0);
    
    T getPivot(final T p0);
    
    boolean isSingular();
    
    double computeDeterminant();
}
