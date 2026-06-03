package ic2.shades.org.ejml.alg.dense.decomposition.lu;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;

public class LUDecompositionAlt_D64 extends LUDecompositionBase_D64 {
  public boolean decompose(DenseMatrix64F a) {
    decomposeCommonInit(a);
    double[] LUcolj = this.vv;
    for (int j = 0; j < this.n; j++) {
      int i;
      for (i = 0; i < this.m; i++)
        LUcolj[i] = this.dataLU[i * this.n + j]; 
      for (i = 0; i < this.m; i++) {
        int rowIndex = i * this.n;
        int kmax = (i < j) ? i : j;
        double s = 0.0D;
        for (int m = 0; m < kmax; m++)
          s += this.dataLU[rowIndex + m] * LUcolj[m]; 
        LUcolj[i] = LUcolj[i] - s;
        this.dataLU[rowIndex + j] = LUcolj[i] - s;
      } 
      int p = j;
      double max = Math.abs(LUcolj[p]);
      for (int k = j + 1; k < this.m; k++) {
        double v = Math.abs(LUcolj[k]);
        if (v > max) {
          p = k;
          max = v;
        } 
      } 
      if (p != j) {
        int rowP = p * this.n;
        int rowJ = j * this.n;
        int endP = rowP + this.n;
        for (; rowP < endP; rowP++, rowJ++) {
          double t = this.dataLU[rowP];
          this.dataLU[rowP] = this.dataLU[rowJ];
          this.dataLU[rowJ] = t;
        } 
        int m = this.pivot[p];
        this.pivot[p] = this.pivot[j];
        this.pivot[j] = m;
        this.pivsign = -this.pivsign;
      } 
      this.indx[j] = p;
      if (j < this.m) {
        double lujj = this.dataLU[j * this.n + j];
        if (lujj != 0.0D)
          for (int m = j + 1; m < this.m; m++)
            this.dataLU[m * this.n + j] = this.dataLU[m * this.n + j] / lujj;  
      } 
    } 
    return true;
  }
}
