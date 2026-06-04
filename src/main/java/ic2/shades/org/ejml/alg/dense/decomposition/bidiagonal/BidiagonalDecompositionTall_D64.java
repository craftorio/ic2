// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.BidiagonalDecomposition;

public class BidiagonalDecompositionTall_D64 implements BidiagonalDecomposition<DenseMatrix64F>
{
    QRPDecomposition<DenseMatrix64F> decompQRP;
    BidiagonalDecomposition<DenseMatrix64F> decompBi;
    DenseMatrix64F B;
    int m;
    int n;
    int min;
    
    public BidiagonalDecompositionTall_D64() {
        this.decompQRP = DecompositionFactory.qrp(500, 100);
        this.decompBi = new BidiagonalDecompositionRow_D64();
        this.B = new DenseMatrix64F(1, 1);
    }
    
    @Override
    public void getDiagonal(final double[] diag, final double[] off) {
        diag[0] = this.B.get(0);
        for (int i = 1; i < this.n; ++i) {
            diag[i] = this.B.unsafe_get(i, i);
            off[i - 1] = this.B.unsafe_get(i - 1, i);
        }
    }
    
    @Override
    public DenseMatrix64F getB(DenseMatrix64F B, final boolean compact) {
        B = BidiagonalDecompositionRow_D64.handleB(B, compact, this.m, this.n, this.min);
        B.set(0, 0, this.B.get(0, 0));
        for (int i = 1; i < this.min; ++i) {
            B.set(i, i, this.B.get(i, i));
            B.set(i - 1, i, this.B.get(i - 1, i));
        }
        if (this.n > this.m) {
            B.set(this.min - 1, this.min, this.B.get(this.min - 1, this.min));
        }
        return B;
    }
    
    @Override
    public DenseMatrix64F getU(DenseMatrix64F U, final boolean transpose, final boolean compact) {
        U = BidiagonalDecompositionRow_D64.handleU(U, false, compact, this.m, this.n, this.min);
        if (compact) {
            final DenseMatrix64F Q1 = this.decompQRP.getQ(null, true);
            final DenseMatrix64F U2 = this.decompBi.getU(null, false, true);
            CommonOps.mult(Q1, U2, U);
        }
        else {
            final DenseMatrix64F Q2 = this.decompQRP.getQ(U, false);
            final DenseMatrix64F U2 = this.decompBi.getU(null, false, true);
            final DenseMatrix64F Q3 = CommonOps.extract(Q2, 0, Q2.numRows, 0, this.min);
            final DenseMatrix64F tmp = new DenseMatrix64F(Q3.numRows, U2.numCols);
            CommonOps.mult(Q3, U2, tmp);
            CommonOps.insert(tmp, Q2, 0, 0);
        }
        if (transpose) {
            CommonOps.transpose(U);
        }
        return U;
    }
    
    @Override
    public DenseMatrix64F getV(final DenseMatrix64F V, final boolean transpose, final boolean compact) {
        return this.decompBi.getV(V, transpose, compact);
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F orig) {
        if (!this.decompQRP.decompose(orig)) {
            return false;
        }
        this.m = orig.numRows;
        this.n = orig.numCols;
        this.min = Math.min(this.m, this.n);
        this.B.reshape(this.min, this.n, false);
        this.decompQRP.getR(this.B, true);
        final DenseMatrix64F result = new DenseMatrix64F(this.min, this.n);
        final DenseMatrix64F P = this.decompQRP.getPivotMatrix(null);
        CommonOps.multTransB(this.B, P, result);
        this.B.set(result);
        return this.decompBi.decompose(this.B);
    }
    
    @Override
    public boolean inputModified() {
        return this.decompQRP.inputModified();
    }
}
