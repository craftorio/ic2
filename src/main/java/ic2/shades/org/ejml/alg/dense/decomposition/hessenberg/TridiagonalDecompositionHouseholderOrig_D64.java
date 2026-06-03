package ic2.shades.org.ejml.alg.dense.decomposition.hessenberg;

import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;

public class TridiagonalDecompositionHouseholderOrig_D64 {
  int N = 1;
  
  DenseMatrix64F QT = new DenseMatrix64F(this.N, this.N);
  
  double[] w = new double[this.N];
  
  double[] b = new double[this.N];
  
  double[] gammas = new double[this.N];
  
  public DenseMatrix64F getQT() {
    return this.QT;
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
    T.data[1] = this.QT.data[1];
    for (int i = 1; i < this.N - 1; i++) {
      T.set(i, i, this.QT.get(i, i));
      T.set(i, i + 1, this.QT.get(i, i + 1));
      T.set(i, i - 1, this.QT.get(i - 1, i));
    } 
    T.data[(this.N - 1) * this.N + this.N - 1] = this.QT.data[(this.N - 1) * this.N + this.N - 1];
    T.data[(this.N - 1) * this.N + this.N - 2] = this.QT.data[(this.N - 2) * this.N + this.N - 1];
    return T;
  }
  
  public DenseMatrix64F getQ(DenseMatrix64F Q) {
    if (Q == null) {
      Q = new DenseMatrix64F(this.N, this.N);
      for (int k = 0; k < this.N; k++)
        Q.data[k * this.N + k] = 1.0D; 
    } else {
      if (this.N != Q.numRows || this.N != Q.numCols)
        throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix."); 
      CommonOps.setIdentity((RowD1Matrix64F)Q);
    } 
    for (int i = 0; i < this.N; ) {
      this.w[i] = 0.0D;
      i++;
    } 
    for (int j = this.N - 2; j >= 0; j--) {
      this.w[j + 1] = 1.0D;
      for (int k = j + 2; k < this.N; k++)
        this.w[k] = this.QT.get(j, k); 
      QrHelperFunctions.rank1UpdateMultR(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N, this.b);
    } 
    return Q;
  }
  
  public void decompose(DenseMatrix64F A) {
    init(A);
    for (int k = 1; k < this.N; k++)
      similarTransform(k); 
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
      double tau = 0.0D;
      for (int j = k; j < this.N; j++) {
        double val = t[rowU + j] = t[rowU + j] / max;
        tau += val * val;
      } 
      tau = Math.sqrt(tau);
      if (t[rowU + k] < 0.0D)
        tau = -tau; 
      double nu = t[rowU + k] + tau;
      t[rowU + k] = 1.0D;
      for (int m = k + 1; m < this.N; m++)
        t[rowU + m] = t[rowU + m] / nu; 
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
      for (int k = row; k < this.N; k++)
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
      for (int k = j; k < this.N; k++) {
        this.QT.data[j * this.N + k] = this.QT.data[j * this.N + k] + ww * this.QT.data[startU + k] + this.w[k] * uu;
        this.QT.data[k * this.N + j] = this.QT.data[j * this.N + k] + ww * this.QT.data[startU + k] + this.w[k] * uu;
      } 
    } 
  }
  
  public void init(DenseMatrix64F A) {
    if (A.numRows != A.numCols)
      throw new IllegalArgumentException("Must be square"); 
    if (A.numCols != this.N) {
      this.N = A.numCols;
      this.QT.reshape(this.N, this.N, false);
      if (this.w.length < this.N) {
        this.w = new double[this.N];
        this.gammas = new double[this.N];
        this.b = new double[this.N];
      } 
    } 
    this.QT.set((D1Matrix64F)A);
  }
  
  public double getGamma(int index) {
    return this.gammas[index];
  }
}
