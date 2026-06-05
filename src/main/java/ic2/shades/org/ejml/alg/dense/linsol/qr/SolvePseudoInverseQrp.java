package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.TriangularSolver;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class SolvePseudoInverseQrp extends BaseLinearSolverQrp {
   private DenseMatrix64F Q = new DenseMatrix64F(1, 1);
   private DenseMatrix64F x_basic = new DenseMatrix64F(1, 1);

   public SolvePseudoInverseQrp(QRPDecomposition<DenseMatrix64F> decomposition, boolean norm2Solution) {
      super(decomposition, norm2Solution);
   }

   @Override
   public boolean setA(DenseMatrix64F A) {
      if (!super.setA(A)) {
         return false;
      }

      this.Q.reshape(A.numRows, A.numRows);
      this.decomposition.getQ(this.Q, false);
      return true;
   }

   public void solve(DenseMatrix64F B, DenseMatrix64F X) {
      if (X.numRows != this.numCols) {
         throw new IllegalArgumentException("Unexpected dimensions for X");
      }

      if (B.numRows == this.numRows && B.numCols == X.numCols) {
         int BnumCols = B.numCols;
         int[] pivots = this.decomposition.getPivots();

         for (int colB = 0; colB < BnumCols; colB++) {
            this.x_basic.reshape(this.numRows, 1);
            this.Y.reshape(this.numRows, 1);

            for (int i = 0; i < this.numRows; i++) {
               this.Y.data[i] = B.get(i, colB);
            }

            CommonOps.multTransA(this.Q, this.Y, this.x_basic);
            TriangularSolver.solveU(this.R11.data, this.x_basic.data, this.rank);
            this.x_basic.reshape(this.numCols, 1, true);

            for (int i = this.rank; i < this.numCols; i++) {
               this.x_basic.data[i] = 0.0;
            }

            if (this.norm2Solution && this.rank < this.numCols) {
               this.upgradeSolution(this.x_basic);
            }

            for (int i = 0; i < this.numCols; i++) {
               X.set(pivots[i], colB, this.x_basic.data[i]);
            }
         }
      } else {
         throw new IllegalArgumentException("Unexpected dimensions for B");
      }
   }

   @Override
   public boolean modifiesA() {
      return this.decomposition.inputModified();
   }

   @Override
   public boolean modifiesB() {
      return false;
   }
}
