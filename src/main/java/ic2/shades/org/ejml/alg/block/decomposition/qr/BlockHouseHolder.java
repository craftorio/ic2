package ic2.shades.org.ejml.alg.block.decomposition.qr;

import ic2.shades.org.ejml.alg.block.BlockInnerMultiplication;
import ic2.shades.org.ejml.alg.block.BlockVectorOps;
import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockHouseHolder {
   public static boolean decomposeQR_block_col(int blockLength, D1Submatrix64F Y, double[] gamma) {
      int width = Y.col1 - Y.col0;
      int height = Y.row1 - Y.row0;
      int min = Math.min(width, height);

      for (int i = 0; i < min; i++) {
         if (!computeHouseHolderCol(blockLength, Y, gamma, i)) {
            return false;
         }

         rank1UpdateMultR_Col(blockLength, Y, i, gamma[Y.col0 + i]);
      }

      return true;
   }

   public static boolean computeHouseHolderCol(int blockLength, D1Submatrix64F Y, double[] gamma, int i) {
      double max = findMaxCol(blockLength, Y, i);
      if (max == 0.0) {
         return false;
      }

      double tau = computeTauAndDivideCol(blockLength, Y, i, max);
      double u_0 = Y.get(i, i) + tau;
      divideElementsCol(blockLength, Y, i, u_0);
      gamma[Y.col0 + i] = u_0 / tau;
      tau *= max;
      Y.set(i, i, -tau);
      return true;
   }

   public static boolean computeHouseHolderRow(int blockLength, D1Submatrix64F Y, double[] gamma, int i) {
      double max = findMaxRow(blockLength, Y, i, i + 1);
      if (max == 0.0) {
         return false;
      }

      double tau = computeTauAndDivideRow(blockLength, Y, i, i + 1, max);
      double u_0 = Y.get(i, i + 1) + tau;
      BlockVectorOps.div_row(blockLength, Y, i, u_0, Y, i, i + 1, Y.col1 - Y.col0);
      gamma[Y.row0 + i] = u_0 / tau;
      Y.set(i, i + 1, -tau * max);
      return true;
   }

   public static void rank1UpdateMultR_Col(int blockLength, D1Submatrix64F A, int col, double gamma) {
      int width = Math.min(blockLength, A.col1 - A.col0);
      double[] dataA = A.original.data;

      for (int j = col + 1; j < width; j++) {
         double total = innerProdCol(blockLength, A, col, width, j, width);
         total *= gamma;

         for (int i = A.row0; i < A.row1; i += blockLength) {
            int height = Math.min(blockLength, A.row1 - i);
            int indexU = i * A.original.numCols + height * A.col0 + col;
            int indexA = i * A.original.numCols + height * A.col0 + j;
            if (i == A.row0) {
               indexU += width * (col + 1);
               indexA += width * col;
               dataA[indexA] -= total;
               indexA += width;

               for (int k = col + 1; k < height; indexA += width) {
                  dataA[indexA] -= total * dataA[indexU];
                  k++;
                  indexU += width;
               }
            } else {
               for (int endU = indexU + width * height; indexU != endU; indexA += width) {
                  dataA[indexA] -= total * dataA[indexU];
                  indexU += width;
               }
            }
         }
      }
   }

   public static void rank1UpdateMultR_TopRow(int blockLength, D1Submatrix64F A, int col, double gamma) {
      double[] dataA = A.original.data;
      int widthCol = Math.min(blockLength, A.col1 - col);

      for (int colStartJ = A.col0 + blockLength; colStartJ < A.col1; colStartJ += blockLength) {
         int widthJ = Math.min(blockLength, A.col1 - colStartJ);

         for (int j = 0; j < widthJ; j++) {
            double total = innerProdCol(blockLength, A, col, widthCol, colStartJ - A.col0 + j, widthJ) * gamma;
            int i = A.row0;
            int height = Math.min(blockLength, A.row1 - i);
            int indexU = i * A.original.numCols + height * A.col0 + col;
            int indexA = i * A.original.numCols + height * colStartJ + j;
            indexU += widthCol * (col + 1);
            indexA += widthJ * col;
            dataA[indexA] -= total;
            indexA += widthJ;

            for (int k = col + 1; k < height; indexA += widthJ) {
               dataA[indexA] -= total * dataA[indexU];
               k++;
               indexU += widthCol;
            }
         }
      }
   }

   public static void rank1UpdateMultL_Row(int blockLength, D1Submatrix64F A, int row, int colStart, double gamma) {
      int height = Math.min(blockLength, A.row1 - A.row0);
      double[] dataA = A.original.data;
      int zeroOffset = colStart - row;

      for (int i = row + 1; i < height; i++) {
         double total = innerProdRow(blockLength, A, row, A, i, zeroOffset);
         total *= gamma;

         for (int j = A.col0; j < A.col1; j += blockLength) {
            int width = Math.min(blockLength, A.col1 - j);
            int indexU = A.row0 * A.original.numCols + height * j + row * width;
            int indexA = A.row0 * A.original.numCols + height * j + i * width;
            if (j == A.col0) {
               indexU += colStart + 1;
               indexA += colStart;
               dataA[indexA++] -= total;

               for (int k = colStart + 1; k < width; k++) {
                  dataA[indexA++] -= total * dataA[indexU++];
               }
            } else {
               for (int k = 0; k < width; k++) {
                  dataA[indexA++] -= total * dataA[indexU++];
               }
            }
         }
      }
   }

   public static void rank1UpdateMultL_LeftCol(int blockLength, D1Submatrix64F A, int row, double gamma, int zeroOffset) {
      int heightU = Math.min(blockLength, A.row1 - A.row0);
      int width = Math.min(blockLength, A.col1 - A.col0);
      double[] data = A.original.data;

      for (int blockStart = A.row0 + blockLength; blockStart < A.row1; blockStart += blockLength) {
         int heightA = Math.min(blockLength, A.row1 - blockStart);

         for (int i = 0; i < heightA; i++) {
            double total = innerProdRow(blockLength, A, row, A, i + (blockStart - A.row0), zeroOffset);
            total *= gamma;
            int indexU = A.row0 * A.original.numCols + heightU * A.col0 + row * width;
            int indexA = blockStart * A.original.numCols + heightA * A.col0 + i * width;
            indexU += zeroOffset + 1;
            indexA += zeroOffset;
            data[indexA++] -= total;

            for (int k = zeroOffset + 1; k < width; k++) {
               data[indexA++] -= total * data[indexU++];
            }
         }
      }
   }

   public static double innerProdCol(int blockLength, D1Submatrix64F A, int colA, int widthA, int colB, int widthB) {
      double total = 0.0;
      double[] data = A.original.data;
      int colBlockA = A.col0 + colA - colA % blockLength;
      int colBlockB = A.col0 + colB - colB % blockLength;
      colA %= blockLength;
      colB %= blockLength;

      for (int i = A.row0; i < A.row1; i += blockLength) {
         int height = Math.min(blockLength, A.row1 - i);
         int indexA = i * A.original.numCols + height * colBlockA + colA;
         int indexB = i * A.original.numCols + height * colBlockB + colB;
         if (i == A.row0) {
            indexA += widthA * (colA + 1);
            indexB += widthB * colA;
            total = data[indexB];
            indexB += widthB;

            for (int endA = indexA + (height - colA - 1) * widthA; indexA != endA; indexB += widthB) {
               total += data[indexA] * data[indexB];
               indexA += widthA;
            }
         } else {
            for (int endA = indexA + widthA * height; indexA != endA; indexB += widthB) {
               total += data[indexA] * data[indexB];
               indexA += widthA;
            }
         }
      }

      return total;
   }

   public static double innerProdRow(int blockLength, D1Submatrix64F A, int rowA, D1Submatrix64F B, int rowB, int zeroOffset) {
      int offset = rowA + zeroOffset;
      if (offset + B.col0 >= B.col1) {
         return 0.0;
      }

      double total = B.get(rowB, offset);
      return total + BlockVectorOps.dot_row(blockLength, A, rowA, B, rowB, offset + 1, A.col1 - A.col0);
   }

   public static void add_row(
      int blockLength, D1Submatrix64F A, int rowA, double alpha, D1Submatrix64F B, int rowB, double beta, D1Submatrix64F C, int rowC, int zeroOffset, int end
   ) {
      int offset = rowA + zeroOffset;
      if (C.col0 + offset < C.col1) {
         C.set(rowC, offset, alpha + B.get(rowB, offset) * beta);
         BlockVectorOps.add_row(blockLength, A, rowA, alpha, B, rowB, beta, C, rowC, offset + 1, end);
      }
   }

   public static void divideElementsCol(int blockLength, D1Submatrix64F Y, int col, double val) {
      int width = Math.min(blockLength, Y.col1 - Y.col0);
      double[] dataY = Y.original.data;

      for (int i = Y.row0; i < Y.row1; i += blockLength) {
         int height = Math.min(blockLength, Y.row1 - i);
         int index = i * Y.original.numCols + height * Y.col0 + col;
         if (i == Y.row0) {
            index += width * (col + 1);

            for (int k = col + 1; k < height; index += width) {
               dataY[index] /= val;
               k++;
            }
         } else {
            for (int endIndex = index + width * height; index != endIndex; index += width) {
               dataY[index] /= val;
            }
         }
      }
   }

   public static void scale_row(int blockLength, D1Submatrix64F Y, D1Submatrix64F W, int row, int zeroOffset, double val) {
      int offset = row + zeroOffset;
      if (offset < W.col1 - W.col0) {
         W.set(row, offset, val);
         BlockVectorOps.scale_row(blockLength, Y, row, val, W, row, offset + 1, Y.col1 - Y.col0);
      }
   }

   public static double computeTauAndDivideCol(int blockLength, D1Submatrix64F Y, int col, double max) {
      int width = Math.min(blockLength, Y.col1 - Y.col0);
      double[] dataY = Y.original.data;
      double top = 0.0;
      double norm2 = 0.0;

      for (int i = Y.row0; i < Y.row1; i += blockLength) {
         int height = Math.min(blockLength, Y.row1 - i);
         int index = i * Y.original.numCols + height * Y.col0 + col;
         if (i == Y.row0) {
            index += width * col;
            top = dataY[index] /= max;
            norm2 += top * top;
            index += width;

            for (int k = col + 1; k < height; index += width) {
               double val = dataY[index] /= max;
               norm2 += val * val;
               k++;
            }
         } else {
            for (int k = 0; k < height; index += width) {
               double val = dataY[index] /= max;
               norm2 += val * val;
               k++;
            }
         }
      }

      norm2 = Math.sqrt(norm2);
      if (top < 0.0) {
         norm2 = -norm2;
      }

      return norm2;
   }

   public static double computeTauAndDivideRow(int blockLength, D1Submatrix64F Y, int row, int colStart, double max) {
      int height = Math.min(blockLength, Y.row1 - Y.row0);
      double[] dataY = Y.original.data;
      double top = 0.0;
      double norm2 = 0.0;
      int startJ = Y.col0 + colStart - colStart % blockLength;
      colStart %= blockLength;

      for (int j = startJ; j < Y.col1; j += blockLength) {
         int width = Math.min(blockLength, Y.col1 - j);
         int index = Y.row0 * Y.original.numCols + height * j + row * width;
         if (j == startJ) {
            index += colStart;
            top = dataY[index] /= max;
            norm2 += top * top;
            index++;

            for (int k = colStart + 1; k < width; k++) {
               double val = dataY[index++] /= max;
               norm2 += val * val;
            }
         } else {
            for (int k = 0; k < width; k++) {
               double val = dataY[index++] /= max;
               norm2 += val * val;
            }
         }
      }

      norm2 = Math.sqrt(norm2);
      if (top < 0.0) {
         norm2 = -norm2;
      }

      return norm2;
   }

   public static double findMaxCol(int blockLength, D1Submatrix64F Y, int col) {
      int width = Math.min(blockLength, Y.col1 - Y.col0);
      double[] dataY = Y.original.data;
      double max = 0.0;

      for (int i = Y.row0; i < Y.row1; i += blockLength) {
         int height = Math.min(blockLength, Y.row1 - i);
         int index = i * Y.original.numCols + height * Y.col0 + col;
         if (i == Y.row0) {
            index += width * col;

            for (int k = col; k < height; index += width) {
               double v = Math.abs(dataY[index]);
               if (v > max) {
                  max = v;
               }

               k++;
            }
         } else {
            for (int k = 0; k < height; index += width) {
               double v = Math.abs(dataY[index]);
               if (v > max) {
                  max = v;
               }

               k++;
            }
         }
      }

      return max;
   }

   public static double findMaxRow(int blockLength, D1Submatrix64F Y, int row, int colStart) {
      int height = Math.min(blockLength, Y.row1 - Y.row0);
      double[] dataY = Y.original.data;
      double max = 0.0;

      for (int j = Y.col0; j < Y.col1; j += blockLength) {
         int width = Math.min(blockLength, Y.col1 - j);
         int index = Y.row0 * Y.original.numCols + height * j + row * width;
         if (j == Y.col0) {
            index += colStart;

            for (int k = colStart; k < width; k++) {
               double v = Math.abs(dataY[index++]);
               if (v > max) {
                  max = v;
               }
            }
         } else {
            for (int k = 0; k < width; k++) {
               double v = Math.abs(dataY[index++]);
               if (v > max) {
                  max = v;
               }
            }
         }
      }

      return max;
   }

   public static void computeW_Column(int blockLength, D1Submatrix64F Y, D1Submatrix64F W, double[] temp, double[] beta, int betaIndex) {
      int widthB = W.col1 - W.col0;
      initializeW(blockLength, W, Y, widthB, beta[betaIndex++]);
      int min = Math.min(widthB, W.row1 - W.row0);

      for (int j = 1; j < min; j++) {
         computeY_t_V(blockLength, Y, j, temp);
         computeZ(blockLength, Y, W, j, temp, beta[betaIndex++]);
      }
   }

   public static void initializeW(int blockLength, D1Submatrix64F W, D1Submatrix64F Y, int widthB, double b) {
      double[] dataW = W.original.data;
      double[] dataY = Y.original.data;
      int i = W.row0;

      while (i < W.row1) {
         int heightW = Math.min(blockLength, W.row1 - i);
         int indexW = i * W.original.numCols + heightW * W.col0;
         int indexY = i * Y.original.numCols + heightW * Y.col0;
         if (i == W.row0) {
            dataW[indexW] = -b;
            indexW += widthB;
            indexY += widthB;

            for (int k = 1; k < heightW; indexY += widthB) {
               dataW[indexW] = -b * dataY[indexY];
               k++;
               indexW += widthB;
            }
         } else {
            for (int k = 0; k < heightW; indexY += widthB) {
               dataW[indexW] = -b * dataY[indexY];
               k++;
               indexW += widthB;
            }
         }

         i += blockLength;
      }
   }

   public static void computeZ(int blockLength, D1Submatrix64F Y, D1Submatrix64F W, int col, double[] temp, double beta) {
      int width = Y.col1 - Y.col0;
      double[] dataW = W.original.data;
      double[] dataY = Y.original.data;
      int colsW = W.original.numCols;
      double beta_neg = -beta;
      int i = Y.row0;

      while (i < Y.row1) {
         int heightW = Math.min(blockLength, Y.row1 - i);
         int indexW = i * colsW + heightW * W.col0;
         int indexZ = i * colsW + heightW * W.col0 + col;
         int indexV = i * Y.original.numCols + heightW * Y.col0 + col;
         if (i == Y.row0) {
            for (int k = 0; k < heightW; indexV += width) {
               double total = 0.0;

               for (int j = 0; j < col; j++) {
                  total += dataW[indexW + j] * temp[j];
               }

               if (k < col) {
                  dataW[indexZ] = -beta * total;
               } else if (k == col) {
                  dataW[indexZ] = beta_neg * (1.0 + total);
               } else {
                  dataW[indexZ] = beta_neg * (dataY[indexV] + total);
               }

               k++;
               indexZ += width;
               indexW += width;
            }
         } else {
            for (int endZ = indexZ + width * heightW; indexZ != endZ; indexV += width) {
               double total = 0.0;

               for (int j = 0; j < col; j++) {
                  total += dataW[indexW + j] * temp[j];
               }

               dataW[indexZ] = beta_neg * (dataY[indexV] + total);
               indexZ += width;
               indexW += width;
            }
         }

         i += blockLength;
      }
   }

   public static void computeY_t_V(int blockLength, D1Submatrix64F Y, int col, double[] temp) {
      int widthB = Y.col1 - Y.col0;

      for (int j = 0; j < col; j++) {
         temp[j] = innerProdCol(blockLength, Y, col, widthB, j, widthB);
      }
   }

   public static void multAdd_zeros(int blockLength, D1Submatrix64F Y, D1Submatrix64F B, D1Submatrix64F C) {
      int widthY = Y.col1 - Y.col0;
      int i = Y.row0;

      while (i < Y.row1) {
         int heightY = Math.min(blockLength, Y.row1 - i);

         for (int j = B.col0; j < B.col1; j += blockLength) {
            int widthB = Math.min(blockLength, B.col1 - j);
            int indexC = (i - Y.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightY;

            for (int k = Y.col0; k < Y.col1; k += blockLength) {
               int indexY = i * Y.original.numCols + k * heightY;
               int indexB = (k - Y.col0 + B.row0) * B.original.numCols + j * widthY;
               if (i == Y.row0) {
                  multBlockAdd_zerosone(Y.original.data, B.original.data, C.original.data, indexY, indexB, indexC, heightY, widthY, widthB);
               } else {
                  BlockInnerMultiplication.blockMultPlus(Y.original.data, B.original.data, C.original.data, indexY, indexB, indexC, heightY, widthY, widthB);
               }
            }
         }

         i += blockLength;
      }
   }

   public static void multBlockAdd_zerosone(
      double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC
   ) {
      for (int i = 0; i < heightA; i++) {
         for (int j = 0; j < widthC; j++) {
            double val = i < widthA ? dataB[i * widthC + j + indexB] : 0.0;
            int end = Math.min(i, widthA);

            for (int k = 0; k < end; k++) {
               val += dataA[i * widthA + k + indexA] * dataB[k * widthC + j + indexB];
            }

            dataC[i * widthC + j + indexC] = dataC[i * widthC + j + indexC] + val;
         }
      }
   }

   public static void multTransA_vecCol(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
      int widthA = A.col1 - A.col0;
      if (widthA > blockLength) {
         throw new IllegalArgumentException("A is expected to be at most one block wide.");
      }

      for (int j = B.col0; j < B.col1; j += blockLength) {
         int widthB = Math.min(blockLength, B.col1 - j);
         int indexC = C.row0 * C.original.numCols + (j - B.col0 + C.col0) * widthA;

         for (int k = A.row0; k < A.row1; k += blockLength) {
            int heightA = Math.min(blockLength, A.row1 - k);
            int indexA = k * A.original.numCols + A.col0 * heightA;
            int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
            if (k == A.row0) {
               multTransABlockSet_lowerTriag(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
            } else {
               BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
            }
         }
      }
   }

   protected static void multTransABlockSet_lowerTriag(
      double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC
   ) {
      for (int i = 0; i < widthA; i++) {
         for (int j = 0; j < widthC; j++) {
            double val = i < heightA ? dataB[i * widthC + j + indexB] : 0.0;

            for (int k = i + 1; k < heightA; k++) {
               val += dataA[k * widthA + i + indexA] * dataB[k * widthC + j + indexB];
            }

            dataC[i * widthC + j + indexC] = val;
         }
      }
   }
}
