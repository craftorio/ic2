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
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
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
import ic2.shades.org.ejml.simple.SimpleBase;
import ic2.shades.org.ejml.simple.SimpleMatrix;

public class DecompositionFactory {
  public static CholeskyDecomposition<DenseMatrix64F> chol(int matrixSize, boolean lower) {
    if (matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY)
      return (CholeskyDecomposition<DenseMatrix64F>)new CholeskyDecompositionInner_D64(lower); 
    if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER)
      return (CholeskyDecomposition<DenseMatrix64F>)new CholeskyDecomposition_B64_to_D64(lower); 
    return (CholeskyDecomposition<DenseMatrix64F>)new CholeskyDecompositionBlock_D64(EjmlParameters.BLOCK_WIDTH_CHOL);
  }
  
  public static CholeskyLDLDecomposition<DenseMatrix64F> cholLDL(int matrixSize) {
    return (CholeskyLDLDecomposition<DenseMatrix64F>)new CholeskyDecompositionLDL_D64();
  }
  
  public static LUDecomposition<DenseMatrix64F> lu(int numRows, int numCol) {
    return (LUDecomposition<DenseMatrix64F>)new LUDecompositionAlt_D64();
  }
  
  public static SingularValueDecomposition<DenseMatrix64F> svd(int numRows, int numCols, boolean needU, boolean needV, boolean compact) {
    return (SingularValueDecomposition<DenseMatrix64F>)new SvdImplicitQrDecompose_D64(compact, needU, needV, false);
  }
  
  public static QRDecomposition<DenseMatrix64F> qr(int numRows, int numCols) {
    return (QRDecomposition<DenseMatrix64F>)new QRDecompositionHouseholderColumn_D64();
  }
  
  public static QRPDecomposition<DenseMatrix64F> qrp(int numRows, int numCols) {
    return (QRPDecomposition<DenseMatrix64F>)new QRColPivDecompositionHouseholderColumn_D64();
  }
  
  public static EigenDecomposition<DenseMatrix64F> eig(int matrixSize, boolean needVectors) {
    return (EigenDecomposition<DenseMatrix64F>)new SwitchingEigenDecomposition(matrixSize, needVectors, 1.0E-8D);
  }
  
  public static EigenDecomposition<DenseMatrix64F> eig(int matrixSize, boolean computeVectors, boolean isSymmetric) {
    if (isSymmetric) {
      TridiagonalSimilarDecomposition<DenseMatrix64F> decomp = tridiagonal(matrixSize);
      return (EigenDecomposition<DenseMatrix64F>)new SymmetricQRAlgorithmDecomposition_D64(decomp, computeVectors);
    } 
    return (EigenDecomposition<DenseMatrix64F>)new WatchedDoubleStepQRDecomposition_D64(computeVectors);
  }
  
  public static double quality(DenseMatrix64F orig, SingularValueDecomposition<DenseMatrix64F> svd) {
    return quality(orig, (DenseMatrix64F)svd.getU(null, false), (DenseMatrix64F)svd.getW(null), (DenseMatrix64F)svd.getV(null, true));
  }
  
  public static double quality(DenseMatrix64F orig, DenseMatrix64F U, DenseMatrix64F W, DenseMatrix64F Vt) {
    SimpleMatrix _U = SimpleMatrix.wrap(U);
    SimpleMatrix _W = SimpleMatrix.wrap(W);
    SimpleMatrix _Vt = SimpleMatrix.wrap(Vt);
    SimpleMatrix foundA = (SimpleMatrix)((SimpleMatrix)_U.mult((SimpleBase)_W)).mult((SimpleBase)_Vt);
    return SpecializedOps.diffNormF((D1Matrix64F)orig, (D1Matrix64F)foundA.getMatrix()) / foundA.normF();
  }
  
  public static double quality(DenseMatrix64F orig, EigenDecomposition<DenseMatrix64F> eig) {
    SimpleMatrix A = SimpleMatrix.wrap(orig);
    SimpleMatrix V = SimpleMatrix.wrap(EigenOps.createMatrixV(eig));
    SimpleMatrix D = SimpleMatrix.wrap(EigenOps.createMatrixD(eig));
    SimpleMatrix L = (SimpleMatrix)A.mult((SimpleBase)V);
    SimpleMatrix R = (SimpleMatrix)V.mult((SimpleBase)D);
    SimpleMatrix diff = (SimpleMatrix)L.minus((SimpleBase)R);
    double top = diff.normF();
    double bottom = L.normF();
    double error = top / bottom;
    return error;
  }
  
  public static TridiagonalSimilarDecomposition<DenseMatrix64F> tridiagonal(int matrixSize) {
    if (matrixSize >= 1800)
      return (TridiagonalSimilarDecomposition<DenseMatrix64F>)new TridiagonalDecomposition_B64_to_D64(); 
    return (TridiagonalSimilarDecomposition<DenseMatrix64F>)new TridiagonalDecompositionHouseholder_D64();
  }
  
  public static <T extends ic2.shades.org.ejml.data.ReshapeMatrix64F> boolean decomposeSafe(DecompositionInterface<T> decomp, T M) {
    if (decomp.inputModified())
      return decomp.decompose(M.copy()); 
    return decomp.decompose((Matrix64F)M);
  }
}
