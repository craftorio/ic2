package ic2.shades.org.ejml.alg.dense.decomposition.chol;

public class CholeskyDecompositionInner_D64 extends CholeskyDecompositionCommon_D64 {
  public CholeskyDecompositionInner_D64() {
    super(true);
  }
  
  public CholeskyDecompositionInner_D64(boolean lower) {
    super(lower);
  }
  
  protected boolean decomposeLower() {
    double div_el_ii = 0.0D;
    int i;
    for (i = 0; i < this.n; i++) {
      for (int j = i; j < this.n; j++) {
        double sum = this.t[i * this.n + j];
        int iEl = i * this.n;
        int jEl = j * this.n;
        int end = iEl + i;
        for (; iEl < end; iEl++, jEl++)
          sum -= this.t[iEl] * this.t[jEl]; 
        if (i == j) {
          if (sum <= 0.0D)
            return false; 
          double el_ii = Math.sqrt(sum);
          this.t[i * this.n + i] = el_ii;
          div_el_ii = 1.0D / el_ii;
        } else {
          this.t[j * this.n + i] = sum * div_el_ii;
        } 
      } 
    } 
    for (i = 0; i < this.n; i++) {
      for (int j = i + 1; j < this.n; j++)
        this.t[i * this.n + j] = 0.0D; 
    } 
    return true;
  }
  
  protected boolean decomposeUpper() {
    double div_el_ii = 0.0D;
    int i;
    for (i = 0; i < this.n; i++) {
      for (int j = i; j < this.n; j++) {
        double sum = this.t[i * this.n + j];
        for (int k = 0; k < i; k++)
          sum -= this.t[k * this.n + i] * this.t[k * this.n + j]; 
        if (i == j) {
          if (sum <= 0.0D)
            return false; 
          double el_ii = Math.sqrt(sum);
          this.t[i * this.n + i] = el_ii;
          div_el_ii = 1.0D / el_ii;
        } else {
          this.t[i * this.n + j] = sum * div_el_ii;
        } 
      } 
    } 
    for (i = 0; i < this.n; i++) {
      for (int j = 0; j < i; j++)
        this.t[i * this.n + j] = 0.0D; 
    } 
    return true;
  }
}
