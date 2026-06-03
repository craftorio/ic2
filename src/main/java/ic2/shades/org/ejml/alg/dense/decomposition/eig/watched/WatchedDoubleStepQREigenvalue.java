package ic2.shades.org.ejml.alg.dense.decomposition.eig.watched;

import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueExtractor;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class WatchedDoubleStepQREigenvalue implements EigenvalueExtractor {
  WatchedDoubleStepQREigen implicitQR = new WatchedDoubleStepQREigen();
  
  int[] splits;
  
  int numSplits;
  
  int x1;
  
  int x2;
  
  public void setup(DenseMatrix64F A) {
    this.implicitQR.setup(A);
    this.implicitQR.setQ(null);
    this.splits = new int[A.numRows];
    this.numSplits = 0;
  }
  
  public boolean process(DenseMatrix64F origA) {
    setup(origA);
    this.x1 = 0;
    this.x2 = origA.numRows - 1;
    while (this.implicitQR.numEigen < origA.numRows) {
      if (this.implicitQR.steps > this.implicitQR.maxIterations)
        return false; 
      this.implicitQR.incrementSteps();
      if (this.x2 < this.x1) {
        moveToNextSplit();
        continue;
      } 
      if (this.x2 - this.x1 == 0) {
        this.implicitQR.addEigenAt(this.x1);
        this.x2--;
        continue;
      } 
      if (this.x2 - this.x1 == 1) {
        this.implicitQR.addComputedEigen2x2(this.x1, this.x2);
        this.x2 -= 2;
        continue;
      } 
      if (this.implicitQR.steps - this.implicitQR.lastExceptional > this.implicitQR.exceptionalThreshold) {
        if (Double.isNaN(this.implicitQR.A.get(this.x2, this.x2)))
          return false; 
        this.implicitQR.exceptionalShift(this.x1, this.x2);
        continue;
      } 
      if (this.implicitQR.isZero(this.x2, this.x2 - 1)) {
        this.implicitQR.addEigenAt(this.x2);
        this.x2--;
        continue;
      } 
      performIteration();
    } 
    return true;
  }
  
  private void moveToNextSplit() {
    if (this.numSplits <= 0)
      throw new RuntimeException("bad"); 
    this.x2 = this.splits[--this.numSplits];
    if (this.numSplits > 0) {
      this.x1 = this.splits[this.numSplits - 1] + 1;
    } else {
      this.x1 = 0;
    } 
  }
  
  private void performIteration() {
    boolean changed = false;
    for (int i = this.x2; i > this.x1; i--) {
      if (this.implicitQR.isZero(i, i - 1)) {
        this.x1 = i;
        this.splits[this.numSplits++] = i - 1;
        changed = true;
        break;
      } 
    } 
    if (!changed)
      this.implicitQR.implicitDoubleStep(this.x1, this.x2); 
  }
  
  public int getNumberOfEigenvalues() {
    return this.implicitQR.getNumberOfEigenvalues();
  }
  
  public Complex64F[] getEigenvalues() {
    return this.implicitQR.getEigenvalues();
  }
  
  public WatchedDoubleStepQREigen getImplicitQR() {
    return this.implicitQR;
  }
}
