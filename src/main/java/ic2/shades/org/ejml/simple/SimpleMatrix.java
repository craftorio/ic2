// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.ops.RandomMatrices;
import java.util.Random;
import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.alg.generic.GenericMatrixOps;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class SimpleMatrix extends SimpleBase<SimpleMatrix>
{
    public static final int END = Integer.MAX_VALUE;
    
    public SimpleMatrix(final int numRows, final int numCols, final boolean rowMajor, final double... data) {
        this.mat = new DenseMatrix64F(numRows, numCols, rowMajor, data);
    }
    
    public SimpleMatrix(final double[][] data) {
        this.mat = new DenseMatrix64F(data);
    }
    
    public SimpleMatrix(final int numRows, final int numCols) {
        this.mat = new DenseMatrix64F(numRows, numCols);
    }
    
    public SimpleMatrix(final SimpleMatrix orig) {
        this.mat = orig.mat.copy();
    }
    
    public SimpleMatrix(final DenseMatrix64F orig) {
        this.mat = orig.copy();
    }
    
    public SimpleMatrix(final ReshapeMatrix64F orig) {
        GenericMatrixOps.copy(orig, this.mat = new DenseMatrix64F(orig.numRows, orig.numCols));
    }
    
    public SimpleMatrix() {
    }
    
    public static SimpleMatrix wrap(final DenseMatrix64F internalMat) {
        final SimpleMatrix ret = new SimpleMatrix();
        ret.mat = internalMat;
        return ret;
    }
    
    public static SimpleMatrix identity(final int width) {
        final SimpleMatrix ret = new SimpleMatrix(width, width);
        CommonOps.setIdentity(ret.mat);
        return ret;
    }
    
    public static SimpleMatrix diag(final double... vals) {
        final DenseMatrix64F m = CommonOps.diag(vals);
        final SimpleMatrix ret = wrap(m);
        return ret;
    }
    
    public static SimpleMatrix random(final int numRows, final int numCols, final double minValue, final double maxValue, final Random rand) {
        final SimpleMatrix ret = new SimpleMatrix(numRows, numCols);
        RandomMatrices.setRandom(ret.mat, minValue, maxValue, rand);
        return ret;
    }
    
    @Override
    protected SimpleMatrix createMatrix(final int numRows, final int numCols) {
        return new SimpleMatrix(numRows, numCols);
    }
}
