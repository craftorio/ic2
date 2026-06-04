// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyLDLDecomposition;

public class CholeskyDecompositionLDL_D64 implements CholeskyLDLDecomposition<DenseMatrix64F>
{
    private int maxWidth;
    private int n;
    private DenseMatrix64F L;
    private double[] el;
    private double[] d;
    double[] vv;
    
    public void setExpectedMaxSize(final int numRows, final int numCols) {
        if (numRows != numCols) {
            throw new IllegalArgumentException("Can only decompose square matrices");
        }
        this.maxWidth = numRows;
        this.L = new DenseMatrix64F(this.maxWidth, this.maxWidth);
        this.el = this.L.data;
        this.vv = new double[this.maxWidth];
        this.d = new double[this.maxWidth];
    }
    
    @Override
    public boolean decompose(final DenseMatrix64F mat) {
        if (mat.numRows > this.maxWidth) {
            this.setExpectedMaxSize(mat.numRows, mat.numCols);
        }
        else if (mat.numRows != mat.numCols) {
            throw new RuntimeException("Can only decompose square matrices");
        }
        this.n = mat.numRows;
        this.L.setReshape(mat);
        double d_inv = 0.0;
        for (int i = 0; i < this.n; ++i) {
            for (int j = i; j < this.n; ++j) {
                double sum = this.el[i * this.n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= this.el[i * this.n + k] * this.el[j * this.n + k] * this.d[k];
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        return false;
                    }
                    this.d[i] = sum;
                    d_inv = 1.0 / sum;
                    this.el[i * this.n + i] = 1.0;
                }
                else {
                    this.el[j * this.n + i] = sum * d_inv;
                }
            }
        }
        for (int i = 0; i < this.n; ++i) {
            for (int j = i + 1; j < this.n; ++j) {
                this.el[i * this.n + j] = 0.0;
            }
        }
        return true;
    }
    
    @Override
    public boolean inputModified() {
        return false;
    }
    
    @Override
    public double[] getDiagonal() {
        return this.d;
    }
    
    public DenseMatrix64F getL() {
        return this.L;
    }
    
    public double[] _getVV() {
        return this.vv;
    }
    
    @Override
    public DenseMatrix64F getL(DenseMatrix64F L) {
        if (L == null) {
            L = this.L.copy();
        }
        else {
            L.set(this.L);
        }
        return L;
    }
    
    @Override
    public DenseMatrix64F getD(final DenseMatrix64F D) {
        return CommonOps.diag(D, this.L.numCols, this.d);
    }
}
