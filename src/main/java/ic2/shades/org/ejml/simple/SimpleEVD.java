package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;

public class SimpleEVD<T extends SimpleMatrix> {
  private EigenDecomposition<DenseMatrix64F> eig;
  
  DenseMatrix64F mat;
  
  public SimpleEVD(DenseMatrix64F mat) {
    this.mat = mat;
    this.eig = DecompositionFactory.eig(mat.numCols, true);
    if (!this.eig.decompose((Matrix64F)mat))
      throw new RuntimeException("Eigenvalue Decomposition failed"); 
  }
  
  public int getNumberOfEigenvalues() {
    return this.eig.getNumberOfEigenvalues();
  }
  
  public Complex64F getEigenvalue(int index) {
    return this.eig.getEigenvalue(index);
  }
  
  public T getEigenVector(int index) {
    return (T)SimpleMatrix.wrap((DenseMatrix64F)this.eig.getEigenVector(index));
  }
  
  public double quality() {
    return DecompositionFactory.quality(this.mat, this.eig);
  }
  
  public EigenDecomposition getEVD() {
    return this.eig;
  }
  
  public int getIndexMax() {
    int indexMax = 0;
    double max = getEigenvalue(0).getMagnitude2();
    int N = getNumberOfEigenvalues();
    for (int i = 1; i < N; i++) {
      double m = getEigenvalue(i).getMagnitude2();
      if (m > max) {
        max = m;
        indexMax = i;
      } 
    } 
    return indexMax;
  }
  
  public int getIndexMin() {
    int indexMin = 0;
    double min = getEigenvalue(0).getMagnitude2();
    int N = getNumberOfEigenvalues();
    for (int i = 1; i < N; i++) {
      double m = getEigenvalue(i).getMagnitude2();
      if (m < min) {
        min = m;
        indexMin = i;
      } 
    } 
    return indexMin;
  }
}
