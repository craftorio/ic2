package ic2.shades.org.ejml.interfaces.linsol;

import ic2.shades.org.ejml.data.Matrix64F;

public interface LinearSolver<T extends Matrix64F> {
   boolean setA(T var1);

   double quality();

   void solve(T var1, T var2);

   void invert(T var1);

   boolean modifiesA();

   boolean modifiesB();
}
