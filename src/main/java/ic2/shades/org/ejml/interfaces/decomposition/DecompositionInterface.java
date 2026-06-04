// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface DecompositionInterface<T extends Matrix64F>
{
    boolean decompose(final T p0);
    
    boolean inputModified();
}
