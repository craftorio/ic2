// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.svd;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecompositionRow_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecompositionTall_D64;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm;
import ic2.shades.org.ejml.interfaces.decomposition.BidiagonalDecomposition;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class SvdImplicitQrDecompose_D64 implements SingularValueDecomposition<DenseMatrix64F>
{
    private int numRows;
    private int numCols;
    private int numRowsT;
    private int numColsT;
    private boolean canUseTallBidiagonal;
    private BidiagonalDecomposition<DenseMatrix64F> bidiag;
    private SvdImplicitQrAlgorithm qralg;
    double[] diag;
    double[] off;
    private DenseMatrix64F Ut;
    private DenseMatrix64F Vt;
    private double[] singularValues;
    private int numSingular;
    private boolean compact;
    private boolean computeU;
    private boolean computeV;
    private boolean prefComputeU;
    private boolean prefComputeV;
    private boolean transposed;
    private DenseMatrix64F A_mod;
    
    public SvdImplicitQrDecompose_D64(final boolean compact, final boolean computeU, final boolean computeV, final boolean canUseTallBidiagonal) {
        this.qralg = new SvdImplicitQrAlgorithm();
        this.A_mod = new DenseMatrix64F(1, 1);
        this.compact = compact;
        this.prefComputeU = computeU;
        this.prefComputeV = computeV;
        this.canUseTallBidiagonal = canUseTallBidiagonal;
    }
    
    @Override
    public double[] getSingularValues() {
        return this.singularValues;
    }
    
    @Override
    public int numberOfSingularValues() {
        return this.numSingular;
    }
    
    @Override
    public boolean isCompact() {
        return this.compact;
    }
    
    @Override
    public DenseMatrix64F getU(DenseMatrix64F U, final boolean transpose) {
        if (!this.prefComputeU) {
            throw new IllegalArgumentException("As requested U was not computed.");
        }
        if (transpose) {
            if (U == null) {
                return this.Ut;
            }
            if (U.numRows != this.Ut.numRows || U.numCols != this.Ut.numCols) {
                throw new IllegalArgumentException("Unexpected shape of U");
            }
            U.set(this.Ut);
        }
        else {
            if (U == null) {
                U = new DenseMatrix64F(this.Ut.numCols, this.Ut.numRows);
            }
            else if (U.numRows != this.Ut.numCols || U.numCols != this.Ut.numRows) {
                throw new IllegalArgumentException("Unexpected shape of U");
            }
            CommonOps.transpose(this.Ut, U);
        }
        return U;
    }
    
    @Override
    public DenseMatrix64F getV(DenseMatrix64F V, final boolean transpose) {
        if (!this.prefComputeV) {
            throw new IllegalArgumentException("As requested V was not computed.");
        }
        if (transpose) {
            if (V == null) {
                return this.Vt;
            }
            if (V.numRows != this.Vt.numRows || V.numCols != this.Vt.numCols) {
                throw new IllegalArgumentException("Unexpected shape of V");
            }
            V.set(this.Vt);
        }
        else {
            if (V == null) {
                V = new DenseMatrix64F(this.Vt.numCols, this.Vt.numRows);
            }
            else if (V.numRows != this.Vt.numCols || V.numCols != this.Vt.numRows) {
                throw new IllegalArgumentException("Unexpected shape of V");
            }
            CommonOps.transpose(this.Vt, V);
        }
        return V;
    }
    
    @Override
    public DenseMatrix64F getW(DenseMatrix64F W) {
        final int m = this.compact ? this.numSingular : this.numRows;
        final int n = this.compact ? this.numSingular : this.numCols;
        if (W == null) {
            W = new DenseMatrix64F(m, n);
        }
        else {
            W.reshape(m, n, false);
            W.zero();
        }
        for (int i = 0; i < this.numSingular; ++i) {
            W.unsafe_set(i, i, this.singularValues[i]);
        }
        return W;
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F orig) {
        if (!this.setup(orig)) {
            return false;
        }
        if (this.bidiagonalization(orig)) {
            return false;
        }
        if (this.computeUWV()) {
            return false;
        }
        this.makeSingularPositive();
        this.undoTranspose();
        return true;
    }
    
    @Override
    public boolean inputModified() {
        return false;
    }
    
    private boolean bidiagonalization(final DenseMatrix64F orig) {
        if (this.transposed) {
            this.A_mod.reshape(orig.numCols, orig.numRows, false);
            CommonOps.transpose(orig, this.A_mod);
        }
        else {
            this.A_mod.reshape(orig.numRows, orig.numCols, false);
            this.A_mod.set(orig);
        }
        return !this.bidiag.decompose(this.A_mod);
    }
    
    private void undoTranspose() {
        if (this.transposed) {
            final DenseMatrix64F temp = this.Vt;
            this.Vt = this.Ut;
            this.Ut = temp;
        }
    }
    
    private boolean computeUWV() {
        this.bidiag.getDiagonal(this.diag, this.off);
        this.qralg.setMatrix(this.numRowsT, this.numColsT, this.diag, this.off);
        if (this.computeU) {
            this.Ut = this.bidiag.getU(this.Ut, true, this.compact);
        }
        if (this.computeV) {
            this.Vt = this.bidiag.getV(this.Vt, true, this.compact);
        }
        this.qralg.setFastValues(false);
        if (this.computeU) {
            this.qralg.setUt(this.Ut);
        }
        else {
            this.qralg.setUt(null);
        }
        if (this.computeV) {
            this.qralg.setVt(this.Vt);
        }
        else {
            this.qralg.setVt(null);
        }
        final boolean ret = !this.qralg.process();
        return ret;
    }
    
    private boolean setup(final DenseMatrix64F orig) {
        this.transposed = (orig.numCols > orig.numRows);
        if (this.transposed) {
            this.computeU = this.prefComputeV;
            this.computeV = this.prefComputeU;
            this.numRowsT = orig.numCols;
            this.numColsT = orig.numRows;
        }
        else {
            this.computeU = this.prefComputeU;
            this.computeV = this.prefComputeV;
            this.numRowsT = orig.numRows;
            this.numColsT = orig.numCols;
        }
        this.numRows = orig.numRows;
        this.numCols = orig.numCols;
        if (this.numRows == 0 || this.numCols == 0) {
            return false;
        }
        if (this.diag == null || this.diag.length < this.numColsT) {
            this.diag = new double[this.numColsT];
            this.off = new double[this.numColsT - 1];
        }
        if (this.canUseTallBidiagonal && this.numRows > this.numCols * 2 && !this.computeU) {
            if (this.bidiag == null || !(this.bidiag instanceof BidiagonalDecompositionTall_D64)) {
                this.bidiag = new BidiagonalDecompositionTall_D64();
            }
        }
        else if (this.bidiag == null || !(this.bidiag instanceof BidiagonalDecompositionRow_D64)) {
            this.bidiag = new BidiagonalDecompositionRow_D64();
        }
        return true;
    }
    
    private void makeSingularPositive() {
        this.numSingular = this.qralg.getNumberOfSingularValues();
        this.singularValues = this.qralg.getSingularValues();
        for (int i = 0; i < this.numSingular; ++i) {
            final double val = this.qralg.getSingularValue(i);
            if (val < 0.0) {
                this.singularValues[i] = 0.0 - val;
                if (this.computeU) {
                    final int start = i * this.Ut.numCols;
                    for (int stop = start + this.Ut.numCols, j = start; j < stop; ++j) {
                        this.Ut.set(j, 0.0 - this.Ut.get(j));
                    }
                }
            }
            else {
                this.singularValues[i] = val;
            }
        }
    }
    
    @Override
    public int numRows() {
        return this.numRows;
    }
    
    @Override
    public int numCols() {
        return this.numCols;
    }
}
