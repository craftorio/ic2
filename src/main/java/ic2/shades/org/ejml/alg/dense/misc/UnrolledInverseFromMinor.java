// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;

public class UnrolledInverseFromMinor
{
    public static final int MAX = 5;
    
    public static void inv(final DenseMatrix64F mat, final DenseMatrix64F inv) {
        double max = Math.abs(mat.data[0]);
        for (int N = mat.getNumElements(), i = 1; i < N; ++i) {
            final double a = Math.abs(mat.data[i]);
            if (a > max) {
                max = a;
            }
        }
        switch (mat.numRows) {
            case 2: {
                inv2(mat, inv, 1.0 / max);
                break;
            }
            case 3: {
                inv3(mat, inv, 1.0 / max);
                break;
            }
            case 4: {
                inv4(mat, inv, 1.0 / max);
                break;
            }
            case 5: {
                inv5(mat, inv, 1.0 / max);
                break;
            }
            default: {
                throw new IllegalArgumentException("Not supported");
            }
        }
    }
    
    public static void inv2(final DenseMatrix64F mat, final DenseMatrix64F inv, final double scale) {
        double[] data = mat.data;
        final double a11 = data[0] * scale;
        final double a12 = data[1] * scale;
        final double a13 = data[2] * scale;
        final double m11;
        final double a14 = m11 = data[3] * scale;
        final double m12 = -a13;
        final double m13 = -a12;
        final double m14 = a11;
        final double det = (a11 * m11 + a12 * m12) / scale;
        data = inv.data;
        data[0] = m11 / det;
        data[1] = m13 / det;
        data[2] = m12 / det;
        data[3] = m14 / det;
    }
    
    public static void inv3(final DenseMatrix64F mat, final DenseMatrix64F inv, final double scale) {
        double[] data = mat.data;
        final double a11 = data[0] * scale;
        final double a12 = data[1] * scale;
        final double a13 = data[2] * scale;
        final double a14 = data[3] * scale;
        final double a15 = data[4] * scale;
        final double a16 = data[5] * scale;
        final double a17 = data[6] * scale;
        final double a18 = data[7] * scale;
        final double a19 = data[8] * scale;
        final double m11 = a15 * a19 - a16 * a18;
        final double m12 = -(a14 * a19 - a16 * a17);
        final double m13 = a14 * a18 - a15 * a17;
        final double m14 = -(a12 * a19 - a13 * a18);
        final double m15 = a11 * a19 - a13 * a17;
        final double m16 = -(a11 * a18 - a12 * a17);
        final double m17 = a12 * a16 - a13 * a15;
        final double m18 = -(a11 * a16 - a13 * a14);
        final double m19 = a11 * a15 - a12 * a14;
        final double det = (a11 * m11 + a12 * m12 + a13 * m13) / scale;
        data = inv.data;
        data[0] = m11 / det;
        data[1] = m14 / det;
        data[2] = m17 / det;
        data[3] = m12 / det;
        data[4] = m15 / det;
        data[5] = m18 / det;
        data[6] = m13 / det;
        data[7] = m16 / det;
        data[8] = m19 / det;
    }
    
