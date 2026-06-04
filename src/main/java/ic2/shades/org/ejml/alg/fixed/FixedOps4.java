// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.fixed;

import ic2.shades.org.ejml.data.FixedMatrix4_64F;
import ic2.shades.org.ejml.data.FixedMatrix4x4_64F;

public class FixedOps4
{
    public static void add(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a14 = a.a14 + b.a14;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a24 = a.a24 + b.a24;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
        c.a34 = a.a34 + b.a34;
        c.a41 = a.a41 + b.a41;
        c.a42 = a.a42 + b.a42;
        c.a43 = a.a43 + b.a43;
        c.a44 = a.a44 + b.a44;
    }
    
    public static void addEquals(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a14 += b.a14;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a24 += b.a24;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
        a.a34 += b.a34;
        a.a41 += b.a41;
        a.a42 += b.a42;
        a.a43 += b.a43;
        a.a44 += b.a44;
    }
    
    public static void transpose(final FixedMatrix4x4_64F m) {
        double tmp = m.a12;
        m.a12 = m.a21;
        m.a21 = tmp;
        tmp = m.a13;
        m.a13 = m.a31;
        m.a31 = tmp;
        tmp = m.a14;
        m.a14 = m.a41;
        m.a41 = tmp;
        tmp = m.a23;
        m.a23 = m.a32;
        m.a32 = tmp;
        tmp = m.a24;
        m.a24 = m.a42;
        m.a42 = tmp;
        tmp = m.a34;
        m.a34 = m.a43;
        m.a43 = tmp;
    }
    
    public static FixedMatrix4x4_64F transpose(FixedMatrix4x4_64F input, final FixedMatrix4x4_64F output) {
        if (input == null) {
            input = new FixedMatrix4x4_64F();
        }
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a14 = input.a41;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a24 = input.a42;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        output.a34 = input.a43;
        output.a41 = input.a14;
        output.a42 = input.a24;
        output.a43 = input.a34;
        output.a44 = input.a44;
        return output;
    }
    
    public static void mult(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a21 + a.a13 * b.a31 + a.a14 * b.a41;
        c.a12 = a.a11 * b.a12 + a.a12 * b.a22 + a.a13 * b.a32 + a.a14 * b.a42;
        c.a13 = a.a11 * b.a13 + a.a12 * b.a23 + a.a13 * b.a33 + a.a14 * b.a43;
        c.a14 = a.a11 * b.a14 + a.a12 * b.a24 + a.a13 * b.a34 + a.a14 * b.a44;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a21 + a.a23 * b.a31 + a.a24 * b.a41;
        c.a22 = a.a21 * b.a12 + a.a22 * b.a22 + a.a23 * b.a32 + a.a24 * b.a42;
        c.a23 = a.a21 * b.a13 + a.a22 * b.a23 + a.a23 * b.a33 + a.a24 * b.a43;
        c.a24 = a.a21 * b.a14 + a.a22 * b.a24 + a.a23 * b.a34 + a.a24 * b.a44;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a21 + a.a33 * b.a31 + a.a34 * b.a41;
        c.a32 = a.a31 * b.a12 + a.a32 * b.a22 + a.a33 * b.a32 + a.a34 * b.a42;
        c.a33 = a.a31 * b.a13 + a.a32 * b.a23 + a.a33 * b.a33 + a.a34 * b.a43;
        c.a34 = a.a31 * b.a14 + a.a32 * b.a24 + a.a33 * b.a34 + a.a34 * b.a44;
        c.a41 = a.a41 * b.a11 + a.a42 * b.a21 + a.a43 * b.a31 + a.a44 * b.a41;
        c.a42 = a.a41 * b.a12 + a.a42 * b.a22 + a.a43 * b.a32 + a.a44 * b.a42;
        c.a43 = a.a41 * b.a13 + a.a42 * b.a23 + a.a43 * b.a33 + a.a44 * b.a43;
        c.a44 = a.a41 * b.a14 + a.a42 * b.a24 + a.a43 * b.a34 + a.a44 * b.a44;
    }
    
