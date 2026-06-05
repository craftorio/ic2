package ic2.shades.org.ejml.alg.dense.decomposition.chol;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public abstract class CholeskyDecompositionCommon_D64 implements CholeskyDecomposition<DenseMatrix64F> {
   protected int maxWidth = -1;
   protected int n;
   protected DenseMatrix64F T;
   protected double[] t;
   protected double[] vv;
   protected boolean lower;

   public CholeskyDecompositionCommon_D64(boolean lower) {
      this.lower = lower;
   }

   public void setExpectedMaxSize(int numRows, int numCols) {
      if (numRows != numCols) {
         throw new IllegalArgumentException("Can only decompose square matrices");
      }

      this.maxWidth = numCols;
      this.vv = new double[this.maxWidth];
   }

   @Override
   public boolean isLower() {
      return this.lower;
   }

   public boolean decompose(DenseMatrix64F mat) {
      if (mat.numRows > this.maxWidth) {
         this.setExpectedMaxSize(mat.numRows, mat.numCols);
      } else if (mat.numRows != mat.numCols) {
         throw new IllegalArgumentException("Must be a square matrix.");
      }

      this.n = mat.numRows;
      this.T = mat;
      this.t = this.T.data;
      return this.lower ? this.decomposeLower() : this.decomposeUpper();
   }

   @Override
   public boolean inputModified() {
      return true;
   }

   protected abstract boolean decomposeLower();

   protected abstract boolean decomposeUpper();

   public DenseMatrix64F getT(DenseMatrix64F T) {
      if (T == null) {
         T = new DenseMatrix64F(this.n, this.n);
      } else {
         if (T.numRows != this.n || T.numCols != this.n) {
            throw new IllegalArgumentException("Unexpected matrix dimension for T.");
         }

         CommonOps.fill(T, 0.0);
      }

      if (this.lower) {
         for (int i = 0; i < this.n; i++) {
            for (int j = 0; j <= i; j++) {
               T.unsafe_set(i, j, this.T.unsafe_get(i, j));
            }
         }
      } else {
         for (int i = 0; i < this.n; i++) {
            for (int j = i; j < this.n; j++) {
               T.unsafe_set(i, j, this.T.unsafe_get(i, j));
            }
         }
      }

      return T;
   }

   public DenseMatrix64F getT() {
      return this.T;
   }

   public double[] _getVV() {
      return this.vv;
   }
}
