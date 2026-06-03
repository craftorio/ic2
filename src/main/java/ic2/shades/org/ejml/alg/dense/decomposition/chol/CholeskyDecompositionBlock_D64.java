package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class CholeskyDecompositionBlock_D64 extends CholeskyDecompositionCommon_D64 {
  private int blockWidth;
  
  private DenseMatrix64F B;
  
  private CholeskyBlockHelper_D64 chol;
  
  public CholeskyDecompositionBlock_D64(int blockWidth) {
    super(true);
    this.blockWidth = blockWidth;
  }
  
  public void setExpectedMaxSize(int numRows, int numCols) {
    super.setExpectedMaxSize(numRows, numCols);
    if (numRows < this.blockWidth) {
      this.B = new DenseMatrix64F(0, 0);
    } else {
      this.B = new DenseMatrix64F(this.blockWidth, this.maxWidth);
    } 
    this.chol = new CholeskyBlockHelper_D64(this.blockWidth);
  }
  
  protected boolean decomposeLower() {
    if (this.n < this.blockWidth) {
      this.B.reshape(0, 0, false);
    } else {
      this.B.reshape(this.blockWidth, this.n - this.blockWidth, false);
    } 
    int numBlocks = this.n / this.blockWidth;
    int remainder = this.n % this.blockWidth;
    if (remainder > 0)
      numBlocks++; 
    this.B.numCols = this.n;
    int i;
    for (i = 0; i < numBlocks; i++) {
      this.B.numCols -= this.blockWidth;
      if (this.B.numCols > 0) {
        if (!this.chol.decompose(this.T, i * this.blockWidth * this.T.numCols + i * this.blockWidth, this.blockWidth))
          return false; 
        int indexSrc = i * this.blockWidth * this.T.numCols + (i + 1) * this.blockWidth;
        int indexDst = (i + 1) * this.blockWidth * this.T.numCols + i * this.blockWidth;
        solveL_special((this.chol.getL()).data, this.T, indexSrc, indexDst, this.B);
        int indexL = (i + 1) * this.blockWidth * this.n + (i + 1) * this.blockWidth;
        symmRankTranA_sub(this.B, this.T, indexL);
      } else {
        int width = (remainder > 0) ? remainder : this.blockWidth;
        if (!this.chol.decompose(this.T, i * this.blockWidth * this.T.numCols + i * this.blockWidth, width))
          return false; 
      } 
    } 
    for (i = 0; i < this.n; i++) {
      for (int j = i + 1; j < this.n; j++)
        this.t[i * this.n + j] = 0.0D; 
    } 
    return true;
  }
  
  protected boolean decomposeUpper() {
    throw new RuntimeException("Not implemented.  Do a lower decomposition and transpose it...");
  }
  
  public static void solveL_special(double[] L, DenseMatrix64F b_src, int indexSrc, int indexDst, DenseMatrix64F B) {
    double[] dataSrc = b_src.data;
    double[] b = B.data;
    int m = B.numRows;
    int n = B.numCols;
    int widthL = m;
    for (int j = 0; j < n; j++) {
      int indexb = j;
      int rowL = 0;
      for (int i = 0; i < widthL; i++, indexb += n, rowL += widthL) {
        double sum = dataSrc[indexSrc + i * b_src.numCols + j];
        int indexL = rowL;
        int endL = indexL + i;
        int indexB = j;
        for (; indexL != endL; indexB += n)
          sum -= L[indexL++] * b[indexB]; 
        double val = sum / L[i * widthL + i];
        dataSrc[indexDst + j * b_src.numCols + i] = val;
        b[indexb] = val;
      } 
    } 
  }
  
  public static void symmRankTranA_sub(DenseMatrix64F a, DenseMatrix64F c, int startIndexC) {
    double[] dataA = a.data;
    double[] dataC = c.data;
    int strideC = c.numCols + 1;
    for (int i = 0; i < a.numCols; i++) {
      int indexA = i;
      int endR = a.numCols;
      for (int k = 0; k < a.numRows; k++, indexA += a.numCols, endR += a.numCols) {
        int indexC = startIndexC;
        double valA = dataA[indexA];
        int indexR = indexA;
        while (indexR < endR)
          dataC[indexC++] = dataC[indexC++] - valA * dataA[indexR++]; 
      } 
      startIndexC += strideC;
    } 
  }
}
