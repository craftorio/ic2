// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig.watched;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueExtractor;

public class WatchedDoubleStepQREigenvalue implements EigenvalueExtractor
{
    WatchedDoubleStepQREigen implicitQR;
    int[] splits;
    int numSplits;
    int x1;
    int x2;
    
    public WatchedDoubleStepQREigenvalue() {
        this.implicitQR = new WatchedDoubleStepQREigen();
    }
    
    public void setup(final DenseMatrix64F A) {
        this.implicitQR.setup(A);
        this.implicitQR.setQ(null);
        this.splits = new int[A.numRows];
        this.numSplits = 0;
    }
    
    @Override
    public boolean process(final DenseMatrix64F origA) {
        this.setup(origA);
        this.x1 = 0;
        this.x2 = origA.numRows - 1;
        while (this.implicitQR.numEigen < origA.numRows) {
            if (this.implicitQR.steps > this.implicitQR.maxIterations) {
                return false;
            }
            this.implicitQR.incrementSteps();
            if (this.x2 < this.x1) {
                this.moveToNextSplit();
            }
            else if (this.x2 - this.x1 == 0) {
                this.implicitQR.addEigenAt(this.x1);
                --this.x2;
            }
            else if (this.x2 - this.x1 == 1) {
                this.implicitQR.addComputedEigen2x2(this.x1, this.x2);
                this.x2 -= 2;
            }
            else if (this.implicitQR.steps - this.implicitQR.lastExceptional > this.implicitQR.exceptionalThreshold) {
                if (Double.isNaN(this.implicitQR.A.get(this.x2, this.x2))) {
                    return false;
                }
                this.implicitQR.exceptionalShift(this.x1, this.x2);
            }
            else if (this.implicitQR.isZero(this.x2, this.x2 - 1)) {
                this.implicitQR.addEigenAt(this.x2);
                --this.x2;
            }
            else {
                this.performIteration();
            }
        }
        return true;
    }
    
    private void moveToNextSplit() {
        if (this.numSplits <= 0) {
            throw new RuntimeException("bad");
        }
        final int[] splits = this.splits;
        final int numSplits = this.numSplits - 1;
        this.numSplits = numSplits;
        this.x2 = splits[numSplits];
        if (this.numSplits > 0) {
            this.x1 = this.splits[this.numSplits - 1] + 1;
        }
        else {
            this.x1 = 0;
        }
    }
    
    private void performIteration() {
        boolean changed = false;
        for (int i = this.x2; i > this.x1; --i) {
            if (this.implicitQR.isZero(i, i - 1)) {
                this.x1 = i;
                this.splits[this.numSplits++] = i - 1;
                changed = true;
                break;
            }
        }
        if (!changed) {
            this.implicitQR.implicitDoubleStep(this.x1, this.x2);
        }
    }
    
    @Override
    public int getNumberOfEigenvalues() {
        return this.implicitQR.getNumberOfEigenvalues();
    }
    
    @Override
    public Complex64F[] getEigenvalues() {
        return this.implicitQR.getEigenvalues();
    }
    
    public WatchedDoubleStepQREigen getImplicitQR() {
        return this.implicitQR;
    }
}
