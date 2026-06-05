package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.alg.generic.GenericMatrixOps;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.RandomMatrices;
import java.util.Random;

public class SimpleMatrix extends SimpleBase<SimpleMatrix> {
   public static final int END = Integer.MAX_VALUE;

   public SimpleMatrix(int numRows, int numCols, boolean rowMajor, double... data) {
      this.mat = new DenseMatrix64F(numRows, numCols, rowMajor, data);
   }

   public SimpleMatrix(double[][] data) {
      this.mat = new DenseMatrix64F(data);
   }

   public SimpleMatrix(int numRows, int numCols) {
      this.mat = new DenseMatrix64F(numRows, numCols);
   }

   public SimpleMatrix(SimpleMatrix orig) {
      this.mat = orig.mat.copy();
   }

   public SimpleMatrix(DenseMatrix64F orig) {
      this.mat = orig.copy();
   }

   public SimpleMatrix(ReshapeMatrix64F orig) {
      this.mat = new DenseMatrix64F(orig.numRows, orig.numCols);
      GenericMatrixOps.copy(orig, this.mat);
   }

   public SimpleMatrix() {
   }

   public static SimpleMatrix wrap(DenseMatrix64F internalMat) {
      SimpleMatrix ret = new SimpleMatrix();
      ret.mat = internalMat;
      return ret;
   }

   public static SimpleMatrix identity(int width) {
      SimpleMatrix ret = new SimpleMatrix(width, width);
      CommonOps.setIdentity(ret.mat);
      return ret;
   }

   public static SimpleMatrix diag(double... vals) {
      DenseMatrix64F m = CommonOps.diag(vals);
      return wrap(m);
   }

   public static SimpleMatrix random(int numRows, int numCols, double minValue, double maxValue, Random rand) {
      SimpleMatrix ret = new SimpleMatrix(numRows, numCols);
      RandomMatrices.setRandom(ret.mat, minValue, maxValue, rand);
      return ret;
   }

   protected SimpleMatrix createMatrix(int numRows, int numCols) {
      return new SimpleMatrix(numRows, numCols);
   }
}
