// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenPowerMethod;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.data.Eigenpair;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class EigenOps
{
    public static double computeEigenValue(final DenseMatrix64F A, final DenseMatrix64F eigenVector) {
        final double bottom = VectorVectorMult.innerProd(eigenVector, eigenVector);
        final double top = VectorVectorMult.innerProdA(eigenVector, A, eigenVector);
        return top / bottom;
    }
    
    public static Eigenpair computeEigenVector(final DenseMatrix64F A, double eigenvalue) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }
        final DenseMatrix64F M = new DenseMatrix64F(A.numRows, A.numCols);
        final DenseMatrix64F x = new DenseMatrix64F(A.numRows, 1);
        final DenseMatrix64F b = new DenseMatrix64F(A.numRows, 1);
        CommonOps.fill(b, 1.0);
        final double origEigenvalue = eigenvalue;
        SpecializedOps.addIdentity(A, M, -eigenvalue);
        final double threshold = NormOps.normPInf(A) * UtilEjml.EPS;
        double prevError = Double.MAX_VALUE;
        boolean hasWorked = false;
        final LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.linear(M.numRows);
        final double perp = 1.0E-4;
        for (int i = 0; i < 200; ++i) {
            boolean failed = false;
            if (!solver.setA(M)) {
                failed = true;
            }
            else {
                solver.solve(b, x);
            }
            if (MatrixFeatures.hasUncountable(x)) {
                failed = true;
            }
            if (failed) {
                if (hasWorked) {
                    return new Eigenpair(eigenvalue, b);
                }
                final double val = (i % 2 == 0) ? (1.0 - perp) : (1.0 + perp);
                eigenvalue = origEigenvalue * Math.pow(val, i / 2 + 1);
                SpecializedOps.addIdentity(A, M, -eigenvalue);
            }
            else {
                hasWorked = true;
                b.set(x);
                NormOps.normalizeF(b);
                CommonOps.mult(M, b, x);
                final double error = NormOps.normPInf(x);
                if (error - prevError > UtilEjml.EPS * 10.0) {
                    prevError = Double.MAX_VALUE;
                    hasWorked = false;
                    final double val2 = (i % 2 == 0) ? (1.0 - perp) : (1.0 + perp);
                    eigenvalue = origEigenvalue * Math.pow(val2, 1.0);
                }
                else {
                    if (error <= threshold || Math.abs(prevError - error) <= UtilEjml.EPS) {
                        return new Eigenpair(eigenvalue, b);
                    }
                    prevError = error;
                    eigenvalue = VectorVectorMult.innerProdA(b, A, b);
                }
                SpecializedOps.addIdentity(A, M, -eigenvalue);
            }
        }
        return null;
    }
    
    public static Eigenpair dominantEigenpair(final DenseMatrix64F A) {
        final EigenPowerMethod power = new EigenPowerMethod(A.numRows);
        if (!power.computeShiftInvert(A, 0.1)) {
            return null;
        }
        return null;
    }
    
    public static double[] boundLargestEigenValue(final DenseMatrix64F A, double[] bound) {
        if (A.numRows != A.numCols) {
            throw new IllegalArgumentException("A must be a square matrix.");
        }
        double min = Double.MAX_VALUE;
        double max = 0.0;
        for (int n = A.numRows, i = 0; i < n; ++i) {
            double total = 0.0;
            for (int j = 0; j < n; ++j) {
                final double v = A.get(i, j);
                if (v < 0.0) {
                    throw new IllegalArgumentException("Matrix must be positive");
                }
                total += v;
            }
            if (total < min) {
                min = total;
            }
            if (total > max) {
                max = total;
            }
        }
        if (bound == null) {
            bound = new double[2];
        }
        bound[0] = min;
        bound[1] = max;
        return bound;
    }
    
    public static DenseMatrix64F createMatrixD(final EigenDecomposition eig) {
        final int N = eig.getNumberOfEigenvalues();
        final DenseMatrix64F D = new DenseMatrix64F(N, N);
        for (int i = 0; i < N; ++i) {
            final Complex64F c = eig.getEigenvalue(i);
            if (c.isReal()) {
                D.set(i, i, c.real);
            }
        }
        return D;
    }
    
    public static DenseMatrix64F createMatrixV(final EigenDecomposition<DenseMatrix64F> eig) {
        final int N = eig.getNumberOfEigenvalues();
        final DenseMatrix64F V = new DenseMatrix64F(N, N);
        for (int i = 0; i < N; ++i) {
            final Complex64F c = eig.getEigenvalue(i);
            if (c.isReal()) {
                final DenseMatrix64F v = eig.getEigenVector(i);
                if (v != null) {
                    for (int j = 0; j < N; ++j) {
                        V.set(j, i, v.get(j, 0));
                    }
                }
            }
        }
        return V;
    }
}
