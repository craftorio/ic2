package ic2.shades.org.ejml.factory;

import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecomposition_B64_to_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.WatchedDoubleStepQRDecomposition_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecomposition_B64_to_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose_D64;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.CholeskyLDLDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.LUDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.SingularValueDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.ops.EigenOps;
import ic2.shades.org.ejml.ops.SpecializedOps;
import ic2.shades.org.ejml.simple.SimpleMatrix;

public class DecompositionFactory
{
	public static CholeskyDecomposition<DenseMatrix64F> chol(int matrixSize, boolean lower)
	{
		if (matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY)
		{
			return new CholeskyDecompositionInner_D64(lower);
		} else
		{
			return EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER
				? new CholeskyDecomposition_B64_to_D64(lower)
				: new CholeskyDecompositionBlock_D64(EjmlParameters.BLOCK_WIDTH_CHOL);
		}
	}

	public static CholeskyLDLDecomposition<DenseMatrix64F> cholLDL(int matrixSize)
	{
		return new CholeskyDecompositionLDL_D64();
	}

	public static LUDecomposition<DenseMatrix64F> lu(int numRows, int numCol)
	{
		return new LUDecompositionAlt_D64();
	}

	public static SingularValueDecomposition<DenseMatrix64F> svd(int numRows, int numCols, boolean needU, boolean needV, boolean compact)
	{
		return new SvdImplicitQrDecompose_D64(compact, needU, needV, false);
	}

	public static QRDecomposition<DenseMatrix64F> qr(int numRows, int numCols)
	{
		return new QRDecompositionHouseholderColumn_D64();
	}

	public static QRPDecomposition<DenseMatrix64F> qrp(int numRows, int numCols)
	{
		return new QRColPivDecompositionHouseholderColumn_D64();
	}

	public static EigenDecomposition<DenseMatrix64F> eig(int matrixSize, boolean needVectors)
	{
		return new SwitchingEigenDecomposition(matrixSize, needVectors, 1.0E-8);
	}

	public static EigenDecomposition<DenseMatrix64F> eig(int matrixSize, boolean computeVectors, boolean isSymmetric)
	{
		if (isSymmetric)
		{
			TridiagonalSimilarDecomposition<DenseMatrix64F> decomp = tridiagonal(matrixSize);
			return new SymmetricQRAlgorithmDecomposition_D64(decomp, computeVectors);
		} else
		{
			return new WatchedDoubleStepQRDecomposition_D64(computeVectors);
		}
	}

	public static double quality(DenseMatrix64F orig, SingularValueDecomposition<DenseMatrix64F> svd)
	{
		return quality(orig, svd.getU(null, false), svd.getW(null), svd.getV(null, true));
	}

	public static double quality(DenseMatrix64F orig, DenseMatrix64F U, DenseMatrix64F W, DenseMatrix64F Vt)
	{
		SimpleMatrix _U = SimpleMatrix.wrap(U);
		SimpleMatrix _W = SimpleMatrix.wrap(W);
		SimpleMatrix _Vt = SimpleMatrix.wrap(Vt);
		SimpleMatrix foundA = _U.mult(_W).mult(_Vt);
		return SpecializedOps.diffNormF(orig, foundA.getMatrix()) / foundA.normF();
	}

	public static double quality(DenseMatrix64F orig, EigenDecomposition<DenseMatrix64F> eig)
	{
		SimpleMatrix A = SimpleMatrix.wrap(orig);
		SimpleMatrix V = SimpleMatrix.wrap(EigenOps.createMatrixV(eig));
		SimpleMatrix D = SimpleMatrix.wrap(EigenOps.createMatrixD(eig));
		SimpleMatrix L = A.mult(V);
		SimpleMatrix R = V.mult(D);
		SimpleMatrix diff = L.minus(R);
		double top = diff.normF();
		double bottom = L.normF();
		return top / bottom;
	}

	public static TridiagonalSimilarDecomposition<DenseMatrix64F> tridiagonal(int matrixSize)
	{
		return matrixSize >= 1800 ? new TridiagonalDecomposition_B64_to_D64() : new TridiagonalDecompositionHouseholder_D64();
	}

	public static <T extends ReshapeMatrix64F> boolean decomposeSafe(DecompositionInterface<T> decomp, T M)
	{
		return decomp.inputModified() ? decomp.decompose(M.copy()) : decomp.decompose(M);
	}
}
