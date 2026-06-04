// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;

public class SimpleEVD<T extends SimpleMatrix>
{
    private EigenDecomposition<DenseMatrix64F> eig;
    DenseMatrix64F mat;
    
    public SimpleEVD(final DenseMatrix64F mat) {
        this.mat = mat;
        this.eig = DecompositionFactory.eig(mat.numCols, true);
        if (!this.eig.decompose(mat)) {
            throw new RuntimeException("Eigenvalue Decomposition failed");
        }
    }
    
    public int getNumberOfEigenvalues() {
        return this.eig.getNumberOfEigenvalues();
    }
    
    public Complex64F getEigenvalue(final int index) {
        return this.eig.getEigenvalue(index);
    }
    
    public T getEigenVector(final int index) {
        return (T)SimpleMatrix.wrap(this.eig.getEigenVector(index));
    }
    
    public double quality() {
        return DecompositionFactory.quality(this.mat, this.eig);
    }
    
    public EigenDecomposition getEVD() {
        return this.eig;
    }
    
    public int getIndexMax() {
        int indexMax = 0;
        double max = this.getEigenvalue(0).getMagnitude2();
        for (int N = this.getNumberOfEigenvalues(), i = 1; i < N; ++i) {
            final double m = this.getEigenvalue(i).getMagnitude2();
            if (m > max) {
                max = m;
                indexMax = i;
            }
        }
        return indexMax;
    }
    
    public int getIndexMin() {
        int indexMin = 0;
        double min = this.getEigenvalue(0).getMagnitude2();
        for (int N = this.getNumberOfEigenvalues(), i = 1; i < N; ++i) {
            final double m = this.getEigenvalue(i).getMagnitude2();
            if (m < min) {
                min = m;
                indexMin = i;
            }
        }
        return indexMin;
    }
}
