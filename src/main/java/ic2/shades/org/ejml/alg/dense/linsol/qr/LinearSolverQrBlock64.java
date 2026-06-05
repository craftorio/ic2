package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.block.linsol.qr.BlockQrHouseHolderSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64;

public class LinearSolverQrBlock64 extends LinearSolver_B64_to_D64 {
   public LinearSolverQrBlock64() {
      super(new BlockQrHouseHolderSolver());
   }
}
