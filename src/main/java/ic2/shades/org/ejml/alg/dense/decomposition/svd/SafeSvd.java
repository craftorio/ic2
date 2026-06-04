// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.svd;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class SafeSvd implements SingularValueDecomposition<DenseMatrix64F>
{
    SingularValueDecomposition<DenseMatrix64F> alg;
    DenseMatrix64F work;
    
    public SafeSvd(final SingularValueDecomposition<DenseMatrix64F> alg) {
        this.work = new DenseMatrix64F(1, 1);
        this.alg = alg;
    }
    
    @Override
    public double[] getSingularValues() {
        return this.alg.getSingularValues();
    }
    
    @Override
    public int numberOfSingularValues() {
        return this.alg.numberOfSingularValues();
    }
    
    @Override
    public boolean isCompact() {
        return this.alg.isCompact();
    }
    
    @Override
    public DenseMatrix64F getU(final DenseMatrix64F U, final boolean transposed) {
        return this.alg.getU(U, transposed);
    }
    
    @Override
    public DenseMatrix64F getV(final DenseMatrix64F V, final boolean transposed) {
        return this.alg.getV(V, transposed);
    }
    
    @Override
    public DenseMatrix64F getW(final DenseMatrix64F W) {
        return this.alg.getW(W);
    }
    
    @Override
    public int numRows() {
        return this.alg.numRows();
    }
    
    @Override
    public int numCols() {
        return this.alg.numCols();
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F orig) {
        if (this.alg.inputModified()) {
            this.work.reshape(orig.numRows, orig.numCols);
            this.work.set(orig);
            return this.alg.decompose(this.work);
        }
        return this.alg.decompose(orig);
    }
    
    @Override
    public boolean inputModified() {
        return false;
    }
}
