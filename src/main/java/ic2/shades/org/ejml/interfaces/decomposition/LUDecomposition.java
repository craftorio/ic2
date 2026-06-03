package ic2.shades.org.ejml.interfaces.decomposition;

public interface LUDecomposition<T extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<T> {
  T getLower(T paramT);
  
  T getUpper(T paramT);
  
  T getPivot(T paramT);
  
  boolean isSingular();
  
  double computeDeterminant();
}
