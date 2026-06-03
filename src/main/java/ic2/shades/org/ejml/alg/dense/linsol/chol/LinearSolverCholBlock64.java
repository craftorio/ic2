package ic2.shades.org.ejml.alg.dense.linsol.chol;

import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverCholBlock64 extends LinearSolver_B64_to_D64 {
  public LinearSolverCholBlock64() {
    super((LinearSolver)new BlockCholeskyOuterSolver());
  }
  
  public void solve(DenseMatrix64F B, DenseMatrix64F X) {
    this.blockB.reshape(B.numRows, B.numCols, false);
    BlockMatrixOps.convert(B, this.blockB);
    this.alg.solve((Matrix64F)this.blockB, null);
    BlockMatrixOps.convert(this.blockB, X);
  }
}
