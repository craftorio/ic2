// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import java.util.Random;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolverSafe;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class CovarianceOps
{
    public static double TOL;
    
    public static boolean isValidFast(final DenseMatrix64F cov) {
        return MatrixFeatures.isDiagonalPositive(cov);
    }
    
    public static int isValid(final DenseMatrix64F cov) {
        if (!MatrixFeatures.isDiagonalPositive(cov)) {
            return 1;
        }
        if (!MatrixFeatures.isSymmetric(cov, CovarianceOps.TOL)) {
            return 2;
        }
        if (!MatrixFeatures.isPositiveSemidefinite(cov)) {
            return 3;
        }
        return 0;
    }
    
    public static boolean invert(final DenseMatrix64F cov) {
        return invert(cov, cov);
    }
    
    public static boolean invert(final DenseMatrix64F cov, final DenseMatrix64F cov_inv) {
        if (cov.numCols <= 4) {
            if (cov.numCols != cov.numRows) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }
            if (cov.numCols >= 2) {
                UnrolledInverseFromMinor.inv(cov, cov_inv);
            }
            else {
                cov_inv.data[0] = 1.0 / cov_inv.data[0];
            }
        }
        else {
            LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.symmPosDef(cov.numRows);
            solver = new LinearSolverSafe<DenseMatrix64F>(solver);
            if (!solver.setA(cov)) {
                return false;
            }
            solver.invert(cov_inv);
        }
        return true;
    }
    
    public static void randomVector(final DenseMatrix64F cov, final DenseMatrix64F vector, final Random rand) {
        final CovarianceRandomDraw rng = new CovarianceRandomDraw(rand, cov);
        rng.next(vector);
    }
    
    static {
        CovarianceOps.TOL = 1.0E-9;
    }
}
