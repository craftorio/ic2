// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.fixed;

import ic2.shades.org.ejml.data.FixedMatrix3_64F;
import ic2.shades.org.ejml.data.FixedMatrix3x3_64F;

public class FixedOps3
{
    public static void add(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
    }
    
    public static void addEquals(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
    }
    
    public static void transpose(final FixedMatrix3x3_64F m) {
        double tmp = m.a12;
        m.a12 = m.a21;
        m.a21 = tmp;
        tmp = m.a13;
        m.a13 = m.a31;
        m.a31 = tmp;
        tmp = m.a23;
        m.a23 = m.a32;
        m.a32 = tmp;
    }
    
    public static FixedMatrix3x3_64F transpose(FixedMatrix3x3_64F input, final FixedMatrix3x3_64F output) {
        if (input == null) {
            input = new FixedMatrix3x3_64F();
        }
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        return output;
    }
    
    public static void mult(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a21 + a.a13 * b.a31;
        c.a12 = a.a11 * b.a12 + a.a12 * b.a22 + a.a13 * b.a32;
        c.a13 = a.a11 * b.a13 + a.a12 * b.a23 + a.a13 * b.a33;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a21 + a.a23 * b.a31;
        c.a22 = a.a21 * b.a12 + a.a22 * b.a22 + a.a23 * b.a32;
        c.a23 = a.a21 * b.a13 + a.a22 * b.a23 + a.a23 * b.a33;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a21 + a.a33 * b.a31;
        c.a32 = a.a31 * b.a12 + a.a32 * b.a22 + a.a33 * b.a32;
        c.a33 = a.a31 * b.a13 + a.a32 * b.a23 + a.a33 * b.a33;
    }
    
    public static void multTransA(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a21 + a.a31 * b.a31;
        c.a12 = a.a11 * b.a12 + a.a21 * b.a22 + a.a31 * b.a32;
        c.a13 = a.a11 * b.a13 + a.a21 * b.a23 + a.a31 * b.a33;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a21 + a.a32 * b.a31;
        c.a22 = a.a12 * b.a12 + a.a22 * b.a22 + a.a32 * b.a32;
        c.a23 = a.a12 * b.a13 + a.a22 * b.a23 + a.a32 * b.a33;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a21 + a.a33 * b.a31;
        c.a32 = a.a13 * b.a12 + a.a23 * b.a22 + a.a33 * b.a32;
        c.a33 = a.a13 * b.a13 + a.a23 * b.a23 + a.a33 * b.a33;
    }
    
    public static void multTransAB(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a12 + a.a31 * b.a13;
        c.a12 = a.a11 * b.a21 + a.a21 * b.a22 + a.a31 * b.a23;
        c.a13 = a.a11 * b.a31 + a.a21 * b.a32 + a.a31 * b.a33;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a12 + a.a32 * b.a13;
        c.a22 = a.a12 * b.a21 + a.a22 * b.a22 + a.a32 * b.a23;
        c.a23 = a.a12 * b.a31 + a.a22 * b.a32 + a.a32 * b.a33;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a12 + a.a33 * b.a13;
        c.a32 = a.a13 * b.a21 + a.a23 * b.a22 + a.a33 * b.a23;
        c.a33 = a.a13 * b.a31 + a.a23 * b.a32 + a.a33 * b.a33;
    }
    
    public static void multTransB(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a12 + a.a13 * b.a13;
        c.a12 = a.a11 * b.a21 + a.a12 * b.a22 + a.a13 * b.a23;
        c.a13 = a.a11 * b.a31 + a.a12 * b.a32 + a.a13 * b.a33;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a12 + a.a23 * b.a13;
        c.a22 = a.a21 * b.a21 + a.a22 * b.a22 + a.a23 * b.a23;
        c.a23 = a.a21 * b.a31 + a.a22 * b.a32 + a.a23 * b.a33;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a12 + a.a33 * b.a13;
        c.a32 = a.a31 * b.a21 + a.a32 * b.a22 + a.a33 * b.a23;
        c.a33 = a.a31 * b.a31 + a.a32 * b.a32 + a.a33 * b.a33;
    }
    
    public static void mult(final FixedMatrix3x3_64F a, final FixedMatrix3_64F b, final FixedMatrix3_64F c) {
        c.a1 = a.a11 * b.a1 + a.a12 * b.a2 + a.a13 * b.a3;
        c.a2 = a.a21 * b.a1 + a.a22 * b.a2 + a.a23 * b.a3;
        c.a3 = a.a31 * b.a1 + a.a32 * b.a2 + a.a33 * b.a3;
    }
    
