// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.simple;

import java.io.IOException;
import ic2.shades.org.ejml.ops.SpecializedOps;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.MatrixIterator;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.ops.MatrixFeatures;
import ic2.shades.org.ejml.factory.SingularMatrixException;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.io.Serializable;

public abstract class SimpleBase<T extends SimpleBase> implements Serializable
{
    protected DenseMatrix64F mat;
    
    public SimpleBase(final int numRows, final int numCols) {
        this.mat = new DenseMatrix64F(numRows, numCols);
    }
    
    protected SimpleBase() {
    }
    
    protected abstract T createMatrix(final int p0, final int p1);
    
    public DenseMatrix64F getMatrix() {
        return this.mat;
    }
    
    public T transpose() {
        final T ret = this.createMatrix(this.mat.numCols, this.mat.numRows);
        CommonOps.transpose(this.mat, ret.getMatrix());
        return ret;
    }
    
    public T mult(final T b) {
        final T ret = this.createMatrix(this.mat.numRows, b.getMatrix().numCols);
        CommonOps.mult(this.mat, b.getMatrix(), ret.getMatrix());
        return ret;
    }
    
    public T kron(final T B) {
        final T ret = this.createMatrix(this.mat.numRows * B.numRows(), this.mat.numCols * B.numCols());
        CommonOps.kron(this.mat, B.getMatrix(), ret.getMatrix());
        return ret;
    }
    
    public T plus(final T b) {
        final T ret = this.copy();
        CommonOps.addEquals(ret.getMatrix(), b.getMatrix());
        return ret;
    }
    
    public T minus(final T b) {
        final T ret = this.copy();
        CommonOps.subtract(this.getMatrix(), b.getMatrix(), ret.getMatrix());
        return ret;
    }
    
    public T minus(final double b) {
        final T ret = this.copy();
        CommonOps.subtract(this.getMatrix(), b, ret.getMatrix());
        return ret;
    }
    
    public T plus(final double beta) {
        final T ret = this.createMatrix(this.numRows(), this.numCols());
        CommonOps.add(this.getMatrix(), beta, ret.getMatrix());
        return ret;
    }
    
    public T plus(final double beta, final T b) {
        final T ret = this.copy();
        CommonOps.addEquals(ret.getMatrix(), beta, b.getMatrix());
        return ret;
    }
    
