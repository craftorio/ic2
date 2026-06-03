package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix4_64F implements FixedMatrix64F {
  public double a1;
  
  public double a2;
  
  public double a3;
  
  public double a4;
  
  public FixedMatrix4_64F() {}
  
  public FixedMatrix4_64F(double a1, double a2, double a3, double a4) {
    this.a1 = a1;
    this.a2 = a2;
    this.a3 = a3;
    this.a4 = a4;
  }
  
  public FixedMatrix4_64F(FixedMatrix4_64F o) {
    this.a1 = o.a1;
    this.a2 = o.a2;
    this.a3 = o.a3;
    this.a4 = o.a4;
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
    if (w == 2)
      return this.a3; 
    if (w == 3)
      return this.a4; 
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
    } else if (w == 2) {
      this.a3 = val;
    } else if (w == 3) {
      this.a4 = val;
    } else {
      throw new IllegalArgumentException("Out of range.  " + w);
    } 
  }
  
  public int getNumRows() {
    return 4;
  }
  
  public int getNumCols() {
    return 1;
  }
  
  public int getNumElements() {
    return 4;
  }
  
  public <T extends Matrix64F> T copy() {
    return (T)new FixedMatrix4_64F(this);
  }
  
  public void print() {
    MatrixIO.print(System.out, this);
  }
}
