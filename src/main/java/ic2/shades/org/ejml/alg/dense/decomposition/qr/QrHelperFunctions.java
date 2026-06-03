package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class QrHelperFunctions {
  public static double findMax(double[] u, int startU, int length) {
    double max = -1.0D;
    int index = startU;
    int stopIndex = startU + length;
    for (; index < stopIndex; index++) {
      double val = u[index];
      val = (val < 0.0D) ? -val : val;
      if (val > max)
        max = val; 
    } 
    return max;
  }
  
  public static void divideElements(int j, int numRows, double[] u, double u_0) {
    for (int i = j; i < numRows; i++)
      u[i] = u[i] / u_0; 
  }
  
  public static void divideElements(int j, int numRows, double[] u, int startU, double u_0) {
    for (int i = j; i < numRows; i++)
      u[i + startU] = u[i + startU] / u_0; 
  }
  
  public static void divideElements_Brow(int j, int numRows, double[] u, double[] b, int startB, double u_0) {
    for (int i = j; i < numRows; i++) {
      b[i + startB] = b[i + startB] / u_0;
      u[i] = b[i + startB] / u_0;
    } 
  }
  
  public static void divideElements_Bcol(int j, int numRows, int numCols, double[] u, double[] b, int startB, double u_0) {
    int indexB = j * numCols + startB;
    for (int i = j; i < numRows; i++, indexB += numCols) {
      u[i] = u[i] / u_0;
      b[indexB] = u[i] / u_0;
    } 
  }
  
  public static double computeTauAndDivide(int j, int numRows, double[] u, int startU, double max) {
    double tau = 0.0D;
    for (int i = j; i < numRows; i++) {
      double d = u[startU + i] = u[startU + i] / max;
      tau += d * d;
    } 
    tau = Math.sqrt(tau);
    if (u[startU + j] < 0.0D)
      tau = -tau; 
    return tau;
  }
  
  public static double computeTauAndDivide(int j, int numRows, double[] u, double max) {
    double tau = 0.0D;
    for (int i = j; i < numRows; i++) {
      double d = u[i] = u[i] / max;
      tau += d * d;
    } 
    tau = Math.sqrt(tau);
    if (u[j] < 0.0D)
      tau = -tau; 
    return tau;
  }
  
  public static void rank1UpdateMultR(DenseMatrix64F A, double[] u, double gamma, int colA0, int w0, int w1, double[] _temp) {
    for (int j = colA0; j < A.numCols; j++)
      _temp[j] = u[w0] * A.data[w0 * A.numCols + j]; 
    for (int k = w0 + 1; k < w1; k++) {
      int indexA = k * A.numCols + colA0;
      double valU = u[k];
      for (int m = colA0; m < A.numCols; m++)
        _temp[m] = _temp[m] + valU * A.data[indexA++]; 
    } 
    int i;
    for (i = colA0; i < A.numCols; i++)
      _temp[i] = _temp[i] * gamma; 
    for (i = w0; i < w1; i++) {
      double valU = u[i];
      int indexA = i * A.numCols + colA0;
      for (int m = colA0; m < A.numCols; m++)
        A.data[indexA++] = A.data[indexA++] - valU * _temp[m]; 
    } 
  }
  
  public static void rank1UpdateMultR(DenseMatrix64F A, double[] u, int offsetU, double gamma, int colA0, int w0, int w1, double[] _temp) {
    for (int j = colA0; j < A.numCols; j++)
      _temp[j] = u[w0 + offsetU] * A.data[w0 * A.numCols + j]; 
    for (int k = w0 + 1; k < w1; k++) {
      int indexA = k * A.numCols + colA0;
      double valU = u[k + offsetU];
      for (int m = colA0; m < A.numCols; m++)
        _temp[m] = _temp[m] + valU * A.data[indexA++]; 
    } 
    int i;
    for (i = colA0; i < A.numCols; i++)
      _temp[i] = _temp[i] * gamma; 
    for (i = w0; i < w1; i++) {
      double valU = u[i + offsetU];
      int indexA = i * A.numCols + colA0;
      for (int m = colA0; m < A.numCols; m++)
        A.data[indexA++] = A.data[indexA++] - valU * _temp[m]; 
    } 
  }
  
  public static void rank1UpdateMultL(DenseMatrix64F A, double[] u, double gamma, int colA0, int w0, int w1) {
    for (int i = colA0; i < A.numRows; i++) {
      int startIndex = i * A.numCols + w0;
      double sum = 0.0D;
      int rowIndex = startIndex;
      int j;
      for (j = w0; j < w1; j++)
        sum += A.data[rowIndex++] * u[j]; 
      sum = -gamma * sum;
      rowIndex = startIndex;
      for (j = w0; j < w1; j++)
        A.data[rowIndex++] = A.data[rowIndex++] + sum * u[j]; 
    } 
  }
}
