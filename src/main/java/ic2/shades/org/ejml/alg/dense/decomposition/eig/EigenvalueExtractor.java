package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public interface EigenvalueExtractor {
  boolean process(DenseMatrix64F paramDenseMatrix64F);
  
  int getNumberOfEigenvalues();
  
  Complex64F[] getEigenvalues();
}
