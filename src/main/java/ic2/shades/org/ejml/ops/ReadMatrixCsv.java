// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import java.util.List;
import java.io.IOException;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.io.InputStream;

public class ReadMatrixCsv extends ReadCsv
{
    public ReadMatrixCsv(final InputStream in) {
        super(in);
    }
    
    public DenseMatrix64F read() throws IOException {
        final List<String> words = this.extractWords();
        if (words.size() != 2) {
            throw new IOException("Unexpected number of words on first line.");
        }
        final int numRows = Integer.parseInt(words.get(0));
        final int numCols = Integer.parseInt(words.get(1));
        if (numRows < 0 || numCols < 0) {
            throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);
        }
        return this.read(numRows, numCols);
    }
    
    public DenseMatrix64F read(final int numRows, final int numCols) throws IOException {
        final DenseMatrix64F A = new DenseMatrix64F(numRows, numCols);
        for (int i = 0; i < numRows; ++i) {
            final List<String> words = this.extractWords();
            if (words == null) {
                throw new IOException("Too few rows found. expected " + numRows + " actual " + i);
            }
            if (words.size() != numCols) {
                throw new IOException("Unexpected number of words in column. Found " + words.size() + " expected " + numCols);
            }
            for (int j = 0; j < numCols; ++j) {
                A.set(i, j, Double.parseDouble(words.get(j)));
            }
        }
        return A;
    }
}
