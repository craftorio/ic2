package ic2.shades.org.ejml.alg.dense.decomposition.eig.watched;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class WatchedDoubleStepQREigenvector {
  WatchedDoubleStepQREigen implicit;
  
  DenseMatrix64F Q;
  
  DenseMatrix64F[] eigenvectors;
  
  DenseMatrix64F eigenvectorTemp;
  
  LinearSolver solver;
  
  Complex64F[] origEigenvalues;
  
  int N;
  
  int[] splits;
  
  int numSplits;
  
  int x1;
  
  int x2;
  
  int indexVal;
  
  boolean onscript;
  
  public boolean process(WatchedDoubleStepQREigen implicit, DenseMatrix64F A, DenseMatrix64F Q_h) {
    this.implicit = implicit;
    if (this.N != A.numRows) {
      this.N = A.numRows;
      this.Q = new DenseMatrix64F(this.N, this.N);
      this.splits = new int[this.N];
      this.origEigenvalues = new Complex64F[this.N];
      this.eigenvectors = new DenseMatrix64F[this.N];
      this.eigenvectorTemp = new DenseMatrix64F(this.N, 1);
      this.solver = LinearSolverFactory.linear(0);
    } else {
      this.eigenvectors = new DenseMatrix64F[this.N];
    } 
    System.arraycopy(implicit.eigenvalues, 0, this.origEigenvalues, 0, this.N);
    implicit.setup(A);
    implicit.setQ(this.Q);
    this.numSplits = 0;
    this.onscript = true;
    if (!findQandR())
      return false; 
    return extractVectors(Q_h);
  }
  
  public boolean extractVectors(DenseMatrix64F Q_h) {
    UtilEjml.memset(this.eigenvectorTemp.data, 0.0D);
    boolean triangular = true;
    for (int i = 0; i < this.N; i++) {
      Complex64F c = this.implicit.eigenvalues[this.N - i - 1];
      if (triangular && !c.isReal())
        triangular = false; 
      if (c.isReal() && this.eigenvectors[this.N - i - 1] == null)
        solveEigenvectorDuplicateEigenvalue(c.real, i, triangular); 
    } 
    if (Q_h != null) {
      DenseMatrix64F temp = new DenseMatrix64F(this.N, 1);
      for (int j = 0; j < this.N; j++) {
        DenseMatrix64F v = this.eigenvectors[j];
        if (v != null) {
          CommonOps.mult((RowD1Matrix64F)Q_h, (RowD1Matrix64F)v, (RowD1Matrix64F)temp);
          this.eigenvectors[j] = temp;
          temp = v;
        } 
      } 
    } 
    return true;
  }
  
  private void solveEigenvectorDuplicateEigenvalue(double real, int first, boolean isTriangle) {
    double scale = Math.abs(real);
    if (scale == 0.0D)
      scale = 1.0D; 
    this.eigenvectorTemp.reshape(this.N, 1, false);
    this.eigenvectorTemp.zero();
    if (first > 0)
      if (isTriangle) {
        solveUsingTriangle(real, first, this.eigenvectorTemp);
      } else {
        solveWithLU(real, first, this.eigenvectorTemp);
      }  
    this.eigenvectorTemp.reshape(this.N, 1, false);
    for (int i = first; i < this.N; i++) {
      Complex64F c = this.implicit.eigenvalues[this.N - i - 1];
      if (c.isReal() && Math.abs(c.real - real) / scale < 100.0D * UtilEjml.EPS) {
        this.eigenvectorTemp.data[i] = 1.0D;
        DenseMatrix64F v = new DenseMatrix64F(this.N, 1);
        CommonOps.multTransA((RowD1Matrix64F)this.Q, (RowD1Matrix64F)this.eigenvectorTemp, (RowD1Matrix64F)v);
        this.eigenvectors[this.N - i - 1] = v;
        NormOps.normalizeF(v);
        this.eigenvectorTemp.data[i] = 0.0D;
      } 
    } 
  }
  
  private void solveUsingTriangle(double real, int index, DenseMatrix64F r) {
    int i;
    for (i = 0; i < index; i++)
      this.implicit.A.add(i, i, -real); 
    SpecializedOps.subvector((RowD1Matrix64F)this.implicit.A, 0, index, index, false, 0, (RowD1Matrix64F)r);
    CommonOps.changeSign((D1Matrix64F)r);
    TriangularSolver.solveU(this.implicit.A.data, r.data, this.implicit.A.numRows, 0, index);
    for (i = 0; i < index; i++)
      this.implicit.A.add(i, i, real); 
  }
  
  private void solveWithLU(double real, int index, DenseMatrix64F r) {
    DenseMatrix64F A = new DenseMatrix64F(index, index);
    CommonOps.extract((ReshapeMatrix64F)this.implicit.A, 0, index, 0, index, (ReshapeMatrix64F)A, 0, 0);
    for (int i = 0; i < index; i++)
      A.add(i, i, -real); 
    r.reshape(index, 1, false);
    SpecializedOps.subvector((RowD1Matrix64F)this.implicit.A, 0, index, index, false, 0, (RowD1Matrix64F)r);
    CommonOps.changeSign((D1Matrix64F)r);
    if (!this.solver.setA((Matrix64F)A))
      throw new RuntimeException("Solve failed"); 
    this.solver.solve((Matrix64F)r, (Matrix64F)r);
  }
  
  public boolean findQandR() {
    CommonOps.setIdentity((RowD1Matrix64F)this.Q);
    this.x1 = 0;
    this.x2 = this.N - 1;
    this.indexVal = 0;
    while (this.indexVal < this.N) {
      if (!findNextEigenvalue())
        return false; 
    } 
    return true;
  }
  
  private boolean findNextEigenvalue() {
    boolean foundEigen = false;
    while (!foundEigen && this.implicit.steps < this.implicit.maxIterations) {
      this.implicit.incrementSteps();
      if (this.x2 < this.x1) {
        moveToNextSplit();
        continue;
      } 
      if (this.x2 - this.x1 == 0) {
        this.implicit.addEigenAt(this.x1);
        this.x2--;
        this.indexVal++;
        foundEigen = true;
        continue;
      } 
      if (this.x2 - this.x1 == 1 && !this.implicit.isReal2x2(this.x1, this.x2)) {
        this.implicit.addComputedEigen2x2(this.x1, this.x2);
        this.x2 -= 2;
        this.indexVal += 2;
        foundEigen = true;
        continue;
      } 
      if (this.implicit.steps - this.implicit.lastExceptional > this.implicit.exceptionalThreshold) {
        this.implicit.exceptionalShift(this.x1, this.x2);
        this.implicit.lastExceptional = this.implicit.steps;
        continue;
      } 
      if (this.implicit.isZero(this.x2, this.x2 - 1)) {
        this.implicit.addEigenAt(this.x2);
        foundEigen = true;
        this.x2--;
        this.indexVal++;
        continue;
      } 
      checkSplitPerformImplicit();
    } 
    return foundEigen;
  }
  
  private void checkSplitPerformImplicit() {
    for (int i = this.x2; i > this.x1; i--) {
      if (this.implicit.isZero(i, i - 1)) {
        this.x1 = i;
        this.splits[this.numSplits++] = i - 1;
        return;
      } 
    } 
    if (this.onscript) {
      if (this.implicit.steps > this.implicit.exceptionalThreshold / 2) {
        this.onscript = false;
      } else {
        Complex64F a = this.origEigenvalues[this.indexVal];
        if (a.isReal()) {
          this.implicit.performImplicitSingleStep(this.x1, this.x2, a.getReal());
        } else if (this.x2 < this.N - 2) {
          this.implicit.performImplicitDoubleStep(this.x1, this.x2, a.real, a.imaginary);
        } else {
          this.onscript = false;
        } 
      } 
    } else if (this.x2 - this.x1 >= 1 && this.x2 < this.N - 2) {
      this.implicit.implicitDoubleStep(this.x1, this.x2);
    } else {
      this.implicit.performImplicitSingleStep(this.x1, this.x2, this.implicit.A.get(this.x2, this.x2));
    } 
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
  
  public DenseMatrix64F getQ() {
    return this.Q;
  }
  
  public WatchedDoubleStepQREigen getImplicit() {
    return this.implicit;
  }
  
  public DenseMatrix64F[] getEigenvectors() {
    return this.eigenvectors;
  }
  
  public Complex64F[] getEigenvalues() {
    return this.implicit.eigenvalues;
  }
}
