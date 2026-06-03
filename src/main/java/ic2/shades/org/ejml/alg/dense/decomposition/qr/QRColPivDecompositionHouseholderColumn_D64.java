package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class QRColPivDecompositionHouseholderColumn_D64 extends QRDecompositionHouseholderColumn_D64 implements QRPDecomposition<DenseMatrix64F> {
  protected int[] pivots;
  
  protected double[] normsCol;
  
  protected double maxAbs;
  
  protected double singularThreshold = UtilEjml.EPS;
  
  protected int rank;
  
  public QRColPivDecompositionHouseholderColumn_D64(double singularThreshold) {
    this.singularThreshold = singularThreshold;
  }
  
  public void setSingularThreshold(double threshold) {
    this.singularThreshold = threshold;
  }
  
  public void setExpectedMaxSize(int numRows, int numCols) {
    super.setExpectedMaxSize(numRows, numCols);
    if (this.pivots == null || this.pivots.length < numCols) {
      this.pivots = new int[numCols];
      this.normsCol = new double[numCols];
    } 
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
    for (int j = this.rank - 1; j >= 0; j--) {
      double[] u = this.dataQR[j];
      double vv = u[j];
      u[j] = 1.0D;
      QrHelperFunctions.rank1UpdateMultR(Q, u, this.gammas[j], j, j, this.numRows, this.v);
      u[j] = vv;
    } 
    return Q;
  }
  
  public boolean decompose(DenseMatrix64F A) {
    setExpectedMaxSize(A.numRows, A.numCols);
    convertToColumnMajor(A);
    this.maxAbs = CommonOps.elementMaxAbs((D1Matrix64F)A);
    setupPivotInfo();
    for (int j = 0; j < this.minLength; j++) {
      if (j > 0)
        updateNorms(j); 
      swapColumns(j);
      if (!householderPivot(j))
        break; 
      updateA(j);
      this.rank = j + 1;
    } 
    return true;
  }
  
  private void setupPivotInfo() {
    for (int col = 0; col < this.numCols; col++) {
      this.pivots[col] = col;
      double[] c = this.dataQR[col];
      double norm = 0.0D;
      for (int row = 0; row < this.numRows; row++) {
        double element = c[row];
        norm += element * element;
      } 
      this.normsCol[col] = norm;
    } 
  }
  
  private void updateNorms(int j) {
    boolean foundNegative = false;
    int col;
    for (col = j; col < this.numCols; col++) {
      double e = this.dataQR[col][j - 1];
      this.normsCol[col] = this.normsCol[col] - e * e;
      if (this.normsCol[col] < 0.0D) {
        foundNegative = true;
        break;
      } 
    } 
    if (foundNegative)
      for (col = j; col < this.numCols; col++) {
        double[] u = this.dataQR[col];
        double actual = 0.0D;
        for (int i = j; i < this.numRows; i++) {
          double v = u[i];
          actual += v * v;
        } 
        this.normsCol[col] = actual;
      }  
  }
  
  private void swapColumns(int j) {
    int largestIndex = j;
    double largestNorm = this.normsCol[j];
    for (int col = j + 1; col < this.numCols; col++) {
      double n = this.normsCol[col];
      if (n > largestNorm) {
        largestNorm = n;
        largestIndex = col;
      } 
    } 
    double[] tempC = this.dataQR[j];
    this.dataQR[j] = this.dataQR[largestIndex];
    this.dataQR[largestIndex] = tempC;
    double tempN = this.normsCol[j];
    this.normsCol[j] = this.normsCol[largestIndex];
    this.normsCol[largestIndex] = tempN;
    int tempP = this.pivots[j];
    this.pivots[j] = this.pivots[largestIndex];
    this.pivots[largestIndex] = tempP;
  }
  
  protected boolean householderPivot(int j) {
    double[] u = this.dataQR[j];
    double max = QrHelperFunctions.findMax(u, j, this.numRows - j);
    if (max <= 0.0D)
      return false; 
    this.tau = QrHelperFunctions.computeTauAndDivide(j, this.numRows, u, max);
    double u_0 = u[j] + this.tau;
    QrHelperFunctions.divideElements(j + 1, this.numRows, u, u_0);
    this.gamma = u_0 / this.tau;
    this.tau *= max;
    u[j] = -this.tau;
    if (Math.abs(this.tau) <= this.singularThreshold)
      return false; 
    this.gammas[j] = this.gamma;
    return true;
  }
  
  public int getRank() {
    return this.rank;
  }
  
  public int[] getPivots() {
    return this.pivots;
  }
  
  public DenseMatrix64F getPivotMatrix(DenseMatrix64F P) {
    if (P == null) {
      P = new DenseMatrix64F(this.numCols, this.numCols);
    } else {
      if (P.numRows != this.numCols)
        throw new IllegalArgumentException("Number of rows must be " + this.numCols); 
      if (P.numCols != this.numCols)
        throw new IllegalArgumentException("Number of columns must be " + this.numCols); 
      P.zero();
    } 
    for (int i = 0; i < this.numCols; i++)
      P.set(this.pivots[i], i, 1.0D); 
    return P;
  }
  
  public QRColPivDecompositionHouseholderColumn_D64() {}
}
