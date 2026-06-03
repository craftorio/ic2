package ic2.shades.org.ejml.alg.dense.linsol.qr;

import ic2.shades.org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrUpdate;
import ic2.shades.org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class AdjLinearSolverQr extends LinearSolverQr implements AdjustableLinearSolver {
  private QrUpdate update;
  
  private DenseMatrix64F A;
  
  public AdjLinearSolverQr() {
    super((QRDecomposition<DenseMatrix64F>)new QRDecompositionHouseholderColumn_D64());
  }
  
  public void setMaxSize(int maxRows, int maxCols) {
    maxRows += 5;
    super.setMaxSize(maxRows, maxCols);
    this.update = new QrUpdate(maxRows, maxCols, true);
    this.A = new DenseMatrix64F(maxRows, maxCols);
  }
  
  public DenseMatrix64F getA() {
    if (this.A.data.length < this.numRows * this.numCols)
      this.A = new DenseMatrix64F(this.numRows, this.numCols); 
    this.A.reshape(this.numRows, this.numCols, false);
    CommonOps.mult((RowD1Matrix64F)this.Q, (RowD1Matrix64F)this.R, (RowD1Matrix64F)this.A);
    return this.A;
  }
  
  public boolean addRowToA(double[] A_row, int rowIndex) {
    if (this.numRows + 1 > this.maxRows) {
      int grow = this.maxRows / 10;
      if (grow < 1)
        grow = 1; 
      this.maxRows = this.numRows + grow;
      this.Q.reshape(this.maxRows, this.maxRows, true);
      this.R.reshape(this.maxRows, this.maxCols, true);
    } 
    this.update.addRow(this.Q, this.R, A_row, rowIndex, true);
    this.numRows++;
    return true;
  }
  
  public boolean removeRowFromA(int index) {
    this.update.deleteRow(this.Q, this.R, index, true);
    this.numRows--;
    return true;
  }
}
