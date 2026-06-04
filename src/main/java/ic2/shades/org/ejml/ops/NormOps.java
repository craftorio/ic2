// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class NormOps
{
    public static void normalizeF(final DenseMatrix64F A) {
        final double val = normF(A);
        if (val == 0.0) {
            return;
        }
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            A.div(i, val);
        }
    }
    
    public static double conditionP(final DenseMatrix64F A, final double p) {
        if (p == 2.0) {
            return conditionP2(A);
        }
        if (A.numRows != A.numCols) {
            final DenseMatrix64F pinv = new DenseMatrix64F(A.numCols, A.numRows);
            CommonOps.pinv(A, pinv);
            return normP(A, p) * normP(pinv, p);
        }
        final DenseMatrix64F A_inv = new DenseMatrix64F(A.numRows, A.numCols);
        if (!CommonOps.invert(A, A_inv)) {
            throw new IllegalArgumentException("A can't be inverted.");
        }
        return normP(A, p) * normP(A_inv, p);
    }
    
    public static double conditionP2(final DenseMatrix64F A) {
        final SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
        svd.decompose(A);
        final double[] singularValues = svd.getSingularValues();
        final int n = SingularOps.rank(svd, 1.0E-12);
        if (n == 0) {
            return 0.0;
        }
        double smallest = Double.MAX_VALUE;
        double largest = Double.MIN_VALUE;
        for (final double s : singularValues) {
            if (s < smallest) {
                smallest = s;
            }
            if (s > largest) {
                largest = s;
            }
        }
        return largest / smallest;
    }
    
    public static double fastNormF(final D1Matrix64F a) {
        double total = 0.0;
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            final double val = a.get(i);
            total += val * val;
        }
        return Math.sqrt(total);
    }
    
    public static double normF(final D1Matrix64F a) {
        double total = 0.0;
        final double scale = CommonOps.elementMaxAbs(a);
        if (scale == 0.0) {
            return 0.0;
        }
        for (int size = a.getNumElements(), i = 0; i < size; ++i) {
            final double val = a.get(i) / scale;
            total += val * val;
        }
        return scale * Math.sqrt(total);
    }
    
    public static double elementP(final RowD1Matrix64F A, final double p) {
        if (p == 1.0) {
            return CommonOps.elementSumAbs(A);
        }
        if (p == 2.0) {
            return normF(A);
        }
        final double max = CommonOps.elementMaxAbs(A);
        if (max == 0.0) {
            return 0.0;
        }
        double total = 0.0;
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            final double a = A.get(i) / max;
            total += Math.pow(Math.abs(a), p);
        }
        return max * Math.pow(total, 1.0 / p);
    }
    
    public static double fastElementP(final D1Matrix64F A, final double p) {
        if (p == 2.0) {
            return fastNormF(A);
        }
        double total = 0.0;
        for (int size = A.getNumElements(), i = 0; i < size; ++i) {
            final double a = A.get(i);
            total += Math.pow(Math.abs(a), p);
        }
        return Math.pow(total, 1.0 / p);
    }
    
    public static double normP(final DenseMatrix64F A, final double p) {
        if (p == 1.0) {
            return normP1(A);
        }
        if (p == 2.0) {
            return normP2(A);
        }
        if (Double.isInfinite(p)) {
            return normPInf(A);
        }
        if (MatrixFeatures.isVector(A)) {
            return elementP(A, p);
        }
        throw new IllegalArgumentException("Doesn't support induced norms yet.");
    }
    
    public static double fastNormP(final DenseMatrix64F A, final double p) {
        if (p == 1.0) {
            return normP1(A);
        }
        if (p == 2.0) {
            return fastNormP2(A);
        }
        if (Double.isInfinite(p)) {
            return normPInf(A);
        }
        if (MatrixFeatures.isVector(A)) {
            return fastElementP(A, p);
        }
        throw new IllegalArgumentException("Doesn't support induced norms yet.");
    }
    
    public static double normP1(final DenseMatrix64F A) {
        if (MatrixFeatures.isVector(A)) {
            return CommonOps.elementSumAbs(A);
        }
        return inducedP1(A);
    }
    
    public static double normP2(final DenseMatrix64F A) {
        if (MatrixFeatures.isVector(A)) {
            return normF(A);
        }
        return inducedP2(A);
    }
    
    public static double fastNormP2(final DenseMatrix64F A) {
        if (MatrixFeatures.isVector(A)) {
            return fastNormF(A);
        }
        return inducedP2(A);
    }
    
    public static double normPInf(final DenseMatrix64F A) {
        if (MatrixFeatures.isVector(A)) {
            return CommonOps.elementMaxAbs(A);
        }
        return inducedPInf(A);
    }
    
    public static double inducedP1(final DenseMatrix64F A) {
        double max = 0.0;
        final int m = A.numRows;
        for (int n = A.numCols, j = 0; j < n; ++j) {
            double total = 0.0;
            for (int i = 0; i < m; ++i) {
                total += Math.abs(A.get(i, j));
            }
            if (total > max) {
                max = total;
            }
        }
        return max;
    }
    
    public static double inducedP2(final DenseMatrix64F A) {
        final SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, false, false, true);
        if (!svd.decompose(A)) {
            throw new RuntimeException("Decomposition failed");
        }
        final double[] singularValues = svd.getSingularValues();
        return UtilEjml.max(singularValues, 0, singularValues.length);
    }
    
    public static double inducedPInf(final DenseMatrix64F A) {
        double max = 0.0;
        final int m = A.numRows;
        final int n = A.numCols;
        for (int i = 0; i < m; ++i) {
            double total = 0.0;
            for (int j = 0; j < n; ++j) {
                total += Math.abs(A.get(i, j));
            }
            if (total > max) {
                max = total;
            }
        }
        return max;
    }
}
