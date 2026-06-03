package ic2.shades.org.ejml.alg.dense.decomposition.hessenberg;

import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class TridiagonalDecompositionHouseholder_D64 implements TridiagonalSimilarDecomposition<DenseMatrix64F> {
  private int N = 1;
  
  private double[] w = new double[this.N];
  
  private double[] b = new double[this.N];
  
  private double[] gammas = new double[this.N];
  
  private DenseMatrix64F QT;
  
  public DenseMatrix64F getQT() {
    return this.QT;
  }
  
  public void getDiagonal(double[] diag, double[] off) {
    for (int i = 0; i < this.N; i++) {
      diag[i] = this.QT.data[i * this.N + i];
      if (i + 1 < this.N)
        off[i] = this.QT.data[i * this.N + i + 1]; 
    } 
  }
  
  public DenseMatrix64F getT(DenseMatrix64F T) {
    if (T == null) {
      T = new DenseMatrix64F(this.N, this.N);
    } else {
      if (this.N != T.numRows || this.N != T.numCols)
        throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix."); 
      T.zero();
    } 
    T.data[0] = this.QT.data[0];
    for (int i = 1; i < this.N; i++) {
      T.set(i, i, this.QT.get(i, i));
      double a = this.QT.get(i - 1, i);
      T.set(i - 1, i, a);
      T.set(i, i - 1, a);
    } 
    if (this.N > 1) {
      T.data[(this.N - 1) * this.N + this.N - 1] = this.QT.data[(this.N - 1) * this.N + this.N - 1];
      T.data[(this.N - 1) * this.N + this.N - 2] = this.QT.data[(this.N - 2) * this.N + this.N - 1];
    } 
    return T;
  }
  
  public DenseMatrix64F getQ(DenseMatrix64F Q, boolean transposed) {
    if (Q == null) {
      Q = CommonOps.identity(this.N);
    } else {
      if (this.N != Q.numRows || this.N != Q.numCols)
        throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix."); 
      CommonOps.setIdentity((RowD1Matrix64F)Q);
    } 
    for (int i = 0; i < this.N; ) {
      this.w[i] = 0.0D;
      i++;
    } 
    if (transposed) {
      for (int j = this.N - 2; j >= 0; j--) {
        this.w[j + 1] = 1.0D;
        for (int k = j + 2; k < this.N; k++)
          this.w[k] = this.QT.data[j * this.N + k]; 
        QrHelperFunctions.rank1UpdateMultL(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N);
      } 
    } else {
      for (int j = this.N - 2; j >= 0; j--) {
        this.w[j + 1] = 1.0D;
        for (int k = j + 2; k < this.N; k++)
          this.w[k] = this.QT.get(j, k); 
        QrHelperFunctions.rank1UpdateMultR(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N, this.b);
      } 
    } 
    return Q;
  }
  
  public boolean decompose(DenseMatrix64F A) {
    init(A);
    for (int k = 1; k < this.N; k++)
      similarTransform(k); 
    return true;
  }
  
  private void similarTransform(int k) {
    double[] t = this.QT.data;
    double max = 0.0D;
    int rowU = (k - 1) * this.N;
    for (int i = k; i < this.N; i++) {
      double val = Math.abs(t[rowU + i]);
      if (val > max)
        max = val; 
    } 
    if (max > 0.0D) {
      double tau = QrHelperFunctions.computeTauAndDivide(k, this.N, t, rowU, max);
      double nu = t[rowU + k] + tau;
      QrHelperFunctions.divideElements(k + 1, this.N, t, rowU, nu);
      t[rowU + k] = 1.0D;
      double gamma = nu / tau;
      this.gammas[k] = gamma;
      householderSymmetric(k, gamma);
      t[rowU + k] = -tau * max;
    } else {
      this.gammas[k] = 0.0D;
    } 
  }
  
  public void householderSymmetric(int row, double gamma) {
    int startU = (row - 1) * this.N;
    for (int i = row; i < this.N; i++) {
      double total = 0.0D;
      int k;
      for (k = row; k < i; k++)
        total += this.QT.data[k * this.N + i] * this.QT.data[startU + k]; 
      for (k = i; k < this.N; k++)
        total += this.QT.data[i * this.N + k] * this.QT.data[startU + k]; 
      this.w[i] = -gamma * total;
    } 
    double alpha = 0.0D;
    int j;
    for (j = row; j < this.N; j++)
      alpha += this.QT.data[startU + j] * this.w[j]; 
    alpha *= -0.5D * gamma;
    for (j = row; j < this.N; j++)
      this.w[j] = this.w[j] + alpha * this.QT.data[startU + j]; 
    for (j = row; j < this.N; j++) {
      double ww = this.w[j];
      double uu = this.QT.data[startU + j];
      int rowA = j * this.N;
      for (int k = j; k < this.N; k++)
        this.QT.data[rowA + k] = this.QT.data[rowA + k] + ww * this.QT.data[startU + k] + this.w[k] * uu; 
    } 
  }
  
  public void init(DenseMatrix64F A) {
    if (A.numRows != A.numCols)
      throw new IllegalArgumentException("Must be square"); 
    if (A.numCols != this.N) {
      this.N = A.numCols;
      if (this.w.length < this.N) {
        this.w = new double[this.N];
        this.gammas = new double[this.N];
        this.b = new double[this.N];
      } 
    } 
    this.QT = A;
  }
  
  public boolean inputModified() {
    return true;
  }
}