    public static void multTransA(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a21 + a.a31 * b.a31 + a.a41 * b.a41;
        c.a12 = a.a11 * b.a12 + a.a21 * b.a22 + a.a31 * b.a32 + a.a41 * b.a42;
        c.a13 = a.a11 * b.a13 + a.a21 * b.a23 + a.a31 * b.a33 + a.a41 * b.a43;
        c.a14 = a.a11 * b.a14 + a.a21 * b.a24 + a.a31 * b.a34 + a.a41 * b.a44;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a21 + a.a32 * b.a31 + a.a42 * b.a41;
        c.a22 = a.a12 * b.a12 + a.a22 * b.a22 + a.a32 * b.a32 + a.a42 * b.a42;
        c.a23 = a.a12 * b.a13 + a.a22 * b.a23 + a.a32 * b.a33 + a.a42 * b.a43;
        c.a24 = a.a12 * b.a14 + a.a22 * b.a24 + a.a32 * b.a34 + a.a42 * b.a44;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a21 + a.a33 * b.a31 + a.a43 * b.a41;
        c.a32 = a.a13 * b.a12 + a.a23 * b.a22 + a.a33 * b.a32 + a.a43 * b.a42;
        c.a33 = a.a13 * b.a13 + a.a23 * b.a23 + a.a33 * b.a33 + a.a43 * b.a43;
        c.a34 = a.a13 * b.a14 + a.a23 * b.a24 + a.a33 * b.a34 + a.a43 * b.a44;
        c.a41 = a.a14 * b.a11 + a.a24 * b.a21 + a.a34 * b.a31 + a.a44 * b.a41;
        c.a42 = a.a14 * b.a12 + a.a24 * b.a22 + a.a34 * b.a32 + a.a44 * b.a42;
        c.a43 = a.a14 * b.a13 + a.a24 * b.a23 + a.a34 * b.a33 + a.a44 * b.a43;
        c.a44 = a.a14 * b.a14 + a.a24 * b.a24 + a.a34 * b.a34 + a.a44 * b.a44;
    }
    
    public static void multTransAB(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a12 + a.a31 * b.a13 + a.a41 * b.a14;
        c.a12 = a.a11 * b.a21 + a.a21 * b.a22 + a.a31 * b.a23 + a.a41 * b.a24;
        c.a13 = a.a11 * b.a31 + a.a21 * b.a32 + a.a31 * b.a33 + a.a41 * b.a34;
        c.a14 = a.a11 * b.a41 + a.a21 * b.a42 + a.a31 * b.a43 + a.a41 * b.a44;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a12 + a.a32 * b.a13 + a.a42 * b.a14;
        c.a22 = a.a12 * b.a21 + a.a22 * b.a22 + a.a32 * b.a23 + a.a42 * b.a24;
        c.a23 = a.a12 * b.a31 + a.a22 * b.a32 + a.a32 * b.a33 + a.a42 * b.a34;
        c.a24 = a.a12 * b.a41 + a.a22 * b.a42 + a.a32 * b.a43 + a.a42 * b.a44;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a12 + a.a33 * b.a13 + a.a43 * b.a14;
        c.a32 = a.a13 * b.a21 + a.a23 * b.a22 + a.a33 * b.a23 + a.a43 * b.a24;
        c.a33 = a.a13 * b.a31 + a.a23 * b.a32 + a.a33 * b.a33 + a.a43 * b.a34;
        c.a34 = a.a13 * b.a41 + a.a23 * b.a42 + a.a33 * b.a43 + a.a43 * b.a44;
        c.a41 = a.a14 * b.a11 + a.a24 * b.a12 + a.a34 * b.a13 + a.a44 * b.a14;
        c.a42 = a.a14 * b.a21 + a.a24 * b.a22 + a.a34 * b.a23 + a.a44 * b.a24;
        c.a43 = a.a14 * b.a31 + a.a24 * b.a32 + a.a34 * b.a33 + a.a44 * b.a34;
        c.a44 = a.a14 * b.a41 + a.a24 * b.a42 + a.a34 * b.a43 + a.a44 * b.a44;
    }
    
