package ic2.shades.org.ejml.interfaces.decomposition;

public interface SingularValueDecomposition<T extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<T> {
  double[] getSingularValues();
  
  int numberOfSingularValues();
  
  boolean isCompact();
  
  T getU(T paramT, boolean paramBoolean);
  
  T getV(T paramT, boolean paramBoolean);
  
  T getW(T paramT);
  
  int numRows();
  
  int numCols();
}
