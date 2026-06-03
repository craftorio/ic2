package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyLDLDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class CholeskyDecompositionLDL_D64 implements CholeskyLDLDecomposition<DenseMatrix64F> {
  private int maxWidth;
  
  private int n;
  
  private DenseMatrix64F L;
  
  private double[] el;
  
  private double[] d;
  
  double[] vv;
  
  public void setExpectedMaxSize(int numRows, int numCols) {
    if (numRows != numCols)
      throw new IllegalArgumentException("Can only decompose square matrices"); 
    this.maxWidth = numRows;
    this.L = new DenseMatrix64F(this.maxWidth, this.maxWidth);
    this.el = this.L.data;
    this.vv = new double[this.maxWidth];
    this.d = new double[this.maxWidth];
  }
  
  public boolean decompose(DenseMatrix64F mat) {
    if (mat.numRows > this.maxWidth) {
      setExpectedMaxSize(mat.numRows, mat.numCols);
    } else if (mat.numRows != mat.numCols) {
      throw new RuntimeException("Can only decompose square matrices");
    } 
    this.n = mat.numRows;
    this.L.setReshape(mat);
    double d_inv = 0.0D;
    int i;
    for (i = 0; i < this.n; i++) {
      for (int j = i; j < this.n; j++) {
        double sum = this.el[i * this.n + j];
        for (int k = 0; k < i; k++)
          sum -= this.el[i * this.n + k] * this.el[j * this.n + k] * this.d[k]; 
        if (i == j) {
          if (sum <= 0.0D)
            return false; 
          this.d[i] = sum;
          d_inv = 1.0D / sum;
          this.el[i * this.n + i] = 1.0D;
        } else {
          this.el[j * this.n + i] = sum * d_inv;
        } 
      } 
    } 
    for (i = 0; i < this.n; i++) {
      for (int j = i + 1; j < this.n; j++)
        this.el[i * this.n + j] = 0.0D; 
    } 
    return true;
  }
  
  public boolean inputModified() {
    return false;
  }
  
  public double[] getDiagonal() {
    return this.d;
  }
  
  public DenseMatrix64F getL() {
    return this.L;
  }
  
  public double[] _getVV() {
    return this.vv;
  }
  
  public DenseMatrix64F getL(DenseMatrix64F L) {
    if (L == null) {
      L = this.L.copy();
    } else {
      L.set((D1Matrix64F)this.L);
    } 
    return L;
  }
  
  public DenseMatrix64F getD(DenseMatrix64F D) {
    return CommonOps.diag(D, this.L.numCols, this.d);
  }
}
