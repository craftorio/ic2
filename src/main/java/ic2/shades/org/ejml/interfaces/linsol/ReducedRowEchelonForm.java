// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.linsol;

import ic2.shades.org.ejml.data.Matrix64F;

public interface ReducedRowEchelonForm<T extends Matrix64F>
{
    void reduce(final T p0, final int p1);
    
    void setTolerance(final double p0);
}
