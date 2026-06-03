package ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal;

import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.BidiagonalDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class BidiagonalDecompositionRow_D64 implements BidiagonalDecomposition<DenseMatrix64F> {
  private DenseMatrix64F UBV;
  
  private int m;
  
  private int n;
  
  private int min;
  
  private double[] gammasU;
  
  private double[] gammasV;
  
  private double[] b;
  
  private double[] u;
  
  public BidiagonalDecompositionRow_D64(int numElements) {
    this.UBV = new DenseMatrix64F(numElements);
    this.gammasU = new double[numElements];
    this.gammasV = new double[numElements];
    this.b = new double[numElements];
    this.u = new double[numElements];
  }
  
  public BidiagonalDecompositionRow_D64() {
    this(1);
  }
  
  public boolean decompose(DenseMatrix64F A) {
    init(A);
    return _decompose();
  }
  
  protected void init(DenseMatrix64F A) {
    this.UBV = A;
    this.m = this.UBV.numRows;
    this.n = this.UBV.numCols;
    this.min = Math.min(this.m, this.n);
    int max = Math.max(this.m, this.n);
    if (this.b.length < max + 1) {
      this.b = new double[max + 1];
      this.u = new double[max + 1];
    } 
    if (this.gammasU.length < this.m)
      this.gammasU = new double[this.m]; 
    if (this.gammasV.length < this.n)
      this.gammasV = new double[this.n]; 
  }
  
  public DenseMatrix64F getUBV() {
    return this.UBV;
  }
  
  public void getDiagonal(double[] diag, double[] off) {
    diag[0] = this.UBV.get(0);
    for (int i = 1; i < this.n; i++) {
      diag[i] = this.UBV.unsafe_get(i, i);
      off[i - 1] = this.UBV.unsafe_get(i - 1, i);
    } 
  }
  
  public DenseMatrix64F getB(DenseMatrix64F B, boolean compact) {
    B = handleB(B, compact, this.m, this.n, this.min);
    B.set(0, 0, this.UBV.get(0, 0));
    for (int i = 1; i < this.min; i++) {
      B.set(i, i, this.UBV.get(i, i));
      B.set(i - 1, i, this.UBV.get(i - 1, i));
    } 
    if (this.n > this.m)
      B.set(this.min - 1, this.min, this.UBV.get(this.min - 1, this.min)); 
    return B;
  }
  
  public static DenseMatrix64F handleB(DenseMatrix64F B, boolean compact, int m, int n, int min) {
    int w = (n > m) ? (min + 1) : min;
    if (compact) {
      if (B == null) {
        B = new DenseMatrix64F(min, w);
      } else {
        B.reshape(min, w, false);
        B.zero();
      } 
    } else if (B == null) {
      B = new DenseMatrix64F(m, n);
    } else {
      B.reshape(m, n, false);
      B.zero();
    } 
    return B;
  }
  
  public DenseMatrix64F getU(DenseMatrix64F U, boolean transpose, boolean compact) {
    U = handleU(U, transpose, compact, this.m, this.n, this.min);
    CommonOps.setIdentity((RowD1Matrix64F)U);
    for (int i = 0; i < this.m; ) {
      this.u[i] = 0.0D;
      i++;
    } 
    for (int j = this.min - 1; j >= 0; j--) {
      this.u[j] = 1.0D;
      for (int k = j + 1; k < this.m; k++)
        this.u[k] = this.UBV.get(k, j); 
      if (transpose) {
        QrHelperFunctions.rank1UpdateMultL(U, this.u, this.gammasU[j], j, j, this.m);
      } else {
        QrHelperFunctions.rank1UpdateMultR(U, this.u, this.gammasU[j], j, j, this.m, this.b);
      } 
    } 
    return U;
  }
  
  public static DenseMatrix64F handleU(DenseMatrix64F U, boolean transpose, boolean compact, int m, int n, int min) {
    if (compact) {
      if (transpose) {
        if (U == null) {
          U = new DenseMatrix64F(min, m);
        } else {
          U.reshape(min, m, false);
        } 
      } else if (U == null) {
        U = new DenseMatrix64F(m, min);
      } else {
        U.reshape(m, min, false);
      } 
    } else if (U == null) {
      U = new DenseMatrix64F(m, m);
    } else {
      U.reshape(m, m, false);
    } 
    return U;
  }
  
  public DenseMatrix64F getV(DenseMatrix64F V, boolean transpose, boolean compact) {
    V = handleV(V, transpose, compact, this.m, this.n, this.min);
    CommonOps.setIdentity((RowD1Matrix64F)V);
    for (int j = this.min - 1; j >= 0; j--) {
      this.u[j + 1] = 1.0D;
      for (int i = j + 2; i < this.n; i++)
        this.u[i] = this.UBV.get(j, i); 
      if (transpose) {
        QrHelperFunctions.rank1UpdateMultL(V, this.u, this.gammasV[j], j + 1, j + 1, this.n);
      } else {
        QrHelperFunctions.rank1UpdateMultR(V, this.u, this.gammasV[j], j + 1, j + 1, this.n, this.b);
      } 
    } 
    return V;
  }
  
  public static DenseMatrix64F handleV(DenseMatrix64F V, boolean transpose, boolean compact, int m, int n, int min) {
    int w = (n > m) ? (min + 1) : min;
    if (compact) {
      if (transpose) {
        if (V == null) {
          V = new DenseMatrix64F(w, n);
        } else {
          V.reshape(w, n, false);
        } 
      } else if (V == null) {
        V = new DenseMatrix64F(n, w);
      } else {
        V.reshape(n, w, false);
      } 
    } else if (V == null) {
      V = new DenseMatrix64F(n, n);
    } else {
      V.reshape(n, n, false);
    } 
    return V;
  }
  
  private boolean _decompose() {
    for (int k = 0; k < this.min; k++) {
      computeU(k);
      computeV(k);
    } 
    return true;
  }
  
  protected void computeU(int k) {
    double[] b = this.UBV.data;
    double max = 0.0D;
    for (int i = k; i < this.m; i++) {
      double val = this.u[i] = b[i * this.n + k];
      val = Math.abs(val);
      if (val > max)
        max = val; 
    } 
    if (max > 0.0D) {
      double tau = QrHelperFunctions.computeTauAndDivide(k, this.m, this.u, max);
      double nu = this.u[k] + tau;
      QrHelperFunctions.divideElements_Bcol(k + 1, this.m, this.n, this.u, b, k, nu);
      this.u[k] = 1.0D;
      double gamma = nu / tau;
      this.gammasU[k] = gamma;
      QrHelperFunctions.rank1UpdateMultR(this.UBV, this.u, gamma, k + 1, k, this.m, this.b);
      b[k * this.n + k] = -tau * max;
    } else {
      this.gammasU[k] = 0.0D;
    } 
  }
  
  protected void computeV(int k) {
    double[] b = this.UBV.data;
    int row = k * this.n;
    double max = QrHelperFunctions.findMax(b, row + k + 1, this.n - k - 1);
    if (max > 0.0D) {
      double tau = QrHelperFunctions.computeTauAndDivide(k + 1, this.n, b, row, max);
      double nu = b[row + k + 1] + tau;
      QrHelperFunctions.divideElements_Brow(k + 2, this.n, this.u, b, row, nu);
      this.u[k + 1] = 1.0D;
      double gamma = nu / tau;
      this.gammasV[k] = gamma;
      QrHelperFunctions.rank1UpdateMultL(this.UBV, this.u, gamma, k + 1, k + 1, this.n);
      b[row + k + 1] = -tau * max;
    } else {
      this.gammasV[k] = 0.0D;
    } 
  }
  
  public double[] getGammasU() {
    return this.gammasU;
  }
  
  public double[] getGammasV() {
    return this.gammasV;
  }
  
  public boolean inputModified() {
    return true;
  }
}
