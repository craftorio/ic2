// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface SingularValueDecomposition<T extends Matrix64F> extends DecompositionInterface<T>
{
    double[] getSingularValues();
    
    int numberOfSingularValues();
    
    boolean isCompact();
    
    T getU(final T p0, final boolean p1);
    
    T getV(final T p0, final boolean p1);
    
    T getW(final T p0);
    
    int numRows();
    
    int numCols();
}
