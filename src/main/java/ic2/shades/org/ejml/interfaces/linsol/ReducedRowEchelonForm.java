package ic2.shades.org.ejml.interfaces.linsol;

public interface ReducedRowEchelonForm<T extends ic2.shades.org.ejml.data.Matrix64F> {
  void reduce(T paramT, int paramInt);
  
  void setTolerance(double paramDouble);
}
