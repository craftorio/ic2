package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.alg.dense.mult.SubmatrixOps;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import java.util.Random;

public class RandomMatrices {
  public static DenseMatrix64F[] createSpan(int dimen, int numVectors, Random rand) {
    if (dimen < numVectors)
      throw new IllegalArgumentException("The number of vectors must be less than or equal to the dimension"); 
    DenseMatrix64F[] u = new DenseMatrix64F[numVectors];
    u[0] = createRandom(dimen, 1, -1.0D, 1.0D, rand);
    NormOps.normalizeF(u[0]);
    for (int i = 1; i < numVectors; i++) {
      DenseMatrix64F a = new DenseMatrix64F(dimen, 1);
      DenseMatrix64F r = null;
      for (int j = 0; j < i; j++) {
        if (j == 0)
          r = createRandom(dimen, 1, -1.0D, 1.0D, rand); 
        a.set((D1Matrix64F)r);
        VectorVectorMult.householder(-2.0D, (D1Matrix64F)u[j], (D1Matrix64F)r, (D1Matrix64F)a);
        CommonOps.add((D1Matrix64F)r, (D1Matrix64F)a, (D1Matrix64F)a);
        CommonOps.scale(0.5D, (D1Matrix64F)a);
        DenseMatrix64F t = a;
        a = r;
        r = t;
        double val = NormOps.normF((D1Matrix64F)r);
        if (val == 0.0D || Double.isNaN(val) || Double.isInfinite(val))
          throw new RuntimeException("Failed sanity check"); 
        CommonOps.divide((D1Matrix64F)r, val);
      } 
      u[i] = r;
    } 
    return u;
  }
  
  public static DenseMatrix64F createInSpan(DenseMatrix64F[] span, double min, double max, Random rand) {
    DenseMatrix64F A = new DenseMatrix64F(span.length, 1);
    DenseMatrix64F B = new DenseMatrix64F(span[0].getNumElements(), 1);
    for (int i = 0; i < span.length; i++) {
      B.set((D1Matrix64F)span[i]);
      double val = rand.nextDouble() * (max - min) + min;
      CommonOps.scale(val, (D1Matrix64F)B);
      CommonOps.add((D1Matrix64F)A, (D1Matrix64F)B, (D1Matrix64F)A);
    } 
    return A;
  }
  
  public static DenseMatrix64F createOrthogonal(int numRows, int numCols, Random rand) {
    if (numRows < numCols)
      throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns"); 
    DenseMatrix64F[] u = createSpan(numRows, numCols, rand);
    DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
    for (int i = 0; i < numCols; i++)
      SubmatrixOps.setSubMatrix((RowD1Matrix64F)u[i], (RowD1Matrix64F)ret, 0, 0, 0, i, numRows, 1); 
    return ret;
  }
  
  public static DenseMatrix64F createDiagonal(int N, double min, double max, Random rand) {
    return createDiagonal(N, N, min, max, rand);
  }
  
  public static DenseMatrix64F createDiagonal(int numRows, int numCols, double min, double max, Random rand) {
    if (max < min)
      throw new IllegalArgumentException("The max must be >= the min"); 
    DenseMatrix64F ret = new DenseMatrix64F(numRows, numCols);
    int N = Math.min(numRows, numCols);
    double r = max - min;
    for (int i = 0; i < N; i++)
      ret.set(i, i, rand.nextDouble() * r + min); 
    return ret;
  }
  
  public static DenseMatrix64F createSingularValues(int numRows, int numCols, Random rand, double... sv) {
    DenseMatrix64F U = createOrthogonal(numRows, numRows, rand);
    DenseMatrix64F V = createOrthogonal(numCols, numCols, rand);
    DenseMatrix64F S = new DenseMatrix64F(numRows, numCols);
    int min = Math.min(numRows, numCols);
    min = Math.min(min, sv.length);
    for (int i = 0; i < min; i++)
      S.set(i, i, sv[i]); 
    DenseMatrix64F tmp = new DenseMatrix64F(numRows, numCols);
    CommonOps.mult((RowD1Matrix64F)U, (RowD1Matrix64F)S, (RowD1Matrix64F)tmp);
    CommonOps.multTransB((RowD1Matrix64F)tmp, (RowD1Matrix64F)V, (RowD1Matrix64F)S);
    return S;
  }
  