    public static void multTransB(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a12 + a.a13 * b.a13 + a.a14 * b.a14;
        c.a12 = a.a11 * b.a21 + a.a12 * b.a22 + a.a13 * b.a23 + a.a14 * b.a24;
        c.a13 = a.a11 * b.a31 + a.a12 * b.a32 + a.a13 * b.a33 + a.a14 * b.a34;
        c.a14 = a.a11 * b.a41 + a.a12 * b.a42 + a.a13 * b.a43 + a.a14 * b.a44;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a12 + a.a23 * b.a13 + a.a24 * b.a14;
        c.a22 = a.a21 * b.a21 + a.a22 * b.a22 + a.a23 * b.a23 + a.a24 * b.a24;
        c.a23 = a.a21 * b.a31 + a.a22 * b.a32 + a.a23 * b.a33 + a.a24 * b.a34;
        c.a24 = a.a21 * b.a41 + a.a22 * b.a42 + a.a23 * b.a43 + a.a24 * b.a44;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a12 + a.a33 * b.a13 + a.a34 * b.a14;
        c.a32 = a.a31 * b.a21 + a.a32 * b.a22 + a.a33 * b.a23 + a.a34 * b.a24;
        c.a33 = a.a31 * b.a31 + a.a32 * b.a32 + a.a33 * b.a33 + a.a34 * b.a34;
        c.a34 = a.a31 * b.a41 + a.a32 * b.a42 + a.a33 * b.a43 + a.a34 * b.a44;
        c.a41 = a.a41 * b.a11 + a.a42 * b.a12 + a.a43 * b.a13 + a.a44 * b.a14;
        c.a42 = a.a41 * b.a21 + a.a42 * b.a22 + a.a43 * b.a23 + a.a44 * b.a24;
        c.a43 = a.a41 * b.a31 + a.a42 * b.a32 + a.a43 * b.a33 + a.a44 * b.a34;
        c.a44 = a.a41 * b.a41 + a.a42 * b.a42 + a.a43 * b.a43 + a.a44 * b.a44;
    }
    
    public static void mult(final FixedMatrix4x4_64F a, final FixedMatrix4_64F b, final FixedMatrix4_64F c) {
        c.a1 = a.a11 * b.a1 + a.a12 * b.a2 + a.a13 * b.a3 + a.a14 * b.a4;
        c.a2 = a.a21 * b.a1 + a.a22 * b.a2 + a.a23 * b.a3 + a.a24 * b.a4;
        c.a3 = a.a31 * b.a1 + a.a32 * b.a2 + a.a33 * b.a3 + a.a34 * b.a4;
        c.a4 = a.a41 * b.a1 + a.a42 * b.a2 + a.a43 * b.a3 + a.a44 * b.a4;
    }
    
    public static void mult(final FixedMatrix4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4_64F c) {
        c.a1 = a.a1 * b.a11 + a.a2 * b.a21 + a.a3 * b.a31 + a.a4 * b.a41;
        c.a2 = a.a1 * b.a12 + a.a2 * b.a22 + a.a3 * b.a32 + a.a4 * b.a42;
        c.a3 = a.a1 * b.a13 + a.a2 * b.a23 + a.a3 * b.a33 + a.a4 * b.a43;
        c.a4 = a.a1 * b.a14 + a.a2 * b.a24 + a.a3 * b.a34 + a.a4 * b.a44;
    }
    
    public static double dot(final FixedMatrix4_64F a, final FixedMatrix4_64F b) {
        return a.a1 * b.a1 + a.a2 * b.a2 + a.a3 * b.a3 + a.a4 * b.a4;
    }
    
    public static void setIdentity(final FixedMatrix4x4_64F a) {
        a.a11 = 1.0;
        a.a21 = 0.0;
        a.a31 = 0.0;
        a.a41 = 0.0;
        a.a12 = 0.0;
        a.a22 = 1.0;
        a.a32 = 0.0;
        a.a42 = 0.0;
        a.a13 = 0.0;
        a.a23 = 0.0;
        a.a33 = 1.0;
        a.a43 = 0.0;
        a.a14 = 0.0;
        a.a24 = 0.0;
        a.a34 = 0.0;
        a.a44 = 1.0;
    }
    
