package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class MatrixMultProduct {
  public static void outer(RowD1Matrix64F a, RowD1Matrix64F c) {
    for (int i = 0; i < a.numRows; i++) {
      int indexC1 = i * c.numCols + i;
      int indexC2 = indexC1;
      for (int j = i; j < a.numRows; j++, indexC2 += c.numCols) {
        int indexA = i * a.numCols;
        int indexB = j * a.numCols;
        double sum = 0.0D;
        int end = indexA + a.numCols;
        for (; indexA < end; indexA++, indexB++)
          sum += a.data[indexA] * a.data[indexB]; 
        c.data[indexC1++] = sum;
        c.data[indexC2] = sum;
      } 
    } 
  }
  
  public static void inner_small(RowD1Matrix64F a, RowD1Matrix64F c) {
    for (int i = 0; i < a.numCols; i++) {
      for (int j = i; j < a.numCols; j++) {
        int indexC1 = i * c.numCols + j;
        int indexC2 = j * c.numCols + i;
        int indexA = i;
        int indexB = j;
        double sum = 0.0D;
        int end = indexA + a.numRows * a.numCols;
        for (; indexA < end; indexA += a.numCols, indexB += a.numCols)
          sum += a.data[indexA] * a.data[indexB]; 
        c.data[indexC2] = sum;
        c.data[indexC1] = sum;
      } 
    } 
  }
  
  public static void inner_reorder(RowD1Matrix64F a, RowD1Matrix64F c) {
    for (int i = 0; i < a.numCols; i++) {
      int indexC = i * c.numCols + i;
      double valAi = a.data[i];
      for (int j = i; j < a.numCols; j++)
        c.data[indexC++] = valAi * a.data[j]; 
      for (int k = 1; k < a.numRows; k++) {
        indexC = i * c.numCols + i;
        int indexB = k * a.numCols + i;
        valAi = a.data[indexB];
        for (int n = i; n < a.numCols; n++)
          c.data[indexC++] = c.data[indexC++] + valAi * a.data[indexB++]; 
      } 
      indexC = i * c.numCols + i;
      int indexC2 = indexC;
      for (int m = i; m < a.numCols; m++, indexC2 += c.numCols)
        c.data[indexC2] = c.data[indexC++]; 
    } 
  }
  
  public static void inner_reorder_upper(RowD1Matrix64F a, RowD1Matrix64F c) {
    for (int i = 0; i < a.numCols; i++) {
      int indexC = i * c.numCols + i;
      double valAi = a.data[i];
      for (int j = i; j < a.numCols; j++)
        c.data[indexC++] = valAi * a.data[j]; 
      for (int k = 1; k < a.numRows; k++) {
        indexC = i * c.numCols + i;
        int indexB = k * a.numCols + i;
        valAi = a.data[indexB];
        for (int m = i; m < a.numCols; m++)
          c.data[indexC++] = c.data[indexC++] + valAi * a.data[indexB++]; 
      } 
    } 
  }
}
