package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class QRDecompositionHouseholder_D64 implements QRDecomposition<DenseMatrix64F> {
  protected DenseMatrix64F QR;
  
  protected double[] u;
  
  protected double[] v;
  
  protected int numCols;
  
  protected int numRows;
  
  protected int minLength;
  
  protected double[] dataQR;
  
  protected double[] gammas;
  
  protected double gamma;
  
  protected double tau;
  
  protected boolean error;
  
  public void setExpectedMaxSize(int numRows, int numCols) {
    this.error = false;
    this.numCols = numCols;
    this.numRows = numRows;
    this.minLength = Math.min(numRows, numCols);
    int maxLength = Math.max(numRows, numCols);
    if (this.QR == null) {
      this.QR = new DenseMatrix64F(numRows, numCols);
      this.u = new double[maxLength];
      this.v = new double[maxLength];
      this.gammas = new double[this.minLength];
    } else {
      this.QR.reshape(numRows, numCols, false);
    } 
    this.dataQR = this.QR.data;
    if (this.u.length < maxLength) {
      this.u = new double[maxLength];
      this.v = new double[maxLength];
    } 
    if (this.gammas.length < this.minLength)
      this.gammas = new double[this.minLength]; 
  }
  
  public DenseMatrix64F getQR() {
    return this.QR;
  }
  
  public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact) {
    if (compact) {
      if (Q == null) {
        Q = CommonOps.identity(this.numRows, this.minLength);
      } else {
        if (Q.numRows != this.numRows || Q.numCols != this.minLength)
          throw new IllegalArgumentException("Unexpected matrix dimension."); 
        CommonOps.setIdentity((RowD1Matrix64F)Q);
      } 
    } else if (Q == null) {
      Q = CommonOps.identity(this.numRows);
    } else {
      if (Q.numRows != this.numRows || Q.numCols != this.numRows)
        throw new IllegalArgumentException("Unexpected matrix dimension."); 
      CommonOps.setIdentity((RowD1Matrix64F)Q);
    } 
    for (int j = this.minLength - 1; j >= 0; j--) {
      this.u[j] = 1.0D;
      for (int i = j + 1; i < this.numRows; i++)
        this.u[i] = this.QR.get(i, j); 
      QrHelperFunctions.rank1UpdateMultR(Q, this.u, this.gammas[j], j, j, this.numRows, this.v);
    } 
    return Q;
  }
  
  public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
    if (R == null) {
      if (compact) {
        R = new DenseMatrix64F(this.minLength, this.numCols);
      } else {
        R = new DenseMatrix64F(this.numRows, this.numCols);
      } 
    } else {
      if (compact) {
        if (R.numCols != this.numCols || R.numRows != this.minLength)
          throw new IllegalArgumentException("Unexpected dimensions"); 
      } else if (R.numCols != this.numCols || R.numRows != this.numRows) {
        throw new IllegalArgumentException("Unexpected dimensions");
      } 
      for (int j = 0; j < R.numRows; j++) {
        int min = Math.min(j, R.numCols);
        for (int k = 0; k < min; k++)
          R.set(j, k, 0.0D); 
      } 
    } 
    for (int i = 0; i < this.minLength; i++) {
      for (int j = i; j < this.numCols; j++) {
        double val = this.QR.get(i, j);
        R.set(i, j, val);
      } 
    } 
    return R;
  }
  
  public boolean decompose(DenseMatrix64F A) {
    commonSetup(A);
    for (int j = 0; j < this.minLength; j++) {
      householder(j);
      updateA(j);
    } 
    return !this.error;
  }
  
  public boolean inputModified() {
    return false;
  }
  
  protected void householder(int j) {
    int index = j + j * this.numCols;
    double max = 0.0D;
    int i;
    for (i = j; i < this.numRows; i++) {
      double d = this.u[i] = this.dataQR[index];
      if (d < 0.0D)
        d = -d; 
      if (max < d)
        max = d; 
      index += this.numCols;
    } 
    if (max == 0.0D) {
      this.gamma = 0.0D;
      this.error = true;
    } else {
      this.tau = 0.0D;
      for (i = j; i < this.numRows; i++) {
        this.u[i] = this.u[i] / max;
        double d = this.u[i];
        this.tau += d * d;
      } 
      this.tau = Math.sqrt(this.tau);
      if (this.u[j] < 0.0D)
        this.tau = -this.tau; 
      double u_0 = this.u[j] + this.tau;
      this.gamma = u_0 / this.tau;
      for (int k = j + 1; k < this.numRows; k++)
        this.u[k] = this.u[k] / u_0; 
      this.u[j] = 1.0D;
      this.tau *= max;
    } 
    this.gammas[j] = this.gamma;
  }
  
  protected void updateA(int w) {
    for (int j = w + 1; j < this.numCols; j++)
      this.v[j] = this.u[w] * this.dataQR[w * this.numCols + j]; 
    for (int k = w + 1; k < this.numRows; k++) {
      int indexQR = k * this.numCols + w + 1;
      for (int m = w + 1; m < this.numCols; m++)
        this.v[m] = this.v[m] + this.u[k] * this.dataQR[indexQR++]; 
    } 
    int i;
    for (i = w + 1; i < this.numCols; i++)
      this.v[i] = this.v[i] * this.gamma; 
    for (i = w; i < this.numRows; i++) {
      double valU = this.u[i];
      int indexQR = i * this.numCols + w + 1;
      for (int m = w + 1; m < this.numCols; m++)
        this.dataQR[indexQR++] = this.dataQR[indexQR++] - valU * this.v[m]; 
    } 
    if (w < this.numCols)
      this.dataQR[w + w * this.numCols] = -this.tau; 
    for (i = w + 1; i < this.numRows; i++)
      this.dataQR[w + i * this.numCols] = this.u[i]; 
  }
  
  protected void commonSetup(DenseMatrix64F A) {
    setExpectedMaxSize(A.numRows, A.numCols);
    this.QR.set((D1Matrix64F)A);
  }
  
  public double[] getGammas() {
    return this.gammas;
  }
}
