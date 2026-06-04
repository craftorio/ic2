// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.fixed;

import ic2.shades.org.ejml.data.FixedMatrix5_64F;
import ic2.shades.org.ejml.data.FixedMatrix5x5_64F;

public class FixedOps5
{
    public static void add(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a14 = a.a14 + b.a14;
        c.a15 = a.a15 + b.a15;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a24 = a.a24 + b.a24;
        c.a25 = a.a25 + b.a25;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
        c.a34 = a.a34 + b.a34;
        c.a35 = a.a35 + b.a35;
        c.a41 = a.a41 + b.a41;
        c.a42 = a.a42 + b.a42;
        c.a43 = a.a43 + b.a43;
        c.a44 = a.a44 + b.a44;
        c.a45 = a.a45 + b.a45;
        c.a51 = a.a51 + b.a51;
        c.a52 = a.a52 + b.a52;
        c.a53 = a.a53 + b.a53;
        c.a54 = a.a54 + b.a54;
        c.a55 = a.a55 + b.a55;
    }
    
    public static void addEquals(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a14 += b.a14;
        a.a15 += b.a15;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a24 += b.a24;
        a.a25 += b.a25;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
        a.a34 += b.a34;
        a.a35 += b.a35;
        a.a41 += b.a41;
        a.a42 += b.a42;
        a.a43 += b.a43;
        a.a44 += b.a44;
        a.a45 += b.a45;
        a.a51 += b.a51;
        a.a52 += b.a52;
        a.a53 += b.a53;
        a.a54 += b.a54;
        a.a55 += b.a55;
    }
    
    public static void transpose(final FixedMatrix5x5_64F m) {
        double tmp = m.a12;
        m.a12 = m.a21;
        m.a21 = tmp;
        tmp = m.a13;
        m.a13 = m.a31;
        m.a31 = tmp;
        tmp = m.a14;
        m.a14 = m.a41;
        m.a41 = tmp;
        tmp = m.a15;
        m.a15 = m.a51;
        m.a51 = tmp;
        tmp = m.a23;
        m.a23 = m.a32;
        m.a32 = tmp;
        tmp = m.a24;
        m.a24 = m.a42;
        m.a42 = tmp;
        tmp = m.a25;
        m.a25 = m.a52;
        m.a52 = tmp;
        tmp = m.a34;
        m.a34 = m.a43;
        m.a43 = tmp;
        tmp = m.a35;
        m.a35 = m.a53;
        m.a53 = tmp;
        tmp = m.a45;
        m.a45 = m.a54;
        m.a54 = tmp;
    }
    
    public static FixedMatrix5x5_64F transpose(FixedMatrix5x5_64F input, final FixedMatrix5x5_64F output) {
        if (input == null) {
            input = new FixedMatrix5x5_64F();
        }
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a14 = input.a41;
        output.a15 = input.a51;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a24 = input.a42;
        output.a25 = input.a52;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        output.a34 = input.a43;
        output.a35 = input.a53;
        output.a41 = input.a14;
        output.a42 = input.a24;
        output.a43 = input.a34;
        output.a44 = input.a44;
        output.a45 = input.a54;
        output.a51 = input.a15;
        output.a52 = input.a25;
        output.a53 = input.a35;
        output.a54 = input.a45;
        output.a55 = input.a55;
        return output;
    }
    
    public static void mult(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a21 + a.a13 * b.a31 + a.a14 * b.a41 + a.a15 * b.a51;
        c.a12 = a.a11 * b.a12 + a.a12 * b.a22 + a.a13 * b.a32 + a.a14 * b.a42 + a.a15 * b.a52;
        c.a13 = a.a11 * b.a13 + a.a12 * b.a23 + a.a13 * b.a33 + a.a14 * b.a43 + a.a15 * b.a53;
        c.a14 = a.a11 * b.a14 + a.a12 * b.a24 + a.a13 * b.a34 + a.a14 * b.a44 + a.a15 * b.a54;
        c.a15 = a.a11 * b.a15 + a.a12 * b.a25 + a.a13 * b.a35 + a.a14 * b.a45 + a.a15 * b.a55;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a21 + a.a23 * b.a31 + a.a24 * b.a41 + a.a25 * b.a51;
        c.a22 = a.a21 * b.a12 + a.a22 * b.a22 + a.a23 * b.a32 + a.a24 * b.a42 + a.a25 * b.a52;
        c.a23 = a.a21 * b.a13 + a.a22 * b.a23 + a.a23 * b.a33 + a.a24 * b.a43 + a.a25 * b.a53;
        c.a24 = a.a21 * b.a14 + a.a22 * b.a24 + a.a23 * b.a34 + a.a24 * b.a44 + a.a25 * b.a54;
        c.a25 = a.a21 * b.a15 + a.a22 * b.a25 + a.a23 * b.a35 + a.a24 * b.a45 + a.a25 * b.a55;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a21 + a.a33 * b.a31 + a.a34 * b.a41 + a.a35 * b.a51;
        c.a32 = a.a31 * b.a12 + a.a32 * b.a22 + a.a33 * b.a32 + a.a34 * b.a42 + a.a35 * b.a52;
        c.a33 = a.a31 * b.a13 + a.a32 * b.a23 + a.a33 * b.a33 + a.a34 * b.a43 + a.a35 * b.a53;
        c.a34 = a.a31 * b.a14 + a.a32 * b.a24 + a.a33 * b.a34 + a.a34 * b.a44 + a.a35 * b.a54;
        c.a35 = a.a31 * b.a15 + a.a32 * b.a25 + a.a33 * b.a35 + a.a34 * b.a45 + a.a35 * b.a55;
        c.a41 = a.a41 * b.a11 + a.a42 * b.a21 + a.a43 * b.a31 + a.a44 * b.a41 + a.a45 * b.a51;
        c.a42 = a.a41 * b.a12 + a.a42 * b.a22 + a.a43 * b.a32 + a.a44 * b.a42 + a.a45 * b.a52;
        c.a43 = a.a41 * b.a13 + a.a42 * b.a23 + a.a43 * b.a33 + a.a44 * b.a43 + a.a45 * b.a53;
        c.a44 = a.a41 * b.a14 + a.a42 * b.a24 + a.a43 * b.a34 + a.a44 * b.a44 + a.a45 * b.a54;
        c.a45 = a.a41 * b.a15 + a.a42 * b.a25 + a.a43 * b.a35 + a.a44 * b.a45 + a.a45 * b.a55;
        c.a51 = a.a51 * b.a11 + a.a52 * b.a21 + a.a53 * b.a31 + a.a54 * b.a41 + a.a55 * b.a51;
        c.a52 = a.a51 * b.a12 + a.a52 * b.a22 + a.a53 * b.a32 + a.a54 * b.a42 + a.a55 * b.a52;
        c.a53 = a.a51 * b.a13 + a.a52 * b.a23 + a.a53 * b.a33 + a.a54 * b.a43 + a.a55 * b.a53;
        c.a54 = a.a51 * b.a14 + a.a52 * b.a24 + a.a53 * b.a34 + a.a54 * b.a44 + a.a55 * b.a54;
        c.a55 = a.a51 * b.a15 + a.a52 * b.a25 + a.a53 * b.a35 + a.a54 * b.a45 + a.a55 * b.a55;
    }
    
