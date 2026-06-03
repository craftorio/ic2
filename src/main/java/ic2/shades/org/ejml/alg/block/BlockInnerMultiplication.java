package ic2.shades.org.ejml.alg.block;

public class BlockInnerMultiplication {
  public static void blockMultPlus(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int a = indexA;
    int rowC = indexC;
    for (int i = 0; i < heightA; i++, rowC += widthC) {
      int b = indexB;
      int endC = rowC + widthC;
      int endA = a + widthA;
      while (a != endA) {
        double valA = dataA[a++];
        int c = rowC;
        while (c != endC)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
      } 
    } 
  }
  
  public static void blockMultPlusTransA(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int rowC = indexC;
    for (int i = 0; i < widthA; i++, rowC += widthC) {
      int colA = i + indexA;
      int endA = colA + widthA * heightA;
      int b = indexB;
      while (colA != endA) {
        double valA = dataA[colA];
        int c = rowC;
        int endB = b + widthC;
        while (b != endB)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
        colA += widthA;
      } 
    } 
  }
  
  public static void blockMultPlusTransB(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    for (int i = 0; i < heightA; i++) {
      for (int j = 0; j < widthC; j++) {
        double val = 0.0D;
        for (int k = 0; k < widthA; k++)
          val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB]; 
        dataC[i * widthC + j + indexC] = dataC[i * widthC + j + indexC] + val;
      } 
    } 
  }
  
  public static void blockMultMinus(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int a = indexA;
    int rowC = indexC;
    for (int i = 0; i < heightA; i++, rowC += widthC) {
      int b = indexB;
      int endC = rowC + widthC;
      int endA = a + widthA;
      while (a != endA) {
        double valA = dataA[a++];
        int c = rowC;
        while (c != endC)
          dataC[c++] = dataC[c++] - valA * dataB[b++]; 
      } 
    } 
  }
  
  public static void blockMultMinusTransA(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int rowC = indexC;
    for (int i = 0; i < widthA; i++, rowC += widthC) {
      int colA = i + indexA;
      int endA = colA + widthA * heightA;
      int b = indexB;
      while (colA != endA) {
        double valA = dataA[colA];
        int c = rowC;
        int endB = b + widthC;
        while (b != endB)
          dataC[c++] = dataC[c++] - valA * dataB[b++]; 
        colA += widthA;
      } 
    } 
  }
  
  public static void blockMultMinusTransB(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    for (int i = 0; i < heightA; i++) {
      for (int j = 0; j < widthC; j++) {
        double val = 0.0D;
        for (int k = 0; k < widthA; k++)
          val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB]; 
        dataC[i * widthC + j + indexC] = dataC[i * widthC + j + indexC] - val;
      } 
    } 
  }
  
  public static void blockMultSet(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int a = indexA;
    int rowC = indexC;
    for (int i = 0; i < heightA; i++, rowC += widthC) {
      int b = indexB;
      int endC = rowC + widthC;
      int endA = a + widthA;
      while (a != endA) {
        double valA = dataA[a++];
        int c = rowC;
        if (b == indexB) {
          while (c != endC)
            dataC[c++] = valA * dataB[b++]; 
          continue;
        } 
        while (c != endC)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
      } 
    } 
  }
  
  public static void blockMultSetTransA(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int rowC = indexC;
    for (int i = 0; i < widthA; i++, rowC += widthC) {
      int colA = i + indexA;
      int endA = colA + widthA * heightA;
      int b = indexB;
      while (colA != endA) {
        double valA = dataA[colA];
        int c = rowC;
        int endB = b + widthC;
        if (b == indexB) {
          while (b != endB)
            dataC[c++] = valA * dataB[b++]; 
        } else {
          while (b != endB)
            dataC[c++] = dataC[c++] + valA * dataB[b++]; 
        } 
        colA += widthA;
      } 
    } 
  }
  
  public static void blockMultSetTransB(double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    for (int i = 0; i < heightA; i++) {
      for (int j = 0; j < widthC; j++) {
        double val = 0.0D;
        for (int k = 0; k < widthA; k++)
          val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB]; 
        dataC[i * widthC + j + indexC] = val;
      } 
    } 
  }
  
  public static void blockMultPlus(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int a = indexA;
    int rowC = indexC;
    for (int i = 0; i < heightA; i++, rowC += widthC) {
      int b = indexB;
      int endC = rowC + widthC;
      int endA = a + widthA;
      while (a != endA) {
        double valA = alpha * dataA[a++];
        int c = rowC;
        while (c != endC)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
      } 
    } 
  }
  
  public static void blockMultPlusTransA(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int rowC = indexC;
    for (int i = 0; i < widthA; i++, rowC += widthC) {
      int colA = i + indexA;
      int endA = colA + widthA * heightA;
      int b = indexB;
      while (colA != endA) {
        double valA = alpha * dataA[colA];
        int c = rowC;
        int endB = b + widthC;
        while (b != endB)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
        colA += widthA;
      } 
    } 
  }
  
  public static void blockMultPlusTransB(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    for (int i = 0; i < heightA; i++) {
      for (int j = 0; j < widthC; j++) {
        double val = 0.0D;
        for (int k = 0; k < widthA; k++)
          val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB]; 
        dataC[i * widthC + j + indexC] = dataC[i * widthC + j + indexC] + alpha * val;
      } 
    } 
  }
  
  public static void blockMultSet(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int a = indexA;
    int rowC = indexC;
    for (int i = 0; i < heightA; i++, rowC += widthC) {
      int b = indexB;
      int endC = rowC + widthC;
      int endA = a + widthA;
      while (a != endA) {
        double valA = alpha * dataA[a++];
        int c = rowC;
        if (b == indexB) {
          while (c != endC)
            dataC[c++] = valA * dataB[b++]; 
          continue;
        } 
        while (c != endC)
          dataC[c++] = dataC[c++] + valA * dataB[b++]; 
      } 
    } 
  }
  
  public static void blockMultSetTransA(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    int rowC = indexC;
    for (int i = 0; i < widthA; i++, rowC += widthC) {
      int colA = i + indexA;
      int endA = colA + widthA * heightA;
      int b = indexB;
      while (colA != endA) {
        double valA = alpha * dataA[colA];
        int c = rowC;
        int endB = b + widthC;
        if (b == indexB) {
          while (b != endB)
            dataC[c++] = valA * dataB[b++]; 
        } else {
          while (b != endB)
            dataC[c++] = dataC[c++] + valA * dataB[b++]; 
        } 
        colA += widthA;
      } 
    } 
  }
  
  public static void blockMultSetTransB(double alpha, double[] dataA, double[] dataB, double[] dataC, int indexA, int indexB, int indexC, int heightA, int widthA, int widthC) {
    for (int i = 0; i < heightA; i++) {
      for (int j = 0; j < widthC; j++) {
        double val = 0.0D;
        for (int k = 0; k < widthA; k++)
          val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB]; 
        dataC[i * widthC + j + indexC] = alpha * val;
      } 
    } 
  }
}