    public static void inv4(final DenseMatrix64F mat, final DenseMatrix64F inv, final double scale) {
        double[] data = mat.data;
        final double a11 = data[0] * scale;
        final double a12 = data[1] * scale;
        final double a13 = data[2] * scale;
        final double a14 = data[3] * scale;
        final double a15 = data[4] * scale;
        final double a16 = data[5] * scale;
        final double a17 = data[6] * scale;
        final double a18 = data[7] * scale;
        final double a19 = data[8] * scale;
        final double a20 = data[9] * scale;
        final double a21 = data[10] * scale;
        final double a22 = data[11] * scale;
        final double a23 = data[12] * scale;
        final double a24 = data[13] * scale;
        final double a25 = data[14] * scale;
        final double a26 = data[15] * scale;
        final double m11 = a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24);
        final double m12 = -(a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23));
        final double m13 = a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23);
        final double m14 = -(a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23));
        final double m15 = -(a12 * (a21 * a26 - a22 * a25) - a13 * (a20 * a26 - a22 * a24) + a14 * (a20 * a25 - a21 * a24));
        final double m16 = a11 * (a21 * a26 - a22 * a25) - a13 * (a19 * a26 - a22 * a23) + a14 * (a19 * a25 - a21 * a23);
        final double m17 = -(a11 * (a20 * a26 - a22 * a24) - a12 * (a19 * a26 - a22 * a23) + a14 * (a19 * a24 - a20 * a23));
        final double m18 = a11 * (a20 * a25 - a21 * a24) - a12 * (a19 * a25 - a21 * a23) + a13 * (a19 * a24 - a20 * a23);
        final double m19 = a12 * (a17 * a26 - a18 * a25) - a13 * (a16 * a26 - a18 * a24) + a14 * (a16 * a25 - a17 * a24);
        final double m20 = -(a11 * (a17 * a26 - a18 * a25) - a13 * (a15 * a26 - a18 * a23) + a14 * (a15 * a25 - a17 * a23));
        final double m21 = a11 * (a16 * a26 - a18 * a24) - a12 * (a15 * a26 - a18 * a23) + a14 * (a15 * a24 - a16 * a23);
        final double m22 = -(a11 * (a16 * a25 - a17 * a24) - a12 * (a15 * a25 - a17 * a23) + a13 * (a15 * a24 - a16 * a23));
        final double m23 = -(a12 * (a17 * a22 - a18 * a21) - a13 * (a16 * a22 - a18 * a20) + a14 * (a16 * a21 - a17 * a20));
        final double m24 = a11 * (a17 * a22 - a18 * a21) - a13 * (a15 * a22 - a18 * a19) + a14 * (a15 * a21 - a17 * a19);
        final double m25 = -(a11 * (a16 * a22 - a18 * a20) - a12 * (a15 * a22 - a18 * a19) + a14 * (a15 * a20 - a16 * a19));
        final double m26 = a11 * (a16 * a21 - a17 * a20) - a12 * (a15 * a21 - a17 * a19) + a13 * (a15 * a20 - a16 * a19);
        final double det = (a11 * m11 + a12 * m12 + a13 * m13 + a14 * m14) / scale;
        data = inv.data;
        data[0] = m11 / det;
        data[1] = m15 / det;
        data[2] = m19 / det;
        data[3] = m23 / det;
        data[4] = m12 / det;
        data[5] = m16 / det;
        data[6] = m20 / det;
        data[7] = m24 / det;
        data[8] = m13 / det;
        data[9] = m17 / det;
        data[10] = m21 / det;
        data[11] = m25 / det;
        data[12] = m14 / det;
        data[13] = m18 / det;
        data[14] = m22 / det;
        data[15] = m26 / det;
    }
    
    public static void inv5(final DenseMatrix64F mat, final DenseMatrix64F inv, final double scale) {
        double[] data = mat.data;
        final double a11 = data[0] * scale;
        final double a12 = data[1] * scale;
        final double a13 = data[2] * scale;
        final double a14 = data[3] * scale;
        final double a15 = data[4] * scale;
        final double a16 = data[5] * scale;
        final double a17 = data[6] * scale;
        final double a18 = data[7] * scale;
        final double a19 = data[8] * scale;
        final double a20 = data[9] * scale;
        final double a21 = data[10] * scale;
        final double a22 = data[11] * scale;
        final double a23 = data[12] * scale;
        final double a24 = data[13] * scale;
        final double a25 = data[14] * scale;
        final double a26 = data[15] * scale;
        final double a27 = data[16] * scale;
        final double a28 = data[17] * scale;
        final double a29 = data[18] * scale;
        final double a30 = data[19] * scale;
        final double a31 = data[20] * scale;
        final double a32 = data[21] * scale;
        final double a33 = data[22] * scale;
        final double a34 = data[23] * scale;
        final double a35 = data[24] * scale;
        final double m11 = a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32));
        final double m12 = -(a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)));
        final double m13 = a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31));
        final double m14 = -(a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31)));
        final double m15 = a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31));
        final double m16 = -(a12 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a13 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a14 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a15 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)));
        final double m17 = a11 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a13 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a14 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a15 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31));
        final double m18 = -(a11 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a12 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a14 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a15 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)));
        final double m19 = a11 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a12 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a13 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a15 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31));
        final double m20 = -(a11 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a12 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a13 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a14 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31)));
        final double m21 = a12 * (a18 * (a29 * a35 - a30 * a34) - a19 * (a28 * a35 - a30 * a33) + a20 * (a28 * a34 - a29 * a33)) - a13 * (a17 * (a29 * a35 - a30 * a34) - a19 * (a27 * a35 - a30 * a32) + a20 * (a27 * a34 - a29 * a32)) + a14 * (a17 * (a28 * a35 - a30 * a33) - a18 * (a27 * a35 - a30 * a32) + a20 * (a27 * a33 - a28 * a32)) - a15 * (a17 * (a28 * a34 - a29 * a33) - a18 * (a27 * a34 - a29 * a32) + a19 * (a27 * a33 - a28 * a32));
        final double m22 = -(a11 * (a18 * (a29 * a35 - a30 * a34) - a19 * (a28 * a35 - a30 * a33) + a20 * (a28 * a34 - a29 * a33)) - a13 * (a16 * (a29 * a35 - a30 * a34) - a19 * (a26 * a35 - a30 * a31) + a20 * (a26 * a34 - a29 * a31)) + a14 * (a16 * (a28 * a35 - a30 * a33) - a18 * (a26 * a35 - a30 * a31) + a20 * (a26 * a33 - a28 * a31)) - a15 * (a16 * (a28 * a34 - a29 * a33) - a18 * (a26 * a34 - a29 * a31) + a19 * (a26 * a33 - a28 * a31)));
        final double m23 = a11 * (a17 * (a29 * a35 - a30 * a34) - a19 * (a27 * a35 - a30 * a32) + a20 * (a27 * a34 - a29 * a32)) - a12 * (a16 * (a29 * a35 - a30 * a34) - a19 * (a26 * a35 - a30 * a31) + a20 * (a26 * a34 - a29 * a31)) + a14 * (a16 * (a27 * a35 - a30 * a32) - a17 * (a26 * a35 - a30 * a31) + a20 * (a26 * a32 - a27 * a31)) - a15 * (a16 * (a27 * a34 - a29 * a32) - a17 * (a26 * a34 - a29 * a31) + a19 * (a26 * a32 - a27 * a31));
        final double m24 = -(a11 * (a17 * (a28 * a35 - a30 * a33) - a18 * (a27 * a35 - a30 * a32) + a20 * (a27 * a33 - a28 * a32)) - a12 * (a16 * (a28 * a35 - a30 * a33) - a18 * (a26 * a35 - a30 * a31) + a20 * (a26 * a33 - a28 * a31)) + a13 * (a16 * (a27 * a35 - a30 * a32) - a17 * (a26 * a35 - a30 * a31) + a20 * (a26 * a32 - a27 * a31)) - a15 * (a16 * (a27 * a33 - a28 * a32) - a17 * (a26 * a33 - a28 * a31) + a18 * (a26 * a32 - a27 * a31)));
        final double m25 = a11 * (a17 * (a28 * a34 - a29 * a33) - a18 * (a27 * a34 - a29 * a32) + a19 * (a27 * a33 - a28 * a32)) - a12 * (a16 * (a28 * a34 - a29 * a33) - a18 * (a26 * a34 - a29 * a31) + a19 * (a26 * a33 - a28 * a31)) + a13 * (a16 * (a27 * a34 - a29 * a32) - a17 * (a26 * a34 - a29 * a31) + a19 * (a26 * a32 - a27 * a31)) - a14 * (a16 * (a27 * a33 - a28 * a32) - a17 * (a26 * a33 - a28 * a31) + a18 * (a26 * a32 - a27 * a31));
        final double m26 = -(a12 * (a18 * (a24 * a35 - a25 * a34) - a19 * (a23 * a35 - a25 * a33) + a20 * (a23 * a34 - a24 * a33)) - a13 * (a17 * (a24 * a35 - a25 * a34) - a19 * (a22 * a35 - a25 * a32) + a20 * (a22 * a34 - a24 * a32)) + a14 * (a17 * (a23 * a35 - a25 * a33) - a18 * (a22 * a35 - a25 * a32) + a20 * (a22 * a33 - a23 * a32)) - a15 * (a17 * (a23 * a34 - a24 * a33) - a18 * (a22 * a34 - a24 * a32) + a19 * (a22 * a33 - a23 * a32)));
        final double m27 = a11 * (a18 * (a24 * a35 - a25 * a34) - a19 * (a23 * a35 - a25 * a33) + a20 * (a23 * a34 - a24 * a33)) - a13 * (a16 * (a24 * a35 - a25 * a34) - a19 * (a21 * a35 - a25 * a31) + a20 * (a21 * a34 - a24 * a31)) + a14 * (a16 * (a23 * a35 - a25 * a33) - a18 * (a21 * a35 - a25 * a31) + a20 * (a21 * a33 - a23 * a31)) - a15 * (a16 * (a23 * a34 - a24 * a33) - a18 * (a21 * a34 - a24 * a31) + a19 * (a21 * a33 - a23 * a31));
        final double m28 = -(a11 * (a17 * (a24 * a35 - a25 * a34) - a19 * (a22 * a35 - a25 * a32) + a20 * (a22 * a34 - a24 * a32)) - a12 * (a16 * (a24 * a35 - a25 * a34) - a19 * (a21 * a35 - a25 * a31) + a20 * (a21 * a34 - a24 * a31)) + a14 * (a16 * (a22 * a35 - a25 * a32) - a17 * (a21 * a35 - a25 * a31) + a20 * (a21 * a32 - a22 * a31)) - a15 * (a16 * (a22 * a34 - a24 * a32) - a17 * (a21 * a34 - a24 * a31) + a19 * (a21 * a32 - a22 * a31)));
        final double m29 = a11 * (a17 * (a23 * a35 - a25 * a33) - a18 * (a22 * a35 - a25 * a32) + a20 * (a22 * a33 - a23 * a32)) - a12 * (a16 * (a23 * a35 - a25 * a33) - a18 * (a21 * a35 - a25 * a31) + a20 * (a21 * a33 - a23 * a31)) + a13 * (a16 * (a22 * a35 - a25 * a32) - a17 * (a21 * a35 - a25 * a31) + a20 * (a21 * a32 - a22 * a31)) - a15 * (a16 * (a22 * a33 - a23 * a32) - a17 * (a21 * a33 - a23 * a31) + a18 * (a21 * a32 - a22 * a31));
        final double m30 = -(a11 * (a17 * (a23 * a34 - a24 * a33) - a18 * (a22 * a34 - a24 * a32) + a19 * (a22 * a33 - a23 * a32)) - a12 * (a16 * (a23 * a34 - a24 * a33) - a18 * (a21 * a34 - a24 * a31) + a19 * (a21 * a33 - a23 * a31)) + a13 * (a16 * (a22 * a34 - a24 * a32) - a17 * (a21 * a34 - a24 * a31) + a19 * (a21 * a32 - a22 * a31)) - a14 * (a16 * (a22 * a33 - a23 * a32) - a17 * (a21 * a33 - a23 * a31) + a18 * (a21 * a32 - a22 * a31)));
        final double m31 = a12 * (a18 * (a24 * a30 - a25 * a29) - a19 * (a23 * a30 - a25 * a28) + a20 * (a23 * a29 - a24 * a28)) - a13 * (a17 * (a24 * a30 - a25 * a29) - a19 * (a22 * a30 - a25 * a27) + a20 * (a22 * a29 - a24 * a27)) + a14 * (a17 * (a23 * a30 - a25 * a28) - a18 * (a22 * a30 - a25 * a27) + a20 * (a22 * a28 - a23 * a27)) - a15 * (a17 * (a23 * a29 - a24 * a28) - a18 * (a22 * a29 - a24 * a27) + a19 * (a22 * a28 - a23 * a27));
        final double m32 = -(a11 * (a18 * (a24 * a30 - a25 * a29) - a19 * (a23 * a30 - a25 * a28) + a20 * (a23 * a29 - a24 * a28)) - a13 * (a16 * (a24 * a30 - a25 * a29) - a19 * (a21 * a30 - a25 * a26) + a20 * (a21 * a29 - a24 * a26)) + a14 * (a16 * (a23 * a30 - a25 * a28) - a18 * (a21 * a30 - a25 * a26) + a20 * (a21 * a28 - a23 * a26)) - a15 * (a16 * (a23 * a29 - a24 * a28) - a18 * (a21 * a29 - a24 * a26) + a19 * (a21 * a28 - a23 * a26)));
        final double m33 = a11 * (a17 * (a24 * a30 - a25 * a29) - a19 * (a22 * a30 - a25 * a27) + a20 * (a22 * a29 - a24 * a27)) - a12 * (a16 * (a24 * a30 - a25 * a29) - a19 * (a21 * a30 - a25 * a26) + a20 * (a21 * a29 - a24 * a26)) + a14 * (a16 * (a22 * a30 - a25 * a27) - a17 * (a21 * a30 - a25 * a26) + a20 * (a21 * a27 - a22 * a26)) - a15 * (a16 * (a22 * a29 - a24 * a27) - a17 * (a21 * a29 - a24 * a26) + a19 * (a21 * a27 - a22 * a26));
        final double m34 = -(a11 * (a17 * (a23 * a30 - a25 * a28) - a18 * (a22 * a30 - a25 * a27) + a20 * (a22 * a28 - a23 * a27)) - a12 * (a16 * (a23 * a30 - a25 * a28) - a18 * (a21 * a30 - a25 * a26) + a20 * (a21 * a28 - a23 * a26)) + a13 * (a16 * (a22 * a30 - a25 * a27) - a17 * (a21 * a30 - a25 * a26) + a20 * (a21 * a27 - a22 * a26)) - a15 * (a16 * (a22 * a28 - a23 * a27) - a17 * (a21 * a28 - a23 * a26) + a18 * (a21 * a27 - a22 * a26)));
        final double m35 = a11 * (a17 * (a23 * a29 - a24 * a28) - a18 * (a22 * a29 - a24 * a27) + a19 * (a22 * a28 - a23 * a27)) - a12 * (a16 * (a23 * a29 - a24 * a28) - a18 * (a21 * a29 - a24 * a26) + a19 * (a21 * a28 - a23 * a26)) + a13 * (a16 * (a22 * a29 - a24 * a27) - a17 * (a21 * a29 - a24 * a26) + a19 * (a21 * a27 - a22 * a26)) - a14 * (a16 * (a22 * a28 - a23 * a27) - a17 * (a21 * a28 - a23 * a26) + a18 * (a21 * a27 - a22 * a26));
        final double det = (a11 * m11 + a12 * m12 + a13 * m13 + a14 * m14 + a15 * m15) / scale;
        data = inv.data;
        data[0] = m11 / det;
        data[1] = m16 / det;
        data[2] = m21 / det;
        data[3] = m26 / det;
        data[4] = m31 / det;
        data[5] = m12 / det;
        data[6] = m17 / det;
        data[7] = m22 / det;
        data[8] = m27 / det;
        data[9] = m32 / det;
        data[10] = m13 / det;
        data[11] = m18 / det;
        data[12] = m23 / det;
        data[13] = m28 / det;
        data[14] = m33 / det;
        data[15] = m14 / det;
        data[16] = m19 / det;
        data[17] = m24 / det;
        data[18] = m29 / det;
        data[19] = m34 / det;
        data[20] = m15 / det;
        data[21] = m20 / det;
        data[22] = m25 / det;
        data[23] = m30 / det;
        data[24] = m35 / det;
    }
}
