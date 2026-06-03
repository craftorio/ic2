package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public interface AdjustableLinearSolver extends LinearSolver<DenseMatrix64F> {
  boolean addRowToA(double[] paramArrayOfdouble, int paramInt);
  
  boolean removeRowFromA(int paramInt);
}
