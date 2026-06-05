package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.ops.MatrixIO;

public class BlockMatrix64F extends D1Matrix64F {
   public int blockLength;

   public BlockMatrix64F(int numRows, int numCols, int blockLength) {
      this.data = new double[numRows * numCols];
      this.blockLength = blockLength;
      this.numRows = numRows;
      this.numCols = numCols;
   }

   public BlockMatrix64F(int numRows, int numCols) {
      this(numRows, numCols, EjmlParameters.BLOCK_WIDTH);
   }

   public BlockMatrix64F() {
   }

   public void set(BlockMatrix64F A) {
      this.blockLength = A.blockLength;
      this.numRows = A.numRows;
      this.numCols = A.numCols;
      int N = this.numCols * this.numRows;
      if (this.data.length < N) {
         this.data = new double[N];
      }

      System.arraycopy(A.data, 0, this.data, 0, N);
   }

   public static BlockMatrix64F wrap(double[] data, int numRows, int numCols, int blockLength) {
      BlockMatrix64F ret = new BlockMatrix64F();
      ret.data = data;
      ret.numRows = numRows;
      ret.numCols = numCols;
      ret.blockLength = blockLength;
      return ret;
   }

   @Override
   public double[] getData() {
      return this.data;
   }

   @Override
   public void reshape(int numRows, int numCols, boolean saveValues) {
      if (numRows * numCols <= this.data.length) {
         this.numRows = numRows;
         this.numCols = numCols;
      } else {
         double[] data = new double[numRows * numCols];
         if (saveValues) {
            System.arraycopy(this.data, 0, data, 0, this.getNumElements());
         }

         this.numRows = numRows;
         this.numCols = numCols;
         this.data = data;
      }
   }

   public void reshape(int numRows, int numCols, int blockLength, boolean saveValues) {
      this.blockLength = blockLength;
      this.reshape(numRows, numCols, saveValues);
   }

   @Override
   public int getIndex(int row, int col) {
      int blockRow = row / this.blockLength;
      int blockCol = col / this.blockLength;
      int localHeight = Math.min(this.numRows - blockRow * this.blockLength, this.blockLength);
      int index = blockRow * this.blockLength * this.numCols + blockCol * localHeight * this.blockLength;
      int localLength = Math.min(this.numCols - this.blockLength * blockCol, this.blockLength);
      row %= this.blockLength;
      col %= this.blockLength;
      return index + localLength * row + col;
   }

   @Override
   public double get(int row, int col) {
      return this.data[this.getIndex(row, col)];
   }

   @Override
   public double unsafe_get(int row, int col) {
      return this.data[this.getIndex(row, col)];
   }

   @Override
   public void set(int row, int col, double val) {
      this.data[this.getIndex(row, col)] = val;
   }

   @Override
   public void unsafe_set(int row, int col, double val) {
      this.data[this.getIndex(row, col)] = val;
   }

   @Override
   public int getNumRows() {
      return this.numRows;
   }

   @Override
   public int getNumCols() {
      return this.numCols;
   }

   @Override
   public int getNumElements() {
      return this.numRows * this.numCols;
   }

   @Override
   public void print() {
      MatrixIO.print(System.out, this);
   }

   public BlockMatrix64F copy() {
      BlockMatrix64F A = new BlockMatrix64F(this.numRows, this.numCols, this.blockLength);
      A.set(this);
      return A;
   }
}
