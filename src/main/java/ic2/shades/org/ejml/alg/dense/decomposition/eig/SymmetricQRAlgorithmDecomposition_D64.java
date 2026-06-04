// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.symm.SymmetricQrAlgorithm;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.symm.SymmetricQREigenHelper;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;

public class SymmetricQRAlgorithmDecomposition_D64 implements EigenDecomposition<DenseMatrix64F>
{
    private TridiagonalSimilarDecomposition<DenseMatrix64F> decomp;
    private SymmetricQREigenHelper helper;
    private SymmetricQrAlgorithm vector;
    private boolean computeVectorsWithValues;
    private double[] values;
    private double[] diag;
    private double[] off;
    private double[] diagSaved;
    private double[] offSaved;
    private DenseMatrix64F V;
    private DenseMatrix64F[] eigenvectors;
    boolean computeVectors;
    
    public SymmetricQRAlgorithmDecomposition_D64(final TridiagonalSimilarDecomposition<DenseMatrix64F> decomp, final boolean computeVectors) {
        this.computeVectorsWithValues = false;
        this.decomp = decomp;
        this.computeVectors = computeVectors;
        this.helper = new SymmetricQREigenHelper();
        this.vector = new SymmetricQrAlgorithm(this.helper);
    }
    
    public SymmetricQRAlgorithmDecomposition_D64(final boolean computeVectors) {
        this(DecompositionFactory.tridiagonal(0), computeVectors);
    }
    
    public void setComputeVectorsWithValues(final boolean computeVectorsWithValues) {
        if (!this.computeVectors) {
            throw new IllegalArgumentException("Compute eigenvalues has been set to false");
        }
        this.computeVectorsWithValues = computeVectorsWithValues;
    }
    
    public void setMaxIterations(final int max) {
        this.vector.setMaxIterations(max);
    }
    
    @Override
    public int getNumberOfEigenvalues() {
        return this.helper.getMatrixSize();
    }
    
    @Override
    public Complex64F getEigenvalue(final int index) {
        return new Complex64F(this.values[index], 0.0);
    }
    
    @Override
    public DenseMatrix64F getEigenVector(final int index) {
        return this.eigenvectors[index];
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F orig) {
        if (orig.numCols != orig.numRows) {
            throw new IllegalArgumentException("Matrix must be square.");
        }
        if (orig.numCols <= 0) {
            return false;
        }
        final int N = orig.numRows;
        if (!this.decomp.decompose(orig)) {
            return false;
        }
        if (this.diag == null || this.diag.length < N) {
            this.diag = new double[N];
            this.off = new double[N - 1];
        }
        this.decomp.getDiagonal(this.diag, this.off);
        this.helper.init(this.diag, this.off, N);
        if (!this.computeVectors) {
            return this.computeEigenValues();
        }
        if (this.computeVectorsWithValues) {
            return this.extractTogether();
        }
        return this.extractSeparate(N);
    }
    
    @Override
    public boolean inputModified() {
        return this.decomp.inputModified();
    }
    
    private boolean extractTogether() {
        this.V = this.decomp.getQ(this.V, true);
        this.helper.setQ(this.V);
        this.vector.setFastEigenvalues(false);
        if (!this.vector.process(-1, null, null)) {
            return false;
        }
        this.eigenvectors = CommonOps.rowsToVector(this.V, this.eigenvectors);
        this.values = this.helper.copyEigenvalues(this.values);
        return true;
    }
    
    private boolean extractSeparate(final int numCols) {
        if (!this.computeEigenValues()) {
            return false;
        }
        this.helper.reset(numCols);
        this.diagSaved = this.helper.swapDiag(this.diagSaved);
        this.offSaved = this.helper.swapOff(this.offSaved);
        this.V = this.decomp.getQ(this.V, true);
        this.vector.setQ(this.V);
        if (!this.vector.process(-1, null, null, this.values)) {
            return false;
        }
        this.values = this.helper.copyEigenvalues(this.values);
        this.eigenvectors = CommonOps.rowsToVector(this.V, this.eigenvectors);
        return true;
    }
    
    private boolean computeEigenValues() {
        this.diagSaved = this.helper.copyDiag(this.diagSaved);
        this.offSaved = this.helper.copyOff(this.offSaved);
        this.vector.setQ(null);
        this.vector.setFastEigenvalues(true);
        if (!this.vector.process(-1, null, null)) {
            return false;
        }
        this.values = this.helper.copyEigenvalues(this.values);
        return true;
    }
}
