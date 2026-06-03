package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.ops.MatrixFeatures;

public class SwitchingEigenDecomposition implements EigenDecomposition<DenseMatrix64F> {
  private double tol;
  
  EigenDecomposition<DenseMatrix64F> symmetricAlg;
  
  EigenDecomposition<DenseMatrix64F> generalAlg;
  
  boolean symmetric;
  
  boolean computeVectors;
  
  DenseMatrix64F A = new DenseMatrix64F(1, 1);
  
  public SwitchingEigenDecomposition(int matrixSize, boolean computeVectors, double tol) {
    this.symmetricAlg = DecompositionFactory.eig(matrixSize, computeVectors, true);
    this.generalAlg = DecompositionFactory.eig(matrixSize, computeVectors, false);
    this.computeVectors = computeVectors;
    this.tol = tol;
  }
  
  public SwitchingEigenDecomposition(int matrixSize) {
    this(matrixSize, true, 1.0E-8D);
  }
  
  public int getNumberOfEigenvalues() {
    return this.symmetric ? this.symmetricAlg.getNumberOfEigenvalues() : this.generalAlg.getNumberOfEigenvalues();
  }
  
  public Complex64F getEigenvalue(int index) {
    return this.symmetric ? this.symmetricAlg.getEigenvalue(index) : this.generalAlg.getEigenvalue(index);
  }
  
  public DenseMatrix64F getEigenVector(int index) {
    if (!this.computeVectors)
      throw new IllegalArgumentException("Configured to not compute eignevectors"); 
    return this.symmetric ? (DenseMatrix64F)this.symmetricAlg.getEigenVector(index) : (DenseMatrix64F)this.generalAlg.getEigenVector(index);
  }
  
  public boolean decompose(DenseMatrix64F orig) {
    this.A.setReshape(orig);
    this.symmetric = MatrixFeatures.isSymmetric(this.A, this.tol);
    return this.symmetric ? this.symmetricAlg.decompose((Matrix64F)this.A) : this.generalAlg.decompose((Matrix64F)this.A);
  }
  
  public boolean inputModified() {
    return false;
  }
}
