package ic2.shades.org.ejml.alg.dense.linsol;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverSafe<T extends ReshapeMatrix64F> implements LinearSolver<T> {
  private LinearSolver<T> alg;
  
  private T A;
  
  private T B;
  
  public LinearSolverSafe(LinearSolver<T> alg) {
    this.alg = alg;
  }
  
  public boolean setA(T A) {
    if (this.alg.modifiesA()) {
      if (this.A == null) {
        this.A = (T)A.copy();
      } else {
        if (((ReshapeMatrix64F)this.A).numRows != ((ReshapeMatrix64F)A).numRows || ((ReshapeMatrix64F)this.A).numCols != ((ReshapeMatrix64F)A).numCols)
          this.A.reshape(((ReshapeMatrix64F)A).numRows, ((ReshapeMatrix64F)A).numCols, false); 
        this.A.set((ReshapeMatrix64F)A);
      } 
      return this.alg.setA((Matrix64F)this.A);
    } 
    return this.alg.setA((Matrix64F)A);
  }
  
  public double quality() {
    return this.alg.quality();
  }
  
  public void solve(T B, T X) {
    if (this.alg.modifiesB()) {
      if (this.B == null) {
        this.B = (T)B.copy();
      } else {
        if (((ReshapeMatrix64F)this.B).numRows != ((ReshapeMatrix64F)B).numRows || ((ReshapeMatrix64F)this.B).numCols != ((ReshapeMatrix64F)B).numCols)
          this.B.reshape(((ReshapeMatrix64F)this.A).numRows, ((ReshapeMatrix64F)B).numCols, false); 
        this.B.set((ReshapeMatrix64F)B);
      } 
      B = this.B;
    } 
    this.alg.solve((Matrix64F)B, (Matrix64F)X);
  }
  
  public void invert(T A_inv) {
    this.alg.invert((Matrix64F)A_inv);
  }
  
  public boolean modifiesA() {
    return false;
  }
  
  public boolean modifiesB() {
    return false;
  }
}
