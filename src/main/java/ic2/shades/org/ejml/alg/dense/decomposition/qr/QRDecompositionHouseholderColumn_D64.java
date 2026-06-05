package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class QRDecompositionHouseholderColumn_D64 implements QRDecomposition<DenseMatrix64F> {
   protected double[][] dataQR;
   protected double[] v;
   protected int numCols;
   protected int numRows;
   protected int minLength;
   protected double[] gammas;
   protected double gamma;
   protected double tau;
   protected boolean error;

   public void setExpectedMaxSize(int numRows, int numCols) {
      this.numCols = numCols;
      this.numRows = numRows;
      this.minLength = Math.min(numCols, numRows);
      int maxLength = Math.max(numCols, numRows);
      if (this.dataQR == null || this.dataQR.length < numCols || this.dataQR[0].length < numRows) {
         this.dataQR = new double[numCols][numRows];
         this.v = new double[maxLength];
         this.gammas = new double[this.minLength];
      }

      if (this.v.length < maxLength) {
         this.v = new double[maxLength];
      }

      if (this.gammas.length < this.minLength) {
         this.gammas = new double[this.minLength];
      }
   }

   public double[][] getQR() {
      return this.dataQR;
   }

   public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact) {
      if (compact) {
         if (Q == null) {
            Q = CommonOps.identity(this.numRows, this.minLength);
         } else {
            if (Q.numRows != this.numRows || Q.numCols != this.minLength) {
               throw new IllegalArgumentException("Unexpected matrix dimension.");
            }

            CommonOps.setIdentity(Q);
         }
      } else if (Q == null) {
         Q = CommonOps.identity(this.numRows);
      } else {
         if (Q.numRows != this.numRows || Q.numCols != this.numRows) {
            throw new IllegalArgumentException("Unexpected matrix dimension.");
         }

         CommonOps.setIdentity(Q);
      }

      for (int j = this.minLength - 1; j >= 0; j--) {
         double[] u = this.dataQR[j];
         double vv = u[j];
         u[j] = 1.0;
         QrHelperFunctions.rank1UpdateMultR(Q, u, this.gammas[j], j, j, this.numRows, this.v);
         u[j] = vv;
      }

      return Q;
   }

   public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
      if (R == null) {
         if (compact) {
            R = new DenseMatrix64F(this.minLength, this.numCols);
         } else {
            R = new DenseMatrix64F(this.numRows, this.numCols);
         }
      } else {
         if (compact) {
            if (R.numCols != this.numCols || R.numRows != this.minLength) {
               throw new IllegalArgumentException(
                  "Unexpected dimensions: found( " + R.numRows + " " + R.numCols + " ) expected( " + this.minLength + " " + this.numCols + " )"
               );
            }
         } else if (R.numCols != this.numCols || R.numRows != this.numRows) {
            throw new IllegalArgumentException("Unexpected dimensions");
         }

         for (int i = 0; i < R.numRows; i++) {
            int min = Math.min(i, R.numCols);

            for (int j = 0; j < min; j++) {
               R.set(i, j, 0.0);
            }
         }
      }

      for (int j = 0; j < this.numCols; j++) {
         double[] colR = this.dataQR[j];
         int l = Math.min(j, this.numRows - 1);

         for (int i = 0; i <= l; i++) {
            double val = colR[i];
            R.set(i, j, val);
         }
      }

      return R;
   }

   public boolean decompose(DenseMatrix64F A) {
      this.setExpectedMaxSize(A.numRows, A.numCols);
      this.convertToColumnMajor(A);
      this.error = false;

      for (int j = 0; j < this.minLength; j++) {
         this.householder(j);
         this.updateA(j);
      }

      return !this.error;
   }

   @Override
   public boolean inputModified() {
      return false;
   }

   protected void convertToColumnMajor(DenseMatrix64F A) {
      for (int x = 0; x < this.numCols; x++) {
         double[] colQ = this.dataQR[x];

         for (int y = 0; y < this.numRows; y++) {
            colQ[y] = A.data[y * this.numCols + x];
         }
      }
   }

   protected void householder(int j) {
      double[] u = this.dataQR[j];
      double max = QrHelperFunctions.findMax(u, j, this.numRows - j);
      if (max == 0.0) {
         this.gamma = 0.0;
         this.error = true;
      } else {
         this.tau = QrHelperFunctions.computeTauAndDivide(j, this.numRows, u, max);
         double u_0 = u[j] + this.tau;
         QrHelperFunctions.divideElements(j + 1, this.numRows, u, u_0);
         this.gamma = u_0 / this.tau;
         this.tau *= max;
         u[j] = -this.tau;
      }

      this.gammas[j] = this.gamma;
   }

   protected void updateA(int w) {
      double[] u = this.dataQR[w];

      for (int j = w + 1; j < this.numCols; j++) {
         double[] colQ = this.dataQR[j];
         double val = colQ[w];

         for (int k = w + 1; k < this.numRows; k++) {
            val += u[k] * colQ[k];
         }

         val *= this.gamma;
         colQ[w] -= val;

         for (int i = w + 1; i < this.numRows; i++) {
            colQ[i] -= u[i] * val;
         }
      }
   }

   public double[] getGammas() {
      return this.gammas;
   }
}
