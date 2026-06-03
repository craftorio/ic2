package ic2.shades.org.ejml.alg.block.linsol.qr;

import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.alg.block.BlockTriangularSolver;
import ic2.shades.org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.ops.SpecializedOps;

public class BlockQrHouseHolderSolver implements LinearSolver<BlockMatrix64F> {
  protected QRDecompositionHouseholder_B64 decomp = new QRDecompositionHouseholder_B64();
  
  protected BlockMatrix64F QR;
  
  public BlockQrHouseHolderSolver() {
    this.decomp.setSaveW(false);
  }
  
  public boolean setA(BlockMatrix64F A) {
    if (A.numRows < A.numCols)
      throw new IllegalArgumentException("Number of rows must be more than or equal to the number of columns.  Can't solve an underdetermined system."); 
    if (!this.decomp.decompose(A))
      return false; 
    this.QR = this.decomp.getQR();
    return true;
  }
  
  public double quality() {
    return SpecializedOps.qualityTriangular(true, (D1Matrix64F)this.decomp.getQR());
  }
  
  public void solve(BlockMatrix64F B, BlockMatrix64F X) {
    if (B.numCols != X.numCols)
      throw new IllegalArgumentException("Columns of B and X do not match"); 
    if (this.QR.numCols != X.numRows)
      throw new IllegalArgumentException("Rows in X do not match the columns in A"); 
    if (this.QR.numRows != B.numRows)
      throw new IllegalArgumentException("Rows in B do not match the rows in A."); 
    if (B.blockLength != this.QR.blockLength || X.blockLength != this.QR.blockLength)
      throw new IllegalArgumentException("All matrices must have the same block length."); 
    this.decomp.applyQTran(B);
    BlockMatrixOps.extractAligned(B, X);
    int M = Math.min(this.QR.numRows, this.QR.numCols);
    BlockTriangularSolver.solve(this.QR.blockLength, true, new D1Submatrix64F((D1Matrix64F)this.QR, 0, M, 0, M), new D1Submatrix64F((D1Matrix64F)X), false);
  }
  
  public void invert(BlockMatrix64F A_inv) {
    int M = Math.min(this.QR.numRows, this.QR.numCols);
    if (A_inv.numRows != M || A_inv.numCols != M)
      throw new IllegalArgumentException("A_inv must be square an have dimension " + M); 
    BlockMatrixOps.setIdentity(A_inv);
    this.decomp.applyQTran(A_inv);
    BlockTriangularSolver.solve(this.QR.blockLength, true, new D1Submatrix64F((D1Matrix64F)this.QR, 0, M, 0, M), new D1Submatrix64F((D1Matrix64F)A_inv), false);
  }
  
  public boolean modifiesA() {
    return this.decomp.inputModified();
  }
  
  public boolean modifiesB() {
    return true;
  }
}
