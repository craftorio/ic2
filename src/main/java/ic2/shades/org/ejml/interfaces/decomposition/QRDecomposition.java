package ic2.shades.org.ejml.interfaces.decomposition;

public interface QRDecomposition<T extends ic2.shades.org.ejml.data.Matrix64F> extends DecompositionInterface<T> {
  T getQ(T paramT, boolean paramBoolean);
  
  T getR(T paramT, boolean paramBoolean);
}
