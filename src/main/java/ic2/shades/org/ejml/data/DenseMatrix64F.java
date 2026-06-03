package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.MatrixIO;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DenseMatrix64F extends RowD1Matrix64F {
  public DenseMatrix64F(int numRows, int numCols, boolean rowMajor, double... data) {
    int length = numRows * numCols;
    this.data = new double[length];
    this.numRows = numRows;
    this.numCols = numCols;
    set(numRows, numCols, rowMajor, data);
  }
  
  public DenseMatrix64F(double[][] data) {
    this.numRows = data.length;
    this.numCols = (data[0]).length;
    this.data = new double[this.numRows * this.numCols];
    int pos = 0;
    for (int i = 0; i < this.numRows; i++) {
      double[] row = data[i];
      if (row.length != this.numCols)
        throw new IllegalArgumentException("All rows must have the same length"); 
      System.arraycopy(row, 0, this.data, pos, this.numCols);
      pos += this.numCols;
    } 
  }
  
  public DenseMatrix64F(int numRows, int numCols) {
    this.data = new double[numRows * numCols];
    this.numRows = numRows;
    this.numCols = numCols;
  }
  
  public DenseMatrix64F(DenseMatrix64F orig) {
    this(orig.numRows, orig.numCols);
    System.arraycopy(orig.data, 0, this.data, 0, orig.getNumElements());
  }
  
  public DenseMatrix64F(int length) {
    this.data = new double[length];
  }
  
  public DenseMatrix64F() {}
  
  public DenseMatrix64F(ReshapeMatrix64F mat) {
    this(mat.numRows, mat.numCols);
    for (int i = 0; i < this.numRows; i++) {
      for (int j = 0; j < this.numCols; j++)
        set(i, j, mat.get(i, j)); 
    } 
  }
  
  public static DenseMatrix64F wrap(int numRows, int numCols, double[] data) {
    DenseMatrix64F s = new DenseMatrix64F();
    s.data = data;
    s.numRows = numRows;
    s.numCols = numCols;
    return s;
  }
  
  public void reshape(int numRows, int numCols, boolean saveValues) {
    if (this.data.length < numRows * numCols) {
      double[] d = new double[numRows * numCols];
      if (saveValues)
        System.arraycopy(this.data, 0, d, 0, getNumElements()); 
      this.data = d;
    } 
    this.numRows = numRows;
    this.numCols = numCols;
  }
  
  public void set(int row, int col, double value) {
    if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows)
      throw new IllegalArgumentException("Specified element is out of bounds: (" + row + " , " + col + ")"); 
    this.data[row * this.numCols + col] = value;
  }
  
  public void unsafe_set(int row, int col, double value) {
    this.data[row * this.numCols + col] = value;
  }
  
  public void add(int row, int col, double value) {
    if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows)
      throw new IllegalArgumentException("Specified element is out of bounds"); 
    this.data[row * this.numCols + col] = this.data[row * this.numCols + col] + value;
  }
  
  public double get(int row, int col) {
    if (col < 0 || col >= this.numCols || row < 0 || row >= this.numRows)
      throw new IllegalArgumentException("Specified element is out of bounds: " + row + " " + col); 
    return this.data[row * this.numCols + col];
  }
  
  public double unsafe_get(int row, int col) {
    return this.data[row * this.numCols + col];
  }
  
  public int getIndex(int row, int col) {
    return row * this.numCols + col;
  }
  
  public boolean isInBounds(int row, int col) {
    return (col >= 0 && col < this.numCols && row >= 0 && row < this.numRows);
  }
  
  public int getNumElements() {
    return this.numRows * this.numCols;
  }
  
  public void setReshape(DenseMatrix64F b) {
    int dataLength = b.getNumElements();
    if (this.data.length < dataLength)
      this.data = new double[dataLength]; 
    this.numRows = b.numRows;
    this.numCols = b.numCols;
    System.arraycopy(b.data, 0, this.data, 0, dataLength);
  }
  
  public void set(int numRows, int numCols, boolean rowMajor, double... data) {
    int length = numRows * numCols;
    if (numRows != this.numRows || numCols != this.numCols)
      throw new IllegalArgumentException("Unexpected matrix shape."); 
    if (length > this.data.length)
      throw new IllegalArgumentException("The length of this matrix's data array is too small."); 
    if (rowMajor) {
      System.arraycopy(data, 0, this.data, 0, length);
    } else {
      int index = 0;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++)
          this.data[index++] = data[j * numRows + i]; 
      } 
    } 
  }
  
  public void zero() {
    CommonOps.fill(this, 0.0D);
  }
  
  public DenseMatrix64F copy() {
    return new DenseMatrix64F(this);
  }
  
  public void print() {
    MatrixIO.print(System.out, this);
  }
  
  public void print(String format) {
    MatrixIO.print(System.out, this, format);
  }
  
  public String toString() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    MatrixIO.print(new PrintStream(stream), this);
    return stream.toString();
  }
}