    public static boolean invert(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F inv) {
        final double scale = 1.0 / elementMaxAbs(a);
        final double a2 = a.a11 * scale;
        final double a3 = a.a12 * scale;
        final double a4 = a.a13 * scale;
        final double a5 = a.a14 * scale;
        final double a6 = a.a21 * scale;
        final double a7 = a.a22 * scale;
        final double a8 = a.a23 * scale;
        final double a9 = a.a24 * scale;
        final double a10 = a.a31 * scale;
        final double a11 = a.a32 * scale;
        final double a12 = a.a33 * scale;
        final double a13 = a.a34 * scale;
        final double a14 = a.a41 * scale;
        final double a15 = a.a42 * scale;
        final double a16 = a.a43 * scale;
        final double a17 = a.a44 * scale;
        final double m11 = a7 * (a12 * a17 - a13 * a16) - a8 * (a11 * a17 - a13 * a15) + a9 * (a11 * a16 - a12 * a15);
        final double m12 = -(a6 * (a12 * a17 - a13 * a16) - a8 * (a10 * a17 - a13 * a14) + a9 * (a10 * a16 - a12 * a14));
        final double m13 = a6 * (a11 * a17 - a13 * a15) - a7 * (a10 * a17 - a13 * a14) + a9 * (a10 * a15 - a11 * a14);
        final double m14 = -(a6 * (a11 * a16 - a12 * a15) - a7 * (a10 * a16 - a12 * a14) + a8 * (a10 * a15 - a11 * a14));
        final double m15 = -(a3 * (a12 * a17 - a13 * a16) - a4 * (a11 * a17 - a13 * a15) + a5 * (a11 * a16 - a12 * a15));
        final double m16 = a2 * (a12 * a17 - a13 * a16) - a4 * (a10 * a17 - a13 * a14) + a5 * (a10 * a16 - a12 * a14);
        final double m17 = -(a2 * (a11 * a17 - a13 * a15) - a3 * (a10 * a17 - a13 * a14) + a5 * (a10 * a15 - a11 * a14));
        final double m18 = a2 * (a11 * a16 - a12 * a15) - a3 * (a10 * a16 - a12 * a14) + a4 * (a10 * a15 - a11 * a14);
        final double m19 = a3 * (a8 * a17 - a9 * a16) - a4 * (a7 * a17 - a9 * a15) + a5 * (a7 * a16 - a8 * a15);
        final double m20 = -(a2 * (a8 * a17 - a9 * a16) - a4 * (a6 * a17 - a9 * a14) + a5 * (a6 * a16 - a8 * a14));
        final double m21 = a2 * (a7 * a17 - a9 * a15) - a3 * (a6 * a17 - a9 * a14) + a5 * (a6 * a15 - a7 * a14);
        final double m22 = -(a2 * (a7 * a16 - a8 * a15) - a3 * (a6 * a16 - a8 * a14) + a4 * (a6 * a15 - a7 * a14));
        final double m23 = -(a3 * (a8 * a13 - a9 * a12) - a4 * (a7 * a13 - a9 * a11) + a5 * (a7 * a12 - a8 * a11));
        final double m24 = a2 * (a8 * a13 - a9 * a12) - a4 * (a6 * a13 - a9 * a10) + a5 * (a6 * a12 - a8 * a10);
        final double m25 = -(a2 * (a7 * a13 - a9 * a11) - a3 * (a6 * a13 - a9 * a10) + a5 * (a6 * a11 - a7 * a10));
        final double m26 = a2 * (a7 * a12 - a8 * a11) - a3 * (a6 * a12 - a8 * a10) + a4 * (a6 * a11 - a7 * a10);
        final double det = (a2 * m11 + a3 * m12 + a4 * m13 + a5 * m14) / scale;
        inv.a11 = m11 / det;
        inv.a12 = m15 / det;
        inv.a13 = m19 / det;
        inv.a14 = m23 / det;
        inv.a21 = m12 / det;
        inv.a22 = m16 / det;
        inv.a23 = m20 / det;
        inv.a24 = m24 / det;
        inv.a31 = m13 / det;
        inv.a32 = m17 / det;
        inv.a33 = m21 / det;
        inv.a34 = m25 / det;
        inv.a41 = m14 / det;
        inv.a42 = m18 / det;
        inv.a43 = m22 / det;
        inv.a44 = m26 / det;
        return !Double.isNaN(det) && !Double.isInfinite(det);
    }
    
