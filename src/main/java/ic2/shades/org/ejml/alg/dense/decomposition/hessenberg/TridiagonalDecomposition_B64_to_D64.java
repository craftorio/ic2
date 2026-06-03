package ic2.shades.org.ejml.alg.dense.decomposition.hessenberg;

import ic2.shades.org.ejml.EjmlParameters;
import ic2.shades.org.ejml.alg.block.decomposition.hessenberg.TridiagonalDecompositionHouseholder_B64;
import ic2.shades.org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.DecompositionInterface;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class TridiagonalDecomposition_B64_to_D64 extends BaseDecomposition_B64_to_D64 implements TridiagonalSimilarDecomposition<DenseMatrix64F> {
  public TridiagonalDecomposition_B64_to_D64() {
    this(EjmlParameters.BLOCK_WIDTH);
  }
  
  public TridiagonalDecomposition_B64_to_D64(int blockSize) {
    super((DecompositionInterface)new TridiagonalDecompositionHouseholder_B64(), blockSize);
  }
  
  public DenseMatrix64F getT(DenseMatrix64F T) {
    int N = this.Ablock.numRows;
    if (T == null) {
      T = new DenseMatrix64F(N, N);
    } else {
      CommonOps.fill((D1Matrix64F)T, 0.0D);
    } 
    double[] diag = new double[N];
    double[] off = new double[N];
    ((TridiagonalDecompositionHouseholder_B64)this.alg).getDiagonal(diag, off);
    T.unsafe_set(0, 0, diag[0]);
    for (int i = 1; i < N; i++) {
      T.unsafe_set(i, i, diag[i]);
      T.unsafe_set(i, i - 1, off[i - 1]);
      T.unsafe_set(i - 1, i, off[i - 1]);
    } 
    return T;
  }
  
  public DenseMatrix64F getQ(DenseMatrix64F Q, boolean transposed) {
    if (Q == null)
      Q = new DenseMatrix64F(this.Ablock.numRows, this.Ablock.numCols); 
    BlockMatrix64F Qblock = new BlockMatrix64F();
    Qblock.numRows = Q.numRows;
    Qblock.numCols = Q.numCols;
    Qblock.blockLength = this.blockLength;
    Qblock.data = Q.data;
    ((TridiagonalDecompositionHouseholder_B64)this.alg).getQ(Qblock, transposed);
    convertBlockToRow(Q.numRows, Q.numCols, this.Ablock.blockLength, Q.data);
    return Q;
  }
  
  public void getDiagonal(double[] diag, double[] off) {
    ((TridiagonalDecompositionHouseholder_B64)this.alg).getDiagonal(diag, off);
  }
}
