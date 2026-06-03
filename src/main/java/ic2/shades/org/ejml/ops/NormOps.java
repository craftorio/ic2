package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class NormOps {
  public static void normalizeF(DenseMatrix64F A) {
    double val = normF((D1Matrix64F)A);
    if (val == 0.0D)
      return; 
    int size = A.getNumElements();
    for (int i = 0; i < size; i++)
      A.div(i, val); 
  }
  
  public static double conditionP(DenseMatrix64F A, double p) {
    if (p == 2.0D)
      return conditionP2(A); 
    if (A.numRows == A.numCols) {
      DenseMatrix64F A_inv = new DenseMatrix64F(A.numRows, A.numCols);
      if (!CommonOps.invert(A, A_inv))
        throw new IllegalArgumentException("A can't be inverted."); 
      return normP(A, p) * normP(A_inv, p);
    } 
    DenseMatrix64F pinv = new DenseMatrix64F(A.numCols, A.numRows);
    CommonOps.pinv(A, pinv);
    return normP(A, p) * normP(pinv, p);
  }
  
  public static double conditionP2(DenseMatrix64F A) {
    SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
    svd.decompose((Matrix64F)A);
    double[] singularValues = svd.getSingularValues();
    int n = SingularOps.rank(svd, 1.0E-12D);
    if (n == 0)
      return 0.0D; 
    double smallest = Double.MAX_VALUE;
    double largest = Double.MIN_VALUE;
    for (double s : singularValues) {
      if (s < smallest)
        smallest = s; 
      if (s > largest)
        largest = s; 
    } 
    return largest / smallest;
  }
  
  public static double fastNormF(D1Matrix64F a) {
    double total = 0.0D;
    int size = a.getNumElements();
    for (int i = 0; i < size; i++) {
      double val = a.get(i);
      total += val * val;
    } 
    return Math.sqrt(total);
  }
  
  public static double normF(D1Matrix64F a) {
    double total = 0.0D;
    double scale = CommonOps.elementMaxAbs(a);
    if (scale == 0.0D)
      return 0.0D; 
    int size = a.getNumElements();
    for (int i = 0; i < size; i++) {
      double val = a.get(i) / scale;
      total += val * val;
    } 
    return scale * Math.sqrt(total);
  }
  
  public static double elementP(RowD1Matrix64F A, double p) {
    if (p == 1.0D)
      return CommonOps.elementSumAbs((D1Matrix64F)A); 
    if (p == 2.0D)
      return normF((D1Matrix64F)A); 
    double max = CommonOps.elementMaxAbs((D1Matrix64F)A);
    if (max == 0.0D)
      return 0.0D; 
    double total = 0.0D;
    int size = A.getNumElements();
    for (int i = 0; i < size; i++) {
      double a = A.get(i) / max;
      total += Math.pow(Math.abs(a), p);
    } 
    return max * Math.pow(total, 1.0D / p);
  }
  
  public static double fastElementP(D1Matrix64F A, double p) {
    if (p == 2.0D)
      return fastNormF(A); 
    double total = 0.0D;
    int size = A.getNumElements();
    for (int i = 0; i < size; i++) {
      double a = A.get(i);
      total += Math.pow(Math.abs(a), p);
    } 
    return Math.pow(total, 1.0D / p);
  }
  
  public static double normP(DenseMatrix64F A, double p) {
    if (p == 1.0D)
      return normP1(A); 
    if (p == 2.0D)
      return normP2(A); 
    if (Double.isInfinite(p))
      return normPInf(A); 
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return elementP((RowD1Matrix64F)A, p); 
    throw new IllegalArgumentException("Doesn't support induced norms yet.");
  }
  
  public static double fastNormP(DenseMatrix64F A, double p) {
    if (p == 1.0D)
      return normP1(A); 
    if (p == 2.0D)
      return fastNormP2(A); 
    if (Double.isInfinite(p))
      return normPInf(A); 
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return fastElementP((D1Matrix64F)A, p); 
    throw new IllegalArgumentException("Doesn't support induced norms yet.");
  }
  
  public static double normP1(DenseMatrix64F A) {
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return CommonOps.elementSumAbs((D1Matrix64F)A); 
    return inducedP1(A);
  }
  
  public static double normP2(DenseMatrix64F A) {
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return normF((D1Matrix64F)A); 
    return inducedP2(A);
  }
  
  public static double fastNormP2(DenseMatrix64F A) {
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return fastNormF((D1Matrix64F)A); 
    return inducedP2(A);
  }
  
  public static double normPInf(DenseMatrix64F A) {
    if (MatrixFeatures.isVector((D1Matrix64F)A))
      return CommonOps.elementMaxAbs((D1Matrix64F)A); 
    return inducedPInf(A);
  }
  
  public static double inducedP1(DenseMatrix64F A) {
    double max = 0.0D;
    int m = A.numRows;
    int n = A.numCols;
    for (int j = 0; j < n; j++) {
      double total = 0.0D;
      for (int i = 0; i < m; i++)
        total += Math.abs(A.get(i, j)); 
      if (total > max)
        max = total; 
    } 
    return max;
  }
  
  public static double inducedP2(DenseMatrix64F A) {
    SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
    if (!svd.decompose((Matrix64F)A))
      throw new RuntimeException("Decomposition failed"); 
    double[] singularValues = svd.getSingularValues();
    return UtilEjml.max(singularValues, 0, singularValues.length);
  }
  
  public static double inducedPInf(DenseMatrix64F A) {
    double max = 0.0D;
    int m = A.numRows;
    int n = A.numCols;
    for (int i = 0; i < m; i++) {
      double total = 0.0D;
      for (int j = 0; j < n; j++)
        total += Math.abs(A.get(i, j)); 
      if (total > max)
        max = total; 
    } 
    return max;
  }
}
