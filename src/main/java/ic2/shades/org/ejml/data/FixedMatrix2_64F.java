package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix2_64F implements FixedMatrix64F {
  public double a1;
  
  public double a2;
  
  public FixedMatrix2_64F() {}
  
  public FixedMatrix2_64F(double a1, double a2) {
    this.a1 = a1;
    this.a2 = a2;
  }
  
  public FixedMatrix2_64F(FixedMatrix2_64F o) {
    this.a1 = o.a1;
    this.a2 = o.a2;
  }
  
  public double get(int row, int col) {
    return unsafe_get(row, col);
  }
  
  public double unsafe_get(int row, int col) {
    if (row != 0 && col != 0)
      throw new IllegalArgumentException("Row or column must be zero since this is a vector"); 
    int w = Math.max(row, col);
    if (w == 0)
      return this.a1; 
    if (w == 1)
      return this.a2; 
    throw new IllegalArgumentException("Out of range.  " + w);
  }
  
  public void set(int row, int col, double val) {
    unsafe_set(row, col, val);
  }
  
  public void unsafe_set(int row, int col, double val) {
    if (row != 0 && col != 0)
      throw new IllegalArgumentException("Row or column must be zero since this is a vector"); 
    int w = Math.max(row, col);
    if (w == 0) {
      this.a1 = val;
    } else if (w == 1) {
      this.a2 = val;
    } else {
      throw new IllegalArgumentException("Out of range.  " + w);
    } 
  }
  
  public int getNumRows() {
    return 2;
  }
  
  public int getNumCols() {
    return 1;
  }
  
  public int getNumElements() {
    return 2;
  }
  
  public <T extends Matrix64F> T copy() {
    return (T)new FixedMatrix2_64F(this);
  }
  
  public void print() {
    MatrixIO.print(System.out, this);
  }
}
