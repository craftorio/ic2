package ic2.shades.org.ejml.alg.block.decomposition.hessenberg;

import ic2.shades.org.ejml.alg.block.BlockInnerMultiplication;
import ic2.shades.org.ejml.alg.block.BlockMultiplication;
import ic2.shades.org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class TridiagonalDecompositionHouseholder_B64 implements TridiagonalSimilarDecomposition<BlockMatrix64F> {
   protected BlockMatrix64F A;
   protected BlockMatrix64F V = new BlockMatrix64F(1, 1);
   protected BlockMatrix64F tmp = new BlockMatrix64F(1, 1);
   protected double[] gammas = new double[1];
   protected DenseMatrix64F zerosM = new DenseMatrix64F(1, 1);

   public BlockMatrix64F getT(BlockMatrix64F T) {
      if (T == null) {
         T = new BlockMatrix64F(this.A.numRows, this.A.numCols, this.A.blockLength);
      } else {
         if (T.numRows != this.A.numRows || T.numCols != this.A.numCols) {
            throw new IllegalArgumentException("T must have the same dimensions as the input matrix");
         }

         CommonOps.fill(T, 0.0);
      }

      T.set(0, 0, this.A.data[0]);

      for (int i = 1; i < this.A.numRows; i++) {
         double d = this.A.get(i - 1, i);
         T.set(i, i, this.A.get(i, i));
         T.set(i - 1, i, d);
         T.set(i, i - 1, d);
      }

      return T;
   }

   public BlockMatrix64F getQ(BlockMatrix64F Q, boolean transposed) {
      Q = QRDecompositionHouseholder_B64.initializeQ(Q, this.A.numRows, this.A.numCols, this.A.blockLength, false);
      int height = Math.min(this.A.blockLength, this.A.numRows);
      this.V.reshape(height, this.A.numCols, false);
      this.tmp.reshape(height, this.A.numCols, false);
      D1Submatrix64F subQ = new D1Submatrix64F(Q);
      D1Submatrix64F subU = new D1Submatrix64F(this.A);
      D1Submatrix64F subW = new D1Submatrix64F(this.V);
      D1Submatrix64F tmp = new D1Submatrix64F(this.tmp);
      int N = this.A.numRows;
      int start = N - N % this.A.blockLength;
      if (start == N) {
         start -= this.A.blockLength;
      }

      if (start < 0) {
         start = 0;
      }

      for (int i = start; i >= 0; i -= this.A.blockLength) {
         int blockSize = Math.min(this.A.blockLength, N - i);
         subW.col0 = i;
         subW.row1 = blockSize;
         subW.original.reshape(subW.row1, subW.col1, false);
         if (transposed) {
            tmp.row0 = i;
            tmp.row1 = this.A.numCols;
            tmp.col0 = 0;
            tmp.col1 = blockSize;
         } else {
            tmp.col0 = i;
            tmp.row1 = blockSize;
         }

         tmp.original.reshape(tmp.row1, tmp.col1, false);
         subU.col0 = i;
         subU.row0 = i;
         subU.row1 = subU.row0 + blockSize;
         this.copyZeros(subU);
         TridiagonalHelper_B64.computeW_row(this.A.blockLength, subU, subW, this.gammas, i);
         subQ.col0 = i;
         subQ.row0 = i;
         if (transposed) {
            BlockMultiplication.multTransB(this.A.blockLength, subQ, subU, tmp);
         } else {
            BlockMultiplication.mult(this.A.blockLength, subU, subQ, tmp);
         }

         if (transposed) {
            BlockMultiplication.multPlus(this.A.blockLength, tmp, subW, subQ);
         } else {
            BlockMultiplication.multPlusTransA(this.A.blockLength, subW, tmp, subQ);
         }

         this.replaceZeros(subU);
      }

      return Q;
   }

   private void copyZeros(D1Submatrix64F subU) {
      int N = Math.min(this.A.blockLength, subU.col1 - subU.col0);

      for (int i = 0; i < N; i++) {
         for (int j = 0; j <= i; j++) {
            this.zerosM.unsafe_set(i, j, subU.get(i, j));
            subU.set(i, j, 0.0);
         }

         if (subU.col0 + i + 1 < subU.original.numCols) {
            this.zerosM.unsafe_set(i, i + 1, subU.get(i, i + 1));
            subU.set(i, i + 1, 1.0);
         }
      }
   }

   private void replaceZeros(D1Submatrix64F subU) {
      int N = Math.min(this.A.blockLength, subU.col1 - subU.col0);

      for (int i = 0; i < N; i++) {
         for (int j = 0; j <= i; j++) {
            subU.set(i, j, this.zerosM.get(i, j));
         }

         if (subU.col0 + i + 1 < subU.original.numCols) {
            subU.set(i, i + 1, this.zerosM.get(i, i + 1));
         }
      }
   }

   @Override
   public void getDiagonal(double[] diag, double[] off) {
      diag[0] = this.A.data[0];

      for (int i = 1; i < this.A.numRows; i++) {
         diag[i] = this.A.get(i, i);
         off[i - 1] = this.A.get(i - 1, i);
      }
   }

   public boolean decompose(BlockMatrix64F orig) {
      if (orig.numCols != orig.numRows) {
         throw new IllegalArgumentException("Input matrix must be square.");
      }

      this.init(orig);
      D1Submatrix64F subA = new D1Submatrix64F(this.A);
      D1Submatrix64F subV = new D1Submatrix64F(this.V);
      D1Submatrix64F subU = new D1Submatrix64F(this.A);
      int N = orig.numCols;

      for (int i = 0; i < N; i += this.A.blockLength) {
         int height = Math.min(this.A.blockLength, this.A.numRows - i);
         subA.col0 = subU.col0 = i;
         subA.row0 = subU.row0 = i;
         subU.row1 = subU.row0 + height;
         subV.col0 = i;
         subV.row1 = height;
         subV.original.reshape(subV.row1, subV.col1, false);
         TridiagonalHelper_B64.tridiagUpperRow(this.A.blockLength, subA, this.gammas, subV);
         if (subU.row1 < orig.numCols) {
            double before = subU.get(this.A.blockLength - 1, this.A.blockLength);
            subU.set(this.A.blockLength - 1, this.A.blockLength, 1.0);
            multPlusTransA(this.A.blockLength, subU, subV, subA);
            multPlusTransA(this.A.blockLength, subV, subU, subA);
            subU.set(this.A.blockLength - 1, this.A.blockLength, before);
         }
      }

      return true;
   }

   public static void multPlusTransA(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
      int heightA = Math.min(blockLength, A.row1 - A.row0);

      for (int i = C.row0 + blockLength; i < C.row1; i += blockLength) {
         int heightC = Math.min(blockLength, C.row1 - i);
         int indexA = A.row0 * A.original.numCols + (i - C.row0 + A.col0) * heightA;

         for (int j = i; j < C.col1; j += blockLength) {
            int widthC = Math.min(blockLength, C.col1 - j);
            int indexC = i * C.original.numCols + j * heightC;
            int indexB = B.row0 * B.original.numCols + (j - C.col0 + B.col0) * heightA;
            BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, heightC, widthC);
         }
      }
   }

   private void init(BlockMatrix64F orig) {
      this.A = orig;
      int height = Math.min(this.A.blockLength, this.A.numRows);
      this.V.reshape(height, this.A.numCols, this.A.blockLength, false);
      this.tmp.reshape(height, this.A.numCols, this.A.blockLength, false);
      if (this.gammas.length < this.A.numCols) {
         this.gammas = new double[this.A.numCols];
      }

      this.zerosM.reshape(this.A.blockLength, this.A.blockLength + 1, false);
   }

   @Override
   public boolean inputModified() {
      return true;
   }
}
