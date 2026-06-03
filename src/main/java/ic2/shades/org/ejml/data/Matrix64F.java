package ic2.shades.org.ejml.data;

import java.io.Serializable;

public interface Matrix64F extends Serializable {
  double get(int paramInt1, int paramInt2);
  
  double unsafe_get(int paramInt1, int paramInt2);
  
  void set(int paramInt1, int paramInt2, double paramDouble);
  
  void unsafe_set(int paramInt1, int paramInt2, double paramDouble);
  
  int getNumRows();
  
  int getNumCols();
  
  int getNumElements();
  
  <T extends Matrix64F> T copy();
  
  void print();
}
