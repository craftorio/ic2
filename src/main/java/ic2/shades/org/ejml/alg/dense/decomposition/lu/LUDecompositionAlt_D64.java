// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.lu;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class LUDecompositionAlt_D64 extends LUDecompositionBase_D64
{
    @Override
    public boolean decompose(final DenseMatrix64F a) {
        this.decomposeCommonInit(a);
        final double[] LUcolj = this.vv;
        for (int j = 0; j < this.n; ++j) {
            for (int i = 0; i < this.m; ++i) {
                LUcolj[i] = this.dataLU[i * this.n + j];
            }
            for (int i = 0; i < this.m; ++i) {
                final int rowIndex = i * this.n;
                final int kmax = (i < j) ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; ++k) {
                    s += this.dataLU[rowIndex + k] * LUcolj[k];
                }
                final double[] dataLU = this.dataLU;
                final int n = rowIndex + j;
                final double[] array = LUcolj;
                final int n2 = i;
                dataLU[n] = (array[n2] -= s);
            }
            int p = j;
            double max = Math.abs(LUcolj[p]);
            for (int l = j + 1; l < this.m; ++l) {
                final double v = Math.abs(LUcolj[l]);
                if (v > max) {
                    p = l;
                    max = v;
                }
            }
            if (p != j) {
                for (int rowP = p * this.n, rowJ = j * this.n, endP = rowP + this.n; rowP < endP; ++rowP, ++rowJ) {
                    final double t = this.dataLU[rowP];
                    this.dataLU[rowP] = this.dataLU[rowJ];
                    this.dataLU[rowJ] = t;
                }
                final int m = this.pivot[p];
                this.pivot[p] = this.pivot[j];
                this.pivot[j] = m;
                this.pivsign = -this.pivsign;
            }
            this.indx[j] = p;
            if (j < this.m) {
                final double lujj = this.dataLU[j * this.n + j];
                if (lujj != 0.0) {
                    for (int i2 = j + 1; i2 < this.m; ++i2) {
                        final double[] dataLU2 = this.dataLU;
                        final int n3 = i2 * this.n + j;
                        dataLU2[n3] /= lujj;
                    }
                }
            }
        }
        return true;
    }
}
