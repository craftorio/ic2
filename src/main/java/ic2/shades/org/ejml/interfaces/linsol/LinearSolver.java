package ic2.shades.org.ejml.interfaces.linsol;

public interface LinearSolver<T extends ic2.shades.org.ejml.data.Matrix64F> {
  boolean setA(T paramT);
  
  double quality();
  
  void solve(T paramT1, T paramT2);
  
  void invert(T paramT);
  
  boolean modifiesA();
  
  boolean modifiesB();
}
