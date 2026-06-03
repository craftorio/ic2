package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class EigenPowerMethod {
  private double tol = 1.0E-10D;
  
  private DenseMatrix64F q0;
  
  private DenseMatrix64F q1;
  
  private DenseMatrix64F q2;
  
  private int maxIterations = 20;
  
  private DenseMatrix64F B;
  
  private DenseMatrix64F seed;
  
  public EigenPowerMethod(int size) {
    this.q0 = new DenseMatrix64F(size, 1);
    this.q1 = new DenseMatrix64F(size, 1);
    this.q2 = new DenseMatrix64F(size, 1);
    this.B = new DenseMatrix64F(size, size);
  }
  
  public void setSeed(DenseMatrix64F seed) {
    this.seed = seed;
  }
  
  public void setOptions(int maxIterations, double tolerance) {
    this.maxIterations = maxIterations;
    this.tol = tolerance;
  }
  
  public boolean computeDirect(DenseMatrix64F A) {
    initPower(A);
    boolean converged = false;
    for (int i = 0; i < this.maxIterations && !converged; i++) {
      CommonOps.mult((RowD1Matrix64F)A, (RowD1Matrix64F)this.q0, (RowD1Matrix64F)this.q1);
      double s = NormOps.normPInf(this.q1);
      CommonOps.divide((D1Matrix64F)this.q1, s, (D1Matrix64F)this.q2);
      converged = checkConverged(A);
    } 
    return converged;
  }
  
  private void initPower(DenseMatrix64F A) {
    if (A.numRows != A.numCols)
      throw new IllegalArgumentException("A must be a square matrix."); 
    if (this.seed != null) {
      this.q0.set((D1Matrix64F)this.seed);
    } else {
      for (int i = 0; i < A.numRows; i++)
        this.q0.data[i] = 1.0D; 
    } 
  }
  
  private boolean checkConverged(DenseMatrix64F A) {
    double worst = 0.0D;
    double worst2 = 0.0D;
    for (int j = 0; j < A.numRows; j++) {
      double val = Math.abs(this.q2.data[j] - this.q0.data[j]);
      if (val > worst)
        worst = val; 
      val = Math.abs(this.q2.data[j] + this.q0.data[j]);
      if (val > worst2)
        worst2 = val; 
    } 
    DenseMatrix64F temp = this.q0;
    this.q0 = this.q2;
    this.q2 = temp;
    if (worst < this.tol)
      return true; 
    if (worst2 < this.tol)
      return true; 
    return false;
  }
  
  public boolean computeShiftDirect(DenseMatrix64F A, double alpha) {
    SpecializedOps.addIdentity((RowD1Matrix64F)A, (RowD1Matrix64F)this.B, -alpha);
    return computeDirect(this.B);
  }
  
  public boolean computeShiftInvert(DenseMatrix64F A, double alpha) {
    initPower(A);
    LinearSolver solver = LinearSolverFactory.linear(A.numCols);
    SpecializedOps.addIdentity((RowD1Matrix64F)A, (RowD1Matrix64F)this.B, -alpha);
    solver.setA((Matrix64F)this.B);
    boolean converged = false;
    for (int i = 0; i < this.maxIterations && !converged; i++) {
      solver.solve((Matrix64F)this.q0, (Matrix64F)this.q1);
      double s = NormOps.normPInf(this.q1);
      CommonOps.divide((D1Matrix64F)this.q1, s, (D1Matrix64F)this.q2);
      converged = checkConverged(A);
    } 
    return converged;
  }
  
  public DenseMatrix64F getEigenVector() {
    return this.q0;
  }
}
