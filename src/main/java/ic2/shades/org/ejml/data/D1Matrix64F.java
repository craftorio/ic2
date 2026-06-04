// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.alg.dense.mult.MatrixDimensionException;

public abstract class D1Matrix64F extends ReshapeMatrix64F
{
    public double[] data;
    
    public double[] getData() {
        return this.data;
    }
    
    public void setData(final double[] data) {
        this.data = data;
    }
    
    public abstract int getIndex(final int p0, final int p1);
    
    public void set(final D1Matrix64F b) {
        if (this.numRows != b.numRows || this.numCols != b.numCols) {
            throw new MatrixDimensionException("The two matrices do not have compatible shapes.");
        }
        final int dataLength = b.getNumElements();
        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }
    
    public double get(final int index) {
        return this.data[index];
    }
    
    public double set(final int index, final double val) {
        return this.data[index] = val;
    }
    
    public double plus(final int index, final double val) {
        final double[] data = this.data;
        return data[index] += val;
    }
    
    public double minus(final int index, final double val) {
        final double[] data = this.data;
        return data[index] -= val;
    }
    
    public double times(final int index, final double val) {
        final double[] data = this.data;
        return data[index] *= val;
    }
    
    public double div(final int index, final double val) {
        final double[] data = this.data;
        return data[index] /= val;
    }
}