    public static double det(final FixedMatrix4x4_64F mat) {
        double a11 = mat.a22;
        double a12 = mat.a23;
        double a13 = mat.a24;
        double a14 = mat.a32;
        double a15 = mat.a33;
        double a16 = mat.a34;
        double a17 = mat.a42;
        double a18 = mat.a43;
        double a19 = mat.a44;
        double ret = 0.0;
        ret += mat.a11 * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a11 = mat.a21;
        a14 = mat.a31;
        a17 = mat.a41;
        ret -= mat.a12 * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a12 = mat.a22;
        a15 = mat.a32;
        a18 = mat.a42;
        ret += mat.a13 * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a13 = mat.a23;
        a16 = mat.a33;
        a19 = mat.a43;
        ret -= mat.a14 * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        return ret;
    }
    
    public static double trace(final FixedMatrix4x4_64F a) {
        return a.a11 + a.a21 + a.a31 + a.a41;
    }
    
    public static void diag(final FixedMatrix4x4_64F input, final FixedMatrix4_64F out) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
        out.a4 = input.a44;
    }
    
    public static double elementMax(final FixedMatrix4x4_64F a) {
        double max = a.a11;
        max = Math.max(max, a.a12);
        max = Math.max(max, a.a13);
        max = Math.max(max, a.a14);
        max = Math.max(max, a.a21);
        max = Math.max(max, a.a22);
        max = Math.max(max, a.a23);
        max = Math.max(max, a.a24);
        max = Math.max(max, a.a31);
        max = Math.max(max, a.a32);
        max = Math.max(max, a.a33);
        max = Math.max(max, a.a34);
        max = Math.max(max, a.a41);
        max = Math.max(max, a.a42);
        max = Math.max(max, a.a43);
        max = Math.max(max, a.a44);
        return max;
    }
    
    public static double elementMaxAbs(final FixedMatrix4x4_64F a) {
        double max = a.a11;
        max = Math.max(max, Math.abs(a.a12));
        max = Math.max(max, Math.abs(a.a13));
        max = Math.max(max, Math.abs(a.a14));
        max = Math.max(max, Math.abs(a.a21));
        max = Math.max(max, Math.abs(a.a22));
        max = Math.max(max, Math.abs(a.a23));
        max = Math.max(max, Math.abs(a.a24));
        max = Math.max(max, Math.abs(a.a31));
        max = Math.max(max, Math.abs(a.a32));
        max = Math.max(max, Math.abs(a.a33));
        max = Math.max(max, Math.abs(a.a34));
        max = Math.max(max, Math.abs(a.a41));
        max = Math.max(max, Math.abs(a.a42));
        max = Math.max(max, Math.abs(a.a43));
        max = Math.max(max, Math.abs(a.a44));
        return max;
    }
    
    public static double elementMin(final FixedMatrix4x4_64F a) {
        double min = a.a11;
        min = Math.min(min, a.a12);
        min = Math.min(min, a.a13);
        min = Math.min(min, a.a14);
        min = Math.min(min, a.a21);
        min = Math.min(min, a.a22);
        min = Math.min(min, a.a23);
        min = Math.min(min, a.a24);
        min = Math.min(min, a.a31);
        min = Math.min(min, a.a32);
        min = Math.min(min, a.a33);
        min = Math.min(min, a.a34);
        min = Math.min(min, a.a41);
        min = Math.min(min, a.a42);
        min = Math.min(min, a.a43);
        min = Math.min(min, a.a44);
        return min;
    }
    
    public static double elementMinAbs(final FixedMatrix4x4_64F a) {
        double min = a.a11;
        min = Math.min(min, Math.abs(a.a12));
        min = Math.min(min, Math.abs(a.a13));
        min = Math.min(min, Math.abs(a.a14));
        min = Math.min(min, Math.abs(a.a21));
        min = Math.min(min, Math.abs(a.a22));
        min = Math.min(min, Math.abs(a.a23));
        min = Math.min(min, Math.abs(a.a24));
        min = Math.min(min, Math.abs(a.a31));
        min = Math.min(min, Math.abs(a.a32));
        min = Math.min(min, Math.abs(a.a33));
        min = Math.min(min, Math.abs(a.a34));
        min = Math.min(min, Math.abs(a.a41));
        min = Math.min(min, Math.abs(a.a42));
        min = Math.min(min, Math.abs(a.a43));
        min = Math.min(min, Math.abs(a.a44));
        return min;
    }
    
    public static void elementMult(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b) {
        a.a11 *= b.a11;
        a.a12 *= b.a12;
        a.a13 *= b.a13;
        a.a14 *= b.a14;
        a.a21 *= b.a21;
        a.a22 *= b.a22;
        a.a23 *= b.a23;
        a.a24 *= b.a24;
        a.a31 *= b.a31;
        a.a32 *= b.a32;
        a.a33 *= b.a33;
        a.a34 *= b.a34;
        a.a41 *= b.a41;
        a.a42 *= b.a42;
        a.a43 *= b.a43;
        a.a44 *= b.a44;
    }
    
    public static void elementMult(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 * b.a11;
        c.a12 = a.a12 * b.a12;
        c.a13 = a.a13 * b.a13;
        c.a14 = a.a14 * b.a14;
        c.a21 = a.a21 * b.a21;
        c.a22 = a.a22 * b.a22;
        c.a23 = a.a23 * b.a23;
        c.a24 = a.a24 * b.a24;
        c.a31 = a.a31 * b.a31;
        c.a32 = a.a32 * b.a32;
        c.a33 = a.a33 * b.a33;
        c.a34 = a.a34 * b.a34;
        c.a41 = a.a41 * b.a41;
        c.a42 = a.a42 * b.a42;
        c.a43 = a.a43 * b.a43;
        c.a44 = a.a44 * b.a44;
    }
    
    public static void elementDiv(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b) {
        a.a11 /= b.a11;
        a.a12 /= b.a12;
        a.a13 /= b.a13;
        a.a14 /= b.a14;
        a.a21 /= b.a21;
        a.a22 /= b.a22;
        a.a23 /= b.a23;
        a.a24 /= b.a24;
        a.a31 /= b.a31;
        a.a32 /= b.a32;
        a.a33 /= b.a33;
        a.a34 /= b.a34;
        a.a41 /= b.a41;
        a.a42 /= b.a42;
        a.a43 /= b.a43;
        a.a44 /= b.a44;
    }
    
    public static void elementDiv(final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b, final FixedMatrix4x4_64F c) {
        c.a11 = a.a11 / b.a11;
        c.a12 = a.a12 / b.a12;
        c.a13 = a.a13 / b.a13;
        c.a14 = a.a14 / b.a14;
        c.a21 = a.a21 / b.a21;
        c.a22 = a.a22 / b.a22;
        c.a23 = a.a23 / b.a23;
        c.a24 = a.a24 / b.a24;
        c.a31 = a.a31 / b.a31;
        c.a32 = a.a32 / b.a32;
        c.a33 = a.a33 / b.a33;
        c.a34 = a.a34 / b.a34;
        c.a41 = a.a41 / b.a41;
        c.a42 = a.a42 / b.a42;
        c.a43 = a.a43 / b.a43;
        c.a44 = a.a44 / b.a44;
    }
    
    public static void scale(final double alpha, final FixedMatrix4x4_64F a) {
        a.a11 *= alpha;
        a.a12 *= alpha;
        a.a13 *= alpha;
        a.a14 *= alpha;
        a.a21 *= alpha;
        a.a22 *= alpha;
        a.a23 *= alpha;
        a.a24 *= alpha;
        a.a31 *= alpha;
        a.a32 *= alpha;
        a.a33 *= alpha;
        a.a34 *= alpha;
        a.a41 *= alpha;
        a.a42 *= alpha;
        a.a43 *= alpha;
        a.a44 *= alpha;
    }
    
    public static void scale(final double alpha, final FixedMatrix4x4_64F a, final FixedMatrix4x4_64F b) {
        b.a11 = a.a11 * alpha;
        b.a12 = a.a12 * alpha;
        b.a13 = a.a13 * alpha;
        b.a14 = a.a14 * alpha;
        b.a21 = a.a21 * alpha;
        b.a22 = a.a22 * alpha;
        b.a23 = a.a23 * alpha;
        b.a24 = a.a24 * alpha;
        b.a31 = a.a31 * alpha;
        b.a32 = a.a32 * alpha;
        b.a33 = a.a33 * alpha;
        b.a34 = a.a34 * alpha;
        b.a41 = a.a41 * alpha;
        b.a42 = a.a42 * alpha;
        b.a43 = a.a43 * alpha;
        b.a44 = a.a44 * alpha;
    }
    
    public static void divide(final FixedMatrix4x4_64F a, final double alpha) {
        a.a11 /= alpha;
        a.a12 /= alpha;
        a.a13 /= alpha;
        a.a14 /= alpha;
        a.a21 /= alpha;
        a.a22 /= alpha;
        a.a23 /= alpha;
        a.a24 /= alpha;
        a.a31 /= alpha;
        a.a32 /= alpha;
        a.a33 /= alpha;
        a.a34 /= alpha;
        a.a41 /= alpha;
        a.a42 /= alpha;
        a.a43 /= alpha;
        a.a44 /= alpha;
    }
    
    public static void divide(final FixedMatrix4x4_64F a, final double alpha, final FixedMatrix4x4_64F b) {
        b.a11 = a.a11 / alpha;
        b.a12 = a.a12 / alpha;
        b.a13 = a.a13 / alpha;
        b.a14 = a.a14 / alpha;
        b.a21 = a.a21 / alpha;
        b.a22 = a.a22 / alpha;
        b.a23 = a.a23 / alpha;
        b.a24 = a.a24 / alpha;
        b.a31 = a.a31 / alpha;
        b.a32 = a.a32 / alpha;
        b.a33 = a.a33 / alpha;
        b.a34 = a.a34 / alpha;
        b.a41 = a.a41 / alpha;
        b.a42 = a.a42 / alpha;
        b.a43 = a.a43 / alpha;
        b.a44 = a.a44 / alpha;
    }
    
    public static void changeSign(final FixedMatrix4x4_64F a) {
        a.a11 = -a.a11;
        a.a12 = -a.a12;
        a.a13 = -a.a13;
        a.a14 = -a.a14;
        a.a21 = -a.a21;
        a.a22 = -a.a22;
        a.a23 = -a.a23;
        a.a24 = -a.a24;
        a.a31 = -a.a31;
        a.a32 = -a.a32;
        a.a33 = -a.a33;
        a.a34 = -a.a34;
        a.a41 = -a.a41;
        a.a42 = -a.a42;
        a.a43 = -a.a43;
        a.a44 = -a.a44;
    }
    
    public static void fill(final FixedMatrix4x4_64F a, final double v) {
        a.a11 = v;
        a.a12 = v;
        a.a13 = v;
        a.a14 = v;
        a.a21 = v;
        a.a22 = v;
        a.a23 = v;
        a.a24 = v;
        a.a31 = v;
        a.a32 = v;
        a.a33 = v;
        a.a34 = v;
        a.a41 = v;
        a.a42 = v;
        a.a43 = v;
        a.a44 = v;
    }
}
