// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block.decomposition.qr;

import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.alg.block.BlockMultiplication;
import ic2.shades.org.ejml.alg.block.BlockMatrixOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.D1Submatrix64F;
import ic2.shades.org.ejml.data.BlockMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.QRDecomposition;

public class QRDecompositionHouseholder_B64 implements QRDecomposition<BlockMatrix64F>
{
    private BlockMatrix64F dataA;
    private BlockMatrix64F dataW;
    private BlockMatrix64F dataWTA;
    private int blockLength;
    private D1Submatrix64F A;
    private D1Submatrix64F Y;
    private D1Submatrix64F W;
    private D1Submatrix64F WTA;
    private double[] temp;
    private double[] gammas;
    private boolean saveW;
    
    public QRDecompositionHouseholder_B64() {
        this.dataW = new BlockMatrix64F(1, 1);
        this.dataWTA = new BlockMatrix64F(1, 1);
        this.A = new D1Submatrix64F();
        this.Y = new D1Submatrix64F();
        this.W = new D1Submatrix64F(this.dataW);
        this.WTA = new D1Submatrix64F(this.dataWTA);
        this.temp = new double[1];
        this.gammas = new double[1];
        this.saveW = false;
    }
    
    public BlockMatrix64F getQR() {
        return this.dataA;
    }
    
