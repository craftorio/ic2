package ic2.shades.org.ejml.interfaces.decomposition;

public interface BidiagonalDecomposition<T extends ic2.shades.org.ejml.data.ReshapeMatrix64F> extends DecompositionInterface<T> {
  T getB(T paramT, boolean paramBoolean);
  
  T getU(T paramT, boolean paramBoolean1, boolean paramBoolean2);
  
  T getV(T paramT, boolean paramBoolean1, boolean paramBoolean2);
  
  void getDiagonal(double[] paramArrayOfdouble1, double[] paramArrayOfdouble2);
}
