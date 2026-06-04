// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

public abstract class ReshapeMatrix64F implements Matrix64F
{
    private static final long serialVersionUID = 423423451942L;
    public int numRows;
    public int numCols;
    
    public abstract void reshape(final int p0, final int p1, final boolean p2);
    
    public void reshape(final int numRows, final int numCols) {
        this.reshape(numRows, numCols, false);
    }
    
    public MatrixIterator iterator(final boolean rowMajor, final int minRow, final int minCol, final int maxRow, final int maxCol) {
        return new MatrixIterator(this, rowMajor, minRow, minCol, maxRow, maxCol);
    }
    
    @Override
    public int getNumRows() {
        return this.numRows;
    }
    
    @Override
    public int getNumCols() {
        return this.numCols;
    }
    
    public void setNumRows(final int numRows) {
        this.numRows = numRows;
    }
    
    public void setNumCols(final int numCols) {
        this.numCols = numCols;
    }
    
    public void set(final ReshapeMatrix64F A) {
        if (A.numRows != this.numRows) {
            throw new IllegalArgumentException("Unexpected number of rows.");
        }
        if (A.numCols != this.numCols) {
            throw new IllegalArgumentException("Unexpected number of columns.");
        }
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                this.set(i, j, A.get(i, j));
            }
        }
    }
}
