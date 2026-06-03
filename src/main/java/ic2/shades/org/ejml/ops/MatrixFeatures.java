package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.LUDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;

public class MatrixFeatures {
  public static boolean hasNaN(D1Matrix64F m) {
    int length = m.getNumElements();
    for (int i = 0; i < length; i++) {
      if (Double.isNaN(m.get(i)))
        return true; 
    } 
    return false;
  }
  
  public static boolean hasUncountable(D1Matrix64F m) {
    int length = m.getNumElements();
    for (int i = 0; i < length; i++) {
      double a = m.get(i);
      if (Double.isNaN(a) || Double.isInfinite(a))
        return true; 
    } 
    return false;
  }
  
  public static boolean isZeros(D1Matrix64F m, double tol) {
    int length = m.getNumElements();
    for (int i = 0; i < length; i++) {
      if (Math.abs(m.get(i)) > tol)
        return false; 
    } 
    return true;
  }
  
  public static boolean isVector(D1Matrix64F mat) {
    return (mat.numCols == 1 || mat.numRows == 1);
  }
  
  public static boolean isPositiveDefinite(DenseMatrix64F A) {
    if (!isSquare((D1Matrix64F)A))
      return false; 
    CholeskyDecompositionInner_D64 chol = new CholeskyDecompositionInner_D64(true);
    if (chol.inputModified())
      A = A.copy(); 
    return chol.decompose(A);
  }
  
  public static boolean isPositiveSemidefinite(DenseMatrix64F A) {
    if (!isSquare((D1Matrix64F)A))
      return false; 
    EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(A.numCols, false);
    if (eig.inputModified())
      A = A.copy(); 
    eig.decompose((Matrix64F)A);
    for (int i = 0; i < A.numRows; i++) {
      Complex64F v = eig.getEigenvalue(i);
      if (v.getReal() < 0.0D)
        return false; 
    } 
    return true;
  }
  
  public static boolean isSquare(D1Matrix64F mat) {
    return (mat.numCols == mat.numRows);
  }
  
  public static boolean isSymmetric(DenseMatrix64F m, double tol) {
    if (m.numCols != m.numRows)
      return false; 
    double max = CommonOps.elementMaxAbs((D1Matrix64F)m);
    for (int i = 0; i < m.numRows; i++) {
      for (int j = 0; j < i; j++) {
        double a = m.get(i, j) / max;
        double b = m.get(j, i) / max;
        double diff = Math.abs(a - b);
        if (diff > tol)
          return false; 
      } 
    } 
    return true;
  }
  
  public static boolean isSymmetric(DenseMatrix64F m) {
    return isSymmetric(m, 0.0D);
  }
  
  public static boolean isSkewSymmetric(DenseMatrix64F A, double tol) {
    if (A.numCols != A.numRows)
      return false; 
    for (int i = 0; i < A.numRows; i++) {
      for (int j = 0; j < i; j++) {
        double a = A.get(i, j);
        double b = A.get(j, i);
        double diff = Math.abs(a + b);
        if (diff > tol)
          return false; 
      } 
    } 
    return true;
  }
  
