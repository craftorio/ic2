// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.lu;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.LUDecomposition;

public abstract class LUDecompositionBase_D64 implements LUDecomposition<DenseMatrix64F>
{
    protected DenseMatrix64F LU;
    protected int maxWidth;
    protected int m;
    protected int n;
    protected double[] dataLU;
    protected double[] vv;
    protected int[] indx;
    protected int[] pivot;
    protected double pivsign;
    
    public LUDecompositionBase_D64() {
        this.maxWidth = -1;
    }
    
    public void setExpectedMaxSize(final int numRows, final int numCols) {
        this.LU = new DenseMatrix64F(numRows, numCols);
        this.dataLU = this.LU.data;
        this.maxWidth = Math.max(numRows, numCols);
        this.vv = new double[this.maxWidth];
        this.indx = new int[this.maxWidth];
        this.pivot = new int[this.maxWidth];
    }
    
    public DenseMatrix64F getLU() {
        return this.LU;
    }
    
    public int[] getIndx() {
        return this.indx;
    }
    
    public int[] getPivot() {
        return this.pivot;
    }
    
    @Override
    public boolean inputModified() {
        return false;
    }
    
    @Override
    public DenseMatrix64F getLower(DenseMatrix64F lower) {
        final int numRows = this.LU.numRows;
        final int numCols = (this.LU.numRows < this.LU.numCols) ? this.LU.numRows : this.LU.numCols;
        if (lower == null) {
            lower = new DenseMatrix64F(numRows, numCols);
        }
        else {
            if (lower.numCols != numCols || lower.numRows != numRows) {
                throw new IllegalArgumentException("Unexpected matrix dimension");
            }
            CommonOps.fill(lower, 0.0);
        }
        for (int i = 0; i < numCols; ++i) {
            lower.set(i, i, 1.0);
            for (int j = 0; j < i; ++j) {
                lower.set(i, j, this.LU.get(i, j));
            }
        }
        if (numRows > numCols) {
            for (int i = numCols; i < numRows; ++i) {
                for (int j = 0; j < numCols; ++j) {
                    lower.set(i, j, this.LU.get(i, j));
                }
            }
        }
        return lower;
    }
    
    @Override
    public DenseMatrix64F getUpper(DenseMatrix64F upper) {
        final int numRows = (this.LU.numRows < this.LU.numCols) ? this.LU.numRows : this.LU.numCols;
        final int numCols = this.LU.numCols;
        if (upper == null) {
            upper = new DenseMatrix64F(numRows, numCols);
        }
        else {
            if (upper.numCols != numCols || upper.numRows != numRows) {
                throw new IllegalArgumentException("Unexpected matrix dimension");
            }
            CommonOps.fill(upper, 0.0);
        }
        for (int i = 0; i < numRows; ++i) {
            for (int j = i; j < numCols; ++j) {
                upper.set(i, j, this.LU.get(i, j));
            }
        }
        return upper;
    }
    
    @Override
    public DenseMatrix64F getPivot(final DenseMatrix64F pivot) {
        return SpecializedOps.pivotMatrix(pivot, this.pivot, this.LU.numRows, false);
    }
    
    protected void decomposeCommonInit(final DenseMatrix64F a) {
        if (a.numRows > this.maxWidth || a.numCols > this.maxWidth) {
            this.setExpectedMaxSize(a.numRows, a.numCols);
        }
        this.m = a.numRows;
        this.n = a.numCols;
        this.LU.setReshape(a);
        for (int i = 0; i < this.m; ++i) {
            this.pivot[i] = i;
        }
        this.pivsign = 1.0;
    }
    
    @Override
    public boolean isSingular() {
        for (int i = 0; i < this.m; ++i) {
            if (Math.abs(this.dataLU[i * this.n + i]) < UtilEjml.EPS) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public double computeDeterminant() {
        if (this.m != this.n) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }
        double ret = this.pivsign;
        for (int total = this.m * this.n, i = 0; i < total; i += this.n + 1) {
            ret *= this.dataLU[i];
        }
        return ret;
    }
    
    public double quality() {
        return SpecializedOps.qualityTriangular(true, this.LU);
    }
    
    public void _solveVectorInternal(final double[] vv) {
        int ii = 0;
        for (int i = 0; i < this.n; ++i) {
            final int ip = this.indx[i];
            double sum = vv[ip];
            vv[ip] = vv[i];
            if (ii != 0) {
                int index = i * this.n + ii - 1;
                for (int j = ii - 1; j < i; ++j) {
                    sum -= this.dataLU[index++] * vv[j];
                }
            }
            else if (sum != 0.0) {
                ii = i + 1;
            }
            vv[i] = sum;
        }
        TriangularSolver.solveU(this.dataLU, vv, this.n);
    }
    
    public double[] _getVV() {
        return this.vv;
    }
}