    public static void multTransA(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a21 + a.a31 * b.a31 + a.a41 * b.a41 + a.a51 * b.a51;
        c.a12 = a.a11 * b.a12 + a.a21 * b.a22 + a.a31 * b.a32 + a.a41 * b.a42 + a.a51 * b.a52;
        c.a13 = a.a11 * b.a13 + a.a21 * b.a23 + a.a31 * b.a33 + a.a41 * b.a43 + a.a51 * b.a53;
        c.a14 = a.a11 * b.a14 + a.a21 * b.a24 + a.a31 * b.a34 + a.a41 * b.a44 + a.a51 * b.a54;
        c.a15 = a.a11 * b.a15 + a.a21 * b.a25 + a.a31 * b.a35 + a.a41 * b.a45 + a.a51 * b.a55;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a21 + a.a32 * b.a31 + a.a42 * b.a41 + a.a52 * b.a51;
        c.a22 = a.a12 * b.a12 + a.a22 * b.a22 + a.a32 * b.a32 + a.a42 * b.a42 + a.a52 * b.a52;
        c.a23 = a.a12 * b.a13 + a.a22 * b.a23 + a.a32 * b.a33 + a.a42 * b.a43 + a.a52 * b.a53;
        c.a24 = a.a12 * b.a14 + a.a22 * b.a24 + a.a32 * b.a34 + a.a42 * b.a44 + a.a52 * b.a54;
        c.a25 = a.a12 * b.a15 + a.a22 * b.a25 + a.a32 * b.a35 + a.a42 * b.a45 + a.a52 * b.a55;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a21 + a.a33 * b.a31 + a.a43 * b.a41 + a.a53 * b.a51;
        c.a32 = a.a13 * b.a12 + a.a23 * b.a22 + a.a33 * b.a32 + a.a43 * b.a42 + a.a53 * b.a52;
        c.a33 = a.a13 * b.a13 + a.a23 * b.a23 + a.a33 * b.a33 + a.a43 * b.a43 + a.a53 * b.a53;
        c.a34 = a.a13 * b.a14 + a.a23 * b.a24 + a.a33 * b.a34 + a.a43 * b.a44 + a.a53 * b.a54;
        c.a35 = a.a13 * b.a15 + a.a23 * b.a25 + a.a33 * b.a35 + a.a43 * b.a45 + a.a53 * b.a55;
        c.a41 = a.a14 * b.a11 + a.a24 * b.a21 + a.a34 * b.a31 + a.a44 * b.a41 + a.a54 * b.a51;
        c.a42 = a.a14 * b.a12 + a.a24 * b.a22 + a.a34 * b.a32 + a.a44 * b.a42 + a.a54 * b.a52;
        c.a43 = a.a14 * b.a13 + a.a24 * b.a23 + a.a34 * b.a33 + a.a44 * b.a43 + a.a54 * b.a53;
        c.a44 = a.a14 * b.a14 + a.a24 * b.a24 + a.a34 * b.a34 + a.a44 * b.a44 + a.a54 * b.a54;
        c.a45 = a.a14 * b.a15 + a.a24 * b.a25 + a.a34 * b.a35 + a.a44 * b.a45 + a.a54 * b.a55;
        c.a51 = a.a15 * b.a11 + a.a25 * b.a21 + a.a35 * b.a31 + a.a45 * b.a41 + a.a55 * b.a51;
        c.a52 = a.a15 * b.a12 + a.a25 * b.a22 + a.a35 * b.a32 + a.a45 * b.a42 + a.a55 * b.a52;
        c.a53 = a.a15 * b.a13 + a.a25 * b.a23 + a.a35 * b.a33 + a.a45 * b.a43 + a.a55 * b.a53;
        c.a54 = a.a15 * b.a14 + a.a25 * b.a24 + a.a35 * b.a34 + a.a45 * b.a44 + a.a55 * b.a54;
        c.a55 = a.a15 * b.a15 + a.a25 * b.a25 + a.a35 * b.a35 + a.a45 * b.a45 + a.a55 * b.a55;
    }
    
    public static void multTransAB(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 * b.a11 + a.a21 * b.a12 + a.a31 * b.a13 + a.a41 * b.a14 + a.a51 * b.a15;
        c.a12 = a.a11 * b.a21 + a.a21 * b.a22 + a.a31 * b.a23 + a.a41 * b.a24 + a.a51 * b.a25;
        c.a13 = a.a11 * b.a31 + a.a21 * b.a32 + a.a31 * b.a33 + a.a41 * b.a34 + a.a51 * b.a35;
        c.a14 = a.a11 * b.a41 + a.a21 * b.a42 + a.a31 * b.a43 + a.a41 * b.a44 + a.a51 * b.a45;
        c.a15 = a.a11 * b.a51 + a.a21 * b.a52 + a.a31 * b.a53 + a.a41 * b.a54 + a.a51 * b.a55;
        c.a21 = a.a12 * b.a11 + a.a22 * b.a12 + a.a32 * b.a13 + a.a42 * b.a14 + a.a52 * b.a15;
        c.a22 = a.a12 * b.a21 + a.a22 * b.a22 + a.a32 * b.a23 + a.a42 * b.a24 + a.a52 * b.a25;
        c.a23 = a.a12 * b.a31 + a.a22 * b.a32 + a.a32 * b.a33 + a.a42 * b.a34 + a.a52 * b.a35;
        c.a24 = a.a12 * b.a41 + a.a22 * b.a42 + a.a32 * b.a43 + a.a42 * b.a44 + a.a52 * b.a45;
        c.a25 = a.a12 * b.a51 + a.a22 * b.a52 + a.a32 * b.a53 + a.a42 * b.a54 + a.a52 * b.a55;
        c.a31 = a.a13 * b.a11 + a.a23 * b.a12 + a.a33 * b.a13 + a.a43 * b.a14 + a.a53 * b.a15;
        c.a32 = a.a13 * b.a21 + a.a23 * b.a22 + a.a33 * b.a23 + a.a43 * b.a24 + a.a53 * b.a25;
        c.a33 = a.a13 * b.a31 + a.a23 * b.a32 + a.a33 * b.a33 + a.a43 * b.a34 + a.a53 * b.a35;
        c.a34 = a.a13 * b.a41 + a.a23 * b.a42 + a.a33 * b.a43 + a.a43 * b.a44 + a.a53 * b.a45;
        c.a35 = a.a13 * b.a51 + a.a23 * b.a52 + a.a33 * b.a53 + a.a43 * b.a54 + a.a53 * b.a55;
        c.a41 = a.a14 * b.a11 + a.a24 * b.a12 + a.a34 * b.a13 + a.a44 * b.a14 + a.a54 * b.a15;
        c.a42 = a.a14 * b.a21 + a.a24 * b.a22 + a.a34 * b.a23 + a.a44 * b.a24 + a.a54 * b.a25;
        c.a43 = a.a14 * b.a31 + a.a24 * b.a32 + a.a34 * b.a33 + a.a44 * b.a34 + a.a54 * b.a35;
        c.a44 = a.a14 * b.a41 + a.a24 * b.a42 + a.a34 * b.a43 + a.a44 * b.a44 + a.a54 * b.a45;
        c.a45 = a.a14 * b.a51 + a.a24 * b.a52 + a.a34 * b.a53 + a.a44 * b.a54 + a.a54 * b.a55;
        c.a51 = a.a15 * b.a11 + a.a25 * b.a12 + a.a35 * b.a13 + a.a45 * b.a14 + a.a55 * b.a15;
        c.a52 = a.a15 * b.a21 + a.a25 * b.a22 + a.a35 * b.a23 + a.a45 * b.a24 + a.a55 * b.a25;
        c.a53 = a.a15 * b.a31 + a.a25 * b.a32 + a.a35 * b.a33 + a.a45 * b.a34 + a.a55 * b.a35;
        c.a54 = a.a15 * b.a41 + a.a25 * b.a42 + a.a35 * b.a43 + a.a45 * b.a44 + a.a55 * b.a45;
        c.a55 = a.a15 * b.a51 + a.a25 * b.a52 + a.a35 * b.a53 + a.a45 * b.a54 + a.a55 * b.a55;
    }
    