  public static boolean isInverse(DenseMatrix64F a, DenseMatrix64F b, double tol) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      return false; 
    int numRows = a.numRows;
    int numCols = a.numCols;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        double total = 0.0D;
        for (int k = 0; k < numCols; k++)
          total += a.get(i, k) * b.get(k, j); 
        if (i == j) {
          if (Math.abs(total - 1.0D) > tol)
            return false; 
        } else if (Math.abs(total) > tol) {
          return false;
        } 
      } 
    } 
    return true;
  }
  
  public static boolean isEquals(D1Matrix64F a, D1Matrix64F b, double tol) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      return false; 
    if (tol == 0.0D)
      return isEquals(a, b); 
    int length = a.getNumElements();
    for (int i = 0; i < length; i++) {
      if (tol < Math.abs(a.get(i) - b.get(i)))
        return false; 
    } 
    return true;
  }
  
  public static boolean isEqualsTriangle(ReshapeMatrix64F a, ReshapeMatrix64F b, boolean upper, double tol) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      return false; 
    if (upper) {
      for (int i = 0; i < a.numRows; i++) {
        for (int j = i; j < a.numCols; j++) {
          if (Math.abs(a.get(i, j) - b.get(i, j)) > tol)
            return false; 
        } 
      } 
    } else {
      for (int i = 0; i < a.numRows; i++) {
        int end = Math.min(i, a.numCols - 1);
        for (int j = 0; j <= end; j++) {
          if (Math.abs(a.get(i, j) - b.get(i, j)) > tol)
            return false; 
        } 
      } 
    } 
    return true;
  }
  
  public static boolean isEquals(D1Matrix64F a, D1Matrix64F b) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      return false; 
    int length = a.getNumElements();
    for (int i = 0; i < length; i++) {
      if (a.get(i) != b.get(i))
        return false; 
    } 
    return true;
  }
  
  public static boolean isIdentical(D1Matrix64F a, D1Matrix64F b, double tol) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      return false; 
    if (tol < 0.0D)
      throw new IllegalArgumentException("Tolerance must be greater than or equal to zero."); 
    int length = a.getNumElements();
    for (int i = 0; i < length; ) {
      double valA = a.get(i);
      double valB = b.get(i);
      double diff = Math.abs(valA - valB);
      if (tol >= diff) {
        i++;
        continue;
      } 
      if (Double.isNaN(valA))
        return Double.isNaN(valB); 
      if (Double.isInfinite(valA))
        return (valA == valB); 
      return false;
    } 
    return true;
  }
  
  public static boolean isOrthogonal(DenseMatrix64F Q, double tol) {
    if (Q.numRows < Q.numCols)
      throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns"); 
    DenseMatrix64F[] u = CommonOps.columnsToVector(Q, null);
    for (int i = 0; i < u.length; i++) {
      DenseMatrix64F a = u[i];
      for (int j = i + 1; j < u.length; j++) {
        double val = VectorVectorMult.innerProd((D1Matrix64F)a, (D1Matrix64F)u[j]);
        if (Math.abs(val) > tol)
          return false; 
      } 
    } 
    return true;
  }
  
  public static boolean isRowsLinearIndependent(DenseMatrix64F A) {
    LUDecomposition<DenseMatrix64F> lu = DecompositionFactory.lu(A.numRows, A.numCols);
    if (lu.inputModified())
      A = A.copy(); 
    if (!lu.decompose((Matrix64F)A))
      throw new RuntimeException("Decompositon failed?"); 
    return !lu.isSingular();
  }
  
  public static boolean isIdentity(DenseMatrix64F mat, double tol) {
    int index = 0;
    for (int i = 0; i < mat.numRows; i++) {
      for (int j = 0; j < mat.numCols; j++) {
        if (i == j) {
          if (Math.abs(mat.get(index++) - 1.0D) > tol)
            return false; 
        } else if (Math.abs(mat.get(index++)) > tol) {
          return false;
        } 
      } 
    } 
    return true;
  }
  
  public static boolean isConstantVal(DenseMatrix64F mat, double val, double tol) {
    int index = 0;
    for (int i = 0; i < mat.numRows; i++) {
      for (int j = 0; j < mat.numCols; j++) {
        if (Math.abs(mat.get(index++) - val) > tol)
          return false; 
      } 
    } 
    return true;
  }
  
  public static boolean isDiagonalPositive(DenseMatrix64F a) {
    for (int i = 0; i < a.numRows; i++) {
      if (a.get(i, i) < 0.0D)
        return false; 
    } 
    return true;
  }
  
  public static boolean isFullRank(DenseMatrix64F a) {
    throw new RuntimeException("Implement");
  }
  
  public static boolean isNegative(D1Matrix64F a, D1Matrix64F b, double tol) {
    if (a.numRows != b.numRows || a.numCols != b.numCols)
      throw new IllegalArgumentException("Matrix dimensions must match"); 
    int length = a.getNumElements();
    for (int i = 0; i < length; i++) {
      if (Math.abs(a.get(i) + b.get(i)) > tol)
        return false; 
    } 
    return true;
  }
  
  public static boolean isUpperTriangle(DenseMatrix64F A, int hessenberg, double tol) {
    if (A.numRows != A.numCols)
      return false; 
    for (int i = hessenberg + 1; i < A.numRows; i++) {
      for (int j = 0; j < i - hessenberg; j++) {
        if (Math.abs(A.get(i, j)) > tol)
          return false; 
      } 
    } 
    return true;
  }
  
  public static int rank(DenseMatrix64F A) {
    return rank(A, UtilEjml.EPS * 100.0D);
  }
  
  public static int rank(DenseMatrix64F A, double threshold) {
    SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
    if (svd.inputModified())
      A = A.copy(); 
    if (!svd.decompose((Matrix64F)A))
      throw new RuntimeException("Decomposition failed"); 
    return SingularOps.rank(svd, threshold);
  }
  
  public static int nullity(DenseMatrix64F A) {
    return nullity(A, UtilEjml.EPS * 100.0D);
  }
  
  public static int nullity(DenseMatrix64F A, double threshold) {
    SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
    if (svd.inputModified())
      A = A.copy(); 
    if (!svd.decompose((Matrix64F)A))
      throw new RuntimeException("Decomposition failed"); 
    return SingularOps.nullity(svd, threshold);
  }
}
