// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.generic;

import java.util.Random;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public class GenericMatrixOps
{
    public static boolean isEquivalent(final ReshapeMatrix64F a, final ReshapeMatrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        for (int i = 0; i < a.numRows; ++i) {
            for (int j = 0; j < a.numCols; ++j) {
                final double diff = Math.abs(a.get(i, j) - b.get(i, j));
                if (diff > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isIdentity(final ReshapeMatrix64F a, final double tol) {
        for (int i = 0; i < a.numRows; ++i) {
            for (int j = 0; j < a.numCols; ++j) {
                if (i == j) {
                    if (Math.abs(a.get(i, j) - 1.0) > tol) {
                        return false;
                    }
                }
                else if (Math.abs(a.get(i, j)) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isEquivalentTriangle(final boolean upper, final ReshapeMatrix64F a, final ReshapeMatrix64F b, final double tol) {
        if (a.numRows != b.numRows || a.numCols != b.numCols) {
            return false;
        }
        if (upper) {
            for (int i = 0; i < a.numRows; ++i) {
                for (int j = i; j < a.numCols; ++j) {
                    final double diff = Math.abs(a.get(i, j) - b.get(i, j));
                    if (diff > tol) {
                        return false;
                    }
                }
            }
        }
        else {
            for (int k = 0; k < a.numCols; ++k) {
                for (int l = k; l < a.numRows; ++l) {
                    final double diff = Math.abs(a.get(l, k) - b.get(l, k));
                    if (diff > tol) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static void copy(final ReshapeMatrix64F from, final ReshapeMatrix64F to) {
        final int numCols = from.getNumCols();
        for (int numRows = from.getNumRows(), i = 0; i < numRows; ++i) {
            for (int j = 0; j < numCols; ++j) {
                to.set(i, j, from.get(i, j));
            }
        }
    }
    
    public static void setRandom(final ReshapeMatrix64F a, final double min, final double max, final Random rand) {
        for (int i = 0; i < a.numRows; ++i) {
            for (int j = 0; j < a.numCols; ++j) {
                final double val = rand.nextDouble() * (max - min) + min;
                a.set(i, j, val);
            }
        }
    }
}
