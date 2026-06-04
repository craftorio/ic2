// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.linsol;

import ic2.shades.org.ejml.data.Matrix64F;

public interface LinearSolver<T extends Matrix64F>
{
    boolean setA(final T p0);
    
    double quality();
    
    void solve(final T p0, final T p1);
    
    void invert(final T p0);
    
    boolean modifiesA();
    
    boolean modifiesB();
}
