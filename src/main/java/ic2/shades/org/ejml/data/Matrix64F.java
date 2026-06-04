// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import java.io.Serializable;

public interface Matrix64F extends Serializable
{
    double get(final int p0, final int p1);
    
    double unsafe_get(final int p0, final int p1);
    
    void set(final int p0, final int p1, final double p2);
    
    void unsafe_set(final int p0, final int p1, final double p2);
    
    int getNumRows();
    
    int getNumCols();
    
    int getNumElements();
    
     <T extends Matrix64F> T copy();
    
    void print();
}