    public double dot(final T v) {
        if (!this.isVector()) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        }
        if (!v.isVector()) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }
        return VectorVectorMult.innerProd(this.mat, v.getMatrix());
    }
    
    public boolean isVector() {
        return this.mat.numRows == 1 || this.mat.numCols == 1;
    }
    
    public T scale(final double val) {
        final T ret = this.copy();
        CommonOps.scale(val, ret.getMatrix());
        return ret;
    }
    
    public T divide(final double val) {
        final T ret = this.copy();
        CommonOps.divide(ret.getMatrix(), val);
        return ret;
    }
    
    public T invert() {
        final T ret = this.createMatrix(this.mat.numRows, this.mat.numCols);
        if (!CommonOps.invert(this.mat, ret.getMatrix())) {
            throw new SingularMatrixException();
        }
        if (MatrixFeatures.hasUncountable(ret.getMatrix())) {
            throw new SingularMatrixException("Solution has uncountable numbers");
        }
        return ret;
    }
    
    public T pseudoInverse() {
        final T ret = this.createMatrix(this.mat.numCols, this.mat.numRows);
        CommonOps.pinv(this.mat, ret.getMatrix());
        return ret;
    }
    
    public T solve(final T b) {
        final T x = this.createMatrix(this.mat.numCols, b.getMatrix().numCols);
        if (!CommonOps.solve(this.mat, b.getMatrix(), x.getMatrix())) {
            throw new SingularMatrixException();
        }
        if (MatrixFeatures.hasUncountable(x.getMatrix())) {
            throw new SingularMatrixException("Solution contains uncountable numbers");
        }
        return x;
    }
    
    public void set(final T a) {
        this.mat.set(a.getMatrix());
    }
    
    public void set(final double val) {
        CommonOps.fill(this.mat, val);
    }
    
    public void zero() {
        this.mat.zero();
    }
    
    public double normF() {
        return NormOps.normF(this.mat);
    }
    
    public double conditionP2() {
        return NormOps.conditionP2(this.mat);
    }
    
    public double determinant() {
        final double ret = CommonOps.det(this.mat);
        if (UtilEjml.isUncountable(ret)) {
            return 0.0;
        }
        return ret;
    }
    
    public double trace() {
        return CommonOps.trace(this.mat);
    }
    
    public void reshape(final int numRows, final int numCols) {
        this.mat.reshape(numRows, numCols, false);
    }
    
    public void set(final int row, final int col, final double value) {
        this.mat.set(row, col, value);
    }
    
    public void set(final int index, final double value) {
        this.mat.set(index, value);
    }
    
    public void setRow(final int row, final int offset, final double... values) {
        for (int i = 0; i < values.length; ++i) {
            this.mat.set(row, offset + i, values[i]);
        }
    }
    
    public void setColumn(final int column, final int offset, final double... values) {
        for (int i = 0; i < values.length; ++i) {
            this.mat.set(offset + i, column, values[i]);
        }
    }
    
    public double get(final int row, final int col) {
        return this.mat.get(row, col);
    }
    
    public double get(final int index) {
        return this.mat.data[index];
    }
    
    public int getIndex(final int row, final int col) {
        return row * this.mat.numCols + col;
    }
    
    public MatrixIterator iterator(final boolean rowMajor, final int minRow, final int minCol, final int maxRow, final int maxCol) {
        return new MatrixIterator(this.mat, rowMajor, minRow, minCol, maxRow, maxCol);
    }
    
    public T copy() {
        final T ret = this.createMatrix(this.mat.numRows, this.mat.numCols);
        ret.getMatrix().set(this.getMatrix());
        return ret;
    }
    
    public int numRows() {
        return this.mat.numRows;
    }
    
    public int numCols() {
        return this.mat.numCols;
    }
    
    public int getNumElements() {
        return this.mat.getNumElements();
    }
    
    public void print() {
        MatrixIO.print(System.out, this.mat);
    }
    
    public void print(final int numChar, final int precision) {
        MatrixIO.print(System.out, this.mat, numChar, precision);
    }
    
    public void print(final String format) {
        MatrixIO.print(System.out, this.mat, format);
    }
    
    @Override
    public String toString() {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixIO.print(new PrintStream(stream), this.mat);
        return stream.toString();
    }
    
    public T extractMatrix(int y0, int y1, int x0, int x1) {
        if (y0 == Integer.MAX_VALUE) {
            y0 = this.mat.numRows;
        }
        if (y1 == Integer.MAX_VALUE) {
            y1 = this.mat.numRows;
        }
        if (x0 == Integer.MAX_VALUE) {
            x0 = this.mat.numCols;
        }
        if (x1 == Integer.MAX_VALUE) {
            x1 = this.mat.numCols;
        }
        final T ret = this.createMatrix(y1 - y0, x1 - x0);
        CommonOps.extract(this.mat, y0, y1, x0, x1, ret.getMatrix(), 0, 0);
        return ret;
    }
    
    public T extractVector(final boolean extractRow, final int element) {
        final int length = extractRow ? this.mat.numCols : this.mat.numRows;
        final T ret = extractRow ? this.createMatrix(1, length) : this.createMatrix(length, 1);
        if (extractRow) {
            SpecializedOps.subvector(this.mat, element, 0, length, true, 0, ret.getMatrix());
        }
        else {
            SpecializedOps.subvector(this.mat, 0, element, length, false, 0, ret.getMatrix());
        }
        return ret;
    }
    
    public T extractDiag() {
        final int N = Math.min(this.mat.numCols, this.mat.numRows);
        final T diag = this.createMatrix(N, 1);
        CommonOps.extractDiag(this.mat, diag.getMatrix());
        return diag;
    }
    
    public boolean isIdentical(final T a, final double tol) {
        return MatrixFeatures.isIdentical(this.mat, a.getMatrix(), tol);
    }
    
    public boolean hasUncountable() {
        return MatrixFeatures.hasUncountable(this.mat);
    }
    
    public SimpleSVD svd() {
        return new SimpleSVD(this.mat, false);
    }
    
    public SimpleSVD svd(final boolean compact) {
        return new SimpleSVD(this.mat, compact);
    }
    
    public SimpleEVD eig() {
        return new SimpleEVD(this.mat);
    }
    
    public void insertIntoThis(final int insertRow, final int insertCol, final T B) {
        CommonOps.insert(B.getMatrix(), this.mat, insertRow, insertCol);
    }
    
    public T combine(int insertRow, int insertCol, final T B) {
        if (insertRow == Integer.MAX_VALUE) {
            insertRow = this.mat.numRows;
        }
        if (insertCol == Integer.MAX_VALUE) {
            insertCol = this.mat.numCols;
        }
        final int maxRow = insertRow + B.numRows();
        final int maxCol = insertCol + B.numCols();
        T ret;
        if (maxRow > this.mat.numRows || maxCol > this.mat.numCols) {
            final int M = Math.max(maxRow, this.mat.numRows);
            final int N = Math.max(maxCol, this.mat.numCols);
            ret = this.createMatrix(M, N);
            ret.insertIntoThis(0, 0, (SimpleBase<SimpleBase<SimpleBase<SimpleBase>>>)this);
        }
        else {
            ret = this.copy();
        }
        ret.insertIntoThis(insertRow, insertCol, B);
        return ret;
    }
    
    public double elementMaxAbs() {
        return CommonOps.elementMaxAbs(this.mat);
    }
    
    public double elementSum() {
        return CommonOps.elementSum(this.mat);
    }
    
    public T elementMult(final T b) {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementMult(this.mat, b.getMatrix(), c.getMatrix());
        return c;
    }
    
    public T elementDiv(final T b) {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementDiv(this.mat, b.getMatrix(), c.getMatrix());
        return c;
    }
    
    public T elementPower(final T b) {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementPower(this.mat, b.getMatrix(), c.getMatrix());
        return c;
    }
    
    public T elementPower(final double b) {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementPower(this.mat, b, c.getMatrix());
        return c;
    }
    
    public T elementExp() {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementExp(this.mat, c.getMatrix());
        return c;
    }
    
    public T elementLog() {
        final T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
        CommonOps.elementLog(this.mat, c.getMatrix());
        return c;
    }
    
    public T negative() {
        final T A = this.copy();
        CommonOps.changeSign(A.getMatrix());
        return A;
    }
    
    public void saveToFileBinary(final String fileName) throws IOException {
        MatrixIO.saveBin(this.mat, fileName);
    }
    
    public static SimpleMatrix loadBinary(final String fileName) throws IOException {
        final ReshapeMatrix64F mat = MatrixIO.loadBin(fileName);
        if (mat instanceof DenseMatrix64F) {
            return SimpleMatrix.wrap((DenseMatrix64F)mat);
        }
        return SimpleMatrix.wrap(new DenseMatrix64F(mat));
    }
    
    public void saveToFileCSV(final String fileName) throws IOException {
        MatrixIO.saveCSV(this.mat, fileName);
    }
    
    public static SimpleMatrix loadCSV(final String fileName) throws IOException {
        final ReshapeMatrix64F mat = MatrixIO.loadCSV(fileName);
        if (mat instanceof DenseMatrix64F) {
            return SimpleMatrix.wrap((DenseMatrix64F)mat);
        }
        return SimpleMatrix.wrap(new DenseMatrix64F(mat));
    }
    
    public boolean isInBounds(final int row, final int col) {
        return row >= 0 && col >= 0 && row < this.mat.numRows && col < this.mat.numCols;
    }
    
    public void printDimensions() {
        System.out.println("[rows = " + this.numRows() + " , cols = " + this.numCols() + " ]");
    }
}