  public static DenseMatrix64F createEigenvaluesSymm(int num, Random rand, double... eigenvalues) {
    DenseMatrix64F V = createOrthogonal(num, num, rand);
    DenseMatrix64F D = CommonOps.diag(eigenvalues);
    DenseMatrix64F temp = new DenseMatrix64F(num, num);
    CommonOps.mult((RowD1Matrix64F)V, (RowD1Matrix64F)D, (RowD1Matrix64F)temp);
    CommonOps.multTransB((RowD1Matrix64F)temp, (RowD1Matrix64F)V, (RowD1Matrix64F)D);
    return D;
  }
  
  public static DenseMatrix64F createRandom(int numRow, int numCol, Random rand) {
    DenseMatrix64F mat = new DenseMatrix64F(numRow, numCol);
    setRandom((D1Matrix64F)mat, 0.0D, 1.0D, rand);
    return mat;
  }
  
  public static void addRandom(DenseMatrix64F A, double min, double max, Random rand) {
    double[] d = A.getData();
    int size = A.getNumElements();
    double r = max - min;
    for (int i = 0; i < size; i++)
      d[i] = d[i] + r * rand.nextDouble() + min; 
  }
  
  public static DenseMatrix64F createRandom(int numRow, int numCol, double min, double max, Random rand) {
    DenseMatrix64F mat = new DenseMatrix64F(numRow, numCol);
    setRandom((D1Matrix64F)mat, min, max, rand);
    return mat;
  }
  
  public static void setRandom(DenseMatrix64F mat, Random rand) {
    setRandom((D1Matrix64F)mat, 0.0D, 1.0D, rand);
  }
  
  public static void setRandom(D1Matrix64F mat, double min, double max, Random rand) {
    double[] d = mat.getData();
    int size = mat.getNumElements();
    double r = max - min;
    for (int i = 0; i < size; i++)
      d[i] = r * rand.nextDouble() + min; 
  }
  
  public static DenseMatrix64F createSymmPosDef(int width, Random rand) {
    DenseMatrix64F a = new DenseMatrix64F(width, 1);
    DenseMatrix64F b = new DenseMatrix64F(width, width);
    int i;
    for (i = 0; i < width; i++)
      a.set(i, 0, rand.nextDouble()); 
    CommonOps.multTransB((RowD1Matrix64F)a, (RowD1Matrix64F)a, (RowD1Matrix64F)b);
    for (i = 0; i < width; i++)
      b.add(i, i, 1.0D); 
    return b;
  }
  
  public static DenseMatrix64F createSymmetric(int length, double min, double max, Random rand) {
    DenseMatrix64F A = new DenseMatrix64F(length, length);
    createSymmetric(A, min, max, rand);
    return A;
  }
  
  public static void createSymmetric(DenseMatrix64F A, double min, double max, Random rand) {
    if (A.numRows != A.numCols)
      throw new IllegalArgumentException("A must be a square matrix"); 
    double range = max - min;
    int length = A.numRows;
    for (int i = 0; i < length; i++) {
      for (int j = i; j < length; j++) {
        double val = rand.nextDouble() * range + min;
        A.set(i, j, val);
        A.set(j, i, val);
      } 
    } 
  }
  
  public static DenseMatrix64F createUpperTriangle(int dimen, int hessenberg, double min, double max, Random rand) {
    if (hessenberg < 0)
      throw new RuntimeException("hessenberg must be more than or equal to 0"); 
    double range = max - min;
    DenseMatrix64F A = new DenseMatrix64F(dimen, dimen);
    for (int i = 0; i < dimen; i++) {
      int start = (i <= hessenberg) ? 0 : (i - hessenberg);
      for (int j = start; j < dimen; j++)
        A.set(i, j, rand.nextDouble() * range + min); 
    } 
    return A;
  }
}
