// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.factory;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecomposition_B64_to_D64;
import ic2.shades.org.ejml.ops.EigenOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.simple.SimpleMatrix;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.WatchedDoubleStepQRDecomposition_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose_D64;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.interfaces.decomposition.LUDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyLDLDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecomposition_B64_to_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyDecomposition;

public class DecompositionFactory
{
    public static CholeskyDecomposition<DenseMatrix64F> chol(final int matrixSize, final boolean lower) {
        if (matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY) {
            return new CholeskyDecompositionInner_D64(lower);
        }
        if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER) {
            return new CholeskyDecomposition_B64_to_D64(lower);
        }
        return new CholeskyDecompositionBlock_D64(EjmlParameters.BLOCK_WIDTH_CHOL);
    }
    
    public static CholeskyLDLDecomposition<DenseMatrix64F> cholLDL(final int matrixSize) {
        return new CholeskyDecompositionLDL_D64();
    }
    
    public static LUDecomposition<DenseMatrix64F> lu(final int numRows, final int numCol) {
        return new LUDecompositionAlt_D64();
    }
    
    public static SingularValueDecomposition<DenseMatrix64F> svd(final int numRows, final int numCols, final boolean needU, final boolean needV, final boolean compact) {
        return new SvdImplicitQrDecompose_D64(compact, needU, needV, false);
    }
    
    public static QRDecomposition<DenseMatrix64F> qr(final int numRows, final int numCols) {
        return new QRDecompositionHouseholderColumn_D64();
    }
    
    public static QRPDecomposition<DenseMatrix64F> qrp(final int numRows, final int numCols) {
        return new QRColPivDecompositionHouseholderColumn_D64();
    }
    
    public static EigenDecomposition<DenseMatrix64F> eig(final int matrixSize, final boolean needVectors) {
        return new SwitchingEigenDecomposition(matrixSize, needVectors, 1.0E-8);
    }
    
    public static EigenDecomposition<DenseMatrix64F> eig(final int matrixSize, final boolean computeVectors, final boolean isSymmetric) {
        if (isSymmetric) {
            final TridiagonalSimilarDecomposition<DenseMatrix64F> decomp = tridiagonal(matrixSize);
            return new SymmetricQRAlgorithmDecomposition_D64(decomp, computeVectors);
        }
        return new WatchedDoubleStepQRDecomposition_D64(computeVectors);
    }
    
    public static double quality(final DenseMatrix64F orig, final SingularValueDecomposition<DenseMatrix64F> svd) {
        return quality(orig, svd.getU(null, false), svd.getW(null), svd.getV(null, true));
    }
    
    public static double quality(final DenseMatrix64F orig, final DenseMatrix64F U, final DenseMatrix64F W, final DenseMatrix64F Vt) {
        final SimpleMatrix _U = SimpleMatrix.wrap(U);
        final SimpleMatrix _W = SimpleMatrix.wrap(W);
        final SimpleMatrix _Vt = SimpleMatrix.wrap(Vt);
        final SimpleMatrix foundA = _U.mult(_W).mult(_Vt);
        return SpecializedOps.diffNormF(orig, foundA.getMatrix()) / foundA.normF();
    }
    
    public static double quality(final DenseMatrix64F orig, final EigenDecomposition<DenseMatrix64F> eig) {
        final SimpleMatrix A = SimpleMatrix.wrap(orig);
        final SimpleMatrix V = SimpleMatrix.wrap(EigenOps.createMatrixV(eig));
        final SimpleMatrix D = SimpleMatrix.wrap(EigenOps.createMatrixD(eig));
        final SimpleMatrix L = A.mult(V);
        final SimpleMatrix R = V.mult(D);
        final SimpleMatrix diff = L.minus(R);
        final double top = diff.normF();
        final double bottom = L.normF();
        final double error = top / bottom;
        return error;
    }
    
    public static TridiagonalSimilarDecomposition<DenseMatrix64F> tridiagonal(final int matrixSize) {
        if (matrixSize >= 1800) {
            return new TridiagonalDecomposition_B64_to_D64();
        }
        return new TridiagonalDecompositionHouseholder_D64();
    }
    
    public static <T extends ReshapeMatrix64F> boolean decomposeSafe(final DecompositionInterface<T> decomp, final T M) {
        if (decomp.inputModified()) {
            return decomp.decompose(M.copy());
        }
        return decomp.decompose(M);
    }
}