    public void setSaveW(final boolean saveW) {
        this.saveW = saveW;
    }
    
    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, final boolean compact) {
        Q = initializeQ(Q, this.dataA.numRows, this.dataA.numCols, this.blockLength, compact);
        this.applyQ(Q, true);
        return Q;
    }
    
    public static BlockMatrix64F initializeQ(BlockMatrix64F Q, final int numRows, final int numCols, final int blockLength, final boolean compact) {
        final int minLength = Math.min(numRows, numCols);
        if (compact) {
            if (Q == null) {
                Q = new BlockMatrix64F(numRows, minLength, blockLength);
                BlockMatrixOps.setIdentity(Q);
            }
            else {
                if (Q.numRows != numRows || Q.numCols != minLength) {
                    throw new IllegalArgumentException("Unexpected matrix dimension. Found " + Q.numRows + " " + Q.numCols);
                }
                BlockMatrixOps.setIdentity(Q);
            }
        }
        else if (Q == null) {
            Q = new BlockMatrix64F(numRows, numRows, blockLength);
            BlockMatrixOps.setIdentity(Q);
        }
        else {
            if (Q.numRows != numRows || Q.numCols != numRows) {
                throw new IllegalArgumentException("Unexpected matrix dimension. Found " + Q.numRows + " " + Q.numCols);
            }
            BlockMatrixOps.setIdentity(Q);
        }
        return Q;
    }
    
    public void applyQ(final BlockMatrix64F B) {
        this.applyQ(B, false);
    }
    
    public void applyQ(final BlockMatrix64F B, final boolean isIdentity) {
        final int minDimen = Math.min(this.dataA.numCols, this.dataA.numRows);
        final D1Submatrix64F subB = new D1Submatrix64F(B);
        final D1Submatrix64F w = this.W;
        final D1Submatrix64F w2 = this.W;
        final int n = 0;
        w2.row0 = n;
        w.col0 = n;
        final D1Submatrix64F y = this.Y;
        final D1Submatrix64F w3 = this.W;
        final int numRows = this.dataA.numRows;
        w3.row1 = numRows;
        y.row1 = numRows;
        final D1Submatrix64F wta = this.WTA;
        final D1Submatrix64F wta2 = this.WTA;
        final int n2 = 0;
        wta2.col0 = n2;
        wta.row0 = n2;
        int start = minDimen - minDimen % this.blockLength;
        if (start == minDimen) {
            start -= this.blockLength;
        }
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i >= 0; i -= this.blockLength) {
            this.Y.col0 = i;
            this.Y.col1 = Math.min(this.Y.col0 + this.blockLength, this.dataA.numCols);
            this.Y.row0 = i;
            if (isIdentity) {
                subB.col0 = i;
            }
            subB.row0 = i;
            this.setW();
            this.WTA.row1 = this.Y.col1 - this.Y.col0;
            this.WTA.col1 = subB.col1 - subB.col0;
            this.WTA.original.reshape(this.WTA.row1, this.WTA.col1, false);
            if (!this.saveW) {
                BlockHouseHolder.computeW_Column(this.blockLength, this.Y, this.W, this.temp, this.gammas, this.Y.col0);
            }
            BlockHouseHolder.multTransA_vecCol(this.blockLength, this.Y, subB, this.WTA);
            BlockMultiplication.multPlus(this.blockLength, this.W, this.WTA, subB);
        }
    }
    
    public void applyQTran(final BlockMatrix64F B) {
        final int minDimen = Math.min(this.dataA.numCols, this.dataA.numRows);
        final D1Submatrix64F subB = new D1Submatrix64F(B);
        final D1Submatrix64F w = this.W;
        final D1Submatrix64F w2 = this.W;
        final int n = 0;
        w2.row0 = n;
        w.col0 = n;
        final D1Submatrix64F y = this.Y;
        final D1Submatrix64F w3 = this.W;
        final int numRows = this.dataA.numRows;
        w3.row1 = numRows;
        y.row1 = numRows;
        final D1Submatrix64F wta = this.WTA;
        final D1Submatrix64F wta2 = this.WTA;
        final int n2 = 0;
        wta2.col0 = n2;
        wta.row0 = n2;
        for (int i = 0; i < minDimen; i += this.blockLength) {
            this.Y.col0 = i;
            this.Y.col1 = Math.min(this.Y.col0 + this.blockLength, this.dataA.numCols);
            this.Y.row0 = i;
            subB.row0 = i;
            this.setW();
            this.WTA.row0 = 0;
            this.WTA.col0 = 0;
            this.WTA.row1 = this.W.col1 - this.W.col0;
            this.WTA.col1 = subB.col1 - subB.col0;
            this.WTA.original.reshape(this.WTA.row1, this.WTA.col1, false);
            if (!this.saveW) {
                BlockHouseHolder.computeW_Column(this.blockLength, this.Y, this.W, this.temp, this.gammas, this.Y.col0);
            }
            BlockMultiplication.multTransA(this.blockLength, this.W, subB, this.WTA);
            BlockHouseHolder.multAdd_zeros(this.blockLength, this.Y, this.WTA, subB);
        }
    }
    
    @Override
    public BlockMatrix64F getR(BlockMatrix64F R, final boolean compact) {
        final int min = Math.min(this.dataA.numRows, this.dataA.numCols);
        if (R == null) {
            if (compact) {
                R = new BlockMatrix64F(min, this.dataA.numCols, this.blockLength);
            }
            else {
                R = new BlockMatrix64F(this.dataA.numRows, this.dataA.numCols, this.blockLength);
            }
        }
        else if (compact) {
            if (R.numCols != this.dataA.numCols || R.numRows != min) {
                throw new IllegalArgumentException("Unexpected dimension.");
            }
        }
        else if (R.numCols != this.dataA.numCols || R.numRows != this.dataA.numRows) {
            throw new IllegalArgumentException("Unexpected dimension.");
        }
        BlockMatrixOps.zeroTriangle(false, R);
        BlockMatrixOps.copyTriangle(true, this.dataA, R);
        return R;
    }
    
    @Override
    public boolean decompose(final BlockMatrix64F orig) {
        this.setup(orig);
        for (int m = Math.min(orig.numCols, orig.numRows), j = 0; j < m; j += this.blockLength) {
            this.Y.col0 = j;
            this.Y.col1 = Math.min(orig.numCols, this.Y.col0 + this.blockLength);
            this.Y.row0 = j;
            if (!BlockHouseHolder.decomposeQR_block_col(this.blockLength, this.Y, this.gammas)) {
                return false;
            }
            this.updateA(this.A);
        }
        return true;
    }
    
    private void setup(final BlockMatrix64F orig) {
        this.blockLength = orig.blockLength;
        this.dataW.blockLength = this.blockLength;
        this.dataWTA.blockLength = this.blockLength;
        this.dataA = orig;
        this.A.original = this.dataA;
        final int l = Math.min(this.blockLength, orig.numCols);
        this.dataW.reshape(orig.numRows, l, false);
        this.dataWTA.reshape(l, orig.numRows, false);
        this.Y.original = orig;
        final D1Submatrix64F y = this.Y;
        final D1Submatrix64F w = this.W;
        final int numRows = orig.numRows;
        w.row1 = numRows;
        y.row1 = numRows;
        if (this.temp.length < this.blockLength) {
            this.temp = new double[this.blockLength];
        }
        if (this.gammas.length < orig.numCols) {
            this.gammas = new double[orig.numCols];
        }
        if (this.saveW) {
            this.dataW.reshape(orig.numRows, orig.numCols, false);
        }
    }
    
    protected void updateA(final D1Submatrix64F A) {
        this.setW();
        A.row0 = this.Y.row0;
        A.row1 = this.Y.row1;
        A.col0 = this.Y.col1;
        A.col1 = this.Y.original.numCols;
        this.WTA.row0 = 0;
        this.WTA.col0 = 0;
        this.WTA.row1 = this.W.col1 - this.W.col0;
        this.WTA.col1 = A.col1 - A.col0;
        this.WTA.original.reshape(this.WTA.row1, this.WTA.col1, false);
        if (A.col1 > A.col0) {
            BlockHouseHolder.computeW_Column(this.blockLength, this.Y, this.W, this.temp, this.gammas, this.Y.col0);
            BlockMultiplication.multTransA(this.blockLength, this.W, A, this.WTA);
            BlockHouseHolder.multAdd_zeros(this.blockLength, this.Y, this.WTA, A);
        }
        else if (this.saveW) {
            BlockHouseHolder.computeW_Column(this.blockLength, this.Y, this.W, this.temp, this.gammas, this.Y.col0);
        }
    }
    
    private void setW() {
        if (this.saveW) {
            this.W.col0 = this.Y.col0;
            this.W.col1 = this.Y.col1;
            this.W.row0 = this.Y.row0;
            this.W.row1 = this.Y.row1;
        }
        else {
            this.W.col1 = this.Y.col1 - this.Y.col0;
            this.W.row0 = this.Y.row0;
        }
    }
    
    @Override
    public boolean inputModified() {
        return true;
    }
}
