// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public interface TridiagonalSimilarDecomposition<MatrixType extends ReshapeMatrix64F> extends DecompositionInterface<MatrixType>
{
    MatrixType getT(final MatrixType p0);
    
    MatrixType getQ(final MatrixType p0, final boolean p1);
    
    void getDiagonal(final double[] p0, final double[] p1);
}
