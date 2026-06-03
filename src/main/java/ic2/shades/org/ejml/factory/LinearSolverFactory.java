package ic2.shades.org.ejml.factory;

import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import ic2.shades.org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import ic2.shades.org.ejml.alg.dense.linsol.chol.LinearSolverCholBlock64;
import ic2.shades.org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import ic2.shades.org.ejml.alg.dense.linsol.qr.AdjLinearSolverQr;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrBlock64;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import ic2.shades.org.ejml.alg.dense.linsol.qr.LinearSolverQrpHouseCol;
import ic2.shades.org.ejml.alg.dense.linsol.qr.SolvePseudoInverseQrp;
import ic2.shades.org.ejml.alg.dense.linsol.svd.SolvePseudoInverseSvd;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRPDecomposition;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

public class LinearSolverFactory {
  public static LinearSolver<DenseMatrix64F> general(int numRows, int numCols) {
    if (numRows == numCols)
      return linear(numRows); 
    return leastSquares(numRows, numCols);
  }
  
  public static LinearSolver<DenseMatrix64F> linear(int matrixSize) {
    return (LinearSolver<DenseMatrix64F>)new LinearSolverLu((LUDecompositionBase_D64)new LUDecompositionAlt_D64());
  }
  
  public static LinearSolver<DenseMatrix64F> leastSquares(int numRows, int numCols) {
    if (numCols < EjmlParameters.SWITCH_BLOCK64_QR)
      return (LinearSolver<DenseMatrix64F>)new LinearSolverQrHouseCol(); 
    if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER)
      return (LinearSolver<DenseMatrix64F>)new LinearSolverQrBlock64(); 
    return (LinearSolver<DenseMatrix64F>)new LinearSolverQrHouseCol();
  }
  
  public static LinearSolver<DenseMatrix64F> symmPosDef(int matrixWidth) {
    if (matrixWidth < EjmlParameters.SWITCH_BLOCK64_CHOLESKY) {
      CholeskyDecompositionInner_D64 choleskyDecompositionInner_D641 = new CholeskyDecompositionInner_D64(true);
      return (LinearSolver<DenseMatrix64F>)new LinearSolverChol((CholeskyDecompositionCommon_D64)choleskyDecompositionInner_D641);
    } 
    if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER)
      return (LinearSolver<DenseMatrix64F>)new LinearSolverCholBlock64(); 
    CholeskyDecompositionInner_D64 choleskyDecompositionInner_D64 = new CholeskyDecompositionInner_D64(true);
    return (LinearSolver<DenseMatrix64F>)new LinearSolverChol((CholeskyDecompositionCommon_D64)choleskyDecompositionInner_D64);
  }
  
  public static LinearSolver<DenseMatrix64F> leastSquaresQrPivot(boolean computeNorm2, boolean computeQ) {
    QRColPivDecompositionHouseholderColumn_D64 decomposition = new QRColPivDecompositionHouseholderColumn_D64();
    if (computeQ)
      return (LinearSolver<DenseMatrix64F>)new SolvePseudoInverseQrp((QRPDecomposition)decomposition, computeNorm2); 
    return (LinearSolver<DenseMatrix64F>)new LinearSolverQrpHouseCol(decomposition, computeNorm2);
  }
  
  public static LinearSolver<DenseMatrix64F> pseudoInverse(boolean useSVD) {
    if (useSVD)
      return (LinearSolver<DenseMatrix64F>)new SolvePseudoInverseSvd(); 
    return leastSquaresQrPivot(true, false);
  }
  
  public static AdjustableLinearSolver adjustable() {
    return (AdjustableLinearSolver)new AdjLinearSolverQr();
  }
}
