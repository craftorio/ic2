package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.MatrixIterator;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.SingularMatrixException;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.MatrixFeatures;
import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.ops.SpecializedOps;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

public abstract class SimpleBase<T extends SimpleBase> implements Serializable {
  protected DenseMatrix64F mat;
  
  public SimpleBase(int numRows, int numCols) {
    this.mat = new DenseMatrix64F(numRows, numCols);
  }
  
  protected SimpleBase() {}
  
  protected abstract T createMatrix(int paramInt1, int paramInt2);
  
  public DenseMatrix64F getMatrix() {
    return this.mat;
  }
  
  public T transpose() {
    T ret = createMatrix(this.mat.numCols, this.mat.numRows);
    CommonOps.transpose(this.mat, ret.getMatrix());
    return ret;
  }
  
  public T mult(T b) {
    T ret = createMatrix(this.mat.numRows, (b.getMatrix()).numCols);
    CommonOps.mult((RowD1Matrix64F)this.mat, (RowD1Matrix64F)b.getMatrix(), (RowD1Matrix64F)ret.getMatrix());
    return ret;
  }
  
  public T kron(T B) {
    T ret = createMatrix(this.mat.numRows * B.numRows(), this.mat.numCols * B.numCols());
    CommonOps.kron(this.mat, B.getMatrix(), ret.getMatrix());
    return ret;
  }
  
  public T plus(T b) {
    T ret = copy();
    CommonOps.addEquals((D1Matrix64F)ret.getMatrix(), (D1Matrix64F)b.getMatrix());
    return ret;
  }
  
  public T minus(T b) {
    T ret = copy();
    CommonOps.subtract((D1Matrix64F)getMatrix(), (D1Matrix64F)b.getMatrix(), (D1Matrix64F)ret.getMatrix());
    return ret;
  }
  
  public T minus(double b) {
    T ret = copy();
    CommonOps.subtract((D1Matrix64F)getMatrix(), b, (D1Matrix64F)ret.getMatrix());
    return ret;
  }
  
  public T plus(double beta) {
    T ret = createMatrix(numRows(), numCols());
    CommonOps.add((D1Matrix64F)getMatrix(), beta, (D1Matrix64F)ret.getMatrix());
    return ret;
  }
  
  public T plus(double beta, T b) {
    T ret = copy();
    CommonOps.addEquals((D1Matrix64F)ret.getMatrix(), beta, (D1Matrix64F)b.getMatrix());
    return ret;
  }
  
  public double dot(T v) {
    if (!isVector())
      throw new IllegalArgumentException("'this' matrix is not a vector."); 
    if (!v.isVector())
      throw new IllegalArgumentException("'v' matrix is not a vector."); 
    return VectorVectorMult.innerProd((D1Matrix64F)this.mat, (D1Matrix64F)v.getMatrix());
  }
  
  public boolean isVector() {
    return (this.mat.numRows == 1 || this.mat.numCols == 1);
  }
  
  public T scale(double val) {
    T ret = copy();
    CommonOps.scale(val, (D1Matrix64F)ret.getMatrix());
    return ret;
  }
  
  public T divide(double val) {
    T ret = copy();
    CommonOps.divide((D1Matrix64F)ret.getMatrix(), val);
    return ret;
  }
  
  public T invert() {
    T ret = createMatrix(this.mat.numRows, this.mat.numCols);
    if (!CommonOps.invert(this.mat, ret.getMatrix()))
      throw new SingularMatrixException(); 
    if (MatrixFeatures.hasUncountable((D1Matrix64F)ret.getMatrix()))
      throw new SingularMatrixException("Solution has uncountable numbers"); 
    return ret;
  }
  
  public T pseudoInverse() {
    T ret = createMatrix(this.mat.numCols, this.mat.numRows);
    CommonOps.pinv(this.mat, ret.getMatrix());
    return ret;
  }
  
  public T solve(T b) {
    T x = createMatrix(this.mat.numCols, (b.getMatrix()).numCols);
    if (!CommonOps.solve(this.mat, b.getMatrix(), x.getMatrix()))
      throw new SingularMatrixException(); 
    if (MatrixFeatures.hasUncountable((D1Matrix64F)x.getMatrix()))
      throw new SingularMatrixException("Solution contains uncountable numbers"); 
    return x;
  }
  
