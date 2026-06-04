// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.ReshapeMatrix64F;

public class EjmlUnitTests
{
    public static void assertCountable(final ReshapeMatrix64F A) {
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                assertTrue(!Double.isNaN(A.get(i, j)), "NaN found at " + i + " " + j);
                assertTrue(!Double.isInfinite(A.get(i, j)), "Infinite found at " + i + " " + j);
            }
        }
    }
    
    public static void assertShape(final ReshapeMatrix64F A, final ReshapeMatrix64F B) {
        assertTrue(A.numRows == B.numRows, "Number of rows do not match");
        assertTrue(A.numCols == B.numCols, "Number of columns do not match");
    }
    
    public static void assertShape(final ReshapeMatrix64F A, final int numRows, final int numCols) {
        assertTrue(A.numRows == numRows, "Unexpected number of rows.");
        assertTrue(A.numCols == numCols, "Unexpected number of columns.");
    }
    
    public static void assertEqualsUncountable(final ReshapeMatrix64F A, final ReshapeMatrix64F B, final double tol) {
        assertShape(A, B);
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                final double valA = A.get(i, j);
                final double valB = B.get(i, j);
                if (Double.isNaN(valA)) {
                    assertTrue(Double.isNaN(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                }
                else if (Double.isInfinite(valA)) {
                    assertTrue(Double.isInfinite(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                }
                else {
                    final double diff = Math.abs(valA - valB);
                    assertTrue(diff <= tol, "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                }
            }
        }
    }
    
    public static void assertEquals(final ReshapeMatrix64F A, final ReshapeMatrix64F B, final double tol) {
        assertShape(A, B);
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                final double valA = A.get(i, j);
                final double valB = B.get(i, j);
                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                assertTrue(Math.abs(valA - valB) <= tol, "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
            }
        }
    }
    
    public static void assertEqualsTrans(final ReshapeMatrix64F A, final ReshapeMatrix64F B, final double tol) {
        assertShape(A, B.numCols, B.numRows);
        for (int i = 0; i < A.numRows; ++i) {
            for (int j = 0; j < A.numCols; ++j) {
                final double valA = A.get(i, j);
                final double valB = B.get(j, i);
                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB), "A(" + i + "," + j + ") = " + valA + ") B(" + j + "," + i + ") = " + valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB), "A(" + i + "," + j + ") = " + valA + ") B(" + j + "," + i + ") = " + valB);
                assertTrue(Math.abs(valA - valB) <= tol, "A(" + i + "," + j + ") = " + valA + ") B(" + j + "," + i + ") = " + valB);
            }
        }
    }
    
    private static void assertTrue(final boolean result, final String message) {
        assert result : message;
        if (!result) {
            throw new TestException(message);
        }
    }
    
    public static class TestException extends RuntimeException
    {
        public TestException(final String message) {
            super(message);
        }
    }
}