    public static void multTransB(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 * b.a11 + a.a12 * b.a12 + a.a13 * b.a13 + a.a14 * b.a14 + a.a15 * b.a15;
        c.a12 = a.a11 * b.a21 + a.a12 * b.a22 + a.a13 * b.a23 + a.a14 * b.a24 + a.a15 * b.a25;
        c.a13 = a.a11 * b.a31 + a.a12 * b.a32 + a.a13 * b.a33 + a.a14 * b.a34 + a.a15 * b.a35;
        c.a14 = a.a11 * b.a41 + a.a12 * b.a42 + a.a13 * b.a43 + a.a14 * b.a44 + a.a15 * b.a45;
        c.a15 = a.a11 * b.a51 + a.a12 * b.a52 + a.a13 * b.a53 + a.a14 * b.a54 + a.a15 * b.a55;
        c.a21 = a.a21 * b.a11 + a.a22 * b.a12 + a.a23 * b.a13 + a.a24 * b.a14 + a.a25 * b.a15;
        c.a22 = a.a21 * b.a21 + a.a22 * b.a22 + a.a23 * b.a23 + a.a24 * b.a24 + a.a25 * b.a25;
        c.a23 = a.a21 * b.a31 + a.a22 * b.a32 + a.a23 * b.a33 + a.a24 * b.a34 + a.a25 * b.a35;
        c.a24 = a.a21 * b.a41 + a.a22 * b.a42 + a.a23 * b.a43 + a.a24 * b.a44 + a.a25 * b.a45;
        c.a25 = a.a21 * b.a51 + a.a22 * b.a52 + a.a23 * b.a53 + a.a24 * b.a54 + a.a25 * b.a55;
        c.a31 = a.a31 * b.a11 + a.a32 * b.a12 + a.a33 * b.a13 + a.a34 * b.a14 + a.a35 * b.a15;
        c.a32 = a.a31 * b.a21 + a.a32 * b.a22 + a.a33 * b.a23 + a.a34 * b.a24 + a.a35 * b.a25;
        c.a33 = a.a31 * b.a31 + a.a32 * b.a32 + a.a33 * b.a33 + a.a34 * b.a34 + a.a35 * b.a35;
        c.a34 = a.a31 * b.a41 + a.a32 * b.a42 + a.a33 * b.a43 + a.a34 * b.a44 + a.a35 * b.a45;
        c.a35 = a.a31 * b.a51 + a.a32 * b.a52 + a.a33 * b.a53 + a.a34 * b.a54 + a.a35 * b.a55;
        c.a41 = a.a41 * b.a11 + a.a42 * b.a12 + a.a43 * b.a13 + a.a44 * b.a14 + a.a45 * b.a15;
        c.a42 = a.a41 * b.a21 + a.a42 * b.a22 + a.a43 * b.a23 + a.a44 * b.a24 + a.a45 * b.a25;
        c.a43 = a.a41 * b.a31 + a.a42 * b.a32 + a.a43 * b.a33 + a.a44 * b.a34 + a.a45 * b.a35;
        c.a44 = a.a41 * b.a41 + a.a42 * b.a42 + a.a43 * b.a43 + a.a44 * b.a44 + a.a45 * b.a45;
        c.a45 = a.a41 * b.a51 + a.a42 * b.a52 + a.a43 * b.a53 + a.a44 * b.a54 + a.a45 * b.a55;
        c.a51 = a.a51 * b.a11 + a.a52 * b.a12 + a.a53 * b.a13 + a.a54 * b.a14 + a.a55 * b.a15;
        c.a52 = a.a51 * b.a21 + a.a52 * b.a22 + a.a53 * b.a23 + a.a54 * b.a24 + a.a55 * b.a25;
        c.a53 = a.a51 * b.a31 + a.a52 * b.a32 + a.a53 * b.a33 + a.a54 * b.a34 + a.a55 * b.a35;
        c.a54 = a.a51 * b.a41 + a.a52 * b.a42 + a.a53 * b.a43 + a.a54 * b.a44 + a.a55 * b.a45;
        c.a55 = a.a51 * b.a51 + a.a52 * b.a52 + a.a53 * b.a53 + a.a54 * b.a54 + a.a55 * b.a55;
    }
    
    public static void mult(final FixedMatrix5x5_64F a, final FixedMatrix5_64F b, final FixedMatrix5_64F c) {
        c.a1 = a.a11 * b.a1 + a.a12 * b.a2 + a.a13 * b.a3 + a.a14 * b.a4 + a.a15 * b.a5;
        c.a2 = a.a21 * b.a1 + a.a22 * b.a2 + a.a23 * b.a3 + a.a24 * b.a4 + a.a25 * b.a5;
        c.a3 = a.a31 * b.a1 + a.a32 * b.a2 + a.a33 * b.a3 + a.a34 * b.a4 + a.a35 * b.a5;
        c.a4 = a.a41 * b.a1 + a.a42 * b.a2 + a.a43 * b.a3 + a.a44 * b.a4 + a.a45 * b.a5;
        c.a5 = a.a51 * b.a1 + a.a52 * b.a2 + a.a53 * b.a3 + a.a54 * b.a4 + a.a55 * b.a5;
    }
    
    public static void mult(final FixedMatrix5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5_64F c) {
        c.a1 = a.a1 * b.a11 + a.a2 * b.a21 + a.a3 * b.a31 + a.a4 * b.a41 + a.a5 * b.a51;
        c.a2 = a.a1 * b.a12 + a.a2 * b.a22 + a.a3 * b.a32 + a.a4 * b.a42 + a.a5 * b.a52;
        c.a3 = a.a1 * b.a13 + a.a2 * b.a23 + a.a3 * b.a33 + a.a4 * b.a43 + a.a5 * b.a53;
        c.a4 = a.a1 * b.a14 + a.a2 * b.a24 + a.a3 * b.a34 + a.a4 * b.a44 + a.a5 * b.a54;
        c.a5 = a.a1 * b.a15 + a.a2 * b.a25 + a.a3 * b.a35 + a.a4 * b.a45 + a.a5 * b.a55;
    }
    
    public static double dot(final FixedMatrix5_64F a, final FixedMatrix5_64F b) {
        return a.a1 * b.a1 + a.a2 * b.a2 + a.a3 * b.a3 + a.a4 * b.a4 + a.a5 * b.a5;
    }
    
    public static void setIdentity(final FixedMatrix5x5_64F a) {
        a.a11 = 1.0;
        a.a21 = 0.0;
        a.a31 = 0.0;
        a.a41 = 0.0;
        a.a51 = 0.0;
        a.a12 = 0.0;
        a.a22 = 1.0;
        a.a32 = 0.0;
        a.a42 = 0.0;
        a.a52 = 0.0;
        a.a13 = 0.0;
        a.a23 = 0.0;
        a.a33 = 1.0;
        a.a43 = 0.0;
        a.a53 = 0.0;
        a.a14 = 0.0;
        a.a24 = 0.0;
        a.a34 = 0.0;
        a.a44 = 1.0;
        a.a54 = 0.0;
        a.a15 = 0.0;
        a.a25 = 0.0;
        a.a35 = 0.0;
        a.a45 = 0.0;
        a.a55 = 1.0;
    }
    
