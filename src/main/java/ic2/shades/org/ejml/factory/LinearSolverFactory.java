// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.factory;

import ic2.shades.org.ejml.alg.dense.linsol.qr.AdjLinearSolverQr;
import ic2.shades.org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import ic2.shades.org.ejml.alg.dense.linsol.svd.SolvePseudoInverseSvd;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrpHouseCol;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.alg.dense.linsol.qr.SolvePseudoInverseQrp;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon_D64;
import ic2.shades.org.ejml.alg.dense.linsol.chol.LinearSolverCholBlock64;
import ic2.shades.org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrBlock64;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverFactory
{
    public static LinearSolver<DenseMatrix64F> general(final int numRows, final int numCols) {
        if (numRows == numCols) {
            return linear(numRows);
        }
        return leastSquares(numRows, numCols);
    }
    
    public static LinearSolver<DenseMatrix64F> linear(final int matrixSize) {
        return new LinearSolverLu(new LUDecompositionAlt_D64());
    }
    
    public static LinearSolver<DenseMatrix64F> leastSquares(final int numRows, final int numCols) {
        if (numCols < EjmlParameters.SWITCH_BLOCK64_QR) {
            return new LinearSolverQrHouseCol();
        }
        if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER) {
            return new LinearSolverQrBlock64();
        }
        return new LinearSolverQrHouseCol();
    }
    
    public static LinearSolver<DenseMatrix64F> symmPosDef(final int matrixWidth) {
        if (matrixWidth < EjmlParameters.SWITCH_BLOCK64_CHOLESKY) {
            final CholeskyDecompositionCommon_D64 decomp = new CholeskyDecompositionInner_D64(true);
            return new LinearSolverChol(decomp);
        }
        if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER) {
            return new LinearSolverCholBlock64();
        }
        final CholeskyDecompositionCommon_D64 decomp = new CholeskyDecompositionInner_D64(true);
        return new LinearSolverChol(decomp);
    }
    
    public static LinearSolver<DenseMatrix64F> leastSquaresQrPivot(final boolean computeNorm2, final boolean computeQ) {
        final QRColPivDecompositionHouseholderColumn_D64 decomposition = new QRColPivDecompositionHouseholderColumn_D64();
        if (computeQ) {
            return new SolvePseudoInverseQrp(decomposition, computeNorm2);
        }
        return new LinearSolverQrpHouseCol(decomposition, computeNorm2);
    }
    
    public static LinearSolver<DenseMatrix64F> pseudoInverse(final boolean useSVD) {
        if (useSVD) {
            return new SolvePseudoInverseSvd();
        }
        return leastSquaresQrPivot(true, false);
    }
    
    public static AdjustableLinearSolver adjustable() {
        return new AdjLinearSolverQr();
    }
}
