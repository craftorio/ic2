// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class MatrixVectorMult
{
    public static void mult(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numRows) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numCols != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numCols != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        if (A.numCols == 0) {
            CommonOps.fill(C, 0.0);
            return;
        }
        int indexA = 0;
        int cIndex = 0;
        final double b0 = B.get(0);
        for (int i = 0; i < A.numRows; ++i) {
            double total = A.get(indexA++) * b0;
            for (int j = 1; j < A.numCols; ++j) {
                total += A.get(indexA++) * B.get(j);
            }
            C.set(cIndex++, total);
        }
    }
    
    public static void multAdd(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numRows) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numCols != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numCols != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        if (A.numCols == 0) {
            return;
        }
        int indexA = 0;
        int cIndex = 0;
        for (int i = 0; i < A.numRows; ++i) {
            double total = A.get(indexA++) * B.get(0);
            for (int j = 1; j < A.numCols; ++j) {
                total += A.get(indexA++) * B.get(j);
            }
            C.plus(cIndex++, total);
        }
    }
    
    public static void multTransA_small(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numCols) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        int cIndex = 0;
        for (int i = 0; i < A.numCols; ++i) {
            double total = 0.0;
            int indexA = i;
            for (int j = 0; j < A.numRows; ++j) {
                total += A.get(indexA) * B.get(j);
                indexA += A.numCols;
            }
            C.set(cIndex++, total);
        }
    }
    
    public static void multTransA_reorder(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numCols) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        if (A.numRows == 0) {
            CommonOps.fill(C, 0.0);
            return;
        }
        double B_val = B.get(0);
        for (int i = 0; i < A.numCols; ++i) {
            C.set(i, A.get(i) * B_val);
        }
        int indexA = A.numCols;
        for (int j = 1; j < A.numRows; ++j) {
            B_val = B.get(j);
            for (int k = 0; k < A.numCols; ++k) {
                C.plus(k, A.get(indexA++) * B_val);
            }
        }
    }
    
    public static void multAddTransA_small(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numCols) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        int cIndex = 0;
        for (int i = 0; i < A.numCols; ++i) {
            double total = 0.0;
            int indexA = i;
            for (int j = 0; j < A.numRows; ++j) {
                total += A.get(indexA) * B.get(j);
                indexA += A.numCols;
            }
            C.plus(cIndex++, total);
        }
    }
    
    public static void multAddTransA_reorder(final RowD1Matrix64F A, final D1Matrix64F B, final D1Matrix64F C) {
        if (C.numCols != 1) {
            throw new MatrixDimensionException("C is not a column vector");
        }
        if (C.numRows != A.numCols) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        else {
            if (B.numCols != 1) {
                throw new MatrixDimensionException("B is not a vector");
            }
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        }
        if (A.numRows == 0) {
            return;
        }
        int indexA = 0;
        for (int j = 0; j < A.numRows; ++j) {
            final double B_val = B.get(j);
            for (int i = 0; i < A.numCols; ++i) {
                C.plus(i, A.get(indexA++) * B_val);
            }
        }
    }
}
