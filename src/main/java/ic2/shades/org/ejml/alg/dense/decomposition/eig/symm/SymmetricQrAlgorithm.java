// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig.symm;

import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class SymmetricQrAlgorithm
{
    private SymmetricQREigenHelper helper;
    private DenseMatrix64F Q;
    private double[] eigenvalues;
    private int exceptionalThresh;
    private int maxIterations;
    private boolean fastEigenvalues;
    private boolean followingScript;
    
    public SymmetricQrAlgorithm(final SymmetricQREigenHelper helper) {
        this.exceptionalThresh = 15;
        this.maxIterations = this.exceptionalThresh * 15;
        this.helper = helper;
    }
    
    public SymmetricQrAlgorithm() {
        this.exceptionalThresh = 15;
        this.maxIterations = this.exceptionalThresh * 15;
        this.helper = new SymmetricQREigenHelper();
    }
    
    public void setMaxIterations(final int maxIterations) {
        this.maxIterations = maxIterations;
    }
    
    public DenseMatrix64F getQ() {
        return this.Q;
    }
    
    public void setQ(final DenseMatrix64F q) {
        this.Q = q;
    }
    
    public void setFastEigenvalues(final boolean fastEigenvalues) {
        this.fastEigenvalues = fastEigenvalues;
    }
    
    public double getEigenvalue(final int index) {
        return this.helper.diag[index];
    }
    
    public int getNumberOfEigenvalues() {
        return this.helper.N;
    }
    
    public boolean process(final int sideLength, final double[] diag, final double[] off, final double[] eigenvalues) {
        if (diag != null) {
            this.helper.init(diag, off, sideLength);
        }
        if (this.Q == null) {
            this.Q = CommonOps.identity(this.helper.N);
        }
        this.helper.setQ(this.Q);
        this.followingScript = true;
        this.eigenvalues = eigenvalues;
        this.fastEigenvalues = false;
        return this._process();
    }
    
    public boolean process(final int sideLength, final double[] diag, final double[] off) {
        if (diag != null) {
            this.helper.init(diag, off, sideLength);
        }
        this.followingScript = false;
        this.eigenvalues = null;
        return this._process();
    }
    
    private boolean _process() {
        while (this.helper.x2 >= 0) {
            if (this.helper.steps > this.maxIterations) {
                return false;
            }
            if (this.helper.x1 == this.helper.x2) {
                this.helper.resetSteps();
                if (!this.helper.nextSplit()) {
                    break;
                }
            }
            else if (this.fastEigenvalues && this.helper.x2 - this.helper.x1 == 1) {
                this.helper.resetSteps();
                this.helper.eigenvalue2by2(this.helper.x1);
                this.helper.setSubmatrix(this.helper.x2, this.helper.x2);
            }
            else if (this.helper.steps - this.helper.lastExceptional > this.exceptionalThresh) {
                this.helper.exceptionalShift();
            }
            else {
                this.performStep();
            }
            this.helper.incrementSteps();
        }
        return true;
    }
    
    public void performStep() {
        for (int i = this.helper.x2 - 1; i >= this.helper.x1; --i) {
            if (this.helper.isZero(i)) {
                this.helper.splits[this.helper.numSplits++] = i;
                this.helper.x1 = i + 1;
                return;
            }
        }
        double lambda;
        if (this.followingScript) {
            if (this.helper.steps > 10) {
                this.followingScript = false;
                return;
            }
            lambda = this.eigenvalues[this.helper.x2];
        }
        else {
            lambda = this.helper.computeShift();
        }
        this.helper.performImplicitSingleStep(lambda, false);
    }
}
