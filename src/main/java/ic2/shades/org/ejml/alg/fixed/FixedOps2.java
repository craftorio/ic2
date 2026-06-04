// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.fixed;

import ic2.shades.org.ejml.data.FixedMatrix2_64F;
import ic2.shades.org.ejml.data.FixedMatrix2x2_64F;

public class FixedOps2
{
    public static void add(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
    }
    
    public static void addEquals(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a21 += b.a21;
        a.a22 += b.a22;
    }
    
    public static void transpose(final FixedMatrix2x2_64F m) {
        final double tmp = m.a12;
        m.a12 = m.a21;
        m.a21 = tmp;
    }
    
    public static FixedMatrix2x2_64F transpose(FixedMatrix2x2_64F input, final FixedMatrix2x2_64F output) {
        if (input == null) {
            input = new FixedMatrix2x2_64F();
        }
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a21 = input.a12;
        output.a22 = input.a22;
        return output;
    }
    
    public static void mult(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a21;
        c.a12 = a.a11 * b.a12 + a.a12 * b.a22;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a21;
        c.a22 = a.a21 * b.a12 + a.a22 * b.a22;
    }
    
    public static void multTransA(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a21;
        c.a12 = a.a11 * b.a12 + a.a21 * b.a22;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a21;
        c.a22 = a.a12 * b.a12 + a.a22 * b.a22;
    }
    
    public static void multTransAB(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a12;
        c.a12 = a.a11 * b.a21 + a.a21 * b.a22;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a12;
        c.a22 = a.a12 * b.a21 + a.a22 * b.a22;
    }
    
    public static void multTransB(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a12;
        c.a12 = a.a11 * b.a21 + a.a12 * b.a22;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a12;
        c.a22 = a.a21 * b.a21 + a.a22 * b.a22;
    }
    
    public static void mult(final FixedMatrix2x2_64F a, final FixedMatrix2_64F b, final FixedMatrix2_64F c) {
        c.a1 = a.a11 * b.a1 + a.a12 * b.a2;
        c.a2 = a.a21 * b.a1 + a.a22 * b.a2;
    }
    
    public static void mult(final FixedMatrix2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2_64F c) {
        c.a1 = a.a1 * b.a11 + a.a2 * b.a21;
        c.a2 = a.a1 * b.a12 + a.a2 * b.a22;
    }
    
    public static double dot(final FixedMatrix2_64F a, final FixedMatrix2_64F b) {
        return a.a1 * b.a1 + a.a2 * b.a2;
    }
    
    public static void setIdentity(final FixedMatrix2x2_64F a) {
        a.a11 = 1.0;
        a.a21 = 0.0;
        a.a12 = 0.0;
        a.a22 = 1.0;
    }
    
    public static boolean invert(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F inv) {
        final double scale = 1.0 / elementMaxAbs(a);
        final double a2 = a.a11 * scale;
        final double a3 = a.a12 * scale;
        final double a4 = a.a21 * scale;
        final double m11;
        final double a5 = m11 = a.a22 * scale;
        final double m12 = -a4;
        final double m13 = -a3;
        final double m14 = a2;
        final double det = (a2 * m11 + a3 * m12) / scale;
        inv.a11 = m11 / det;
        inv.a12 = m13 / det;
        inv.a21 = m12 / det;
        inv.a22 = m14 / det;
        return !Double.isNaN(det) && !Double.isInfinite(det);
    }
    
    public static double det(final FixedMatrix2x2_64F mat) {
        return mat.a11 * mat.a22 - mat.a12 * mat.a21;
    }
    
    public static double trace(final FixedMatrix2x2_64F a) {
        return a.a11 + a.a21;
    }
    
    public static void diag(final FixedMatrix2x2_64F input, final FixedMatrix2_64F out) {
        out.a1 = input.a11;
        out.a2 = input.a22;
    }
    
    public static double elementMax(final FixedMatrix2x2_64F a) {
        double max = a.a11;
        max = Math.max(max, a.a12);
        max = Math.max(max, a.a21);
        max = Math.max(max, a.a22);
        return max;
    }
    
    public static double elementMaxAbs(final FixedMatrix2x2_64F a) {
        double max = a.a11;
        max = Math.max(max, Math.abs(a.a12));
        max = Math.max(max, Math.abs(a.a21));
        max = Math.max(max, Math.abs(a.a22));
        return max;
    }
    
    public static double elementMin(final FixedMatrix2x2_64F a) {
        double min = a.a11;
        min = Math.min(min, a.a12);
        min = Math.min(min, a.a21);
        min = Math.min(min, a.a22);
        return min;
    }
    
    public static double elementMinAbs(final FixedMatrix2x2_64F a) {
        double min = a.a11;
        min = Math.min(min, Math.abs(a.a12));
        min = Math.min(min, Math.abs(a.a21));
        min = Math.min(min, Math.abs(a.a22));
        return min;
    }
    
    public static void elementMult(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b) {
        a.a11 *= b.a11;
        a.a12 *= b.a12;
        a.a21 *= b.a21;
        a.a22 *= b.a22;
    }
    
    public static void elementMult(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 * b.a11;
        c.a12 = a.a12 * b.a12;
        c.a21 = a.a21 * b.a21;
        c.a22 = a.a22 * b.a22;
    }
    
    public static void elementDiv(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b) {
        a.a11 /= b.a11;
        a.a12 /= b.a12;
        a.a21 /= b.a21;
        a.a22 /= b.a22;
    }
    
    public static void elementDiv(final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b, final FixedMatrix2x2_64F c) {
        c.a11 = a.a11 / b.a11;
        c.a12 = a.a12 / b.a12;
        c.a21 = a.a21 / b.a21;
        c.a22 = a.a22 / b.a22;
    }
    
    public static void scale(final double alpha, final FixedMatrix2x2_64F a) {
        a.a11 *= alpha;
        a.a12 *= alpha;
        a.a21 *= alpha;
        a.a22 *= alpha;
    }
    
    public static void scale(final double alpha, final FixedMatrix2x2_64F a, final FixedMatrix2x2_64F b) {
        b.a11 = a.a11 * alpha;
        b.a12 = a.a12 * alpha;
        b.a21 = a.a21 * alpha;
        b.a22 = a.a22 * alpha;
    }
    
    public static void divide(final FixedMatrix2x2_64F a, final double alpha) {
        a.a11 /= alpha;
        a.a12 /= alpha;
        a.a21 /= alpha;
        a.a22 /= alpha;
    }
    
    public static void divide(final FixedMatrix2x2_64F a, final double alpha, final FixedMatrix2x2_64F b) {
        b.a11 = a.a11 / alpha;
        b.a12 = a.a12 / alpha;
        b.a21 = a.a21 / alpha;
        b.a22 = a.a22 / alpha;
    }
    
    public static void changeSign(final FixedMatrix2x2_64F a) {
        a.a11 = -a.a11;
        a.a12 = -a.a12;
        a.a21 = -a.a21;
        a.a22 = -a.a22;
    }
    
    public static void fill(final FixedMatrix2x2_64F a, final double v) {
        a.a11 = v;
        a.a12 = v;
        a.a21 = v;
        a.a22 = v;
    }
}
