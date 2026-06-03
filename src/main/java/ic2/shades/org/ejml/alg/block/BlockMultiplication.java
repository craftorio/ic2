package ic2.shades.org.ejml.alg.block;

import ic2.shades.org.ejml.data.D1Submatrix64F;

public class BlockMultiplication {
  public static void mult(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.row0; i < A.row1; i += blockLength) {
      int heightA = Math.min(blockLength, A.row1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
        int k;
        for (k = A.col0; k < A.col1; k += blockLength) {
          int widthA = Math.min(blockLength, A.col1 - k);
          int indexA = i * A.original.numCols + k * heightA;
          int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
          if (k == A.col0) {
            BlockInnerMultiplication.blockMultSet(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
          } else {
            BlockInnerMultiplication.blockMultPlus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
          } 
        } 
      } 
    } 
  }
  
  public static void multPlus(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.row0; i < A.row1; i += blockLength) {
      int heightA = Math.min(blockLength, A.row1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
        int k;
        for (k = A.col0; k < A.col1; k += blockLength) {
          int widthA = Math.min(blockLength, A.col1 - k);
          int indexA = i * A.original.numCols + k * heightA;
          int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
          BlockInnerMultiplication.blockMultPlus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
        } 
      } 
    } 
  }
  
  public static void multMinus(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    checkInput(blockLength, A, B, C);
    int i;
    for (i = A.row0; i < A.row1; i += blockLength) {
      int heightA = Math.min(blockLength, A.row1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * heightA;
        int k;
        for (k = A.col0; k < A.col1; k += blockLength) {
          int widthA = Math.min(blockLength, A.col1 - k);
          int indexA = i * A.original.numCols + k * heightA;
          int indexB = (k - A.col0 + B.row0) * B.original.numCols + j * widthA;
          BlockInnerMultiplication.blockMultMinus(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
        } 
      } 
    } 
  }
  
  private static void checkInput(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int Arow = A.getRows(), Acol = A.getCols();
    int Brow = B.getRows(), Bcol = B.getCols();
    int Crow = C.getRows(), Ccol = C.getCols();
    if (Arow != Crow)
      throw new RuntimeException("Mismatch A and C rows"); 
    if (Bcol != Ccol)
      throw new RuntimeException("Mismatch B and C columns"); 
    if (Acol != Brow)
      throw new RuntimeException("Mismatch A columns and B rows"); 
    if (!BlockMatrixOps.blockAligned(blockLength, A))
      throw new RuntimeException("Sub-Matrix A is not block aligned"); 
    if (!BlockMatrixOps.blockAligned(blockLength, B))
      throw new RuntimeException("Sub-Matrix B is not block aligned"); 
    if (!BlockMatrixOps.blockAligned(blockLength, C))
      throw new RuntimeException("Sub-Matrix C is not block aligned"); 
  }
  
  public static void multTransA(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.col0; i < A.col1; i += blockLength) {
      int widthA = Math.min(blockLength, A.col1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
        int k;
        for (k = A.row0; k < A.row1; k += blockLength) {
          int heightA = Math.min(blockLength, A.row1 - k);
          int indexA = k * A.original.numCols + i * heightA;
          int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
          if (k == A.row0) {
            BlockInnerMultiplication.blockMultSetTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
          } else {
            BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
          } 
        } 
      } 
    } 
  }
  
  public static void multPlusTransA(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.col0; i < A.col1; i += blockLength) {
      int widthA = Math.min(blockLength, A.col1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
        int k;
        for (k = A.row0; k < A.row1; k += blockLength) {
          int heightA = Math.min(blockLength, A.row1 - k);
          int indexA = k * A.original.numCols + i * heightA;
          int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
          BlockInnerMultiplication.blockMultPlusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
        } 
      } 
    } 
  }
  
  public static void multMinusTransA(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.col0; i < A.col1; i += blockLength) {
      int widthA = Math.min(blockLength, A.col1 - i);
      int j;
      for (j = B.col0; j < B.col1; j += blockLength) {
        int widthB = Math.min(blockLength, B.col1 - j);
        int indexC = (i - A.col0 + C.row0) * C.original.numCols + (j - B.col0 + C.col0) * widthA;
        int k;
        for (k = A.row0; k < A.row1; k += blockLength) {
          int heightA = Math.min(blockLength, A.row1 - k);
          int indexA = k * A.original.numCols + i * heightA;
          int indexB = (k - A.row0 + B.row0) * B.original.numCols + j * heightA;
          BlockInnerMultiplication.blockMultMinusTransA(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthB);
        } 
      } 
    } 
  }
  
  public static void multTransB(int blockLength, D1Submatrix64F A, D1Submatrix64F B, D1Submatrix64F C) {
    int i;
    for (i = A.row0; i < A.row1; i += blockLength) {
      int heightA = Math.min(blockLength, A.row1 - i);
      int j;
      for (j = B.row0; j < B.row1; j += blockLength) {
        int widthC = Math.min(blockLength, B.row1 - j);
        int indexC = (i - A.row0 + C.row0) * C.original.numCols + (j - B.row0 + C.col0) * heightA;
        int k;
        for (k = A.col0; k < A.col1; k += blockLength) {
          int widthA = Math.min(blockLength, A.col1 - k);
          int indexA = i * A.original.numCols + k * heightA;
          int indexB = j * B.original.numCols + (k - A.col0 + B.col0) * widthC;
          if (k == A.col0) {
            BlockInnerMultiplication.blockMultSetTransB(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthC);
          } else {
            BlockInnerMultiplication.blockMultPlusTransB(A.original.data, B.original.data, C.original.data, indexA, indexB, indexC, heightA, widthA, widthC);
          } 
        } 
      } 
    } 
  }
}
