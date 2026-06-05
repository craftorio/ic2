package ic2.shades.org.ejml.alg.block.linsol.chol;

import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.BlockTriangularSolver;
import ic2.shades.org.ejml.alg.block.decomposition.chol.CholeskyOuterForm_B64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class BlockCholeskyOuterSolver implements LinearSolver<BlockMatrix64F> {
   private CholeskyOuterForm_B64 chol = new CholeskyOuterForm_B64(true);
   private int blockLength;
   private double[] temp;

   public boolean setA(BlockMatrix64F A) {
      if (!this.chol.decompose(A)) {
         return false;
      }

      this.blockLength = A.blockLength;
      return true;
   }

   @Override
   public double quality() {
      return SpecializedOps.qualityTriangular(false, this.chol.getT((BlockMatrix64F)null));
   }

   public void solve(BlockMatrix64F B, BlockMatrix64F X) {
      if (B.blockLength != this.blockLength) {
         throw new IllegalArgumentException("Unexpected blocklength in B.");
      }

      D1Submatrix64F L = new D1Submatrix64F(this.chol.getT((BlockMatrix64F)null));
      if (X != null) {
         if (X.blockLength != this.blockLength) {
            throw new IllegalArgumentException("Unexpected blocklength in X.");
         }

         if (X.numRows != L.col1) {
            throw new IllegalArgumentException("Not enough rows in X");
         }
      }

      if (B.numRows != L.col1) {
         throw new IllegalArgumentException("Not enough rows in B");
      }

      BlockTriangularSolver.solve(this.blockLength, false, L, new D1Submatrix64F(B), false);
      BlockTriangularSolver.solve(this.blockLength, false, L, new D1Submatrix64F(B), true);
      if (X != null) {
         BlockMatrixOps.extractAligned(B, X);
      }
   }

   public void invert(BlockMatrix64F A_inv) {
      BlockMatrix64F T = this.chol.getT((BlockMatrix64F)null);
      if (A_inv.numRows == T.numRows && A_inv.numCols == T.numCols) {
         if (this.temp == null || this.temp.length < this.blockLength * this.blockLength) {
            this.temp = new double[this.blockLength * this.blockLength];
         }

         BlockMatrixOps.zeroTriangle(true, A_inv);
         D1Submatrix64F L = new D1Submatrix64F(T);
         D1Submatrix64F B = new D1Submatrix64F(A_inv);
         BlockTriangularSolver.invert(this.blockLength, false, L, B, this.temp);
         BlockTriangularSolver.solveL(this.blockLength, L, B, true);
      } else {
         throw new IllegalArgumentException("Unexpected number or rows and/or columns");
      }
   }

   @Override
   public boolean modifiesA() {
      return this.chol.inputModified();
   }

   @Override
   public boolean modifiesB() {
      return true;
   }
}
