package ic2.shades.org.ejml.data;

import java.io.Serializable;

public interface Matrix64F extends Serializable {
   double get(int var1, int var2);

   double unsafe_get(int var1, int var2);

   void set(int var1, int var2, double var3);

   void unsafe_set(int var1, int var2, double var3);

   int getNumRows();

   int getNumCols();

   int getNumElements();

   <T extends Matrix64F> T copy();

   void print();
}
