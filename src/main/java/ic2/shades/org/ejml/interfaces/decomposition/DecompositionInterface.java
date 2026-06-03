package ic2.shades.org.ejml.interfaces.decomposition;

public interface DecompositionInterface<T extends ic2.shades.org.ejml.data.Matrix64F> {
  boolean decompose(T paramT);
  
  boolean inputModified();
}
