package ic2.shades.org.ejml.alg.dense.decomposition.svd;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class SafeSvd implements SingularValueDecomposition<DenseMatrix64F> {
  SingularValueDecomposition<DenseMatrix64F> alg;
  
  DenseMatrix64F work = new DenseMatrix64F(1, 1);
  
  public SafeSvd(SingularValueDecomposition<DenseMatrix64F> alg) {
    this.alg = alg;
  }
  
  public double[] getSingularValues() {
    return this.alg.getSingularValues();
  }
  
  public int numberOfSingularValues() {
    return this.alg.numberOfSingularValues();
  }
  
  public boolean isCompact() {
    return this.alg.isCompact();
  }
  
  public DenseMatrix64F getU(DenseMatrix64F U, boolean transposed) {
    return (DenseMatrix64F)this.alg.getU((Matrix64F)U, transposed);
  }
  
  public DenseMatrix64F getV(DenseMatrix64F V, boolean transposed) {
    return (DenseMatrix64F)this.alg.getV((Matrix64F)V, transposed);
  }
  
  public DenseMatrix64F getW(DenseMatrix64F W) {
    return (DenseMatrix64F)this.alg.getW((Matrix64F)W);
  }
  
  public int numRows() {
    return this.alg.numRows();
  }
  
  public int numCols() {
    return this.alg.numCols();
  }
  
  public boolean decompose(DenseMatrix64F orig) {
    if (this.alg.inputModified()) {
      this.work.reshape(orig.numRows, orig.numCols);
      this.work.set((D1Matrix64F)orig);
      return this.alg.decompose((Matrix64F)this.work);
    } 
    return this.alg.decompose((Matrix64F)orig);
  }
  
  public boolean inputModified() {
    return false;
  }
}
