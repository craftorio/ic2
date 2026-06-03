package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class QRDecompositionHouseholderTran_D64 implements QRDecomposition<DenseMatrix64F> {
  protected DenseMatrix64F QR;
  
  protected double[] v;
  
  protected int numCols;
  
  protected int numRows;
  
  protected int minLength;
  
  protected double[] gammas;
  
  protected double gamma;
  
  protected double tau;
  
  protected boolean error;
  
  public void setExpectedMaxSize(int numRows, int numCols) {
    this.numCols = numCols;
    this.numRows = numRows;
    this.minLength = Math.min(numCols, numRows);
    int maxLength = Math.max(numCols, numRows);
    if (this.QR == null) {
      this.QR = new DenseMatrix64F(numCols, numRows);
      this.v = new double[maxLength];
      this.gammas = new double[this.minLength];
    } else {
      this.QR.reshape(numCols, numRows, false);
    } 
    if (this.v.length < maxLength)
      this.v = new double[maxLength]; 
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
      int diagIndex = j * this.numRows + j;
      double before = this.QR.data[diagIndex];
      this.QR.data[diagIndex] = 1.0D;
      QrHelperFunctions.rank1UpdateMultR(Q, this.QR.data, j * this.numRows, this.gammas[j], j, j, this.numRows, this.v);
      this.QR.data[diagIndex] = before;
    } 
    return Q;
  }
  
  public void applyQ(DenseMatrix64F A) {
    if (A.numRows != this.numRows)
      throw new IllegalArgumentException("A must have at least " + this.numRows + " rows."); 
    for (int j = this.minLength - 1; j >= 0; j--) {
      int diagIndex = j * this.numRows + j;
      double before = this.QR.data[diagIndex];
      this.QR.data[diagIndex] = 1.0D;
      QrHelperFunctions.rank1UpdateMultR(A, this.QR.data, j * this.numRows, this.gammas[j], 0, j, this.numRows, this.v);
      this.QR.data[diagIndex] = before;
    } 
  }
  
  public void applyTranQ(DenseMatrix64F A) {
    for (int j = 0; j < this.minLength; j++) {
      int diagIndex = j * this.numRows + j;
      double before = this.QR.data[diagIndex];
      this.QR.data[diagIndex] = 1.0D;
      QrHelperFunctions.rank1UpdateMultR(A, this.QR.data, j * this.numRows, this.gammas[j], 0, j, this.numRows, this.v);
      this.QR.data[diagIndex] = before;
    } 
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
          R.unsafe_set(j, k, 0.0D); 
      } 
    } 
    for (int i = 0; i < R.numRows; i++) {
      for (int j = i; j < R.numCols; j++)
        R.unsafe_set(i, j, this.QR.unsafe_get(j, i)); 
    } 
    return R;
  }
  
  public boolean decompose(DenseMatrix64F A) {
    setExpectedMaxSize(A.numRows, A.numCols);
    CommonOps.transpose(A, this.QR);
    this.error = false;
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
    int startQR = j * this.numRows;
    int endQR = startQR + this.numRows;
    startQR += j;
    double max = QrHelperFunctions.findMax(this.QR.data, startQR, this.numRows - j);
    if (max == 0.0D) {
      this.gamma = 0.0D;
      this.error = true;
    } else {
      this.tau = QrHelperFunctions.computeTauAndDivide(startQR, endQR, this.QR.data, max);
      double u_0 = this.QR.data[startQR] + this.tau;
      QrHelperFunctions.divideElements(startQR + 1, endQR, this.QR.data, u_0);
      this.gamma = u_0 / this.tau;
      this.tau *= max;
      this.QR.data[startQR] = -this.tau;
    } 
    this.gammas[j] = this.gamma;
  }
  
  protected void updateA(int w) {
    double[] data = this.QR.data;
    int rowW = w * this.numRows + w + 1;
    int rowJ = rowW + this.numRows;
    int rowJEnd = rowJ + (this.numCols - w - 1) * this.numRows;
    int indexWEnd = rowW + this.numRows - w - 1;
    for (; rowJEnd != rowJ; rowJ += this.numRows) {
      double val = data[rowJ - 1];
      int indexW = rowW;
      int indexJ = rowJ;
      while (indexW != indexWEnd)
        val += data[indexW++] * data[indexJ++]; 
      val *= this.gamma;
      data[rowJ - 1] = data[rowJ - 1] - val;
      indexW = rowW;
      indexJ = rowJ;
      while (indexW != indexWEnd)
        data[indexJ++] = data[indexJ++] - data[indexW++] * val; 
    } 
  }
  
  public double[] getGammas() {
    return this.gammas;
  }
}
