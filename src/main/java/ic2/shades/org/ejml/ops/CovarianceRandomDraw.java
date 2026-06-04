// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import java.util.Random;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class CovarianceRandomDraw
{
    private DenseMatrix64F A;
    private Random rand;
    private DenseMatrix64F r;
    
    public CovarianceRandomDraw(final Random rand, DenseMatrix64F cov) {
        this.r = new DenseMatrix64F(cov.numRows, 1);
        final CholeskyDecompositionInner_D64 cholesky = new CholeskyDecompositionInner_D64(true);
        if (cholesky.inputModified()) {
            cov = cov.copy();
        }
        if (!cholesky.decompose(cov)) {
            throw new RuntimeException("Decomposition failed!");
        }
        this.A = cholesky.getT();
        this.rand = rand;
    }
    
    public void next(final DenseMatrix64F x) {
        for (int i = 0; i < this.r.numRows; ++i) {
            this.r.set(i, 0, this.rand.nextGaussian());
        }
        CommonOps.multAdd(this.A, this.r, x);
    }
    
    public double computeLikelihoodP() {
        double ret = 1.0;
        for (int i = 0; i < this.r.numRows; ++i) {
            final double a = this.r.get(i, 0);
            ret *= Math.exp(-a * a / 2.0);
        }
        return ret;
    }
}