  public void set(T a) {
    this.mat.set((D1Matrix64F)a.getMatrix());
  }
  
  public void set(double val) {
    CommonOps.fill((D1Matrix64F)this.mat, val);
  }
  
  public void zero() {
    this.mat.zero();
  }
  
  public double normF() {
    return NormOps.normF((D1Matrix64F)this.mat);
  }
  
  public double conditionP2() {
    return NormOps.conditionP2(this.mat);
  }
  
  public double determinant() {
    double ret = CommonOps.det(this.mat);
    if (UtilEjml.isUncountable(ret))
      return 0.0D; 
    return ret;
  }
  
  public double trace() {
    return CommonOps.trace((RowD1Matrix64F)this.mat);
  }
  
  public void reshape(int numRows, int numCols) {
    this.mat.reshape(numRows, numCols, false);
  }
  
  public void set(int row, int col, double value) {
    this.mat.set(row, col, value);
  }
  
  public void set(int index, double value) {
    this.mat.set(index, value);
  }
  
  public void setRow(int row, int offset, double... values) {
    for (int i = 0; i < values.length; i++)
      this.mat.set(row, offset + i, values[i]); 
  }
  
  public void setColumn(int column, int offset, double... values) {
    for (int i = 0; i < values.length; i++)
      this.mat.set(offset + i, column, values[i]); 
  }
  
  public double get(int row, int col) {
    return this.mat.get(row, col);
  }
  
  public double get(int index) {
    return this.mat.data[index];
  }
  
  public int getIndex(int row, int col) {
    return row * this.mat.numCols + col;
  }
  
