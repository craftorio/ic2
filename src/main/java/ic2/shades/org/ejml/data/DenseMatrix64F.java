// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.ops.CommonOps;

public class DenseMatrix64F extends RowD1Matrix64F
{
    public DenseMatrix64F(final int numRows, final int numCols, final boolean rowMajor, final double... data) {
        final int length = numRows * numCols;
        this.data = new double[length];
        this.set(this.numRows = numRows, this.numCols = numCols, rowMajor, data);
    }
    
    public DenseMatrix64F(final double[][] data) {
        this.numRows = data.length;
        this.numCols = data[0].length;
        this.data = new double[this.numRows * this.numCols];
        int pos = 0;
        for (int i = 0; i < this.numRows; ++i) {
            final double[] row = data[i];
            if (row.length != this.numCols) {
                throw new IllegalArgumentException("All rows must have the same length");
            }
            System.arraycopy(row, 0, this.data, pos, this.numCols);
            pos += this.numCols;
        }
    }
    
    public DenseMatrix64F(final int numRows, final int numCols) {
        this.data = new double[numRows * numCols];
        this.numRows = numRows;
        this.numCols = numCols;
    }
    
    public DenseMatrix64F(final DenseMatrix64F orig) {
        this(orig.numRows, orig.numCols);
        System.arraycopy(orig.data, 0, this.data, 0, orig.getNumElements());
    }
    
    public DenseMatrix64F(final int length) {
        this.data = new double[length];
    }
    
    public DenseMatrix64F() {
    }
    
    public DenseMatrix64F(final ReshapeMatrix64F mat) {
        this(mat.numRows, mat.numCols);
        for (int i = 0; i < this.numRows; ++i) {
            for (int j = 0; j < this.numCols; ++j) {
                this.set(i, j, mat.get(i, j));
            }
        }
    }
    
    public static DenseMatrix64F wrap(final int numRows, final int numCols, final double[] data) {
        final DenseMatrix64F s = new DenseMatrix64F();
        s.data = data;
        s.numRows = numRows;
        s.numCols = numCols;
        return s;
    }
    
    @Override
    public void reshape(final int numRows, final int numCols, final boolean saveValues) {
        if (this.data.length < numRows * numCols) {
            final double[] d = new double[numRows * numCols];
            if (saveValues) {
                System.arraycopy(this.data, 0, d, 0, this.getNumElements());
            }
            this.data = d;
        }
        this.numRows = numRows;
        this.numCols = numCols;
    }
    
    @Override
    public void set(final int row, final int col, final double value) {
        if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows) {
            throw new IllegalArgumentException("Specified element is out of bounds: (" + row + " , " + col + ")");
        }
        this.data[row * this.numCols + col] = value;
    }
    
    @Override
    public void unsafe_set(final int row, final int col, final double value) {
        this.data[row * this.numCols + col] = value;
    }
    
    public void add(final int row, final int col, final double value) {
        if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows) {
            throw new IllegalArgumentException("Specified element is out of bounds");
        }
        final double[] data = this.data;
        final int n = row * this.numCols + col;
        data[n] += value;
    }
    
    @Override
    public double get(final int row, final int col) {
        if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows) {
            throw new IllegalArgumentException("Specified element is out of bounds: " + row + " " + col);
        }
        return this.data[row * this.numCols + col];
    }
    
    @Override
    public double unsafe_get(final int row, final int col) {
        return this.data[row * this.numCols + col];
    }
    
    @Override
    public int getIndex(final int row, final int col) {
        return row * this.numCols + col;
    }
    
    public boolean isInBounds(final int row, final int col) {
        return col >= 0 && col < this.numCols && row >= 0 && row < this.numRows;
    }
    
    @Override
    public int getNumElements() {
        return this.numRows * this.numCols;
    }
    
    public void setReshape(final DenseMatrix64F b) {
        final int dataLength = b.getNumElements();
        if (this.data.length < dataLength) {
            this.data = new double[dataLength];
        }
        this.numRows = b.numRows;
        this.numCols = b.numCols;
        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }
    
    public void set(final int numRows, final int numCols, final boolean rowMajor, final double... data) {
        final int length = numRows * numCols;
        if (numRows != this.numRows || numCols != this.numCols) {
            throw new IllegalArgumentException("Unexpected matrix shape.");
        }
        if (length > this.data.length) {
            throw new IllegalArgumentException("The length of this matrix's data array is too small.");
        }
        if (rowMajor) {
            System.arraycopy(data, 0, this.data, 0, length);
        }
        else {
            int index = 0;
            for (int i = 0; i < numRows; ++i) {
                for (int j = 0; j < numCols; ++j) {
                    this.data[index++] = data[j * numRows + i];
                }
            }
        }
    }
    
    public void zero() {
        CommonOps.fill(this, 0.0);
    }
    
    @Override
    public DenseMatrix64F copy() {
        return new DenseMatrix64F(this);
    }
    
    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }
    
    public void print(final String format) {
        MatrixIO.print(System.out, this, format);
    }
    
    @Override
    public String toString() {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixIO.print(new PrintStream(stream), this);
        return stream.toString();
    }
}
