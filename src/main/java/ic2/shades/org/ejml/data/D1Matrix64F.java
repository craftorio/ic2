package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.alg.dense.mult.MatrixDimensionException;

public abstract class D1Matrix64F extends ReshapeMatrix64F {
   public double[] data;

   public double[] getData() {
      return this.data;
   }

   public void setData(double[] data) {
      this.data = data;
   }

   public abstract int getIndex(int var1, int var2);

   public void set(D1Matrix64F b) {
      if (this.numRows == b.numRows && this.numCols == b.numCols) {
         int dataLength = b.getNumElements();
         System.arraycopy(b.data, 0, this.data, 0, dataLength);
      } else {
         throw new MatrixDimensionException("The two matrices do not have compatible shapes.");
      }
   }

   public double get(int index) {
      return this.data[index];
   }

   public double set(int index, double val) {
      return this.data[index] = val;
   }

   public double plus(int index, double val) {
      return this.data[index] = this.data[index] + val;
   }

   public double minus(int index, double val) {
      return this.data[index] = this.data[index] - val;
   }

   public double times(int index, double val) {
      return this.data[index] = this.data[index] * val;
   }

   public double div(int index, double val) {
      return this.data[index] = this.data[index] / val;
   }
}