    public static boolean invert(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F inv) {
        final double scale = 1.0 / elementMaxAbs(a);
        final double a2 = a.a11 * scale;
        final double a3 = a.a12 * scale;
        final double a4 = a.a13 * scale;
        final double a5 = a.a14 * scale;
        final double a6 = a.a15 * scale;
        final double a7 = a.a21 * scale;
        final double a8 = a.a22 * scale;
        final double a9 = a.a23 * scale;
        final double a10 = a.a24 * scale;
        final double a11 = a.a25 * scale;
        final double a12 = a.a31 * scale;
        final double a13 = a.a32 * scale;
        final double a14 = a.a33 * scale;
        final double a15 = a.a34 * scale;
        final double a16 = a.a35 * scale;
        final double a17 = a.a41 * scale;
        final double a18 = a.a42 * scale;
        final double a19 = a.a43 * scale;
        final double a20 = a.a44 * scale;
        final double a21 = a.a45 * scale;
        final double a22 = a.a51 * scale;
        final double a23 = a.a52 * scale;
        final double a24 = a.a53 * scale;
        final double a25 = a.a54 * scale;
        final double a26 = a.a55 * scale;
        final double m11 = a8 * (a14 * (a20 * a26 - a21 * a25) - a15 * (a19 * a26 - a21 * a24) + a16 * (a19 * a25 - a20 * a24)) - a9 * (a13 * (a20 * a26 - a21 * a25) - a15 * (a18 * a26 - a21 * a23) + a16 * (a18 * a25 - a20 * a23)) + a10 * (a13 * (a19 * a26 - a21 * a24) - a14 * (a18 * a26 - a21 * a23) + a16 * (a18 * a24 - a19 * a23)) - a11 * (a13 * (a19 * a25 - a20 * a24) - a14 * (a18 * a25 - a20 * a23) + a15 * (a18 * a24 - a19 * a23));
        final double m12 = -(a7 * (a14 * (a20 * a26 - a21 * a25) - a15 * (a19 * a26 - a21 * a24) + a16 * (a19 * a25 - a20 * a24)) - a9 * (a12 * (a20 * a26 - a21 * a25) - a15 * (a17 * a26 - a21 * a22) + a16 * (a17 * a25 - a20 * a22)) + a10 * (a12 * (a19 * a26 - a21 * a24) - a14 * (a17 * a26 - a21 * a22) + a16 * (a17 * a24 - a19 * a22)) - a11 * (a12 * (a19 * a25 - a20 * a24) - a14 * (a17 * a25 - a20 * a22) + a15 * (a17 * a24 - a19 * a22)));
        final double m13 = a7 * (a13 * (a20 * a26 - a21 * a25) - a15 * (a18 * a26 - a21 * a23) + a16 * (a18 * a25 - a20 * a23)) - a8 * (a12 * (a20 * a26 - a21 * a25) - a15 * (a17 * a26 - a21 * a22) + a16 * (a17 * a25 - a20 * a22)) + a10 * (a12 * (a18 * a26 - a21 * a23) - a13 * (a17 * a26 - a21 * a22) + a16 * (a17 * a23 - a18 * a22)) - a11 * (a12 * (a18 * a25 - a20 * a23) - a13 * (a17 * a25 - a20 * a22) + a15 * (a17 * a23 - a18 * a22));
        final double m14 = -(a7 * (a13 * (a19 * a26 - a21 * a24) - a14 * (a18 * a26 - a21 * a23) + a16 * (a18 * a24 - a19 * a23)) - a8 * (a12 * (a19 * a26 - a21 * a24) - a14 * (a17 * a26 - a21 * a22) + a16 * (a17 * a24 - a19 * a22)) + a9 * (a12 * (a18 * a26 - a21 * a23) - a13 * (a17 * a26 - a21 * a22) + a16 * (a17 * a23 - a18 * a22)) - a11 * (a12 * (a18 * a24 - a19 * a23) - a13 * (a17 * a24 - a19 * a22) + a14 * (a17 * a23 - a18 * a22)));
        final double m15 = a7 * (a13 * (a19 * a25 - a20 * a24) - a14 * (a18 * a25 - a20 * a23) + a15 * (a18 * a24 - a19 * a23)) - a8 * (a12 * (a19 * a25 - a20 * a24) - a14 * (a17 * a25 - a20 * a22) + a15 * (a17 * a24 - a19 * a22)) + a9 * (a12 * (a18 * a25 - a20 * a23) - a13 * (a17 * a25 - a20 * a22) + a15 * (a17 * a23 - a18 * a22)) - a10 * (a12 * (a18 * a24 - a19 * a23) - a13 * (a17 * a24 - a19 * a22) + a14 * (a17 * a23 - a18 * a22));
        final double m16 = -(a3 * (a14 * (a20 * a26 - a21 * a25) - a15 * (a19 * a26 - a21 * a24) + a16 * (a19 * a25 - a20 * a24)) - a4 * (a13 * (a20 * a26 - a21 * a25) - a15 * (a18 * a26 - a21 * a23) + a16 * (a18 * a25 - a20 * a23)) + a5 * (a13 * (a19 * a26 - a21 * a24) - a14 * (a18 * a26 - a21 * a23) + a16 * (a18 * a24 - a19 * a23)) - a6 * (a13 * (a19 * a25 - a20 * a24) - a14 * (a18 * a25 - a20 * a23) + a15 * (a18 * a24 - a19 * a23)));
        final double m17 = a2 * (a14 * (a20 * a26 - a21 * a25) - a15 * (a19 * a26 - a21 * a24) + a16 * (a19 * a25 - a20 * a24)) - a4 * (a12 * (a20 * a26 - a21 * a25) - a15 * (a17 * a26 - a21 * a22) + a16 * (a17 * a25 - a20 * a22)) + a5 * (a12 * (a19 * a26 - a21 * a24) - a14 * (a17 * a26 - a21 * a22) + a16 * (a17 * a24 - a19 * a22)) - a6 * (a12 * (a19 * a25 - a20 * a24) - a14 * (a17 * a25 - a20 * a22) + a15 * (a17 * a24 - a19 * a22));
        final double m18 = -(a2 * (a13 * (a20 * a26 - a21 * a25) - a15 * (a18 * a26 - a21 * a23) + a16 * (a18 * a25 - a20 * a23)) - a3 * (a12 * (a20 * a26 - a21 * a25) - a15 * (a17 * a26 - a21 * a22) + a16 * (a17 * a25 - a20 * a22)) + a5 * (a12 * (a18 * a26 - a21 * a23) - a13 * (a17 * a26 - a21 * a22) + a16 * (a17 * a23 - a18 * a22)) - a6 * (a12 * (a18 * a25 - a20 * a23) - a13 * (a17 * a25 - a20 * a22) + a15 * (a17 * a23 - a18 * a22)));
        final double m19 = a2 * (a13 * (a19 * a26 - a21 * a24) - a14 * (a18 * a26 - a21 * a23) + a16 * (a18 * a24 - a19 * a23)) - a3 * (a12 * (a19 * a26 - a21 * a24) - a14 * (a17 * a26 - a21 * a22) + a16 * (a17 * a24 - a19 * a22)) + a4 * (a12 * (a18 * a26 - a21 * a23) - a13 * (a17 * a26 - a21 * a22) + a16 * (a17 * a23 - a18 * a22)) - a6 * (a12 * (a18 * a24 - a19 * a23) - a13 * (a17 * a24 - a19 * a22) + a14 * (a17 * a23 - a18 * a22));
        final double m20 = -(a2 * (a13 * (a19 * a25 - a20 * a24) - a14 * (a18 * a25 - a20 * a23) + a15 * (a18 * a24 - a19 * a23)) - a3 * (a12 * (a19 * a25 - a20 * a24) - a14 * (a17 * a25 - a20 * a22) + a15 * (a17 * a24 - a19 * a22)) + a4 * (a12 * (a18 * a25 - a20 * a23) - a13 * (a17 * a25 - a20 * a22) + a15 * (a17 * a23 - a18 * a22)) - a5 * (a12 * (a18 * a24 - a19 * a23) - a13 * (a17 * a24 - a19 * a22) + a14 * (a17 * a23 - a18 * a22)));
        final double m21 = a3 * (a9 * (a20 * a26 - a21 * a25) - a10 * (a19 * a26 - a21 * a24) + a11 * (a19 * a25 - a20 * a24)) - a4 * (a8 * (a20 * a26 - a21 * a25) - a10 * (a18 * a26 - a21 * a23) + a11 * (a18 * a25 - a20 * a23)) + a5 * (a8 * (a19 * a26 - a21 * a24) - a9 * (a18 * a26 - a21 * a23) + a11 * (a18 * a24 - a19 * a23)) - a6 * (a8 * (a19 * a25 - a20 * a24) - a9 * (a18 * a25 - a20 * a23) + a10 * (a18 * a24 - a19 * a23));
        final double m22 = -(a2 * (a9 * (a20 * a26 - a21 * a25) - a10 * (a19 * a26 - a21 * a24) + a11 * (a19 * a25 - a20 * a24)) - a4 * (a7 * (a20 * a26 - a21 * a25) - a10 * (a17 * a26 - a21 * a22) + a11 * (a17 * a25 - a20 * a22)) + a5 * (a7 * (a19 * a26 - a21 * a24) - a9 * (a17 * a26 - a21 * a22) + a11 * (a17 * a24 - a19 * a22)) - a6 * (a7 * (a19 * a25 - a20 * a24) - a9 * (a17 * a25 - a20 * a22) + a10 * (a17 * a24 - a19 * a22)));
        final double m23 = a2 * (a8 * (a20 * a26 - a21 * a25) - a10 * (a18 * a26 - a21 * a23) + a11 * (a18 * a25 - a20 * a23)) - a3 * (a7 * (a20 * a26 - a21 * a25) - a10 * (a17 * a26 - a21 * a22) + a11 * (a17 * a25 - a20 * a22)) + a5 * (a7 * (a18 * a26 - a21 * a23) - a8 * (a17 * a26 - a21 * a22) + a11 * (a17 * a23 - a18 * a22)) - a6 * (a7 * (a18 * a25 - a20 * a23) - a8 * (a17 * a25 - a20 * a22) + a10 * (a17 * a23 - a18 * a22));
        final double m24 = -(a2 * (a8 * (a19 * a26 - a21 * a24) - a9 * (a18 * a26 - a21 * a23) + a11 * (a18 * a24 - a19 * a23)) - a3 * (a7 * (a19 * a26 - a21 * a24) - a9 * (a17 * a26 - a21 * a22) + a11 * (a17 * a24 - a19 * a22)) + a4 * (a7 * (a18 * a26 - a21 * a23) - a8 * (a17 * a26 - a21 * a22) + a11 * (a17 * a23 - a18 * a22)) - a6 * (a7 * (a18 * a24 - a19 * a23) - a8 * (a17 * a24 - a19 * a22) + a9 * (a17 * a23 - a18 * a22)));
        final double m25 = a2 * (a8 * (a19 * a25 - a20 * a24) - a9 * (a18 * a25 - a20 * a23) + a10 * (a18 * a24 - a19 * a23)) - a3 * (a7 * (a19 * a25 - a20 * a24) - a9 * (a17 * a25 - a20 * a22) + a10 * (a17 * a24 - a19 * a22)) + a4 * (a7 * (a18 * a25 - a20 * a23) - a8 * (a17 * a25 - a20 * a22) + a10 * (a17 * a23 - a18 * a22)) - a5 * (a7 * (a18 * a24 - a19 * a23) - a8 * (a17 * a24 - a19 * a22) + a9 * (a17 * a23 - a18 * a22));
        final double m26 = -(a3 * (a9 * (a15 * a26 - a16 * a25) - a10 * (a14 * a26 - a16 * a24) + a11 * (a14 * a25 - a15 * a24)) - a4 * (a8 * (a15 * a26 - a16 * a25) - a10 * (a13 * a26 - a16 * a23) + a11 * (a13 * a25 - a15 * a23)) + a5 * (a8 * (a14 * a26 - a16 * a24) - a9 * (a13 * a26 - a16 * a23) + a11 * (a13 * a24 - a14 * a23)) - a6 * (a8 * (a14 * a25 - a15 * a24) - a9 * (a13 * a25 - a15 * a23) + a10 * (a13 * a24 - a14 * a23)));
        final double m27 = a2 * (a9 * (a15 * a26 - a16 * a25) - a10 * (a14 * a26 - a16 * a24) + a11 * (a14 * a25 - a15 * a24)) - a4 * (a7 * (a15 * a26 - a16 * a25) - a10 * (a12 * a26 - a16 * a22) + a11 * (a12 * a25 - a15 * a22)) + a5 * (a7 * (a14 * a26 - a16 * a24) - a9 * (a12 * a26 - a16 * a22) + a11 * (a12 * a24 - a14 * a22)) - a6 * (a7 * (a14 * a25 - a15 * a24) - a9 * (a12 * a25 - a15 * a22) + a10 * (a12 * a24 - a14 * a22));
        final double m28 = -(a2 * (a8 * (a15 * a26 - a16 * a25) - a10 * (a13 * a26 - a16 * a23) + a11 * (a13 * a25 - a15 * a23)) - a3 * (a7 * (a15 * a26 - a16 * a25) - a10 * (a12 * a26 - a16 * a22) + a11 * (a12 * a25 - a15 * a22)) + a5 * (a7 * (a13 * a26 - a16 * a23) - a8 * (a12 * a26 - a16 * a22) + a11 * (a12 * a23 - a13 * a22)) - a6 * (a7 * (a13 * a25 - a15 * a23) - a8 * (a12 * a25 - a15 * a22) + a10 * (a12 * a23 - a13 * a22)));
        final double m29 = a2 * (a8 * (a14 * a26 - a16 * a24) - a9 * (a13 * a26 - a16 * a23) + a11 * (a13 * a24 - a14 * a23)) - a3 * (a7 * (a14 * a26 - a16 * a24) - a9 * (a12 * a26 - a16 * a22) + a11 * (a12 * a24 - a14 * a22)) + a4 * (a7 * (a13 * a26 - a16 * a23) - a8 * (a12 * a26 - a16 * a22) + a11 * (a12 * a23 - a13 * a22)) - a6 * (a7 * (a13 * a24 - a14 * a23) - a8 * (a12 * a24 - a14 * a22) + a9 * (a12 * a23 - a13 * a22));
        final double m30 = -(a2 * (a8 * (a14 * a25 - a15 * a24) - a9 * (a13 * a25 - a15 * a23) + a10 * (a13 * a24 - a14 * a23)) - a3 * (a7 * (a14 * a25 - a15 * a24) - a9 * (a12 * a25 - a15 * a22) + a10 * (a12 * a24 - a14 * a22)) + a4 * (a7 * (a13 * a25 - a15 * a23) - a8 * (a12 * a25 - a15 * a22) + a10 * (a12 * a23 - a13 * a22)) - a5 * (a7 * (a13 * a24 - a14 * a23) - a8 * (a12 * a24 - a14 * a22) + a9 * (a12 * a23 - a13 * a22)));
        final double m31 = a3 * (a9 * (a15 * a21 - a16 * a20) - a10 * (a14 * a21 - a16 * a19) + a11 * (a14 * a20 - a15 * a19)) - a4 * (a8 * (a15 * a21 - a16 * a20) - a10 * (a13 * a21 - a16 * a18) + a11 * (a13 * a20 - a15 * a18)) + a5 * (a8 * (a14 * a21 - a16 * a19) - a9 * (a13 * a21 - a16 * a18) + a11 * (a13 * a19 - a14 * a18)) - a6 * (a8 * (a14 * a20 - a15 * a19) - a9 * (a13 * a20 - a15 * a18) + a10 * (a13 * a19 - a14 * a18));
        final double m32 = -(a2 * (a9 * (a15 * a21 - a16 * a20) - a10 * (a14 * a21 - a16 * a19) + a11 * (a14 * a20 - a15 * a19)) - a4 * (a7 * (a15 * a21 - a16 * a20) - a10 * (a12 * a21 - a16 * a17) + a11 * (a12 * a20 - a15 * a17)) + a5 * (a7 * (a14 * a21 - a16 * a19) - a9 * (a12 * a21 - a16 * a17) + a11 * (a12 * a19 - a14 * a17)) - a6 * (a7 * (a14 * a20 - a15 * a19) - a9 * (a12 * a20 - a15 * a17) + a10 * (a12 * a19 - a14 * a17)));
        final double m33 = a2 * (a8 * (a15 * a21 - a16 * a20) - a10 * (a13 * a21 - a16 * a18) + a11 * (a13 * a20 - a15 * a18)) - a3 * (a7 * (a15 * a21 - a16 * a20) - a10 * (a12 * a21 - a16 * a17) + a11 * (a12 * a20 - a15 * a17)) + a5 * (a7 * (a13 * a21 - a16 * a18) - a8 * (a12 * a21 - a16 * a17) + a11 * (a12 * a18 - a13 * a17)) - a6 * (a7 * (a13 * a20 - a15 * a18) - a8 * (a12 * a20 - a15 * a17) + a10 * (a12 * a18 - a13 * a17));
        final double m34 = -(a2 * (a8 * (a14 * a21 - a16 * a19) - a9 * (a13 * a21 - a16 * a18) + a11 * (a13 * a19 - a14 * a18)) - a3 * (a7 * (a14 * a21 - a16 * a19) - a9 * (a12 * a21 - a16 * a17) + a11 * (a12 * a19 - a14 * a17)) + a4 * (a7 * (a13 * a21 - a16 * a18) - a8 * (a12 * a21 - a16 * a17) + a11 * (a12 * a18 - a13 * a17)) - a6 * (a7 * (a13 * a19 - a14 * a18) - a8 * (a12 * a19 - a14 * a17) + a9 * (a12 * a18 - a13 * a17)));
        final double m35 = a2 * (a8 * (a14 * a20 - a15 * a19) - a9 * (a13 * a20 - a15 * a18) + a10 * (a13 * a19 - a14 * a18)) - a3 * (a7 * (a14 * a20 - a15 * a19) - a9 * (a12 * a20 - a15 * a17) + a10 * (a12 * a19 - a14 * a17)) + a4 * (a7 * (a13 * a20 - a15 * a18) - a8 * (a12 * a20 - a15 * a17) + a10 * (a12 * a18 - a13 * a17)) - a5 * (a7 * (a13 * a19 - a14 * a18) - a8 * (a12 * a19 - a14 * a17) + a9 * (a12 * a18 - a13 * a17));
        final double det = (a2 * m11 + a3 * m12 + a4 * m13 + a5 * m14 + a6 * m15) / scale;
        inv.a11 = m11 / det;
        inv.a12 = m16 / det;
        inv.a13 = m21 / det;
        inv.a14 = m26 / det;
        inv.a15 = m31 / det;
        inv.a21 = m12 / det;
        inv.a22 = m17 / det;
        inv.a23 = m22 / det;
        inv.a24 = m27 / det;
        inv.a25 = m32 / det;
        inv.a31 = m13 / det;
        inv.a32 = m18 / det;
        inv.a33 = m23 / det;
        inv.a34 = m28 / det;
        inv.a35 = m33 / det;
        inv.a41 = m14 / det;
        inv.a42 = m19 / det;
        inv.a43 = m24 / det;
        inv.a44 = m29 / det;
        inv.a45 = m34 / det;
        inv.a51 = m15 / det;
        inv.a52 = m20 / det;
        inv.a53 = m25 / det;
        inv.a54 = m30 / det;
        inv.a55 = m35 / det;
        return !Double.isNaN(det) && !Double.isInfinite(det);
    }
    
