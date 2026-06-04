// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.EjmlParameters;

public class BlockMatrix64F extends D1Matrix64F
{
    public int blockLength;
    
    public BlockMatrix64F(final int numRows, final int numCols, final int blockLength) {
        this.data = new double[numRows * numCols];
        this.blockLength = blockLength;
        this.numRows = numRows;
        this.numCols = numCols;
    }
    
    public BlockMatrix64F(final int numRows, final int numCols) {
        this(numRows, numCols, EjmlParameters.BLOCK_WIDTH);
    }
    
    public BlockMatrix64F() {
    }
    
    public void set(final BlockMatrix64F A) {
        this.blockLength = A.blockLength;
        this.numRows = A.numRows;
        this.numCols = A.numCols;
        final int N = this.numCols * this.numRows;
        if (this.data.length < N) {
            this.data = new double[N];
        }
        System.arraycopy(A.data, 0, this.data, 0, N);
    }
    
    public static BlockMatrix64F wrap(final double[] data, final int numRows, final int numCols, final int blockLength) {
        final BlockMatrix64F ret = new BlockMatrix64F();
        ret.data = data;
        ret.numRows = numRows;
        ret.numCols = numCols;
        ret.blockLength = blockLength;
        return ret;
    }
    
    @Override
    public double[] getData() {
        return this.data;
    }
    
    @Override
    public void reshape(final int numRows, final int numCols, final boolean saveValues) {
        if (numRows * numCols <= this.data.length) {
            this.numRows = numRows;
            this.numCols = numCols;
        }
        else {
            final double[] data = new double[numRows * numCols];
            if (saveValues) {
                System.arraycopy(this.data, 0, data, 0, this.getNumElements());
            }
            this.numRows = numRows;
            this.numCols = numCols;
            this.data = data;
        }
    }
    
    public void reshape(final int numRows, final int numCols, final int blockLength, final boolean saveValues) {
        this.blockLength = blockLength;
        this.reshape(numRows, numCols, saveValues);
    }
    
    @Override
    public int getIndex(int row, int col) {
        final int blockRow = row / this.blockLength;
        final int blockCol = col / this.blockLength;
        final int localHeight = Math.min(this.numRows - blockRow * this.blockLength, this.blockLength);
        final int index = blockRow * this.blockLength * this.numCols + blockCol * localHeight * this.blockLength;
        final int localLength = Math.min(this.numCols - this.blockLength * blockCol, this.blockLength);
        row %= this.blockLength;
        col %= this.blockLength;
        return index + localLength * row + col;
    }
    
    @Override
    public double get(final int row, final int col) {
        return this.data[this.getIndex(row, col)];
    }
    
    @Override
    public double unsafe_get(final int row, final int col) {
        return this.data[this.getIndex(row, col)];
    }
    
    @Override
    public void set(final int row, final int col, final double val) {
        this.data[this.getIndex(row, col)] = val;
    }
    
    @Override
    public void unsafe_set(final int row, final int col, final double val) {
        this.data[this.getIndex(row, col)] = val;
    }
    
    @Override
    public int getNumRows() {
        return this.numRows;
    }
    
    @Override
    public int getNumCols() {
        return this.numCols;
    }
    
    @Override
    public int getNumElements() {
        return this.numRows * this.numCols;
    }
    
    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }
    
    @Override
    public BlockMatrix64F copy() {
        final BlockMatrix64F A = new BlockMatrix64F(this.numRows, this.numCols, this.blockLength);
        A.set(this);
        return A;
    }
}
