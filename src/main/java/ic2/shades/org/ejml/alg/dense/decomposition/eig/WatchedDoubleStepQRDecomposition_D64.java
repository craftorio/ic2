// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvector;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvalue;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.HessenbergSimilarDecomposition_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;

public class WatchedDoubleStepQRDecomposition_D64 implements EigenDecomposition<DenseMatrix64F>
{
    HessenbergSimilarDecomposition_D64 hessenberg;
    WatchedDoubleStepQREigenvalue algValue;
    WatchedDoubleStepQREigenvector algVector;
    DenseMatrix64F H;
    boolean computeVectors;
    
    public WatchedDoubleStepQRDecomposition_D64(final boolean computeVectors) {
        this.hessenberg = new HessenbergSimilarDecomposition_D64(10);
        this.algValue = new WatchedDoubleStepQREigenvalue();
        this.algVector = new WatchedDoubleStepQREigenvector();
        this.computeVectors = computeVectors;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F A) {
        if (!this.hessenberg.decompose(A)) {
            return false;
        }
        this.H = this.hessenberg.getH(null);
        this.algValue.getImplicitQR().createR = false;
        if (!this.algValue.process(this.H)) {
            return false;
        }
        this.algValue.getImplicitQR().createR = true;
        return !this.computeVectors || this.algVector.process(this.algValue.getImplicitQR(), this.H, this.hessenberg.getQ(null));
    }
    
    @Override
    public boolean inputModified() {
        return this.hessenberg.inputModified();
    }
    
    @Override
    public int getNumberOfEigenvalues() {
        return this.algValue.getEigenvalues().length;
    }
    
    @Override
    public Complex64F getEigenvalue(final int index) {
        return this.algValue.getEigenvalues()[index];
    }
    
    @Override
    public DenseMatrix64F getEigenVector(final int index) {
        return this.algVector.getEigenvectors()[index];
    }
}