  public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol) {
    return new MatrixIterator((ReshapeMatrix64F)this.mat, rowMajor, minRow, minCol, maxRow, maxCol);
  }
  
  public T copy() {
    T ret = createMatrix(this.mat.numRows, this.mat.numCols);
    ret.getMatrix().set((D1Matrix64F)getMatrix());
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
    MatrixIO.print(System.out, (Matrix64F)this.mat);
  }
  
  public void print(int numChar, int precision) {
    MatrixIO.print(System.out, (Matrix64F)this.mat, numChar, precision);
  }
  
  public void print(String format) {
    MatrixIO.print(System.out, (Matrix64F)this.mat, format);
  }
  
  public String toString() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    MatrixIO.print(new PrintStream(stream), (Matrix64F)this.mat);
    return stream.toString();
  }
  
  public T extractMatrix(int y0, int y1, int x0, int x1) {
    if (y0 == Integer.MAX_VALUE)
      y0 = this.mat.numRows; 
    if (y1 == Integer.MAX_VALUE)
      y1 = this.mat.numRows; 
    if (x0 == Integer.MAX_VALUE)
      x0 = this.mat.numCols; 
    if (x1 == Integer.MAX_VALUE)
      x1 = this.mat.numCols; 
    T ret = createMatrix(y1 - y0, x1 - x0);
    CommonOps.extract((ReshapeMatrix64F)this.mat, y0, y1, x0, x1, (ReshapeMatrix64F)ret.getMatrix(), 0, 0);
    return ret;
  }
  
  public T extractVector(boolean extractRow, int element) {
    int length = extractRow ? this.mat.numCols : this.mat.numRows;
    T ret = extractRow ? createMatrix(1, length) : createMatrix(length, 1);
    if (extractRow) {
      SpecializedOps.subvector((RowD1Matrix64F)this.mat, element, 0, length, true, 0, (RowD1Matrix64F)ret.getMatrix());
    } else {
      SpecializedOps.subvector((RowD1Matrix64F)this.mat, 0, element, length, false, 0, (RowD1Matrix64F)ret.getMatrix());
    } 
    return ret;
  }
  
  public T extractDiag() {
    int N = Math.min(this.mat.numCols, this.mat.numRows);
    T diag = createMatrix(N, 1);
    CommonOps.extractDiag(this.mat, diag.getMatrix());
    return diag;
  }
  
  public boolean isIdentical(T a, double tol) {
    return MatrixFeatures.isIdentical((D1Matrix64F)this.mat, (D1Matrix64F)a.getMatrix(), tol);
  }
  
  public boolean hasUncountable() {
    return MatrixFeatures.hasUncountable((D1Matrix64F)this.mat);
  }
  
  public SimpleSVD svd() {
    return new SimpleSVD<SimpleMatrix>(this.mat, false);
  }
  
  public SimpleSVD svd(boolean compact) {
    return new SimpleSVD<SimpleMatrix>(this.mat, compact);
  }
  
  public SimpleEVD eig() {
    return new SimpleEVD<SimpleMatrix>(this.mat);
  }
  
  public void insertIntoThis(int insertRow, int insertCol, T B) {
    CommonOps.insert((ReshapeMatrix64F)B.getMatrix(), (ReshapeMatrix64F)this.mat, insertRow, insertCol);
  }
  
  public T combine(int insertRow, int insertCol, T B) {
    T ret;
    if (insertRow == Integer.MAX_VALUE)
      insertRow = this.mat.numRows; 
    if (insertCol == Integer.MAX_VALUE)
      insertCol = this.mat.numCols; 
    int maxRow = insertRow + B.numRows();
    int maxCol = insertCol + B.numCols();
    if (maxRow > this.mat.numRows || maxCol > this.mat.numCols) {
      int M = Math.max(maxRow, this.mat.numRows);
      int N = Math.max(maxCol, this.mat.numCols);
      ret = createMatrix(M, N);
      ret.insertIntoThis(0, 0, this);
    } else {
      ret = copy();
    } 
    ret.insertIntoThis(insertRow, insertCol, B);
    return ret;
  }
  
  public double elementMaxAbs() {
    return CommonOps.elementMaxAbs((D1Matrix64F)this.mat);
  }
  
  public double elementSum() {
    return CommonOps.elementSum((D1Matrix64F)this.mat);
  }
  
  public T elementMult(T b) {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementMult((D1Matrix64F)this.mat, (D1Matrix64F)b.getMatrix(), (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T elementDiv(T b) {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementDiv((D1Matrix64F)this.mat, (D1Matrix64F)b.getMatrix(), (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T elementPower(T b) {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementPower((D1Matrix64F)this.mat, (D1Matrix64F)b.getMatrix(), (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T elementPower(double b) {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementPower((D1Matrix64F)this.mat, b, (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T elementExp() {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementExp((D1Matrix64F)this.mat, (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T elementLog() {
    T c = createMatrix(this.mat.numRows, this.mat.numCols);
    CommonOps.elementLog((D1Matrix64F)this.mat, (D1Matrix64F)c.getMatrix());
    return c;
  }
  
  public T negative() {
    T A = copy();
    CommonOps.changeSign((D1Matrix64F)A.getMatrix());
    return A;
  }
  
  public void saveToFileBinary(String fileName) throws IOException {
    MatrixIO.saveBin((ReshapeMatrix64F)this.mat, fileName);
  }
  
  public static SimpleMatrix loadBinary(String fileName) throws IOException {
    ReshapeMatrix64F mat = MatrixIO.loadBin(fileName);
    if (mat instanceof DenseMatrix64F)
      return SimpleMatrix.wrap((DenseMatrix64F)mat); 
    return SimpleMatrix.wrap(new DenseMatrix64F(mat));
  }
  
  public void saveToFileCSV(String fileName) throws IOException {
    MatrixIO.saveCSV((ReshapeMatrix64F)this.mat, fileName);
  }
  
  public static SimpleMatrix loadCSV(String fileName) throws IOException {
    DenseMatrix64F denseMatrix64F = MatrixIO.loadCSV(fileName);
    if (denseMatrix64F instanceof DenseMatrix64F)
      return SimpleMatrix.wrap(denseMatrix64F); 
    return SimpleMatrix.wrap(new DenseMatrix64F((ReshapeMatrix64F)denseMatrix64F));
  }
  
  public boolean isInBounds(int row, int col) {
    return (row >= 0 && col >= 0 && row < this.mat.numRows && col < this.mat.numCols);
  }
  
  public void printDimensions() {
    System.out.println("[rows = " + numRows() + " , cols = " + numCols() + " ]");
  }
}
