// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.Matrix64F;

public interface EigenDecomposition<MatrixType extends Matrix64F> extends DecompositionInterface<MatrixType>
{
    int getNumberOfEigenvalues();
    
    Complex64F getEigenvalue(final int p0);
    
    MatrixType getEigenVector(final int p0);
}