    public static void mult(final FixedMatrix3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3_64F c) {
        c.a1 = a.a1 * b.a11 + a.a2 * b.a21 + a.a3 * b.a31;
        c.a2 = a.a1 * b.a12 + a.a2 * b.a22 + a.a3 * b.a32;
        c.a3 = a.a1 * b.a13 + a.a2 * b.a23 + a.a3 * b.a33;
    }
    
    public static double dot(final FixedMatrix3_64F a, final FixedMatrix3_64F b) {
        return a.a1 * b.a1 + a.a2 * b.a2 + a.a3 * b.a3;
    }
    
    public static void setIdentity(final FixedMatrix3x3_64F a) {
        a.a11 = 1.0;
        a.a21 = 0.0;
        a.a31 = 0.0;
        a.a12 = 0.0;
        a.a22 = 1.0;
        a.a32 = 0.0;
        a.a13 = 0.0;
        a.a23 = 0.0;
        a.a33 = 1.0;
    }
    
    public static boolean invert(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F inv) {
        final double scale = 1.0 / elementMaxAbs(a);
        final double a2 = a.a11 * scale;
        final double a3 = a.a12 * scale;
        final double a4 = a.a13 * scale;
        final double a5 = a.a21 * scale;
        final double a6 = a.a22 * scale;
        final double a7 = a.a23 * scale;
        final double a8 = a.a31 * scale;
        final double a9 = a.a32 * scale;
        final double a10 = a.a33 * scale;
        final double m11 = a6 * a10 - a7 * a9;
        final double m12 = -(a5 * a10 - a7 * a8);
        final double m13 = a5 * a9 - a6 * a8;
        final double m14 = -(a3 * a10 - a4 * a9);
        final double m15 = a2 * a10 - a4 * a8;
        final double m16 = -(a2 * a9 - a3 * a8);
        final double m17 = a3 * a7 - a4 * a6;
        final double m18 = -(a2 * a7 - a4 * a5);
        final double m19 = a2 * a6 - a3 * a5;
        final double det = (a2 * m11 + a3 * m12 + a4 * m13) / scale;
        inv.a11 = m11 / det;
        inv.a12 = m14 / det;
        inv.a13 = m17 / det;
        inv.a21 = m12 / det;
        inv.a22 = m15 / det;
        inv.a23 = m18 / det;
        inv.a31 = m13 / det;
        inv.a32 = m16 / det;
        inv.a33 = m19 / det;
        return !Double.isNaN(det) && !Double.isInfinite(det);
    }
    
    public static double det(final FixedMatrix3x3_64F mat) {
        final double a = mat.a11 * (mat.a22 * mat.a33 - mat.a23 * mat.a32);
        final double b = mat.a12 * (mat.a21 * mat.a33 - mat.a23 * mat.a31);
        final double c = mat.a13 * (mat.a21 * mat.a32 - mat.a31 * mat.a22);
        return a - b + c;
    }
    
    public static double trace(final FixedMatrix3x3_64F a) {
        return a.a11 + a.a21 + a.a31;
    }
    
