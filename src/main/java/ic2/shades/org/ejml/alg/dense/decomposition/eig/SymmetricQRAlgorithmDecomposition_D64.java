package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.alg.dense.decomposition.eig.symm.SymmetricQREigenHelper;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.symm.SymmetricQrAlgorithm;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import ic2.shades.org.ejml.ops.CommonOps;

public class SymmetricQRAlgorithmDecomposition_D64 implements EigenDecomposition<DenseMatrix64F> {
  private TridiagonalSimilarDecomposition<DenseMatrix64F> decomp;
  
  private SymmetricQREigenHelper helper;
  
  private SymmetricQrAlgorithm vector;
  
  private boolean computeVectorsWithValues = false;
  
  private double[] values;
  
  private double[] diag;
  
  private double[] off;
  
  private double[] diagSaved;
  
  private double[] offSaved;
  
  private DenseMatrix64F V;
  
  private DenseMatrix64F[] eigenvectors;
  
  boolean computeVectors;
  
  public SymmetricQRAlgorithmDecomposition_D64(TridiagonalSimilarDecomposition<DenseMatrix64F> decomp, boolean computeVectors) {
    this.decomp = decomp;
    this.computeVectors = computeVectors;
    this.helper = new SymmetricQREigenHelper();
    this.vector = new SymmetricQrAlgorithm(this.helper);
  }
  
  public SymmetricQRAlgorithmDecomposition_D64(boolean computeVectors) {
    this(DecompositionFactory.tridiagonal(0), computeVectors);
  }
  
  public void setComputeVectorsWithValues(boolean computeVectorsWithValues) {
    if (!this.computeVectors)
      throw new IllegalArgumentException("Compute eigenvalues has been set to false"); 
    this.computeVectorsWithValues = computeVectorsWithValues;
  }
  
  public void setMaxIterations(int max) {
    this.vector.setMaxIterations(max);
  }
  
  public int getNumberOfEigenvalues() {
    return this.helper.getMatrixSize();
  }
  
  public Complex64F getEigenvalue(int index) {
    return new Complex64F(this.values[index], 0.0D);
  }
  
  public DenseMatrix64F getEigenVector(int index) {
    return this.eigenvectors[index];
  }
  
  public boolean decompose(DenseMatrix64F orig) {
    if (orig.numCols != orig.numRows)
      throw new IllegalArgumentException("Matrix must be square."); 
    if (orig.numCols <= 0)
      return false; 
    int N = orig.numRows;
    if (!this.decomp.decompose((Matrix64F)orig))
      return false; 
    if (this.diag == null || this.diag.length < N) {
      this.diag = new double[N];
      this.off = new double[N - 1];
    } 
    this.decomp.getDiagonal(this.diag, this.off);
    this.helper.init(this.diag, this.off, N);
    if (this.computeVectors) {
      if (this.computeVectorsWithValues)
        return extractTogether(); 
      return extractSeparate(N);
    } 
    return computeEigenValues();
  }
  
  public boolean inputModified() {
    return this.decomp.inputModified();
  }
  
  private boolean extractTogether() {
    this.V = (DenseMatrix64F)this.decomp.getQ((ReshapeMatrix64F)this.V, true);
    this.helper.setQ(this.V);
    this.vector.setFastEigenvalues(false);
    if (!this.vector.process(-1, null, null))
      return false; 
    this.eigenvectors = CommonOps.rowsToVector(this.V, this.eigenvectors);
    this.values = this.helper.copyEigenvalues(this.values);
    return true;
  }
  
  private boolean extractSeparate(int numCols) {
    if (!computeEigenValues())
      return false; 
    this.helper.reset(numCols);
    this.diagSaved = this.helper.swapDiag(this.diagSaved);
    this.offSaved = this.helper.swapOff(this.offSaved);
    this.V = (DenseMatrix64F)this.decomp.getQ((ReshapeMatrix64F)this.V, true);
    this.vector.setQ(this.V);
    if (!this.vector.process(-1, null, null, this.values))
      return false; 
    this.values = this.helper.copyEigenvalues(this.values);
    this.eigenvectors = CommonOps.rowsToVector(this.V, this.eigenvectors);
    return true;
  }
  
  private boolean computeEigenValues() {
    this.diagSaved = this.helper.copyDiag(this.diagSaved);
    this.offSaved = this.helper.copyOff(this.offSaved);
    this.vector.setQ(null);
    this.vector.setFastEigenvalues(true);
    if (!this.vector.process(-1, null, null))
      return false; 
    this.values = this.helper.copyEigenvalues(this.values);
    return true;
  }
}
