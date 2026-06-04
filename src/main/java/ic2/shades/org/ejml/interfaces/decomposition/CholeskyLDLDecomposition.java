// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface CholeskyLDLDecomposition<MatrixType extends Matrix64F> extends DecompositionInterface<MatrixType>
{
    MatrixType getL(final MatrixType p0);
    
    double[] getDiagonal();
    
    MatrixType getD(final MatrixType p0);
}
