// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix3_64F implements FixedMatrix64F
{
    public double a1;
    public double a2;
    public double a3;
    
    public FixedMatrix3_64F() {
    }
    
    public FixedMatrix3_64F(final double a1, final double a2, final double a3) {
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }
    
    public FixedMatrix3_64F(final FixedMatrix3_64F o) {
        this.a1 = o.a1;
        this.a2 = o.a2;
        this.a3 = o.a3;
    }
    
    @Override
    public double get(final int row, final int col) {
        return this.unsafe_get(row, col);
    }
    
    @Override
    public double unsafe_get(final int row, final int col) {
        if (row != 0 && col != 0) {
            throw new IllegalArgumentException("Row or column must be zero since this is a vector");
        }
        final int w = Math.max(row, col);
        if (w == 0) {
            return this.a1;
        }
        if (w == 1) {
            return this.a2;
        }
        if (w == 2) {
            return this.a3;
        }
        throw new IllegalArgumentException("Out of range.  " + w);
    }
    
    @Override
    public void set(final int row, final int col, final double val) {
        this.unsafe_set(row, col, val);
    }
    
    @Override
    public void unsafe_set(final int row, final int col, final double val) {
        if (row != 0 && col != 0) {
            throw new IllegalArgumentException("Row or column must be zero since this is a vector");
        }
        final int w = Math.max(row, col);
        if (w == 0) {
            this.a1 = val;
        }
        else if (w == 1) {
            this.a2 = val;
        }
        else {
            if (w != 2) {
                throw new IllegalArgumentException("Out of range.  " + w);
            }
            this.a3 = val;
        }
    }
    
    @Override
    public int getNumRows() {
        return 3;
    }
    
    @Override
    public int getNumCols() {
        return 1;
    }
    
    @Override
    public int getNumElements() {
        return 3;
    }
    
    @Override
    public <T extends Matrix64F> T copy() {
        return (T)new FixedMatrix3_64F(this);
    }
    
    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }
}
