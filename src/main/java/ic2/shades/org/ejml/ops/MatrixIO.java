// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public class MatrixIO
{
    public static void saveBin(final ReshapeMatrix64F A, final String fileName) throws IOException {
        final FileOutputStream fileStream = new FileOutputStream(fileName);
        final ObjectOutputStream stream = new ObjectOutputStream(fileStream);
        try {
            stream.writeObject(A);
            stream.flush();
        }
        finally {
            try {
                stream.close();
            }
            finally {
                fileStream.close();
            }
        }
    }
    
    public static <T extends ReshapeMatrix64F> T loadBin(final String fileName) throws IOException {
        final FileInputStream fileStream = new FileInputStream(fileName);
        final ObjectInputStream stream = new ObjectInputStream(fileStream);
        T ret;
        try {
            ret = (T)stream.readObject();
            if (stream.available() != 0) {
                throw new RuntimeException("File not completely read?");
            }
        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        stream.close();
        return ret;
    }
    
    public static void saveCSV(final ReshapeMatrix64F A, final String fileName) throws IOException {
        final PrintStream fileStream = new PrintStream(fileName);
        fileStream.print(A.getNumRows() + " ");
        fileStream.println(A.getNumCols());
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                fileStream.print(A.get(i, j) + " ");
            }
            fileStream.println();
        }
        fileStream.close();
    }
    
    public static DenseMatrix64F loadCSV(final String fileName) throws IOException {
        final FileInputStream fileStream = new FileInputStream(fileName);
        final ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);
        final DenseMatrix64F ret = csv.read();
        fileStream.close();
        return ret;
    }
    
    public static DenseMatrix64F loadCSV(final String fileName, final int numRows, final int numCols) throws IOException {
        final FileInputStream fileStream = new FileInputStream(fileName);
        final ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);
        final DenseMatrix64F ret = csv.read(numRows, numCols);
        fileStream.close();
        return ret;
    }
    
    public static void print(final PrintStream out, final Matrix64F mat) {
        print(out, mat, 6, 3);
    }
    
    public static void print(final PrintStream out, final Matrix64F mat, final int numChar, final int precision) {
        final String format = "%" + numChar + "." + precision + "f ";
        print(out, mat, format);
    }
    
    public static void print(final PrintStream out, final Matrix64F mat, String format) {
        final String type = ReshapeMatrix64F.class.isAssignableFrom(mat.getClass()) ? "dense" : "dense fixed";
        out.println("Type = " + type + " , numRows = " + mat.getNumRows() + " , numCols = " + mat.getNumCols());
        format += " ";
        for (int y = 0; y < mat.getNumRows(); ++y) {
            for (int x = 0; x < mat.getNumCols(); ++x) {
                out.printf(format, mat.get(y, x));
            }
            out.println();
        }
    }
    
    public static void print(final PrintStream out, final ReshapeMatrix64F mat, String format, final int row0, final int row1, final int col0, final int col1) {
        out.println("Type = submatrix , rows " + row0 + " to " + row1 + "  columns " + col0 + " to " + col1);
        format += " ";
        for (int y = row0; y < row1; ++y) {
            for (int x = col0; x < col1; ++x) {
                out.printf(format, mat.get(y, x));
            }
            out.println();
        }
    }
}