    public static double det(final FixedMatrix5x5_64F mat) {
        double a11 = mat.a22;
        double a12 = mat.a23;
        double a13 = mat.a24;
        double a14 = mat.a25;
        double a15 = mat.a32;
        double a16 = mat.a33;
        double a17 = mat.a34;
        double a18 = mat.a35;
        double a19 = mat.a42;
        double a20 = mat.a43;
        double a21 = mat.a44;
        double a22 = mat.a45;
        double a23 = mat.a52;
        double a24 = mat.a53;
        double a25 = mat.a54;
        double a26 = mat.a55;
        double ret = 0.0;
        ret += mat.a11 * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a11 = mat.a21;
        a15 = mat.a31;
        a19 = mat.a41;
        a23 = mat.a51;
        ret -= mat.a12 * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a12 = mat.a22;
        a16 = mat.a32;
        a20 = mat.a42;
        a24 = mat.a52;
        ret += mat.a13 * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a13 = mat.a23;
        a17 = mat.a33;
        a21 = mat.a43;
        a25 = mat.a53;
        ret -= mat.a14 * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a14 = mat.a24;
        a18 = mat.a34;
        a22 = mat.a44;
        a26 = mat.a54;
        ret += mat.a15 * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        return ret;
    }
    
    public static double trace(final FixedMatrix5x5_64F a) {
        return a.a11 + a.a21 + a.a31 + a.a41 + a.a51;
    }
    