    public static void diag(final FixedMatrix3x3_64F input, final FixedMatrix3_64F out) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
    }
    
    public static double elementMax(final FixedMatrix3x3_64F a) {
        double max = a.a11;
        max = Math.max(max, a.a12);
        max = Math.max(max, a.a13);
        max = Math.max(max, a.a21);
        max = Math.max(max, a.a22);
        max = Math.max(max, a.a23);
        max = Math.max(max, a.a31);
        max = Math.max(max, a.a32);
        max = Math.max(max, a.a33);
        return max;
    }
    
    public static double elementMaxAbs(final FixedMatrix3x3_64F a) {
        double max = a.a11;
        max = Math.max(max, Math.abs(a.a12));
        max = Math.max(max, Math.abs(a.a13));
        max = Math.max(max, Math.abs(a.a21));
        max = Math.max(max, Math.abs(a.a22));
        max = Math.max(max, Math.abs(a.a23));
        max = Math.max(max, Math.abs(a.a31));
        max = Math.max(max, Math.abs(a.a32));
        max = Math.max(max, Math.abs(a.a33));
        return max;
    }
    
    public static double elementMin(final FixedMatrix3x3_64F a) {
        double min = a.a11;
        min = Math.min(min, a.a12);
        min = Math.min(min, a.a13);
        min = Math.min(min, a.a21);
        min = Math.min(min, a.a22);
        min = Math.min(min, a.a23);
        min = Math.min(min, a.a31);
        min = Math.min(min, a.a32);
        min = Math.min(min, a.a33);
        return min;
    }
    
    public static double elementMinAbs(final FixedMatrix3x3_64F a) {
        double min = a.a11;
        min = Math.min(min, Math.abs(a.a12));
        min = Math.min(min, Math.abs(a.a13));
        min = Math.min(min, Math.abs(a.a21));
        min = Math.min(min, Math.abs(a.a22));
        min = Math.min(min, Math.abs(a.a23));
        min = Math.min(min, Math.abs(a.a31));
        min = Math.min(min, Math.abs(a.a32));
        min = Math.min(min, Math.abs(a.a33));
        return min;
    }
    
    public static void elementMult(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b) {
        a.a11 *= b.a11;
        a.a12 *= b.a12;
        a.a13 *= b.a13;
        a.a21 *= b.a21;
        a.a22 *= b.a22;
        a.a23 *= b.a23;
        a.a31 *= b.a31;
        a.a32 *= b.a32;
        a.a33 *= b.a33;
    }
    
    public static void elementMult(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 * b.a11;
        c.a12 = a.a12 * b.a12;
        c.a13 = a.a13 * b.a13;
        c.a21 = a.a21 * b.a21;
        c.a22 = a.a22 * b.a22;
        c.a23 = a.a23 * b.a23;
        c.a31 = a.a31 * b.a31;
        c.a32 = a.a32 * b.a32;
        c.a33 = a.a33 * b.a33;
    }
    
    public static void elementDiv(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b) {
        a.a11 /= b.a11;
        a.a12 /= b.a12;
        a.a13 /= b.a13;
        a.a21 /= b.a21;
        a.a22 /= b.a22;
        a.a23 /= b.a23;
        a.a31 /= b.a31;
        a.a32 /= b.a32;
        a.a33 /= b.a33;
    }
    
    public static void elementDiv(final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b, final FixedMatrix3x3_64F c) {
        c.a11 = a.a11 / b.a11;
        c.a12 = a.a12 / b.a12;
        c.a13 = a.a13 / b.a13;
        c.a21 = a.a21 / b.a21;
        c.a22 = a.a22 / b.a22;
        c.a23 = a.a23 / b.a23;
        c.a31 = a.a31 / b.a31;
        c.a32 = a.a32 / b.a32;
        c.a33 = a.a33 / b.a33;
    }
    
    public static void scale(final double alpha, final FixedMatrix3x3_64F a) {
        a.a11 *= alpha;
        a.a12 *= alpha;
        a.a13 *= alpha;
        a.a21 *= alpha;
        a.a22 *= alpha;
        a.a23 *= alpha;
        a.a31 *= alpha;
        a.a32 *= alpha;
        a.a33 *= alpha;
    }
    
    public static void scale(final double alpha, final FixedMatrix3x3_64F a, final FixedMatrix3x3_64F b) {
        b.a11 = a.a11 * alpha;
        b.a12 = a.a12 * alpha;
        b.a13 = a.a13 * alpha;
        b.a21 = a.a21 * alpha;
        b.a22 = a.a22 * alpha;
        b.a23 = a.a23 * alpha;
        b.a31 = a.a31 * alpha;
        b.a32 = a.a32 * alpha;
        b.a33 = a.a33 * alpha;
    }
    
    public static void divide(final FixedMatrix3x3_64F a, final double alpha) {
        a.a11 /= alpha;
        a.a12 /= alpha;
        a.a13 /= alpha;
        a.a21 /= alpha;
        a.a22 /= alpha;
        a.a23 /= alpha;
        a.a31 /= alpha;
        a.a32 /= alpha;
        a.a33 /= alpha;
    }
    
    public static void divide(final FixedMatrix3x3_64F a, final double alpha, final FixedMatrix3x3_64F b) {
        b.a11 = a.a11 / alpha;
        b.a12 = a.a12 / alpha;
        b.a13 = a.a13 / alpha;
        b.a21 = a.a21 / alpha;
        b.a22 = a.a22 / alpha;
        b.a23 = a.a23 / alpha;
        b.a31 = a.a31 / alpha;
        b.a32 = a.a32 / alpha;
        b.a33 = a.a33 / alpha;
    }
    
    public static void changeSign(final FixedMatrix3x3_64F a) {
        a.a11 = -a.a11;
        a.a12 = -a.a12;
        a.a13 = -a.a13;
        a.a21 = -a.a21;
        a.a22 = -a.a22;
        a.a23 = -a.a23;
        a.a31 = -a.a31;
        a.a32 = -a.a32;
        a.a33 = -a.a33;
    }
    
    public static void fill(final FixedMatrix3x3_64F a, final double v) {
        a.a11 = v;
        a.a12 = v;
        a.a13 = v;
        a.a21 = v;
        a.a22 = v;
        a.a23 = v;
        a.a31 = v;
        a.a32 = v;
        a.a33 = v;
    }
}
