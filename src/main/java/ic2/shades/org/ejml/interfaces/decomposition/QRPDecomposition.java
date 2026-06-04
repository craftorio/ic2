// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;

public interface QRPDecomposition<T extends Matrix64F> extends QRDecomposition<T>
{
    void setSingularThreshold(final double p0);
    
    int getRank();
    
    int[] getPivots();
    
    DenseMatrix64F getPivotMatrix(final DenseMatrix64F p0);
}