    public static void diag(final FixedMatrix5x5_64F input, final FixedMatrix5_64F out) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
        out.a4 = input.a44;
        out.a5 = input.a55;
    }
    
    public static double elementMax(final FixedMatrix5x5_64F a) {
        double max = a.a11;
        max = Math.max(max, a.a12);
        max = Math.max(max, a.a13);
        max = Math.max(max, a.a14);
        max = Math.max(max, a.a15);
        max = Math.max(max, a.a21);
        max = Math.max(max, a.a22);
        max = Math.max(max, a.a23);
        max = Math.max(max, a.a24);
        max = Math.max(max, a.a25);
        max = Math.max(max, a.a31);
        max = Math.max(max, a.a32);
        max = Math.max(max, a.a33);
        max = Math.max(max, a.a34);
        max = Math.max(max, a.a35);
        max = Math.max(max, a.a41);
        max = Math.max(max, a.a42);
        max = Math.max(max, a.a43);
        max = Math.max(max, a.a44);
        max = Math.max(max, a.a45);
        max = Math.max(max, a.a51);
        max = Math.max(max, a.a52);
        max = Math.max(max, a.a53);
        max = Math.max(max, a.a54);
        max = Math.max(max, a.a55);
        return max;
    }
    
    public static double elementMaxAbs(final FixedMatrix5x5_64F a) {
        double max = a.a11;
        max = Math.max(max, Math.abs(a.a12));
        max = Math.max(max, Math.abs(a.a13));
        max = Math.max(max, Math.abs(a.a14));
        max = Math.max(max, Math.abs(a.a15));
        max = Math.max(max, Math.abs(a.a21));
        max = Math.max(max, Math.abs(a.a22));
        max = Math.max(max, Math.abs(a.a23));
        max = Math.max(max, Math.abs(a.a24));
        max = Math.max(max, Math.abs(a.a25));
        max = Math.max(max, Math.abs(a.a31));
        max = Math.max(max, Math.abs(a.a32));
        max = Math.max(max, Math.abs(a.a33));
        max = Math.max(max, Math.abs(a.a34));
        max = Math.max(max, Math.abs(a.a35));
        max = Math.max(max, Math.abs(a.a41));
        max = Math.max(max, Math.abs(a.a42));
        max = Math.max(max, Math.abs(a.a43));
        max = Math.max(max, Math.abs(a.a44));
        max = Math.max(max, Math.abs(a.a45));
        max = Math.max(max, Math.abs(a.a51));
        max = Math.max(max, Math.abs(a.a52));
        max = Math.max(max, Math.abs(a.a53));
        max = Math.max(max, Math.abs(a.a54));
        max = Math.max(max, Math.abs(a.a55));
        return max;
    }
    
    public static double elementMin(final FixedMatrix5x5_64F a) {
        double min = a.a11;
        min = Math.min(min, a.a12);
        min = Math.min(min, a.a13);
        min = Math.min(min, a.a14);
        min = Math.min(min, a.a15);
        min = Math.min(min, a.a21);
        min = Math.min(min, a.a22);
        min = Math.min(min, a.a23);
        min = Math.min(min, a.a24);
        min = Math.min(min, a.a25);
        min = Math.min(min, a.a31);
        min = Math.min(min, a.a32);
        min = Math.min(min, a.a33);
        min = Math.min(min, a.a34);
        min = Math.min(min, a.a35);
        min = Math.min(min, a.a41);
        min = Math.min(min, a.a42);
        min = Math.min(min, a.a43);
        min = Math.min(min, a.a44);
        min = Math.min(min, a.a45);
        min = Math.min(min, a.a51);
        min = Math.min(min, a.a52);
        min = Math.min(min, a.a53);
        min = Math.min(min, a.a54);
        min = Math.min(min, a.a55);
        return min;
    }
    
    public static double elementMinAbs(final FixedMatrix5x5_64F a) {
        double min = a.a11;
        min = Math.min(min, Math.abs(a.a12));
        min = Math.min(min, Math.abs(a.a13));
        min = Math.min(min, Math.abs(a.a14));
        min = Math.min(min, Math.abs(a.a15));
        min = Math.min(min, Math.abs(a.a21));
        min = Math.min(min, Math.abs(a.a22));
        min = Math.min(min, Math.abs(a.a23));
        min = Math.min(min, Math.abs(a.a24));
        min = Math.min(min, Math.abs(a.a25));
        min = Math.min(min, Math.abs(a.a31));
        min = Math.min(min, Math.abs(a.a32));
        min = Math.min(min, Math.abs(a.a33));
        min = Math.min(min, Math.abs(a.a34));
        min = Math.min(min, Math.abs(a.a35));
        min = Math.min(min, Math.abs(a.a41));
        min = Math.min(min, Math.abs(a.a42));
        min = Math.min(min, Math.abs(a.a43));
        min = Math.min(min, Math.abs(a.a44));
        min = Math.min(min, Math.abs(a.a45));
        min = Math.min(min, Math.abs(a.a51));
        min = Math.min(min, Math.abs(a.a52));
        min = Math.min(min, Math.abs(a.a53));
        min = Math.min(min, Math.abs(a.a54));
        min = Math.min(min, Math.abs(a.a55));
        return min;
    }
    
    public static void elementMult(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b) {
        a.a11 *= b.a11;
        a.a12 *= b.a12;
        a.a13 *= b.a13;
        a.a14 *= b.a14;
        a.a15 *= b.a15;
        a.a21 *= b.a21;
        a.a22 *= b.a22;
        a.a23 *= b.a23;
        a.a24 *= b.a24;
        a.a25 *= b.a25;
        a.a31 *= b.a31;
        a.a32 *= b.a32;
        a.a33 *= b.a33;
        a.a34 *= b.a34;
        a.a35 *= b.a35;
        a.a41 *= b.a41;
        a.a42 *= b.a42;
        a.a43 *= b.a43;
        a.a44 *= b.a44;
        a.a45 *= b.a45;
        a.a51 *= b.a51;
        a.a52 *= b.a52;
        a.a53 *= b.a53;
        a.a54 *= b.a54;
        a.a55 *= b.a55;
    }
    
    public static void elementMult(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 * b.a11;
        c.a12 = a.a12 * b.a12;
        c.a13 = a.a13 * b.a13;
        c.a14 = a.a14 * b.a14;
        c.a15 = a.a15 * b.a15;
        c.a21 = a.a21 * b.a21;
        c.a22 = a.a22 * b.a22;
        c.a23 = a.a23 * b.a23;
        c.a24 = a.a24 * b.a24;
        c.a25 = a.a25 * b.a25;
        c.a31 = a.a31 * b.a31;
        c.a32 = a.a32 * b.a32;
        c.a33 = a.a33 * b.a33;
        c.a34 = a.a34 * b.a34;
        c.a35 = a.a35 * b.a35;
        c.a41 = a.a41 * b.a41;
        c.a42 = a.a42 * b.a42;
        c.a43 = a.a43 * b.a43;
        c.a44 = a.a44 * b.a44;
        c.a45 = a.a45 * b.a45;
        c.a51 = a.a51 * b.a51;
        c.a52 = a.a52 * b.a52;
        c.a53 = a.a53 * b.a53;
        c.a54 = a.a54 * b.a54;
        c.a55 = a.a55 * b.a55;
    }
    
    public static void elementDiv(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b) {
        a.a11 /= b.a11;
        a.a12 /= b.a12;
        a.a13 /= b.a13;
        a.a14 /= b.a14;
        a.a15 /= b.a15;
        a.a21 /= b.a21;
        a.a22 /= b.a22;
        a.a23 /= b.a23;
        a.a24 /= b.a24;
        a.a25 /= b.a25;
        a.a31 /= b.a31;
        a.a32 /= b.a32;
        a.a33 /= b.a33;
        a.a34 /= b.a34;
        a.a35 /= b.a35;
        a.a41 /= b.a41;
        a.a42 /= b.a42;
        a.a43 /= b.a43;
        a.a44 /= b.a44;
        a.a45 /= b.a45;
        a.a51 /= b.a51;
        a.a52 /= b.a52;
        a.a53 /= b.a53;
        a.a54 /= b.a54;
        a.a55 /= b.a55;
    }
    
    public static void elementDiv(final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b, final FixedMatrix5x5_64F c) {
        c.a11 = a.a11 / b.a11;
        c.a12 = a.a12 / b.a12;
        c.a13 = a.a13 / b.a13;
        c.a14 = a.a14 / b.a14;
        c.a15 = a.a15 / b.a15;
        c.a21 = a.a21 / b.a21;
        c.a22 = a.a22 / b.a22;
        c.a23 = a.a23 / b.a23;
        c.a24 = a.a24 / b.a24;
        c.a25 = a.a25 / b.a25;
        c.a31 = a.a31 / b.a31;
        c.a32 = a.a32 / b.a32;
        c.a33 = a.a33 / b.a33;
        c.a34 = a.a34 / b.a34;
        c.a35 = a.a35 / b.a35;
        c.a41 = a.a41 / b.a41;
        c.a42 = a.a42 / b.a42;
        c.a43 = a.a43 / b.a43;
        c.a44 = a.a44 / b.a44;
        c.a45 = a.a45 / b.a45;
        c.a51 = a.a51 / b.a51;
        c.a52 = a.a52 / b.a52;
        c.a53 = a.a53 / b.a53;
        c.a54 = a.a54 / b.a54;
        c.a55 = a.a55 / b.a55;
    }
    
    public static void scale(final double alpha, final FixedMatrix5x5_64F a) {
        a.a11 *= alpha;
        a.a12 *= alpha;
        a.a13 *= alpha;
        a.a14 *= alpha;
        a.a15 *= alpha;
        a.a21 *= alpha;
        a.a22 *= alpha;
        a.a23 *= alpha;
        a.a24 *= alpha;
        a.a25 *= alpha;
        a.a31 *= alpha;
        a.a32 *= alpha;
        a.a33 *= alpha;
        a.a34 *= alpha;
        a.a35 *= alpha;
        a.a41 *= alpha;
        a.a42 *= alpha;
        a.a43 *= alpha;
        a.a44 *= alpha;
        a.a45 *= alpha;
        a.a51 *= alpha;
        a.a52 *= alpha;
        a.a53 *= alpha;
        a.a54 *= alpha;
        a.a55 *= alpha;
    }
    
    public static void scale(final double alpha, final FixedMatrix5x5_64F a, final FixedMatrix5x5_64F b) {
        b.a11 = a.a11 * alpha;
        b.a12 = a.a12 * alpha;
        b.a13 = a.a13 * alpha;
        b.a14 = a.a14 * alpha;
        b.a15 = a.a15 * alpha;
        b.a21 = a.a21 * alpha;
        b.a22 = a.a22 * alpha;
        b.a23 = a.a23 * alpha;
        b.a24 = a.a24 * alpha;
        b.a25 = a.a25 * alpha;
        b.a31 = a.a31 * alpha;
        b.a32 = a.a32 * alpha;
        b.a33 = a.a33 * alpha;
        b.a34 = a.a34 * alpha;
        b.a35 = a.a35 * alpha;
        b.a41 = a.a41 * alpha;
        b.a42 = a.a42 * alpha;
        b.a43 = a.a43 * alpha;
        b.a44 = a.a44 * alpha;
        b.a45 = a.a45 * alpha;
        b.a51 = a.a51 * alpha;
        b.a52 = a.a52 * alpha;
        b.a53 = a.a53 * alpha;
        b.a54 = a.a54 * alpha;
        b.a55 = a.a55 * alpha;
    }
    
    public static void divide(final FixedMatrix5x5_64F a, final double alpha) {
        a.a11 /= alpha;
        a.a12 /= alpha;
        a.a13 /= alpha;
        a.a14 /= alpha;
        a.a15 /= alpha;
        a.a21 /= alpha;
        a.a22 /= alpha;
        a.a23 /= alpha;
        a.a24 /= alpha;
        a.a25 /= alpha;
        a.a31 /= alpha;
        a.a32 /= alpha;
        a.a33 /= alpha;
        a.a34 /= alpha;
        a.a35 /= alpha;
        a.a41 /= alpha;
        a.a42 /= alpha;
        a.a43 /= alpha;
        a.a44 /= alpha;
        a.a45 /= alpha;
        a.a51 /= alpha;
        a.a52 /= alpha;
        a.a53 /= alpha;
        a.a54 /= alpha;
        a.a55 /= alpha;
    }
    
    public static void divide(final FixedMatrix5x5_64F a, final double alpha, final FixedMatrix5x5_64F b) {
        b.a11 = a.a11 / alpha;
        b.a12 = a.a12 / alpha;
        b.a13 = a.a13 / alpha;
        b.a14 = a.a14 / alpha;
        b.a15 = a.a15 / alpha;
        b.a21 = a.a21 / alpha;
        b.a22 = a.a22 / alpha;
        b.a23 = a.a23 / alpha;
        b.a24 = a.a24 / alpha;
        b.a25 = a.a25 / alpha;
        b.a31 = a.a31 / alpha;
        b.a32 = a.a32 / alpha;
        b.a33 = a.a33 / alpha;
        b.a34 = a.a34 / alpha;
        b.a35 = a.a35 / alpha;
        b.a41 = a.a41 / alpha;
        b.a42 = a.a42 / alpha;
        b.a43 = a.a43 / alpha;
        b.a44 = a.a44 / alpha;
        b.a45 = a.a45 / alpha;
        b.a51 = a.a51 / alpha;
        b.a52 = a.a52 / alpha;
        b.a53 = a.a53 / alpha;
        b.a54 = a.a54 / alpha;
        b.a55 = a.a55 / alpha;
    }
    
    public static void changeSign(final FixedMatrix5x5_64F a) {
        a.a11 = -a.a11;
        a.a12 = -a.a12;
        a.a13 = -a.a13;
        a.a14 = -a.a14;
        a.a15 = -a.a15;
        a.a21 = -a.a21;
        a.a22 = -a.a22;
        a.a23 = -a.a23;
        a.a24 = -a.a24;
        a.a25 = -a.a25;
        a.a31 = -a.a31;
        a.a32 = -a.a32;
        a.a33 = -a.a33;
        a.a34 = -a.a34;
        a.a35 = -a.a35;
        a.a41 = -a.a41;
        a.a42 = -a.a42;
        a.a43 = -a.a43;
        a.a44 = -a.a44;
        a.a45 = -a.a45;
        a.a51 = -a.a51;
        a.a52 = -a.a52;
        a.a53 = -a.a53;
        a.a54 = -a.a54;
        a.a55 = -a.a55;
    }
    
    public static void fill(final FixedMatrix5x5_64F a, final double v) {
        a.a11 = v;
        a.a12 = v;
        a.a13 = v;
        a.a14 = v;
        a.a15 = v;
        a.a21 = v;
        a.a22 = v;
        a.a23 = v;
        a.a24 = v;
        a.a25 = v;
        a.a31 = v;
        a.a32 = v;
        a.a33 = v;
        a.a34 = v;
        a.a35 = v;
        a.a41 = v;
        a.a42 = v;
        a.a43 = v;
        a.a44 = v;
        a.a45 = v;
        a.a51 = v;
        a.a52 = v;
        a.a53 = v;
        a.a54 = v;
        a.a55 = v;
    }
}
